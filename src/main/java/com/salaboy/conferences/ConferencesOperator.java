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
//    private boolean pipelineWatchRegistered = false;
//    private boolean pipelineResourceWatchRegistered = false;
//    private boolean pipelineRunWatchRegistered = false;
//    private boolean taskWatchRegistered = false;
//    private boolean taskRunWatchRegistered = false;


    private String conferencesResourceVersion;
    private String pipelineActivitiesResourceVersion;
//    private String pipelinesResourceVersion;
//    private String pipelineResourcesResourceVersion;
//    private String pipelineRunsResourceVersion;
//
//    private String tasksResourceVersion;
//    private String taskRunsResourceVersion;


    private NonNamespaceOperation<Conference, ConferenceList, DoneableConference, Resource<Conference, DoneableConference>> conferenceCRDClient;
    //    private NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> pipelineCRDClient;
//    private NonNamespaceOperation<PipelineResource, PipelineResourceList, DoneablePipelineResource, Resource<PipelineResource, DoneablePipelineResource>> pipelineResourceCRDClient;
    private NonNamespaceOperation<PipelineActivity, PipelineActivityList, DoneablePipelineActivity, Resource<PipelineActivity, DoneablePipelineActivity>> pipelineActivityCRDClient;
//    private NonNamespaceOperation<PipelineRun, PipelineRunList, DoneablePipelineRun, Resource<PipelineRun, DoneablePipelineRun>> pipelineRunCRDClient;
//    private NonNamespaceOperation<Task, TaskList, DoneableTask, Resource<Task, DoneableTask>> taskCRDClient;
//    private NonNamespaceOperation<TaskRun, TaskRunList, DoneableTaskRun, Resource<TaskRun, DoneableTaskRun>> taskRunCRDClient;


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
//                    if (ConferenceCRDs.TEKTON_PIPELINE_CRD_NAME.equals(name)) {
//                        pipelineCRD = crd;
//                    }
//                    if (ConferenceCRDs.TEKTON_PIPELINERESOURCE_CRD_NAME.equals(name)) {
//                        pipelineResourceCRD = crd;
//                    }
//                    if (ConferenceCRDs.TEKTON_PIPELINERUN_CRD_NAME.equals(name)) {
//                        pipelineRunCRD = crd;
//                    }
//                    if (ConferenceCRDs.TEKTON_TASK_CRD_NAME.equals(name)) {
//                        taskCRD = crd;
//                    }
//                    if (ConferenceCRDs.TEKTON_TASKRUN_CRD_NAME.equals(name)) {
//                        taskRunCRD = crd;
//                    }
                    if (ConferenceCRDs.JENKINSX_PIPELINEACTIVITY_CRD_NAME.equals(name)) {
                        pipelineActivityCRD = crd;
                    }
                }
            }
            if (allCRDsFound()) {
                logger.info("\t > Conference CRD: " + conferenceCRD.getMetadata().getName());
//                logger.info("\t > Tekton Pipeline CRD: " + pipelineCRD.getMetadata().getName());
//                logger.info("\t > Tekton PipelineResource CRD: " + pipelineResourceCRD.getMetadata().getName());
//                logger.info("\t > Tekton PipelineRun CRD: " + pipelineRunCRD.getMetadata().getName());
//                logger.info("\t > Tekton Task CRD: " + taskCRD.getMetadata().getName());
//                logger.info("\t > Tekton TaskRun CRD: " + taskRunCRD.getMetadata().getName());
                logger.info("\t > Jenkins X PipelineActivity CRD: " + pipelineActivityCRD.getMetadata().getName());

                return true;
            } else {
                logger.error("> Custom CRDs required to work not found please check your installation!");
                logger.error("\t > Conference CRD: " + ((conferenceCRD == null) ? " NOT FOUND " : conferenceCRD.getMetadata().getName()));
//                logger.error("\t > Tekton Pipeline CRD: " + ((pipelineCRD == null) ? " NOT FOUND " : pipelineCRD.getMetadata().getName()));
//                logger.error("\t > Tekton PipelineResource CRD: " + ((pipelineResourceCRD == null) ? " NOT FOUND " : pipelineResourceCRD.getMetadata().getName()));
//                logger.error("\t > Tekton PipelineRun CRD: " + ((pipelineRunCRD == null) ? " NOT FOUND " : pipelineRunCRD.getMetadata().getName()));
//                logger.error("\t > Tekton Task CRD: " + ((taskCRD == null) ? " NOT FOUND " : taskCRD.getMetadata().getName()));
//                logger.error("\t > Tekton TaskRun CRD: " + ((taskRunCRD == null) ? " NOT FOUND " : taskRunCRD.getMetadata().getName()));
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
//        pipelineCRDClient = k8SCoreRuntime.customResourcesClient(pipelineCRD, Pipeline.class, PipelineList.class, DoneablePipeline.class).inNamespace("jx");
//        pipelineResourceCRDClient = k8SCoreRuntime.customResourcesClient(pipelineResourceCRD, PipelineResource.class, PipelineResourceList.class, DoneablePipelineResource.class).inNamespace("jx");
//        pipelineRunCRDClient = k8SCoreRuntime.customResourcesClient(pipelineRunCRD, PipelineRun.class, PipelineRunList.class, DoneablePipelineRun.class).inNamespace("jx");
//        taskCRDClient = k8SCoreRuntime.customResourcesClient(taskCRD, Task.class, TaskList.class, DoneableTask.class).inNamespace("jx");
//        taskRunCRDClient = k8SCoreRuntime.customResourcesClient(taskRunCRD, TaskRun.class, TaskRunList.class, DoneableTaskRun.class).inNamespace("jx");
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
            // || pipelineCRD == null || pipelineResourceCRD == null
            //|| pipelineRunCRD == null || taskCRD == null || taskRunCRD == null

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
//        if (!pipelineWatchRegistered) {
//            registerPipelineWatch();
//        }
//        if (!pipelineResourceWatchRegistered) {
//            registerPipelineResourceWatch();
//        }
//        if (!pipelineRunWatchRegistered) {
//            registerPipelineRunWatch();
//        }
        if (!pipelineActivityWatchRegistered) {
            registerPipelineActivityWatch();
        }
//        if (!taskWatchRegistered) {
//            registerTaskWatch();
//        }
//        if (!taskRunWatchRegistered) {
//            registerTaskRunWatch();
//        }
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

//        List<Pipeline> pipelineList = pipelineCRDClient.list().getItems();
//        if (!pipelineList.isEmpty()) {
//            pipelinesResourceVersion = pipelineList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> Pipeline Resource Version: " + pipelinesResourceVersion);
//            logger.info(">>>> Looking for pipelines: ");
//            for (Pipeline p : pipelineList) {
//                logger.info("> Pipeline: " + p);
//            }
//        }
//
//        List<PipelineResource> pipelineResourceList = pipelineResourceCRDClient.list().getItems();
//        if (!pipelineResourceList.isEmpty()) {
//            pipelineResourcesResourceVersion = pipelineResourceList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> Pipeline Resources Resource Version: " + pipelineResourcesResourceVersion);
//            logger.info(">>>> Looking for pipelines resources: ");
//            for (PipelineResource pr : pipelineResourceList) {
//                logger.info("> PipelineResource: " + pr);
//            }
//        }
//
//        List<PipelineRun> pipelineRunList = pipelineRunCRDClient.list().getItems();
//        if (!pipelineRunList.isEmpty()) {
//            pipelineRunsResourceVersion = pipelineRunList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> PipelineRun Resource Version: " + pipelineRunsResourceVersion);
//            logger.info(">>>> Looking for pipeline runs: ");
//            for (PipelineRun pr : pipelineRunList) {
//                logger.info("> PipelineRun: " + pr);
//            }
//        }


//
//        List<Task> taskList = taskCRDClient.list().getItems();
//        if (!taskList.isEmpty()) {
//            tasksResourceVersion = taskList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> Task Resource Version: " + tasksResourceVersion);
//            logger.info(">>>> Looking for pipeline runs: ");
//            for (Task t : taskList) {
//                logger.info("> Task: " + t);
//            }
//        }
//
//        List<TaskRun> taskRunList = taskRunCRDClient.list().getItems();
//        if (!taskRunList.isEmpty()) {
//            taskRunsResourceVersion = taskRunList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> Task Run Resource Version: " + taskRunsResourceVersion);
//            logger.info(">>>> Looking for pipeline runs: ");
//            for (TaskRun tr : taskRunList) {
//                logger.info("> TaskRun: " + tr);
//            }
//        }


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


//    /*
//     * Register a Task Resource Watch
//     *  - This watch is in charge of adding and removing pipelines to/from the In memory desired state
//     */
//    private void registerTaskWatch() {
//        logger.info("> Registering Task CRD Watch");
//        taskCRDClient.withResourceVersion(tasksResourceVersion).watch(new Watcher<Task>() {
//            @Override
//            public void eventReceived(Watcher.Action action, Task task) {
//                if (action.equals(Action.ADDED)) {
//                    logger.info(">> Task Added: " + task.getMetadata().getName());
//
//                }
//                if (action.equals(Action.DELETED)) {
//                    logger.info(">> Deleting Task: " + task.getMetadata().getName());
//
//                }
//                if (action.equals(Action.MODIFIED)) {
//                    logger.debug(">> Modifying Task: " + task.getMetadata().getName());
//                }
//
//            }
//
//            @Override
//            public void onClose(KubernetesClientException cause) {
//            }
//        });
//        taskWatchRegistered = true;
//
//    }
//
//    /*
//     * Register a Task Resource Watch
//     *  - This watch is in charge of adding and removing pipelines to/from the In memory desired state
//     */
//    private void registerTaskRunWatch() {
//        logger.info("> Registering TaskRun CRD Watch");
//        taskRunCRDClient.withResourceVersion(taskRunsResourceVersion).watch(new Watcher<TaskRun>() {
//            @Override
//            public void eventReceived(Watcher.Action action, TaskRun taskRun) {
//                TaskRunStatus status = taskRun.getStatus();
//                if (action.equals(Action.ADDED)) {
//                    logger.info(">> TaskRun Added: " + taskRun);
//
//                    List<TaskRunStep> steps = status.getSteps();
//                    if (steps != null) {
//                        for (TaskRunStep s : steps) {
//                            logger.info("\t > Task Run Step: " + s);
//                        }
//                    }
//
//                }
//                if (action.equals(Action.DELETED)) {
//                    logger.info(">> Deleting TaskRun: " + taskRun);
//
//                }
//                if (action.equals(Action.MODIFIED)) {
//                    logger.info(">> Modifying TaskRun: " + taskRun);
//
//                    List<TaskRunStep> steps = status.getSteps();
//                    if (steps != null) {
//                        for (TaskRunStep s : steps) {
//                            logger.info("\t > Task Run Step: " + s);
//                        }
//                    }
//                }
//
//            }
//
//            @Override
//            public void onClose(KubernetesClientException cause) {
//            }
//        });
//        taskRunWatchRegistered = true;
//
//    }
//
//    /*
//     * Register an Pipeline Resource Watch
//     *  - This watch is in charge of adding and removing pipelines to/from the In memory desired state
//     */
//    private void registerPipelineWatch() {
//        logger.info("> Registering Pipeline CRD Watch");
//        pipelineCRDClient.withResourceVersion(pipelinesResourceVersion).watch(new Watcher<Pipeline>() {
//            @Override
//            public void eventReceived(Watcher.Action action, Pipeline pipeline) {
//                if (action.equals(Action.ADDED)) {
//                    logger.info(">> Pipeline Added: " + pipeline.getMetadata().getName());
//
//                }
//                if (action.equals(Action.DELETED)) {
//                    logger.info(">> Deleting Pipeline: " + pipeline.getMetadata().getName());
//
//                }
//                if (action.equals(Action.MODIFIED)) {
//                    logger.debug(">> Modifying Pipeline: " + pipeline.getMetadata().getName());
//                }
//
//            }
//
//            @Override
//            public void onClose(KubernetesClientException cause) {
//            }
//        });
//        pipelineWatchRegistered = true;
//
//    }
//
//    /*
//     * Register an PipelineResource Resource Watch
//     *  - This watch is in charge of adding and removing pipelineResource to/from the In memory desired state
//     */
//    private void registerPipelineResourceWatch() {
//        logger.info("> Registering Pipeline Resource CRD Watch");
//        pipelineResourceCRDClient.withResourceVersion(pipelineResourcesResourceVersion).watch(new Watcher<PipelineResource>() {
//            @Override
//            public void eventReceived(Watcher.Action action, PipelineResource pipelineResource) {
//                if (action.equals(Action.ADDED)) {
//                    logger.info(">> PipelineResource Added: " + pipelineResource.getMetadata().getName());
//                    // LOOK FOR THE CONFERENCE THAT HAS THE PIPELINE ASSOCIATED
//                }
//                if (action.equals(Action.DELETED)) {
//                    logger.info(">> Deleting PipelineResource: " + pipelineResource.getMetadata().getName());
//
//                }
//                if (action.equals(Action.MODIFIED)) {
//                    logger.debug(">> Modifying PipelineResource: " + pipelineResource.getMetadata().getName());
//                    // DID THE STATUS CHANGED? We need to update the conference
//                }
//
//            }
//
//            @Override
//            public void onClose(KubernetesClientException cause) {
//            }
//        });
//        pipelineResourceWatchRegistered = true;
//
//    }


    /*
     * Register an PipelineActivity Resource Watch
     *  - This watch is in charge of adding and removing pipelineActivity to/from the In memory desired state
     */
    private void registerPipelineActivityWatch() {
        logger.info("> Registering Pipeline Activity CRD Watch");
        pipelineActivityCRDClient.watch(new Watcher<PipelineActivity>() {
            @Override
            public void eventReceived(Watcher.Action action, PipelineActivity pipelineActivity) {
                logger.info(">>> Pipeline Activity Event Recieved: " + action.toString());
                String repository = pipelineActivity.getMetadata().getLabels().get("repository");

                String pipeline = pipelineActivity.getSpec().getPipeline();

                String version = pipelineActivity.getSpec().getVersion();
                String status = pipelineActivity.getSpec().getStatus();

                if (action.equals(Action.ADDED)) {
                    logger.info(">> PipelineActivity Added: " + pipelineActivity.getMetadata().getName());


                    logger.info(">>>>!!!!! Pipeline: " + pipeline + ",  for repo:  " + repository + ", in version:  " + version + ", with status: " + status);
                    linkPipelineToConferenceModule(pipelineActivity);


                }
                if (action.equals(Action.DELETED)) {
                    logger.info(">> Deleting PipelineActivity: " + pipelineActivity.getMetadata().getName());

                }
                if (action.equals(Action.MODIFIED)) {
                    logger.info(">> Modifying PipelineActivity: " + pipelineActivity.getMetadata().getName());
                    logger.info(">>>>!!!!! Pipeline: " + pipeline + ",  for repo:  " + repository + ", in version:  " + version + ", with status: " + status);
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

//    /*
//     * Register an PipelineRun Resource Watch
//     *  - This watch is in charge of adding and removing pipelineRuns to/from the In memory desired state
//     */
//    private void registerPipelineRunWatch() {
//        logger.info("> Registering Pipeline Run CRD Watch");
//        pipelineRunCRDClient.withResourceVersion(pipelineRunsResourceVersion).watch(new Watcher<PipelineRun>() {
//            @Override
//            public void eventReceived(Watcher.Action action, PipelineRun pipelineRun) {
//                if (action.equals(Action.ADDED)) {
//                    logger.info(">> PipelineRun Added: " + pipelineRun.getMetadata().getName());
//
//                }
//                if (action.equals(Action.DELETED)) {
//                    logger.info(">> Deleting PipelineRun: " + pipelineRun.getMetadata().getName());
//
//                }
//                if (action.equals(Action.MODIFIED)) {
//                    logger.debug(">> Modifying PipelineRun: " + pipelineRun.getMetadata().getName());
//                }
//
//            }
//
//            @Override
//            public void onClose(KubernetesClientException cause) {
//            }
//        });
//        pipelineRunWatchRegistered = true;
//
//    }


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


//    private void linkGatewayToApp(Application application) {
//        String gatewayName = application.getSpec().getGateway();
//        Gateway gateway = gatewaysCRDClient.withName(gatewayName).get();
//        if (gateway != null) {
//            if (gateway.getMetadata().getLabels().get("app") != null &&
//                    gateway.getMetadata().getLabels().get("app").equals(application.getMetadata().getName())) {
//                // This just set the Application as the Owner of the Gateway
//                Gateway updatedGateway = conferenceService.owns(application, gateway);
//                gatewaysCRDClient.createOrReplace(updatedGateway);
//            } else {
//                logger.info("This gateway (" + gateway + ") belongs to a different application"
//                        + gateway.getMetadata().getLabels().get("app"));
//            }
//        } else {
//            logger.error("Gateway: " + gatewayName + " doesn't exist!");
//        }
//    }
//
//    private void linkRegistryToApp(Application application) {
//        String registryName = application.getSpec().getRegistry();
//        Registry registry = registriesCRDClient.withName(registryName).get();
//        if (registry != null) {
//            if (registry.getMetadata().getLabels().get("app") != null &&
//                    registry.getMetadata().getLabels().get("app").equals(application.getMetadata().getName())) {
//                // This just set the Application as the Owner of the Registry
//                Registry updatedRegistry = conferenceService.owns(application, registry);
//                registriesCRDClient.createOrReplace(updatedRegistry);
//            } else {
//                logger.info("This registry (" + registry + ") belongs to a different application"
//                        + registry.getMetadata().getLabels().get("app"));
//            }
//        } else {
//            logger.error("Registry: " + registryName + " doesn't exist!");
//        }
//
//    }
//
//    private void linkMicroServicesToApp(Application application) {
//        Set<MicroServiceDescr> microservices = application.getSpec().getMicroservices();
//        if (microservices != null && !microservices.isEmpty()) {
//            for (MicroServiceDescr msd : microservices) {
//                MicroService microService = microServicesCRDClient.withName(msd.getName()).get();
//                if (microService != null) {
//                    if (microService.getMetadata().getLabels().get("app") != null &&
//                            microService.getMetadata().getLabels().get("app").equals(application.getMetadata().getName())) {
//                        // This just set the Application as the Owner of the MicroService
//                        MicroService updatedMicroService = conferenceService.owns(application, microService);
//                        microServicesCRDClient.createOrReplace(updatedMicroService);
//                    } else {
//                        logger.debug("This microservice (" + microService + ") belongs to a different application"
//                                + microService.getMetadata().getLabels().get("app"));
//                    }
//                } else {
//                    logger.error("MicroService: " + msd.getName() + " doesn't exist!");
//                }
//
//            }
//        }
//    }


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
            // Is App Structure ok
            if (areConferencePipelinesOK(conference)) {

//                        // Is the APP Healthy??
//                        boolean confHealthy = false;
////                        if (config.isK8sServiceCheckEnabled()) {
////                            confHealthy = conferenceService.isConferenceHealthy(conference, true);
////                        } else {
//                        // If we have K8s services disabled and the structure is ok we will set it as healthy
//                        confHealthy = true;
////                        }
//                        if (confHealthy) {
                // YES: Change the state and provide a URL
//                            conference.getSpec().getMicroservices().forEach(m -> logger.info("\t> MicroService found: " + m));
                if (conference.getSpec() == null) {
                    conference.setSpec(new ConferenceSpec());
                }
                conference.getSpec().setStatus("HEALTHY");
                String url = conferenceService.exposeAndSetConferenceURL(conference, true);
                conference.getSpec().setUrl(url);
                logger.info("\t> Conference: " + confName + ", status:  HEALTHY, URL: " + url + " \n");

            } else {
                // NO: Change the state and remove the URL
                logger.error("\t > Conference Name: " + confName + " is down due missing services");
//                            if (conference.getSpec().getMicroservices() == null || conference.getSpec().getMicroservices().isEmpty()) {
//                                logger.info("\t>App: " + appName + ": No MicroService found. ");
//                            } else {
//                                conference.getSpec().getMicroservices().forEach(m -> logger.info("\t> MicroService found: " + m));
//                            }
                if (conference.getSpec() == null) {
                    conference.setSpec(new ConferenceSpec());
                }
                conference.getSpec().setStatus("UNHEALTHY");
                conferenceService.exposeAndSetConferenceURL(conference, false);
                conference.getSpec().setUrl("N/A");
                logger.info("\t> Conference: " + confName + ", status: UNHEALTHY. \n ");
            }



            // Notify K8s about the updates required
            conferenceCRDClient.createOrReplace(conference);

            //} //else {
            // logger.error("The application " + conference.getMetadata().getName() + " structure is not complete please check the resources required by this application");
            //    }
            //}
        });

    }

    private boolean areConferencePipelinesOK(Conference conference) {

        List<ModuleRef> modules = conference.getSpec().getModules();
        Map<String, List<PipelineActivity>> conferencePipelines = conferenceService.getConferencePipeline(conference.getMetadata().getName());
        if (conferencePipelines.keySet() == null || conferencePipelines.keySet().isEmpty()) {
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


    /*
     * Delete a Conference by name
     */
    public void deleteConference(String conferenceName) {
        Conference conference = conferenceService.getConference(conferenceName);
        conferenceCRDClient.delete(conference);
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
