package com.yanzihan;

import io.javalin.websocket.WsContext;

public class OnlineUser extends User {
    private WsContext wxContext;

    public WsContext getWxContext() {
        return wxContext;
    }

    public void setWxContext(WsContext wxContext) {
        this.wxContext = wxContext;
    }
}
