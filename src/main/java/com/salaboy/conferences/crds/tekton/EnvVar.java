package com.salaboy.conferences.crds.tekton;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvVar {
    private String name;
    private String value;
//    private String valueFrom;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

//    public String getValueFrom() {
//        return valueFrom;
//    }
//
//    public void setValueFrom(String valueFrom) {
//        this.valueFrom = valueFrom;
//    }

    @Override
    public String toString() {
        return "EnvVar{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
//                ", valueFrom='" + valueFrom + '\'' +
                '}';
    }
}
