package modules;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import core.AdamantApi;
import core.encryption.Encryptor;
import providers.ApiListProvider;
import providers.EncryptorProvider;
import providers.FireBaseAdminInitializer;

import java.util.List;

public class InitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<List<AdamantApi>>() {})
                .toProvider(ApiListProvider.class);
        bind(Encryptor.class)
                .toProvider(EncryptorProvider.class)
                .asEagerSingleton();
        bind(FireBaseAdminInitializer.class).asEagerSingleton();
    }


}
