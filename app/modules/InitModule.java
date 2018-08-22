package modules;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import core.AdamantApi;
import providers.ApiListProvider;
import providers.FireBaseAdminProvider;

import java.util.List;

public class InitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<List<AdamantApi>>() {})
                .toProvider(ApiListProvider.class);
        bind(FireBaseAdminProvider.class).asEagerSingleton();
    }


}
