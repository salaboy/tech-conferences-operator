package com.salaboy.conferences.crds.tekton.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourcesRef {
    @JsonProperty("inputs")
    private List<InputRef> inputRefs;

    public ResourcesRef() {
    }

    public ResourcesRef(List<InputRef> inputRefs) {
        this.inputRefs = inputRefs;
    }

    public List<InputRef> getInputRefs() {
        return inputRefs;
    }

    public void setInputRefs(List<InputRef> inputRefs) {
        this.inputRefs = inputRefs;
    }

    @Override
    public String toString() {
        return "ResourcesRef{" +
                "inputs=" + Arrays.toString(inputRefs.toArray()) +
                '}';
    }
}
