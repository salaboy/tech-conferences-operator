package com.salaboy.conferences.tekton;

import io.radanalytics.operator.common.AbstractOperator;
import io.radanalytics.operator.common.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Operator(forKind=PipelineInfo.class, prefix = "tekton.dev/v1alpha1")
public class PipelineOperator extends AbstractOperator<PipelineInfo> {

    private Logger logger = LoggerFactory.getLogger(PipelineOperator.class);

    @Override
    protected void onAdd(PipelineInfo pipelineInfo) {
        logger.info("Adding Pipeline: " + pipelineInfo);

    }

    @Override
    protected void onDelete(PipelineInfo pipelineInfo) {
        logger.info("Deleting pipeline: " + pipelineInfo);
    }
}
