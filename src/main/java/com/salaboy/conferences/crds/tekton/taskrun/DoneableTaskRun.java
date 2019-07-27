package com.salaboy.conferences.crds.tekton.taskrun;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableTaskRun extends CustomResourceDoneable<TaskRun> {

    public DoneableTaskRun(TaskRun resource, Function<TaskRun, TaskRun> function) {
        super(resource, function);
    }
}
