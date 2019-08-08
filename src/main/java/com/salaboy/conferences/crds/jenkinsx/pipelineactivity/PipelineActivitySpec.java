package com.salaboy.conferences.crds.jenkinsx.pipelineactivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Date;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineActivitySpec implements KubernetesResource {
    private String author;
    private String pipeline;
    private String status;
    private String gitUrl;
    private String version;
    private Date startedTimestamp;
    private Date completedTimestamp;
    private String build;
    private String lastCommitMessage;

    public PipelineActivitySpec() {
    }

    public PipelineActivitySpec(String author, String pipeline, String status, String gitUrl, String version, Date startedTimestamp, Date completedTimestamp, String build, String lastCommitMessage) {
        this.author = author;
        this.pipeline = pipeline;
        this.status = status;
        this.gitUrl = gitUrl;
        this.version = version;
        this.startedTimestamp = startedTimestamp;
        this.completedTimestamp = completedTimestamp;
        this.build = build;
        this.lastCommitMessage = lastCommitMessage;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getStartedTimestamp() {
        return startedTimestamp;
    }

    public void setStartedTimestamp(Date startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public Date getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(Date completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getLastCommitMessage() {
        return lastCommitMessage;
    }

    public void setLastCommitMessage(String lastCommitMessage) {
        this.lastCommitMessage = lastCommitMessage;
    }


    @Override
    public String toString() {
        return "PipelineActivitySpec{" +
                "author='" + author + '\'' +
                ", pipeline='" + pipeline + '\'' +
                ", status='" + status + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", version='" + version + '\'' +
                ", startedTimestamp=" + startedTimestamp +
                ", completedTimestamp=" + completedTimestamp +
                ", build='" + build + '\'' +
                ", lastCommitMessage='" + lastCommitMessage + '\'' +
                '}';
    }
}
