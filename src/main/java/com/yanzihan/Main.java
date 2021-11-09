package com.yanzihan;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;

public class Main {
    private static ChatController chatController;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // 打包时取消下面的注释
            // config.addStaticFiles("/public", Location.CLASSPATH);
            // 打包时注释下面的代码
            config.addStaticFiles("./src/main/resources/public", Location.EXTERNAL);
        }).start(8080);

        Nitrite db = Nitrite.builder()
                .compressed()
                .filePath("./test.db")
                .openOrCreate();

        ObjectRepository<User> userRepository = db.getRepository(User.class);

        chatController = new ChatController(userRepository);

        app.ws("/chat", ws -> {
            ws.onConnect(chatController::onConnect);
            ws.onClose(chatController::onClose);
            ws.onMessage(chatController::onMessage);
        });
        app.post("/signup", chatController::signup);
        app.post("/signin", chatController::signin);
        app.get("/currentUser", chatController::currentUser);
    }

}
