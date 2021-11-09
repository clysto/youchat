package com.yanzihan;

public class Message {
    public static enum Type {
        EXCEPTION, TEXT, IMAGE, INFORMATION
    }

    private Type type;
    private String content;
    private String from;
    private String to;

    public Message() {
    }

    public Message(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
