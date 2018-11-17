package tasks;

import akka.actor.ActorSystem;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.inject.Inject;
import contexts.SendNotificationExecutionContext;
import entities.MessageData;
import entities.PushToken;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import scala.concurrent.duration.Duration;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SendNotificationTask {
    private static final String TOKEN_NOT_REGISTERED = "registration-token-not-registered";
    private static final String MISSMATCHED_CREDENTIAL = "mismatched-credential";
    private static final int BATCH_SIZE = 100;
    private final ActorSystem actorSystem;
    private final SendNotificationExecutionContext executionContext;

    @Inject
    public SendNotificationTask(ActorSystem actorSystem, SendNotificationExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        init();
    }

    private void init() {
        this.actorSystem.scheduler().schedule(
                Duration.create(0, TimeUnit.SECONDS), // initialDelay
                Duration.create(3, TimeUnit.SECONDS),
                () -> {
                    List<MessageData> messageDataList = lockAndSelectMessages();
                    for (MessageData messageData: messageDataList) {
                        sendPushNotifications(messageData);
                    }
                },
                executionContext
        );
    }

    private List<MessageData> lockAndSelectMessages() {
        List<MessageData> messageDatas = new ArrayList<>();
                Timestamp now = new Timestamp(System.currentTimeMillis());
        Ebean.beginTransaction();
        try {

            SqlUpdate lockQuery = Ebean.createSqlUpdate("Update messages set attempts=attempts+1, locked_at=:locked where id in (Select id from messages where locked_at is null and sended=false order by attempts asc, id desc limit :batch_size);");
            lockQuery.setParameter("locked", now);
            lockQuery.setParameter("batch_size", BATCH_SIZE);
            lockQuery.execute();

            messageDatas = MessageData
                    .finder
                    .query()
                    .where()
                    .eq("locked_at", now)
                    .findList();

            Ebean.commitTransaction();
        }catch (Exception ex){
            Ebean.rollbackTransaction();
            ex.printStackTrace();
        }

        return messageDatas;
    }

    private void sendPushNotifications(MessageData data) {
            Logger.getGlobal().info("PUSH TO: " + data.getPushToken().getToken() + ". TransactionId: " + data.getTransactionId());

            //TODO: Build rich push-message
            Message message = Message.builder()
                    .setToken(data.getPushToken().getToken())
                    .build();

            ApiFuture<String> future = FirebaseMessaging.getInstance().sendAsync(message);
            addResultCallback(future, data);
    }

    private void addResultCallback(ApiFuture<String> future, MessageData data) {
        ApiFutures.addCallback(future, new ApiFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Logger.getGlobal().warning("Operation completed with result: " + result);

                try {
                    Ebean.delete(data);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Logger.getGlobal().severe("Operation failed with error: " + t);
                try {
                    if (t instanceof FirebaseMessagingException){
                        FirebaseMessagingException exception = (FirebaseMessagingException)t;
                        String errCode = exception.getErrorCode();
                        Logger.getGlobal().severe("Error code: " + errCode);
                        switch (errCode){
                            case MISSMATCHED_CREDENTIAL:
                            case TOKEN_NOT_REGISTERED : {
                                Ebean.delete(data.getPushToken());
                            }
                            break;
                        }
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

}
