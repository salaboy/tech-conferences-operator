package com.salaboy.conferences.crds.jenkinsx.pipelineactivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineActivity extends CustomResource {
    private PipelineActivitySpec spec;

    public PipelineActivitySpec getSpec() {
        return spec;
    }

    public void setSpec(PipelineActivitySpec spec) {
        this.spec = spec;
    }

    public String getKind() {
        return "PipelineResource";
    }

    @Override
    public String toString() {
        return "PipelineActivity{" +
                "name=" + getMetadata().getName() + "," +
                "spec=" + spec +
                '}';
    }
}
