package com.salaboy.conferences.crds.tekton.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Parameter;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import org.apache.naming.ResourceRef;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskSpec implements KubernetesResource {
    private List<Parameter> params;
    private List<ResourceRef> resources;
    private List<TaskStep> steps;

    public TaskSpec() {
    }

    public TaskSpec(List<Parameter> params, List<ResourceRef> resources, List<TaskStep> steps) {
        this.params = params;
        this.resources = resources;
        this.steps = steps;
    }

    public List<Parameter> getParams() {
        return params;
    }

    public void setParams(List<Parameter> params) {
        this.params = params;
    }

    public List<ResourceRef> getResources() {
        return resources;
    }

    public void setResources(List<ResourceRef> resources) {
        this.resources = resources;
    }


    public List<TaskStep> getSteps() {
        return steps;
    }

    public void setSteps(List<TaskStep> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "TaskSpec{" +
                "params=" + ((params != null) ? Arrays.toString(params.toArray()) : params) +
                ", resources=" + ((resources != null) ? Arrays.toString(resources.toArray()) : resources) +
                ", steps=" + ((steps != null) ? Arrays.toString(steps.toArray()) : steps) +
                '}';
    }
}
