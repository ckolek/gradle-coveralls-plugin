package me.kolek.gradle.plugin.coveralls.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Git {
    private Head head;
    private String branch;
    private List<Remote> remotes;

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("branch")
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @JsonProperty("remotes")
    public List<Remote> getRemotes() {
        return remotes;
    }

    public void setRemotes(List<Remote> remotes) {
        this.remotes = remotes;
    }

    public static class Head {
        private String id;
        private String authorName;
        private String authorEmail;
        private String committerName;
        private String committerEmail;
        private String message;

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @JsonProperty("author_name")
        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        @JsonProperty("author_email")
        public String getAuthorEmail() {
            return authorEmail;
        }

        public void setAuthorEmail(String authorEmail) {
            this.authorEmail = authorEmail;
        }

        @JsonProperty("committer_name")
        public String getCommitterName() {
            return committerName;
        }

        public void setCommitterName(String committerName) {
            this.committerName = committerName;
        }

        @JsonProperty("committer_email")
        public String getCommitterEmail() {
            return committerEmail;
        }

        public void setCommitterEmail(String committerEmail) {
            this.committerEmail = committerEmail;
        }

        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class Remote {
        private String name;
        private String url;

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty("url")
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
