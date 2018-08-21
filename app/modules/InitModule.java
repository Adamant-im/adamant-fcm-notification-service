package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import core.AdamantApi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import play.Environment;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class InitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<List<AdamantApi>>() {})
                .toProvider(ApiListProvider.class);
    }

    static class ApiListProvider implements Provider<List<AdamantApi>> {
        private Config config;
        private Environment environment;

        @Inject
        public ApiListProvider(Config config, Environment environment) {
            this.config = config;
            this.environment = environment;
        }
        @Override
        public List<AdamantApi> get() {
            List<AdamantApi> adamantApis = new ArrayList<>();

            Config adamantConfig = config.getConfig("adamant");
            if (adamantConfig == null){
                Logger.getGlobal().severe("Adamant section not configured!");
                return adamantApis;
            }

            String basePath = adamantConfig.getString("base-path");

            List<String> nodes = config.getStringList("nodes");
            if (nodes == null){
                Logger.getGlobal().severe("List of nodes not configured!");
                return adamantApis;
            }

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            if (environment.isDev()){
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(logging);
            }

            for (String nodeAddress : nodes) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(nodeAddress + basePath)
                        .addConverterFactory(JacksonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .client(httpClient.build())
                        .build();

                adamantApis.add(retrofit.create(AdamantApi.class));
            }

            return adamantApis;
        }
    }
}
