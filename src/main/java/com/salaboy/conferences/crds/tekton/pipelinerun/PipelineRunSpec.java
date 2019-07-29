package com.salaboy.conferences.crds.tekton.pipelinerun;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Parameter;
import com.salaboy.conferences.crds.tekton.Resource;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineRunSpec implements KubernetesResource {
    private List<Parameter> params;
    private List<Resource> resources;


    public PipelineRunSpec() {
    }

    public PipelineRunSpec(List<Parameter> params, List<Resource> resources) {
        this.params = params;
        this.resources = resources;
    }

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


    @Override
    public String toString() {
        return "PipelineRunSpec{" +
                "params=" + params +
                ", resources=" + resources +
                '}';
    }
}
