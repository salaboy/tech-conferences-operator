package com.salaboy.conferences;

import com.salaboy.conferences.core.K8SCoreRuntime;
import com.salaboy.conferences.crds.conference.*;
import com.salaboy.conferences.crds.jenkinsx.pipelineactivity.DoneablePipelineActivity;
import com.salaboy.conferences.crds.jenkinsx.pipelineactivity.PipelineActivity;
import com.salaboy.conferences.crds.jenkinsx.pipelineactivity.PipelineActivityList;
import com.salaboy.conferences.utils.ComparableVersion;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ConferencesOperator {

    // Is the service On?
    private boolean on = true;
    private boolean initDone = false;
    private boolean crdsFound = false;

    private Logger logger = LoggerFactory.getLogger(ConferencesOperator.class);
    private CustomResourceDefinition conferenceCRD = null;
    private CustomResourceDefinition pipelineCRD = null;
    private CustomResourceDefinition pipelineResourceCRD = null;
    private CustomResourceDefinition pipelineRunCRD = null;
    private CustomResourceDefinition pipelineActivityCRD = null;
    private CustomResourceDefinition taskCRD = null;
    private CustomResourceDefinition taskRunCRD = null;
    private boolean conferenceWatchRegistered = false;
    private boolean pipelineActivityWatchRegistered = false;
    private String conferencesResourceVersion;
    private String pipelineActivitiesResourceVersion;
    private NonNamespaceOperation<Conference, ConferenceList, DoneableConference, Resource<Conference, DoneableConference>> conferenceCRDClient;
    private NonNamespaceOperation<PipelineActivity, PipelineActivityList, DoneablePipelineActivity, Resource<PipelineActivity, DoneablePipelineActivity>> pipelineActivityCRDClient;


    @Autowired
    private ConferenceService conferenceService;


    @Autowired
    private K8SCoreRuntime k8SCoreRuntime;

    public void bootstrap() {
        crdsFound = areRequiredCRDsPresent();
        if (crdsFound) {
            initDone = init();
        }
    }

    /*
     * Check for Required CRDs
     */
    private boolean areRequiredCRDsPresent() {
        try {
            conferenceService.registerCustomResourcesForRuntime();

            CustomResourceDefinitionList crds = k8SCoreRuntime.getCustomResourceDefinitionList();
            for (CustomResourceDefinition crd : crds.getItems()) {
                ObjectMeta metadata = crd.getMetadata();
                if (metadata != null) {
                    String name = metadata.getName();


                    if (ConferenceCRDs.CONF_CRD_NAME.equals(name)) {
                        conferenceCRD = crd;
                    }
                    if (ConferenceCRDs.JENKINSX_PIPELINEACTIVITY_CRD_NAME.equals(name)) {
                        pipelineActivityCRD = crd;
                    }
                }
            }
            if (allCRDsFound()) {
                logger.info("\t > Conference CRD: " + conferenceCRD.getMetadata().getName());
                logger.info("\t > Jenkins X PipelineActivity CRD: " + pipelineActivityCRD.getMetadata().getName());

                return true;
            } else {
                logger.error("> Custom CRDs required to work not found please check your installation!");
                logger.error("\t > Conference CRD: " + ((conferenceCRD == null) ? " NOT FOUND " : conferenceCRD.getMetadata().getName()));
                logger.error("\t > Jenkins X PipelineActivity CRD: " + ((pipelineActivityCRD == null) ? " NOT FOUND " : pipelineActivityCRD.getMetadata().getName()));

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("> Init sequence not done");
        }
        return false;
    }

    /*
     * Init can only be called if all the required CRDs are present
     *  - It creates the CRD clients to be able to watch and execute operations
     *  - It loads the existing resources (current state in the cluster)
     *  - It register the watches for our CRDs
     */
    private boolean init() {
        logger.info("> Conferences K8s Operator is Starting!");
        // Creating CRDs Clients
        conferenceCRDClient = k8SCoreRuntime.customResourcesClient(conferenceCRD, Conference.class, ConferenceList.class, DoneableConference.class).inNamespace(k8SCoreRuntime.getNamespace());
        pipelineActivityCRDClient = k8SCoreRuntime.customResourcesClient(pipelineActivityCRD, PipelineActivity.class, PipelineActivityList.class, DoneablePipelineActivity.class).inNamespace("jx");

        if (loadExistingResources() && watchOurCRDs()) {
            return true;
        }

        return false;

    }

    /*
     * Check that all the CRDs are found for this operator to work
     */
    private boolean allCRDsFound() {
        if (conferenceCRD == null && pipelineActivityCRD == null) {

            return false;
        }
        return true;
    }

    /*
     * Watch our CRDs
     *  Register watches if they were not registered yet
     */
    private boolean watchOurCRDs() {
        // Watch for our CRDs
        if (!conferenceWatchRegistered) {
            registerConferenceWatch();
        }
        if (!pipelineActivityWatchRegistered) {
            registerPipelineActivityWatch();
        }
        if (conferenceWatchRegistered && pipelineActivityWatchRegistered) {
            //&& pipelineWatchRegistered && pipelineResourceWatchRegistered
            // && pipelineRunWatchRegistered && taskWatchRegistered && taskRunWatchRegistered) {
            logger.info("> All CRDs Found, init complete");
            return true;
        } else {
            logger.error("> CRDs missing, check your installation and run init again");
            return false;
        }
    }

    /*
     * Load existing instances of our CRDs
     *  - This checks the existing resources and make sure that they are loaded correctly
     *  - This also performs the binding of a service to its app
     */
    private boolean loadExistingResources() {
        // Load Existing Conferences
        List<Conference> conferenceList = conferenceCRDClient.list().getItems();
        if (!conferenceList.isEmpty()) {
            conferencesResourceVersion = conferenceList.get(0).getMetadata().getResourceVersion();
            logger.info(">> Conference Resource Version: " + conferencesResourceVersion);
            conferenceList.forEach(conference -> {
                conferenceService.addConference(conference.getMetadata().getName(), conference);
                lookForPipelineActivitiesForEachModule(conference);
                logger.info("> Conf " + conference + " found.");
            });

        }

        List<PipelineActivity> pipelineActivityList = pipelineActivityCRDClient.list().getItems();
        if (!pipelineActivityList.isEmpty()) {
            pipelineActivitiesResourceVersion = pipelineActivityList.get(0).getMetadata().getResourceVersion();
            logger.info(">> PipelineRun Activities Version: " + pipelineActivitiesResourceVersion);
            logger.info(">>>> Looking for pipeline activities: ");
            for (PipelineActivity pa : pipelineActivityList) {
                logger.info("> PipelineActivity: " + pa);
                linkPipelineToConferenceModule(pa);
            }
        }

        logger.info(">>>> FINISHING LOADING EXISTING RESOURCES: ");
        logger.info(">>> Conferences: ");
        conferenceService.getConferencesMap().forEach((key, value) -> logger.info("\t" + key + ":" + value));
        logger.info(">>> Conferences Pipelines: ");
        for (String confName : conferenceService.getConferencePipelines().keySet()) {
            logger.info("Conference Name: " + confName);
            conferenceService.getConferencePipeline(confName).forEach((key, value) -> logger.info("\t" + key + ":" + value));
        }


        return true;
    }

    private void linkPipelineToConferenceModule(PipelineActivity pa) {
        for (Conference conf : conferenceService.getConferencesMap().values()) {
            List<ModuleRef> modules = conf.getSpec().getModules();
            for (ModuleRef module : modules) {
                // Let's use the repository name to match with the conference module name
                String repository = pa.getMetadata().getLabels().get("repository");
                if (repository.equals(module.getName())) {
                    conferenceService.addPipelineToConf(conf.getMetadata().getName(), repository, pa);
                }
            }
        }
    }


    private <T extends CustomService> T checkAndAddOwnerReferences(T service) {
        if (service.getMetadata().getOwnerReferences() != null) {
            String appName = service.getMetadata().getLabels().get("app");
            if (appName != null && !appName.isEmpty()) {
                Conference conference = conferenceService.getConference(appName);
                return conferenceService.owns(conference, service);
            }
        }
        return service;
    }


    /*
     * Register an PipelineActivity Resource Watch
     *  - This watch is in charge of adding and removing pipelineActivity to/from the In memory desired state
     */
    private void registerPipelineActivityWatch() {
        logger.info("> Registering Pipeline Activity CRD Watch");
        pipelineActivityCRDClient.watch(new Watcher<PipelineActivity>() {
            @Override
            public void eventReceived(Watcher.Action action, PipelineActivity pipelineActivity) {
                String repository = pipelineActivity.getMetadata().getLabels().get("repository");

                String pipeline = pipelineActivity.getSpec().getPipeline();

                String version = pipelineActivity.getSpec().getVersion();
                String status = pipelineActivity.getSpec().getStatus();

                if (action.equals(Action.ADDED)) {


                    logger.info(">>>>!!!!!ADDED PipelineActivity: " + pipeline + ",  for repo:  " + repository + ", in version:  " + version + ", with status: " + status);
                    linkPipelineToConferenceModule(pipelineActivity);


                }
                if (action.equals(Action.DELETED)) {
                    logger.info(">> Deleting PipelineActivity: " + pipelineActivity.getMetadata().getName());
                    //@TODO: unlink activity from conference module

                }
                if (action.equals(Action.MODIFIED)) {
                    logger.info(">>>>!!!!! Modified PipelineActivity: " + pipeline + ",  for repo:  " + repository + ", in version:  " + version + ", with status: " + status);
                    linkPipelineToConferenceModule(pipelineActivity);
                }

            }

            @Override
            public void onClose(KubernetesClientException cause) {
                logger.error(">>> ERROR ON WATCH: " + cause.toString());
            }
        });
        pipelineActivityWatchRegistered = true;

    }


    /*
     * Register an Conference Resource Watch
     *  - This watch is in charge of adding and removing conferences to/from the In memory desired state
     */
    private void registerConferenceWatch() {
        logger.info("> Registering Conference CRD Watch");
        conferenceCRDClient.watch(new Watcher<Conference>() {
            @Override
            public void eventReceived(Watcher.Action action, Conference conference) {
                if (action.equals(Action.ADDED)) {
                    logger.info(">> Adding Conf: " + conference.getMetadata().getName());
                    conferenceService.addConference(conference.getMetadata().getName(), conference);
                    lookForPipelineActivitiesForEachModule(conference);
                }
                if (action.equals(Action.DELETED)) {
                    logger.info(">> Deleting Conf: " + conference.getMetadata().getName());
                    conferenceService.removeConference(conference.getMetadata().getName());
                }
                if (action.equals(Action.MODIFIED)) {
                    logger.info(">> Modifying Conf: " + conference.getMetadata().getName());
                    conferenceService.addConference(conference.getMetadata().getName(), conference);
                    lookForPipelineActivitiesForEachModule(conference);
                }

            }

            @Override
            public void onClose(KubernetesClientException cause) {
            }
        });
        conferenceWatchRegistered = true;

    }

    private void lookForPipelineActivitiesForEachModule(Conference conference) {
        for (ModuleRef mr : conference.getSpec().getModules()) {
            List<PipelineActivity> pipelineActivityList = pipelineActivityCRDClient.list().getItems();
            for (PipelineActivity pa : pipelineActivityList) {
                if (pa.getMetadata().getLabels().get("repository").equals(mr.getName())) {
                    conferenceService.addPipelineToConf(conference.getMetadata().getName(), mr.getName(), pa);
                }
            }

        }
    }


    /*
     * Reconcile contains the logic that understand how services relates to applications and the application state
     *   matches the desired state with current state in K8s
     */
    public void reconcile() {
        if (conferenceService.getConferences().isEmpty()) {
            logger.info("> No Conferences found.");
        }
        // For each App Desired State
        conferenceService.getConferencesMap().keySet().forEach(confName ->
        {
            Conference conference = conferenceService.getConference(confName);
            logger.info("> Conference Found: " + confName + ". Scanning ...");

            if (conferenceService.areConferenceServicesHealthy(conference, true)) {

                String url = conferenceService.exposeAndSetConferenceURL(conference, true);
                conference.getSpec().setUrl(url);
                if (areConferencePipelinesOK(conference)) {
                    conference.getSpec().setStatus("HEALTHY");
                    logger.info("\t> Conference: " + confName + ", status:  HEALTHY, URL: " + url + " \n");
                } else {
                    conference.getSpec().setStatus("UNHEALTHY");
                    logger.warn("\t > Conference Name: " + confName + " is not healthy  due failing pipelines");
                    logger.info("\t> Conference: " + confName + ", status:  UNHEALTHY, URL: " + url + " \n");
                }

            } else {
                // NO: Change the state and remove the URL
                logger.error("\t > Conference Name: " + confName + " is down due missing services");
                conference.getSpec().setStatus("DOWN");
                conferenceService.exposeAndSetConferenceURL(conference, false);
                conference.getSpec().setUrl("N/A");
                logger.info("\t> Conference: " + confName + ", status: DOWN. \n ");
            }

            // Notify K8s about the updates required
            conferenceCRDClient.createOrReplace(conference);

        });

    }

    private boolean areConferencePipelinesOK(Conference conference) {

        List<ModuleRef> modules = conference.getSpec().getModules();
        Map<String, List<PipelineActivity>> conferencePipelines = conferenceService.getConferencePipeline(conference.getMetadata().getName());

        if (conferencePipelines == null || conferencePipelines.keySet() == null || conferencePipelines.keySet().isEmpty()) {
            return false;
        }
        if (modules.size() == conferencePipelines.keySet().size()) {
            for (ModuleRef m : modules) {
                List<PipelineActivity> pipelineActivities = conferencePipelines.get(m.getName());

                if (pipelineActivities == null || pipelineActivities.isEmpty()) {
                    logger.error("> Module not found: " + m.getName() + ", for conference: " + conference.getMetadata().getName());
                    return false;
                }
                pipelineActivities.sort(new Comparator<PipelineActivity>() {
                    @Override
                    public int compare(PipelineActivity o1, PipelineActivity o2) {
                        return new ComparableVersion(o2.getSpec().getVersion()).compareTo(new ComparableVersion(o1.getSpec().getVersion()));
                    }
                });
                PipelineActivity pipelineActivity = pipelineActivities.get(0);
                logger.info(">>>Last Pipeline Activity: " + pipelineActivity.getSpec().getPipeline()
                        + ", version: " + pipelineActivity.getSpec().getVersion()
                        + ", status:" + pipelineActivity.getSpec().getStatus());
                if (!pipelineActivity.getSpec().getStatus().equals("Succeeded")) {
                    return false;
                }

            }
            return true;
        }


        return false;
    }



    public CustomResourceDefinition getConferenceCRD() {
        return conferenceCRD;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public boolean isInitDone() {
        return initDone;
    }


}
