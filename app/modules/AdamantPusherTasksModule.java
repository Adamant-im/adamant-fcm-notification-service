package modules;

import com.google.inject.AbstractModule;
import tasks.NewMessageListenTask;
import tasks.NewTokenListenTask;
import tasks.SendNotificationTask;

public class AdamantPusherTasksModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NewTokenListenTask.class).asEagerSingleton();
        bind(NewMessageListenTask.class).asEagerSingleton();
        bind(SendNotificationTask.class).asEagerSingleton();
    }
}
