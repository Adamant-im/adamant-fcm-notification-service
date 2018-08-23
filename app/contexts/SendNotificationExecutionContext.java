package contexts;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;

public class SendNotificationExecutionContext extends CustomExecutionContext {
    public static final String NAME = "send-notification-execution-context";

    @Inject
    public SendNotificationExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, NAME);
    }
}
