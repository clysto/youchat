package com.yanzihan;

import io.javalin.websocket.WsContext;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OnlineUsersStore implements Iterable<OnlineUser> {
    final Map<String, OnlineUser> users = new ConcurrentHashMap<>();

    public OnlineUser getUser(String sessionId) {
        return users.get(sessionId);
    }

    public void userJoin(String username, WsContext context) {
        OnlineUser onlineUser = new OnlineUser();
        onlineUser.setUsername(username);
        onlineUser.setWxContext(context);
        users.put(username, onlineUser);
    }

    public void userLeave(String username) {
        users.remove(username);
    }

    @NotNull
    @Override
    public Iterator<OnlineUser> iterator() {
        return users.values().iterator();
    }

    @Override
    public Spliterator<OnlineUser> spliterator() {
        return users.values().stream().spliterator();
    }

    public Stream<OnlineUser> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

}
