package com.salaboy.conferences.crds.tekton.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pipeline extends CustomResource {
    private PipelineSpec spec;

    public PipelineSpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineSpec spec) {
        this.spec = spec;
    }

    public String getKind() {
        return "Pipeline";
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "name=" + getMetadata().getName() + "," +
                "spec=" + spec +
                '}';
    }
}
