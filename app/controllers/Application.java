package controllers;

import com.typesafe.config.Config;
import play.*;
import play.mvc.*;

import us.raudi.pushraven.FcmResponse;
import us.raudi.pushraven.Message;
import us.raudi.pushraven.Notification;
import us.raudi.pushraven.Pushraven;
import us.raudi.pushraven.configs.AndroidConfig;
import us.raudi.pushraven.notifications.AndroidNotification;
import views.html.*;

import javax.inject.Inject;
import java.io.File;

public class Application extends Controller {

    private Config config;

    @Inject
    public Application(Config config) {
        this.config = config;
    }

    public Result index() {
//        Config pusher = config.getConfig("pusher");
//        String filePath = pusher.getString("server-key-path");
//        String projectId = pusher.getString("project-id");
//
//        File file = new File(filePath);
//
//        Pushraven.ACCOUNT_FILE = file;
//        Pushraven.setProjectId(projectId);
//
//        Notification not = new Notification()
//                .title("Hello World")
//                .body("This is a notification");
//
//        AndroidConfig droidCfg = new AndroidConfig()
//                .notification(
//                        new AndroidNotification()
//                                .color("#ff0000")
//                )
//                .priority(AndroidConfig.Priority.HIGH);
//
//        Message raven = new Message()
//                .name("id")
//                .notification(not)
//                .token("dnGxlkpbnJs:APA91bGmGcmyi8Ktkt0klj4Kiv8t6GI4hqktD_7wnT8-H0I4bv0FXZn_LacmDlpJKTjkwHkNnP2vBojDNVO-47XaPMY6B-KQxYUvlcwohSYDil4CkoC8f3JWm3JfeADLVyU4jn-q4AfkOQ7HASOfxBCGgCOvy373jQ")
//                .android(droidCfg);
//
//        Pushraven.push(raven);
//// or (if you want to access the response)
//        FcmResponse response = Pushraven.push(raven);

        return ok("ADAMANT FCM pusher welcome you.");
    }

}
