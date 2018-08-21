package contexts;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;

public class SubscribeListenerExecutionContext extends CustomExecutionContext {
    public static final String NAME = "subscribe-listener-execution-context";

    @Inject
    public SubscribeListenerExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, NAME);
    }
}
