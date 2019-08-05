package com.salaboy.conferences.crds.tekton.pipelineresource;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneablePipelineResource extends CustomResourceDoneable<PipelineResource> {

    public DoneablePipelineResource(PipelineResource resource, Function<PipelineResource, PipelineResource> function) {
        super(resource, function);
    }
}
