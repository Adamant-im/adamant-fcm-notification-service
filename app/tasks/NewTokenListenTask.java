package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.typesafe.config.Config;
import contexts.SubscribeListenerExecutionContext;
import core.AdamantApi;
import core.encryption.Encryptor;
import core.entities.TransactionMessage;
import core.entities.transaction_assets.TransactionChatAsset;
import core.responses.TransactionList;
import entities.PushToken;
import helpers.Misc;
import io.ebean.Ebean;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class NewTokenListenTask {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private Config listenerConfig;
    private List<AdamantApi> adamantApis;
    private Encryptor encryptor;

    private long offsetItems = 0;
    private long countItems = 0;
    private long currentHeight = 1;

    private CompositeDisposable subscriptions = new CompositeDisposable();

    @Inject
    public NewTokenListenTask(
            Config config,
            ActorSystem actorSystem,
            List<AdamantApi> adamantApis,
            Encryptor encryptor,
            SubscribeListenerExecutionContext executionContext
    ) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.adamantApis = adamantApis;
        this.encryptor = encryptor;

        listenerConfig = config.getConfig("subscribe-listener");
        if(listenerConfig == null){
            Logger.getGlobal().warning("NewTokenListenTask not configured!");
            return;
        }

        initialize();
    }

    private void  initialize() {
        String address = listenerConfig.getString("address");
        String passphrase = listenerConfig.getString("passphrase");

        KeyPair keyPair = encryptor.getKeyPairFromPassPhrase(passphrase);

        String privateKey = keyPair.getSecretKey().getAsHexString();

        //Grow Up mode (load all records from blockchain)
        //TODO: Max Height Mode (ignore old records in blockchain)
        Disposable disposable = Flowable
                .defer(() -> Flowable.just(currentHeight))
                .subscribeOn(Schedulers.io())
                .flatMap((height) -> {
                    AdamantApi api = Misc.randomItem(adamantApis);
                    Flowable<TransactionList<TransactionChatAsset>> transactionFlowable = null;
                    if (offsetItems > 0) {
                        transactionFlowable = api.getMessageTransactions(
                                address,
                                AdamantApi.ORDER_BY_TIMESTAMP_ASC,
                                offsetItems,
                                TransactionMessage.SIGNAL_MESSAGE_TYPE
                        );
                    } else {
                        transactionFlowable = api.getMessageTransactions(
                                address,
                                height,
                                AdamantApi.ORDER_BY_TIMESTAMP_ASC,
                                TransactionMessage.SIGNAL_MESSAGE_TYPE
                        );
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
                    Logger.getGlobal().info("Subscribe transaction: " + transaction.getId());
                    TransactionChatAsset asset = transaction.getAsset();
                    if (asset == null){return;}

                    TransactionMessage transactionMessage = asset.getChat();
                    if (transactionMessage == null){return;}

                    try {
                        String decryptedMessage = encryptor.decryptMessage(transactionMessage.getMessage(), transactionMessage.getOwnMessage(), transaction.getSenderPublicKey(), privateKey);

                        JsonNode parsedSubscription = Json.parse(decryptedMessage);
                        boolean valid = (parsedSubscription != null && parsedSubscription.has("provider") && parsedSubscription.has("token"));

                        if (valid){
                            String token = parsedSubscription.get("token").asText();
                            String provider = parsedSubscription.get("provider").asText();

                            if (!provider.equalsIgnoreCase("fcm")){return;}

                            PushToken pushToken = new PushToken();
                            pushToken.setAddress(transaction.getSenderId());
                            pushToken.setToken(token);
                            pushToken.setProvider(provider);

                            PushToken existingToken = PushToken
                                    .finder
                                    .query()
                                    .where()
                                    .eq("token", pushToken.getToken())
                                    .eq("address", pushToken.getAddress())
                                    .findOne();

                            if (existingToken == null){
                                Ebean.save(pushToken);
                            }
                        }

                    }catch (Exception ex) {
                        ex.printStackTrace();
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

        subscriptions.add(disposable);
    }

    @Override
    protected void finalize() throws Throwable {
        subscriptions.dispose();
        subscriptions.clear();

        super.finalize();
    }
}
