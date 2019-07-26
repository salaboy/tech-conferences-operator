package com.salaboy.conferences.crds.tekton;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneablePipeline extends CustomResourceDoneable<Pipeline> {

    public DoneablePipeline(Pipeline resource, Function<Pipeline, Pipeline> function) {
        super(resource, function);
    }
}
