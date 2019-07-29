package com.salaboy.conferences.crds.tekton.taskrun;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.Parameter;
import com.salaboy.conferences.crds.tekton.task.TaskInputsRef;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import org.apache.naming.ResourceRef;

import java.util.List;
import java.util.Map;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRunSpec implements KubernetesResource {
    private TaskInputsRef inputs;

    public TaskInputsRef getInputs() {
        return inputs;
    }

    public void setInputs(TaskInputsRef inputs) {
        this.inputs = inputs;
    }

    @Override
    public String toString() {
        return "TaskRunSpec{" +
                "inputs=" + inputs +
                '}';
    }
}
