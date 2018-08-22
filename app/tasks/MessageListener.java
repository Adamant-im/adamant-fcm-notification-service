package tasks;

import akka.actor.ActorSystem;
import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.typesafe.config.Config;
import core.AdamantApi;
import core.entities.Transaction;
import core.entities.transaction_assets.TransactionChatAsset;
import core.responses.TransactionList;
import entities.PushToken;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MessageListener {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private Config listenerConfig;
    private List<AdamantApi> adamantApis;

    private long offsetItems = 0;
    private long countItems = 0;
    private long currentHeight = 1;

    @Inject
    public MessageListener(ActorSystem actorSystem, ExecutionContext executionContext, Config listenerConfig, List<AdamantApi> adamantApis) {
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
                .subscribe(
                        this::startScan,
                        error -> Logger.getGlobal().warning(error.getMessage())
                );
    }

    private void startScan(long maxHeight) {
        if (maxHeight == 0){
            Logger.getGlobal().severe("Max Height is 0!");
        } else {
            currentHeight = maxHeight;
            Disposable disposable = Flowable
                    .defer(() -> Flowable.just(currentHeight))
                    .subscribeOn(Schedulers.io())
                    .flatMap((height) -> {
                        Logger.getGlobal().warning("Height: " + height);
                        AdamantApi api = randomApi();
                        Flowable<TransactionList<TransactionChatAsset>> transactionFlowable = null;
                        if (offsetItems > 0) {
                            transactionFlowable = api.getMessageTransactions(AdamantApi.ORDER_BY_TIMESTAMP_ASC, offsetItems);
                        } else {
                            transactionFlowable = api.getMessageTransactions(height, AdamantApi.ORDER_BY_TIMESTAMP_ASC);
                        }
                        return transactionFlowable;
                    })
                    .flatMap(transactionResponse -> {
                        if (transactionResponse.isSuccess()){
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
                        Logger.getGlobal().warning("Check transaction: " + transaction.getId());
                        List<PushToken> pushTokens = PushToken
                                .finder
                                .query()
                                .where()
                                .eq("address", transaction.getRecipientId())
                                .findList();
                        if (pushTokens != null){
                            sendPush(pushTokens, transaction);
                        }
                    })
                    .doOnError(error -> Logger.getGlobal().warning(error.getMessage()))
                    .repeatUntil(() -> {
                        boolean noRepeat = countItems < AdamantApi.MAX_TRANSACTIONS_PER_REQUEST;
                        if (noRepeat){
                            countItems = 0;
                            offsetItems = 0;
                        } else {
                            offsetItems += countItems;
                            countItems = 0;

                        }
                        return  noRepeat;
                    })
                    .retryWhen((retryHandler) -> retryHandler.delay(AdamantApi.SYNCHRONIZE_DELAY_SECONDS, TimeUnit.SECONDS))
                    .repeatWhen((completed) -> completed.delay(AdamantApi.SYNCHRONIZE_DELAY_SECONDS, TimeUnit.SECONDS))
                    .subscribe();
        }
    }

    private void sendPush(List<PushToken> pushTokens, Transaction<TransactionChatAsset> transaction) {
        Logger.getGlobal().warning("PUSH TO: " + pushTokens + ". TransactionId: " + transaction.getId());
        for (PushToken token : pushTokens){
            try {
                //TODO: Build rich push-message
                Message message = Message.builder()
                        .setToken(token.getToken())
                        .build();

                //TODO: Send Async and another thread
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private AdamantApi randomApi() {
        int index =  (int) Math.round(Math.floor(Math.random() * adamantApis.size()));
        if (index >= adamantApis.size()){index = adamantApis.size() - 1;}

        return adamantApis.get(index);
    }
}
