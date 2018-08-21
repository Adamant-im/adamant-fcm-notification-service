package contexts;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;

public class MessageListenerExecutionContext extends CustomExecutionContext {
    public static final String NAME = "message-listener-execution-context";

    @Inject
    public MessageListenerExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, NAME);
    }
}
