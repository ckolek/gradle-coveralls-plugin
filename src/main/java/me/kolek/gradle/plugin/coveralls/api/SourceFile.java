package me.kolek.gradle.plugin.coveralls.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceFile {
    private String name;
    private String sourceDigest;
    private Integer[] coverage;
    private Integer[] branches;
    private String source;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("source_digest")
    public String getSourceDigest() {
        return sourceDigest;
    }

    public void setSourceDigest(String sourceDigest) {
        this.sourceDigest = sourceDigest;
    }

    @JsonProperty("coverage")
    public Integer[] getCoverage() {
        return coverage;
    }

    public void setCoverage(Integer[] coverage) {
        this.coverage = coverage;
    }

    @JsonProperty("branches")
    public Integer[] getBranches() {
        return branches;
    }

    public void setBranches(Integer[] branches) {
        this.branches = branches;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
