package me.kolek.gradle.plugin.coveralls.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public class Job {
    private String repoToken;
    private String serviceName;
    private String serviceNumber;
    private String serviceJobId;
    private String servicePullRequest;
    private List<SourceFile> sourceFiles;
    private boolean parallel;
    private Git git;
    private String commitSha;
    private OffsetDateTime runAt;

    @JsonProperty("repo_token")
    public String getRepoToken() {
        return repoToken;
    }

    public void setRepoToken(String repoToken) {
        this.repoToken = repoToken;
    }

    @JsonProperty("service_name")
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty("service_number")
    public String getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(String serviceNumber) {
        this.serviceNumber = serviceNumber;
    }

    @JsonProperty("service_job_id")
    public String getServiceJobId() {
        return serviceJobId;
    }

    public void setServiceJobId(String serviceJobId) {
        this.serviceJobId = serviceJobId;
    }

    @JsonProperty("service_pull_request")
    public String getServicePullRequest() {
        return servicePullRequest;
    }

    public void setServicePullRequest(String servicePullRequest) {
        this.servicePullRequest = servicePullRequest;
    }

    @JsonProperty("source_files")
    public List<SourceFile> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<SourceFile> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @JsonProperty("parallel")
    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    @JsonProperty("git")
    public Git getGit() {
        return git;
    }

    public void setGit(Git git) {
        this.git = git;
    }

    @JsonProperty("commit_sha")
    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    @JsonProperty("run_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z")
    public OffsetDateTime getRunAt() {
        return runAt;
    }

    public void setRunAt(OffsetDateTime runAt) {
        this.runAt = runAt;
    }
}
