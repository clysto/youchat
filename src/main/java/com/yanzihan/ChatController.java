package com.yanzihan;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.json.JSONObject;

import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChatController {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final OnlineUsersStore onlineUsersStore = new OnlineUsersStore();
    ObjectRepository<User> userRepository;

    public ChatController(ObjectRepository<User> userRepository) {
        this.userRepository = userRepository;
    }

    public void onConnect(WsConnectContext ctx) {
        String jwtToken = ctx.queryParam("token");
        try {
            String username = (String) Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken)
                    .getBody().get("username");
            ctx.attribute("username", username);
            onlineUsersStore.userJoin(username, ctx);
            broadcastMessage("[Server]", new Message(Message.Type.INFORMATION, username + "加入房间"));
        } catch (JwtException e) {
            ctx.send(new Message(Message.Type.EXCEPTION, "身份验证失败"));
            ctx.session.close();
        }
    }

    public void onClose(WsCloseContext ctx) {
        if (ctx.attribute("username") != null) {
            onlineUsersStore.userLeave(ctx.attribute("username"));
        }
    }

    public void onMessage(WsMessageContext ctx) {
        ctx.getSessionId();
        String from = ctx.attribute("username");
        Message message = ctx.messageAsClass(Message.class);
        String to = message.getTo();
        if (to.equals("public")) {
            broadcastMessage(from, message);
        } else {
            sendMessage(from, to, message);
        }
    }

    private void broadcastMessage(String sender, Message message) {
        message.setFrom(sender);
        message.setTo("public");
        onlineUsersStore
                .stream()
                .filter(onlineUser -> onlineUser.getWxContext().session.isOpen())
                .forEach(onlineUser -> {
                    onlineUser.getWxContext().send(message);
                });
    }

    private void sendMessage(String sender, String to, Message message) {
        List<String> users = Arrays.asList(to.split("-"));
        message.setFrom(sender);
        onlineUsersStore
                .stream()
                .filter(onlineUser -> users.contains(onlineUser.getUsername()))
                .filter(onlineUser -> onlineUser.getWxContext().session.isOpen())
                .forEach(onlineUser -> {
                    onlineUser.getWxContext().send(message);
                });
    }

    public void signup(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        userRepository.insert(user);
        ctx.json(user);
    }

    public void signin(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        User storedUser = userRepository.find(ObjectFilters.eq("username", user.getUsername())).firstOrDefault();
        if (storedUser == null) {
            throw new BadRequestResponse("用户不存在");
        } else {
            if (Objects.equals(storedUser.getPassword(), user.getPassword())) {
                String jws = Jwts.builder().claim("username", user.getUsername()).signWith(key).compact();
                ctx.json(new JSONObject().put("token", jws).toString());
            } else {
                throw new UnauthorizedResponse("密码错误");
            }
        }
    }

    public void currentUser(Context ctx) {
        String jwtToken = ctx.queryParam("token");
        try {
            String username = (String) Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken)
                    .getBody().get("username");
            User user = new User();
            user.setUsername(username);
            ctx.json(user);
        } catch (JwtException e) {
            throw new UnauthorizedResponse("身份验证失败");
        }
    }

}
