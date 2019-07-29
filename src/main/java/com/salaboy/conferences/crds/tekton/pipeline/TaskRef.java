package com.salaboy.conferences.crds.tekton.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Parameter;


import java.util.Arrays;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRef {
    private String name;
    private List<Parameter> params;
    private ResourcesRef resources;

    public TaskRef() {
    }

    public TaskRef(String name, List<Parameter> params, ResourcesRef resources) {
        this.name = name;
        this.params = params;
        this.resources = resources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParams() {
        return params;
    }

    public void setParams(List<Parameter> params) {
        this.params = params;
    }

    public ResourcesRef getResources() {
        return resources;
    }

    public void setResources(ResourcesRef resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "TaskRef{" +
                "name='" + name + '\'' +
                ", params=" + Arrays.toString(params.toArray()) +
                ", resources=" + resources +
                '}';
    }
}
