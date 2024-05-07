package com.example.courseassistantapp_auth;

public class Comment {
    public String authorEmail;
    public String content;
    public long timestamp;

    public Comment() {} // Default constructor

    public Comment(String authorEmail, String content, long timestamp) {
        this.authorEmail = authorEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

}
