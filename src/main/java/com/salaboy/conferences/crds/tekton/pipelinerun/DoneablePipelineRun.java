package com.salaboy.conferences.crds.tekton.pipelinerun;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneablePipelineRun extends CustomResourceDoneable<PipelineRun> {

    public DoneablePipelineRun(PipelineRun resource, Function<PipelineRun, PipelineRun> function) {
        super(resource, function);
    }
}
