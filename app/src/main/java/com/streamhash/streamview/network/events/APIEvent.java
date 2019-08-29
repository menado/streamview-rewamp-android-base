package com.streamhash.streamview.network.events;

public class APIEvent {
    private String message;
    private int errorCode;

    public APIEvent(String message, int errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "APIEvent{" +
                "message='" + message + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
