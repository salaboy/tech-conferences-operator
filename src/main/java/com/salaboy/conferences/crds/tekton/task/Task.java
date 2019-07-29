package com.salaboy.conferences.crds.tekton.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task extends CustomResource {
    private TaskSpec spec;

    public TaskSpec getSpec() {
        return spec;
    }

    public void setSpec(TaskSpec spec) {
        this.spec = spec;
    }

    public String getKind() {
        return "Pipeline";
    }

    @Override
    public String toString() {
        return "Task{" +
                "spec=" + spec +
                '}';
    }
}
