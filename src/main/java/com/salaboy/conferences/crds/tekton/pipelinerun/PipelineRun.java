package com.salaboy.conferences.crds.tekton.pipelinerun;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Status;
import io.fabric8.kubernetes.client.CustomResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineRun extends CustomResource {
    private PipelineRunSpec spec;
    private Status status;

    public PipelineRunSpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineRunSpec spec) {
        this.spec = spec;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getKind() {
        return "Pipeline";
    }

    @Override
    public String toString() {
        return "PipelineRun{" +
                "name=" + getMetadata().getName() + "," +
                "spec=" + spec + "," +
                "status=" + status +
                '}';
    }
}
