package com.salaboy.conferences.crds.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;

import java.util.Objects;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conference extends CustomResource {

    private ConferenceSpec spec;


    public ConferenceSpec getSpec() {
        return spec;
    }

    public void setSpec(ConferenceSpec spec) {
        this.spec = spec;
    }


    @Override
    public String toString() {
        return "Conference{" +
                super.toString() +
                "spec=" + spec +
                '}';
    }


}
