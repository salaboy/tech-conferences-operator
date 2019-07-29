package com.salaboy.conferences.crds.tekton;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.pipelinerun.Condition;

import java.util.Date;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    private Date completionTime;
    private Date startTime;
    private List<Condition> conditions;


    public Status(Date completionTime, Date startTime, List<Condition> conditions) {
        this.completionTime = completionTime;
        this.startTime = startTime;
        this.conditions = conditions;
    }

    public Status() {
    }

    public Date getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String toString() {
        return "Status{" +
                "completionTime=" + completionTime +
                ", startTime=" + startTime +
                ", conditions=" + conditions +
                '}';
    }
}
