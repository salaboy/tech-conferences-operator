package com.salaboy.conferences;

public class ConferenceCRDs {
    public static String CONF_CRD_GROUP = "alpha.k8s.salaboy.com";
    public static String CONF_CRD_VERSION = "/v1";
    public static String CONF_CRD_GROUP_VERSION = CONF_CRD_GROUP + CONF_CRD_VERSION;
    public static String CONF_CRD_NAME = "conferences." + CONF_CRD_GROUP;

    public static String TEKTON_CRD_GROUP = "tekton.dev";
    public static String TEKTON_CRD_VERSION = "/v1alpha1";
    public static String TEKTON_CRD_GROUP_VERSION = TEKTON_CRD_GROUP + TEKTON_CRD_VERSION;
    public static String TEKTON_PIPELINERUN_CRD_NAME = "pipelineruns." + TEKTON_CRD_GROUP;
    public static String TEKTON_PIPELINE_CRD_NAME = "pipelines." + TEKTON_CRD_GROUP;
    public static String TEKTON_TASKRUN_CRD_NAME = "taskruns." + TEKTON_CRD_GROUP;
    public static String TEKTON_TASK_CRD_NAME = "tasks." + TEKTON_CRD_GROUP;

//    public static String MICROSERVICE_CRD_NAME = "microservices." + CONF_CRD_GROUP;
//    public static String GATEWAY_CRD_NAME = "gateways." + CONF_CRD_GROUP;
//    public static String REGISTRY_CRD_NAME = "registries." + CONF_CRD_GROUP;

}
