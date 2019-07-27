package com.salaboy.conferences;

import com.salaboy.conferences.core.K8SCoreRuntime;
import com.salaboy.conferences.crds.conference.Conference;
import com.salaboy.conferences.crds.conference.CustomService;
import com.salaboy.conferences.crds.tekton.pipeline.Pipeline;
import com.salaboy.conferences.crds.tekton.pipelinerun.PipelineRun;
import com.salaboy.conferences.crds.tekton.task.Task;
import com.salaboy.conferences.crds.tekton.taskrun.TaskRun;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConferenceService {
    private Logger logger = LoggerFactory.getLogger(ConferenceService.class);
    private Map<String, Conference> conferences = new ConcurrentHashMap<>();
    private Map<String, String> appsUrls = new HashMap<>();

    @Autowired
    private K8SCoreRuntime k8SCoreRuntime;


    /*
     * Add the logic to define what are the rules for your application to be UP or DOWN
     */
    public boolean isConferenceHealthy(Conference conference, boolean log) {

//        String gateway = conference.getSpec().getGateway();
//        if (!k8SCoreRuntime.isServiceAvailable(gateway)) {
//            logger.error("Service: " + gateway + " doesn't exist. ");
//            return false;
//        }
//        String registry = conference.getSpec().getRegistry();
//        if (!k8SCoreRuntime.isServiceAvailable(registry)) {
//            logger.error("Service: " + registry + " doesn't exist. ");
//            return false;
//        }
//        Set<MicroServiceDescr> microservices = conference.getSpec().getMicroservices();
//        for (MicroServiceDescr microServiceDescr : microservices) {
//            if (!k8SCoreRuntime.isServiceAvailable(microServiceDescr.getServiceName())) {
//                logger.error("Service: " + microServiceDescr.getServiceName() + " doesn't exist. ");
//                return false;
//            }
//        }


        return true;
    }

    public boolean checkMicroServicesAvailability(int size, boolean[] microServicesAvailable) {
        if (microServicesAvailable.length == size && microServicesAvailable.length > 0) {
            for (boolean a : microServicesAvailable) {
                if (!a) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /*
     * Add CustomService to Application and set up the owner references
     * @return the modified CustomService resource
     */
    public <T extends CustomService> T owns(Conference conference, T service) {

        if (conference != null) {
            //Set OwnerReferences: the Application Owns the MicroService
            List<OwnerReference> ownerReferencesFromApp = createOwnerReferencesFromApp(conference);
            ObjectMeta objectMetaMicroService = service.getMetadata();
            objectMetaMicroService.setOwnerReferences(ownerReferencesFromApp);
            service.setMetadata(objectMetaMicroService);


        }

        return service;
    }


    /*
     * Create owner references for modules of an application
     */
    private List<OwnerReference> createOwnerReferencesFromApp(Conference conference) {
        if (conference.getMetadata().getUid() == null || conference.getMetadata().getUid().isEmpty()) {
            throw new IllegalStateException("The app needs to be saved first, the UUID needs to be present.");
        }
        OwnerReference ownerReference = new OwnerReference();
        ownerReference.setUid(conference.getMetadata().getUid());
        ownerReference.setName(conference.getMetadata().getName());
        ownerReference.setKind(conference.getKind());
        ownerReference.setController(true);
        ownerReference.setBlockOwnerDeletion(true);
        ownerReference.setApiVersion(conference.getApiVersion());

        return Arrays.asList(ownerReference);

    }

    public List<String> getConferences() {
        return conferences.values().stream()
                .filter(app -> isConferenceHealthy(app, false))
                .map(a -> a.getMetadata().getName())
                .collect(Collectors.toList());
    }

    public void addConference(String conferenceName, Conference conference) {
        conferences.put(conferenceName, conference);
    }

    public Conference removeConference(String conferenceName) {
        return conferences.remove(conferenceName);
    }

    public Conference getConference(String conferenceName) {
        return conferences.get(conferenceName);
    }

    public String getConferenceUrl(String conferenceName) {
        return appsUrls.get(conferenceName);
    }

    public String createAndSetConferenceURL(String appName, String appVersion) {
        String externalIp = k8SCoreRuntime.findExternalIP();
        String url = "http://" + externalIp + "/apps/" + appName + "/" + appVersion + "/";
        appsUrls.put(appName, url);
        return url;
    }

    public Map<String, Conference> getConferencesMap() {
        return conferences;
    }

    public Map<String, String> getAppsUrls() {
        return appsUrls;
    }

    public void registerCustomResourcesForRuntime() {
//        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.APP_CRD_GROUP + "/v1", "MicroService", MicroService.class);
//        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.APP_CRD_GROUP + "/v1", "Gateway", Gateway.class);
//        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.APP_CRD_GROUP + "/v1", "Registry", Registry.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.CONF_CRD_GROUP_VERSION, "Conference", Conference.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "Pipeline", Pipeline.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "PipelineRun", PipelineRun.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "Task", Task.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "TaskRun", TaskRun.class);

    }
}
