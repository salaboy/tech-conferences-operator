package com.salaboy.conferences.crds.conference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Objects;
import java.util.Set;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConferenceSpec implements KubernetesResource {

    private String version;
    private String selector;

    private String status = "UNKNOWN";

    private String url = "NO URL YET.";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }


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

    @Override
    public String toString() {
        return "ConferenceSpec{" +
                "version='" + version + '\'' +
                ", selector='" + selector + '\'' +
                ", status='" + status + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
