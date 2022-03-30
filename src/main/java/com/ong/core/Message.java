package com.ong.core;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a single Message (Direct Message and Group message). The class is used to transfer different messages
 * between the Client and the Server. Not mandatory a Chat message.
 */
public class Message implements Serializable {
    /**
     * Object that is used to transfer different types of Data between the Client and the Server.
     * The Object type is determined by the Message type.
     * The Object must be serializable.
     */
    private Object freeObject;

    private MessageType type;
    private String toUsername;
    private final String fromUsername;
    private String toGroup;
    private String toCategory;
    private String toChannel;
    private final String message;
    private final LocalDateTime dateAndTime;

    /**
     * Private message constructor
     * @param toUsername
     * @param fromUsername
     * @param message
     * @param dateAndTime
     */
    public Message(String toUsername, String fromUsername, String message, LocalDateTime dateAndTime) {
        this.type = MessageType.DIRECT_MESSAGE;
        this.toUsername = toUsername;
        this.fromUsername = fromUsername;
        this.message = message;
        this.dateAndTime = dateAndTime;
    }

    /**
     * Group message constructor
     * @param toGroup
     * @param toCategory
     * @param toChannel
     * @param fromUsername
     * @param message
     * @param dateAndTime
     */
    public Message(String toGroup, String toCategory,
                   String toChannel, String fromUsername, String message, LocalDateTime dateAndTime) {
        this.type = MessageType.GROUP_MESSAGE;
        this.fromUsername = fromUsername;
        this.toGroup = toGroup;
        this.toCategory = toCategory;
        this.toChannel = toChannel;
        this.message = message;
        this.dateAndTime = dateAndTime;
    }

    public Message(Message other) {
        this.type = other.type;
        this.freeObject = other.freeObject;
        this.toUsername = other.toUsername;
        this.fromUsername = other.fromUsername;
        this.toGroup = other.toGroup;
        this.toCategory = other.toCategory;
        this.toChannel = other.toChannel;
        this.message = other.message;
        this.dateAndTime = other.dateAndTime;
    }

    public String getToUsername() {
        return toUsername;
    }

    public String getToGroup() {
        return toGroup;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public String getMessage() {
        return message;
    }

    public String getToCategory() {
        return toCategory;
    }

    public String getToChannel() {
        return toChannel;
    }

    public MessageType getType() {
        return type;
    }

    public LocalDateTime getDateAndTime() {
        return dateAndTime;
    }

    public Object getFreeObject() {
        return freeObject;
    }

    public void setFreeObject(Object freeObject) {
        this.freeObject = freeObject;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type + ": " + message;
    }

    public enum MessageType {
        DIRECT_MESSAGE,
        GROUP_MESSAGE,
        REQUEST_FRIENDS_ONLINE,
        RESPONSE_FRIENDS_ONLINE,
        REQUEST_MEMBERS_ONLINE,
        RESPONSE_MEMBERS_ONLINE
    }
}
