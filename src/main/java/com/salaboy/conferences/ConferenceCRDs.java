package com.salaboy.conferences;

public class ConferenceCRDs {
    // Conference
    public static String CONF_CRD_GROUP = "alpha.k8s.salaboy.com";
    public static String CONF_CRD_VERSION = "/v1";
    public static String CONF_CRD_GROUP_VERSION = CONF_CRD_GROUP + CONF_CRD_VERSION;
    public static String CONF_CRD_NAME = "conferences." + CONF_CRD_GROUP;


    // Jenkins X
    public static String JENKINSX_CRD_GROUP = "jenkins.io";
    public static String JENKINSX_CRD_VERSION = "/v1";
    public static String JENKINSX_CRD_GROUP_VERSION = JENKINSX_CRD_GROUP + JENKINSX_CRD_VERSION;
    public static String JENKINSX_PIPELINEACTIVITY_CRD_NAME = "pipelineactivities." + JENKINSX_CRD_GROUP;


}
