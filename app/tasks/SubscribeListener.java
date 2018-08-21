package tasks;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import contexts.SubscribeListenerExecutionContext;
import core.AdamantApi;
import io.reactivex.disposables.CompositeDisposable;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SubscribeListener {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private Config listenerConfig;
    private List<AdamantApi> adamantApis;

    private CompositeDisposable subscriptions = new CompositeDisposable();

    @Inject
    public SubscribeListener(
            Config config,
            ActorSystem actorSystem,
            List<AdamantApi> adamantApis,
            SubscribeListenerExecutionContext executionContext
    ) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.adamantApis = adamantApis;

        listenerConfig = config.getConfig("subscribe-listener");
        if(listenerConfig == null){
            Logger.getGlobal().warning("SubscribeListener not configured!");
            return;
        }

        initialize();
    }

    private void  initialize() {
        String address = listenerConfig.getString("address");
        String privateKey = listenerConfig.getString("private-key");

        this.actorSystem.scheduler().schedule(
                Duration.create(0, TimeUnit.SECONDS), // initialDelay
                Duration.create(30, TimeUnit.SECONDS),
                () -> {

                },
                this.executionContext);
    }
}
