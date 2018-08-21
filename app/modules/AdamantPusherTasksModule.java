package modules;

import com.google.inject.AbstractModule;
import tasks.MessageListener;
import tasks.SubscribeListener;

public class AdamantPusherTasksModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SubscribeListener.class).asEagerSingleton();
        bind(MessageListener.class).asEagerSingleton();
    }
}
