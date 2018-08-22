package providers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import java.io.FileInputStream;
import java.util.logging.Logger;

public class FireBaseAdminInitializer {
    private Config config;

    @Inject
    public FireBaseAdminInitializer(Config config) {
        this.config = config;
        init();
    }

    private void init(){
        Config configFirebaseAdmin = config.getConfig("pusher");
        if (configFirebaseAdmin == null){
            Logger.getGlobal().warning("Pusher not configured!");
            return;
        }

        String pathKeyFile = configFirebaseAdmin.getString("server-key-path");
        try (FileInputStream keyFile = new FileInputStream(pathKeyFile)) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
