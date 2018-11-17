package tasks;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import core.AdamantApi;
import core.entities.Transaction;
import core.entities.transaction_assets.TransactionChatAsset;
import core.responses.TransactionList;
import entities.MessageData;
import entities.PushToken;
import helpers.Misc;
import io.ebean.Ebean;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.MaybeSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class NewMessageListenTask {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private Config listenerConfig;
    private List<AdamantApi> adamantApis;

    private long offsetItems = 0;
    private long countItems = 0;
    private long currentHeight = 1;

    private CompositeDisposable subscriptions = new CompositeDisposable();

    @Inject
    public NewMessageListenTask(ActorSystem actorSystem, ExecutionContext executionContext, Config listenerConfig, List<AdamantApi> adamantApis) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.listenerConfig = listenerConfig;
        this.adamantApis = adamantApis;

        init();
    }

    private void init() {

        Disposable subscribe = Flowable
                .fromIterable(adamantApis)
                .subscribeOn(Schedulers.io())
                .flatMap(AdamantApi::getHeight)
                .flatMap(result -> {
                    if (result.isSuccess()) {
                        return Flowable.just(result.getHeight());
                    } else {
                        return Flowable.error(new Exception(result.getError()));
                    }
                })
                .onErrorReturn(error -> {
                    Logger.getGlobal().warning(error.getMessage());
                    return 0L;
                })
                .reduce((previous, now) -> {
                    if (previous.compareTo(now) < 1) {
                        return now;
                    } else {
                        return previous;
                    }
                })
                .toObservable()
                .toFlowable(BackpressureStrategy.LATEST)
                .flatMap(this::startScan)
                .subscribe(
                        transaction -> {},
                        error -> Logger.getGlobal().severe(error.getMessage()),
                        () -> {Logger.getGlobal().severe("New message listener was completed");}
                );

        subscriptions.add(subscribe);
    }

    private Flowable<Transaction<TransactionChatAsset>> startScan(long maxHeight) {
        if (maxHeight == 0){
            return Flowable.error(new Exception("Max Height is 0!"));
        } else {
            currentHeight = maxHeight;
            return Flowable
                    .defer(() -> Flowable.just(currentHeight))
                    .subscribeOn(Schedulers.io())
                    .flatMap((height) -> {
                        Logger.getGlobal().info("Height: " + height);
                        AdamantApi api = Misc.randomItem(adamantApis);
                        Flowable<TransactionList<TransactionChatAsset>> transactionFlowable = null;
                        if (offsetItems > 0) {
                            transactionFlowable = api.getMessageTransactions(AdamantApi.ORDER_BY_TIMESTAMP_ASC, offsetItems);
                        } else {
                            transactionFlowable = api.getMessageTransactions(height, AdamantApi.ORDER_BY_TIMESTAMP_ASC);
                        }
                        return transactionFlowable;
                    })
                    .flatMap(transactionResponse -> {
                        if (transactionResponse.isSuccess()) {
                            return Flowable.just(transactionResponse.getTransactions());
                        } else {
                            return Flowable.error(new Exception(transactionResponse.getError()));
                        }
                    })
                    .onErrorReturn(error -> {
                        Logger.getGlobal().warning(error.getMessage());
                        return new ArrayList<>();
                    })
                    .flatMapIterable(item -> item)
                    .doOnNext(transaction -> {
                        countItems++;
                        if (transaction.getHeight() > currentHeight) {
                            currentHeight = transaction.getHeight();
                        }
                    })
                    .doOnNext(transaction -> {
                        //TODO: Implement batch processing
                        Logger.getGlobal().info("Check transaction: " + transaction.getId());
                        List<PushToken> pushTokens = PushToken
                                .finder
                                .query()
                                .where()
                                .eq("address", transaction.getRecipientId())
                                .findList();

                        if (pushTokens.size() > 0) {
                            List<MessageData> messageDataList = new ArrayList<>();

                            for (PushToken pushToken : pushTokens) {
                                MessageData messageData = new MessageData();
                                messageData.setPushToken(pushToken);
                                messageData.setTransactionId(transaction.getId());
                                messageDataList.add(messageData);
                            }

                            Ebean.saveAll(messageDataList);
                        }
                    })
                    .doOnError(error -> Logger.getGlobal().warning(error.getMessage()))
                    .repeatUntil(() -> {
                        boolean noRepeat = countItems < AdamantApi.MAX_TRANSACTIONS_PER_REQUEST;
                        if (noRepeat) {
                            countItems = 0;
                            offsetItems = 0;
                        } else {
                            offsetItems += countItems;
                            countItems = 0;

                        }
                        return noRepeat;
                    })
                    .retryWhen((retryHandler) -> retryHandler.delay(AdamantApi.SYNCHRONIZE_DELAY_SECONDS, TimeUnit.SECONDS))
                    .repeatWhen((completed) -> completed.delay(AdamantApi.SYNCHRONIZE_DELAY_SECONDS, TimeUnit.SECONDS));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        subscriptions.dispose();
        subscriptions.clear();

        super.finalize();
    }

}
