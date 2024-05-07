package com.example.courseassistantapp_auth;

public class Post {
    public String postId;
    public String authorEmail;
    public String content;
    public long timestamp;

    public Post() {}

    public Post(String postId, String authorEmail, String content, long timestamp) {
        this.postId = postId;
        this.authorEmail = authorEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

}
