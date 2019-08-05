package com.salaboy.conferences.crds.tekton.taskrun;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Status;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRunStatus extends Status {
    private List<TaskRunStep> steps;

    public List<TaskRunStep> getSteps() {
        return steps;
    }

    public void setSteps(List<TaskRunStep> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "TaskRunStatus{" +
                "steps=" + Arrays.toString(steps.toArray()) +
                '}';
    }
}
