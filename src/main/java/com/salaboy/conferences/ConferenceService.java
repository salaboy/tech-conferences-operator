package com.salaboy.conferences;

import com.salaboy.conferences.core.K8SCoreRuntime;
import com.salaboy.conferences.crds.conference.Conference;
import com.salaboy.conferences.crds.conference.CustomService;
import com.salaboy.conferences.crds.conference.ModuleRef;
import com.salaboy.conferences.crds.jenkinsx.pipelineactivity.PipelineActivity;
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
    private Map<String, Map<String, String>> conferenceServices = new ConcurrentHashMap<>();
    private Map<String, String> conferencesUrls = new HashMap<>();

    @Autowired
    private K8SCoreRuntime k8SCoreRuntime;


    /*
     * Add the logic to define what are the rules for your conference to be UP or DOWN
     */
    public boolean areConferenceServicesHealthy(Conference conference, boolean log) {
        int serviceDown = 0;
        if (conferenceServices.get(conference.getMetadata().getName()) == null) {
            conferenceServices.put(conference.getMetadata().getName(), new HashMap<>());
        }
        for (ModuleRef mr : conference.getSpec().getModules()) {
            if (k8SCoreRuntime.isServiceAvailable(mr.getName())) {
                conferenceServices.get(conference.getMetadata().getName()).put(mr.getName(), "UP");
            } else {
                conferenceServices.get(conference.getMetadata().getName()).put(mr.getName(), "DOWN");
                serviceDown++;
            }
        }
        if (serviceDown == 0) {
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
            List<OwnerReference> ownerReferencesFromApp = createOwnerReferencesFromConference(conference);
            ObjectMeta objectMetaMicroService = service.getMetadata();
            objectMetaMicroService.setOwnerReferences(ownerReferencesFromApp);
            service.setMetadata(objectMetaMicroService);


        }

        return service;
    }


    /*
     * Create owner references for modules of an application
     */
    private List<OwnerReference> createOwnerReferencesFromConference(Conference conference) {
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
                .filter(app -> areConferenceServicesHealthy(app, false))
                .map(a -> a.getMetadata().getName())
                .collect(Collectors.toList());
    }

    public void addConference(String conferenceName, Conference conference) {
        conferences.put(conferenceName, conference);
    }

    public Conference removeConference(String conferenceName) {
        conferencePipelines.remove(conferenceName);
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


    public void registerCustomResourcesForRuntime() {
        k8SCoreRuntime.registerCustomKind(ConferenceCRDs.CONF_CRD_GROUP_VERSION, "Conference", Conference.class);
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


    public String getModuleServiceStatus(String confName, String module) {
        if (conferenceServices.get(confName) != null && conferenceServices.get(confName).get(module) != null) {
            return conferenceServices.get(confName).get(module);
        }
        return "";
    }

    public String getPipelineActivityLastStatusForModule(String confName, String module) {
        if (conferencePipelines.get(confName) != null) {
            List<PipelineActivity> pipelineActivities = conferencePipelines.get(confName).get(module);
            if (pipelineActivities != null) {
                pipelineActivities.sort((o1, o2) -> new ComparableVersion(o2.getSpec().getVersion()).compareTo(new ComparableVersion(o1.getSpec().getVersion())));
                return pipelineActivities.get(0).getSpec().getStatus();
            }
        }
        return "";
    }

    public String getPipelineActivityLastVersionForModule(String confName, String module) {
        if (conferencePipelines.get(confName) != null) {
            List<PipelineActivity> pipelineActivities = conferencePipelines.get(confName).get(module);
            if (pipelineActivities != null) {
                pipelineActivities.sort((o1, o2) -> new ComparableVersion(o2.getSpec().getVersion()).compareTo(new ComparableVersion(o1.getSpec().getVersion())));
                return pipelineActivities.get(0).getSpec().getVersion();
            }
        }
        return "";
    }
}
