package com.salaboy.conferences.tekton;

import io.radanalytics.operator.common.EntityInfo;

public class PipelineInfo extends EntityInfo {
    private String tasks;

    public PipelineInfo() {
    }

    public PipelineInfo(String tasks) {
        this.tasks = tasks;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "PipelineInfo{" +
                "tasks='" + tasks + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
