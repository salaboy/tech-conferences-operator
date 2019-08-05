package com.salaboy.conferences.crds.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConferenceSpec implements KubernetesResource {

    private String status = "UNKNOWN";

    private String url = "NO URL YET.";

    private List<ModuleRef> modules;

    private String owner;
    private String location;
    private String year;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ModuleRef> getModules() {
        return modules;
    }

    public void setModules(List<ModuleRef> modules) {
        this.modules = modules;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "ConferenceSpec{" +
                "status='" + status + '\'' +
                ", url='" + url + '\'' +
                ", modules=" + Arrays.toString(modules.toArray()) +
                ", owner='" + owner + '\'' +
                ", location='" + location + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
