package com.salaboy.conferences;

import com.salaboy.conferences.core.K8SCoreRuntime;
import com.salaboy.conferences.crds.conference.Conference;
import com.salaboy.conferences.crds.conference.CustomService;
import com.salaboy.conferences.crds.conference.ModuleRef;
import com.salaboy.conferences.crds.jenkinsx.pipelineactivity.PipelineActivity;
import com.salaboy.conferences.crds.tekton.pipeline.Pipeline;
import com.salaboy.conferences.crds.tekton.pipelineresource.PipelineResource;
import com.salaboy.conferences.crds.tekton.pipelinerun.PipelineRun;
import com.salaboy.conferences.crds.tekton.task.Task;
import com.salaboy.conferences.crds.tekton.taskrun.TaskRun;
import com.salaboy.conferences.utils.ComparableVersion;
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
    private Map<String, Map<String, List<PipelineActivity>>> conferencePipelines = new ConcurrentHashMap<>();
    private Map<String, String> conferencesUrls = new HashMap<>();

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
        return conferencesUrls.get(conferenceName);
    }

    public String exposeAndSetConferenceURL(Conference conference, boolean exposed) {
        List<ModuleRef> modules = conference.getSpec().getModules();
        String siteName = "";
        for (ModuleRef mr : modules) {
            if (mr.getName().endsWith("-site")) {
                siteName = mr.getName();
            }
        }
        String externalIp = k8SCoreRuntime.exposeConferenceSite(conference.getMetadata().getName(), siteName, exposed);
        if (exposed) {
            conferencesUrls.put(conference.getMetadata().getName(), externalIp);
        } else {
            conferencesUrls.remove(conference.getMetadata().getName());
        }
        return externalIp;
    }

    public Map<String, Conference> getConferencesMap() {
        return conferences;
    }

    public Map<String, String> getConferencesUrls() {
        return conferencesUrls;
    }

    public void registerCustomResourcesForRuntime() {
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.CONF_CRD_GROUP_VERSION, "Conference", Conference.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "Pipeline", Pipeline.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "PipelineResource", PipelineResource.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "PipelineRun", PipelineRun.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "Task", Task.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.TEKTON_CRD_GROUP_VERSION, "TaskRun", TaskRun.class);
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.JENKINSX_CRD_GROUP_VERSION, "PipelineActivity", PipelineActivity.class);

    }

    public void addPipelineToConf(String confName, String module, PipelineActivity pa) {
        if (pa.getSpec().getVersion() == null) {
            return;
        }
        Map<String, List<PipelineActivity>> conferenceModules = conferencePipelines.get(confName);
        if (conferenceModules == null) {
            conferencePipelines.put(confName, new ConcurrentHashMap<>());
        }
        List<PipelineActivity> pipelineActivities = conferencePipelines.get(confName).get(module);
        if (pipelineActivities == null) {
            conferencePipelines.get(confName).put(module, new ArrayList<>());
        }
        //Before adding check if there is a pipelineActivity for that version, if so update
        boolean update = false;
        PipelineActivity pipelineActivityToUpdate = null;

        for (PipelineActivity pipelineActivity : conferencePipelines.get(confName).get(module)) {
            if (pipelineActivity.getSpec().getVersion().equals(pa.getSpec().getVersion())) {
                update = true;
                pipelineActivityToUpdate = pipelineActivity;
            }
        }
        if (update) {
            conferencePipelines.get(confName).get(module).remove(pipelineActivityToUpdate);
        }
        conferencePipelines.get(confName).get(module).add(pa);
    }

    public Map<String, Map<String, List<PipelineActivity>>> getConferencePipelines() {
        return conferencePipelines;
    }

    public Map<String, List<PipelineActivity>> getConferencePipeline(String confName) {
        return conferencePipelines.get(confName);
    }


    public String getPipelineActivityLastStatusForModule(String confName, String module) {
        List<PipelineActivity> pipelineActivities = conferencePipelines.get(confName).get(module);
        pipelineActivities.sort((o1, o2) -> new ComparableVersion(o2.getSpec().getVersion()).compareTo(new ComparableVersion(o1.getSpec().getVersion())));
        return pipelineActivities.get(0).getSpec().getStatus();
    }

    public String getPipelineActivityLastVersionForModule(String confName, String module) {
        List<PipelineActivity> pipelineActivities = conferencePipelines.get(confName).get(module);
        pipelineActivities.sort((o1, o2) -> new ComparableVersion(o2.getSpec().getVersion()).compareTo(new ComparableVersion(o1.getSpec().getVersion())));
        return pipelineActivities.get(0).getSpec().getVersion();
    }
}
