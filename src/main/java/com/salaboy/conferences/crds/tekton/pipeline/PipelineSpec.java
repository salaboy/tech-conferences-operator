package com.salaboy.conferences.crds.tekton.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Parameter;
import com.salaboy.conferences.crds.tekton.Resource;
import com.salaboy.conferences.crds.tekton.task.TaskSpec;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineSpec implements KubernetesResource {
    private List<Parameter> params;
    private List<Resource> resources;
    private List<TaskRef> tasks;

    public List<Parameter> getParams() {
        return params;
    }

    public void setParams(List<Parameter> params) {
        this.params = params;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<TaskRef> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskRef> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "PipelineSpec{" +
                "params=" + Arrays.toString(params.toArray()) +
                ", resources=" + Arrays.toString(resources.toArray()) +
                ", tasks=" + Arrays.toString(tasks.toArray()) +
                '}';
    }
}
