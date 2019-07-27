package com.salaboy.conferences.crds.tekton.pipelinerun;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineRun extends CustomResource {
    private PipelineRunSpec spec;

    public PipelineRunSpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineRunSpec spec) {
        this.spec = spec;
    }

    public String getKind() {
        return "Pipeline";
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "spec=" + spec +
                '}';
    }
}