package com.example.courseassistantapp_auth;

public class ForumMessage {
    public String topic;
    public String courseCode;
    public String userEmail;
    public String messageText;
    public String timestamp;
    public  String recipent;

    public ForumMessage() {
        // Boş yapıcı (Firebase için gereklidir)
    }

    public ForumMessage(String topic, String courseCode, String userEmail, String recipent, String messageText, String timestamp) {
        this.topic = topic;
        this.courseCode = courseCode;
        this.userEmail = userEmail;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.recipent = recipent;
    }
}
