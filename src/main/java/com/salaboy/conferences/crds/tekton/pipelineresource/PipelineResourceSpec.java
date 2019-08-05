package com.salaboy.conferences.crds.tekton.pipelineresource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Parameter;
import com.salaboy.conferences.crds.tekton.Resource;
import com.salaboy.conferences.crds.tekton.pipeline.TaskRef;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineResourceSpec implements KubernetesResource {
    private List<Parameter> params;
    private String type;


    public List<Parameter> getParams() {
        return params;
    }

    public void setParams(List<Parameter> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PipelineResourceSpec{" +
                "params=" + Arrays.toString(params.toArray()) +
                ", type='" + type + '\'' +
                '}';
    }
}
