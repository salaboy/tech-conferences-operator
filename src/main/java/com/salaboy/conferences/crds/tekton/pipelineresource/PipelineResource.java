package com.salaboy.conferences.crds.tekton.pipelineresource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineResource extends CustomResource {
    private PipelineResourceSpec spec;

    public PipelineResourceSpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineResourceSpec spec) {
        this.spec = spec;
    }

    public String getKind() {
        return "PipelineResource";
    }

    @Override
    public String toString() {
        return "PipelineResource{" +
                "name=" + getMetadata().getName() + "," +
                "spec=" + spec +
                '}';
    }
}
