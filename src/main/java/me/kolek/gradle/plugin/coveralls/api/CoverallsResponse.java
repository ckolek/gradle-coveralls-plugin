package me.kolek.gradle.plugin.coveralls.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CoverallsResponse {
    private int code;
    private String message;
    private boolean error;
    private String url;

    @JsonIgnore
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("error")
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "CoverallsResponse { code: " + code + ", message: " + message + ", error: " + error + ", url: " + url +
                " }";
    }
}
