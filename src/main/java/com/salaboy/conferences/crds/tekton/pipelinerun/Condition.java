package com.salaboy.conferences.crds.tekton.pipelinerun;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Condition {
    private String status;
    private String type;
    private String reason;
    private String message;
    private Date lastTransitionTime;

    public Condition() {
    }

    public Condition(String status, String type, String reason, String message, Date lastTransitionTime) {
        this.status = status;
        this.type = type;
        this.reason = reason;
        this.message = message;
        this.lastTransitionTime = lastTransitionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getLastTransitionTime() {
        return lastTransitionTime;
    }

    public void setLastTransitionTime(Date lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", reason='" + reason + '\'' +
                ", message='" + message + '\'' +
                ", lastTransitionTime=" + lastTransitionTime +
                '}';
    }
}
