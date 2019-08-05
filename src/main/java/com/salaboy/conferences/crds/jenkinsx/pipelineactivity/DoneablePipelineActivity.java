package com.salaboy.conferences.crds.jenkinsx.pipelineactivity;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneablePipelineActivity extends CustomResourceDoneable<PipelineActivity> {

    public DoneablePipelineActivity(PipelineActivity resource, Function<PipelineActivity, PipelineActivity> function) {
        super(resource, function);
    }
}
