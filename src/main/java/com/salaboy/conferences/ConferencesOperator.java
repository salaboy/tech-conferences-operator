package com.salaboy.conferences;

import com.salaboy.conferences.core.K8SCoreRuntime;
import com.salaboy.conferences.crds.conference.Conference;
import com.salaboy.conferences.crds.conference.ConferenceList;
import com.salaboy.conferences.crds.conference.CustomService;
import com.salaboy.conferences.crds.conference.DoneableConference;
import com.salaboy.conferences.crds.tekton.pipeline.DoneablePipeline;
import com.salaboy.conferences.crds.tekton.pipeline.Pipeline;
import com.salaboy.conferences.crds.tekton.pipeline.PipelineList;
import com.salaboy.conferences.crds.tekton.pipelinerun.DoneablePipelineRun;
import com.salaboy.conferences.crds.tekton.pipelinerun.PipelineRun;
import com.salaboy.conferences.crds.tekton.pipelinerun.PipelineRunList;
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

import java.util.List;

@Service
public class ConferencesOperator {

    // Is the service On?
    private boolean on = true;
    private boolean initDone = false;
    private boolean crdsFound = false;

    private Logger logger = LoggerFactory.getLogger(ConferencesOperator.class);
    //    private CustomResourceDefinition microServiceCRD = null;
//    private CustomResourceDefinition gatewayCRD = null;
//    private CustomResourceDefinition registryCRD = null;
    private CustomResourceDefinition conferenceCRD = null;
    private CustomResourceDefinition pipelineCRD = null;
    private CustomResourceDefinition pipelineRunCRD = null;
    private boolean conferenceWatchRegistered = false;
    private String conferencesResourceVersion;
//    private String microServicesResourceVersion;
//    private String registriesResourceVersion;
//    private String gatewaysResourceVersion;

    private NonNamespaceOperation<Conference, ConferenceList, DoneableConference, Resource<Conference, DoneableConference>> conferenceCRDClient;
    private NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> pipelineCRDClient;
    private NonNamespaceOperation<PipelineRun, PipelineRunList, DoneablePipelineRun, Resource<PipelineRun, DoneablePipelineRun>> pipelineRunCRDClient;
//    private NonNamespaceOperation<MicroService, MicroServiceList, DoneableMicroService, Resource<MicroService, DoneableMicroService>> microServicesCRDClient;
//    private NonNamespaceOperation<Gateway, GatewayList, DoneableGateway, Resource<Gateway, DoneableGateway>> gatewaysCRDClient;
//    private NonNamespaceOperation<Registry, RegistryList, DoneableRegistry, Resource<Registry, DoneableRegistry>> registriesCRDClient;


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
//                    if (ConferenceCRDs.MICROSERVICE_CRD_NAME.equals(name)) {
//                        microServiceCRD = crd;
//                    }
//                    if (ConferenceCRDs.GATEWAY_CRD_NAME.equals(name)) {
//                        gatewayCRD = crd;
//                    }
//                    if (ConferenceCRDs.REGISTRY_CRD_NAME.equals(name)) {
//                        registryCRD = crd;
//                    }

                    if (ConferenceCRDs.CONF_CRD_NAME.equals(name)) {
                        conferenceCRD = crd;
                    }
                    if (ConferenceCRDs.TEKTON_PIPELINE_CRD_NAME.equals(name)) {
                        pipelineCRD = crd;
                    }
                    if (ConferenceCRDs.TEKTON_PIPELINERUN_CRD_NAME.equals(name)) {
                        pipelineRunCRD = crd;
                    }
                }
            }
            if (allCRDsFound()) {
                logger.info("\t > Conference CRD: " + conferenceCRD.getMetadata().getName());
                logger.info("\t > Tekton Pipeline CRD: " + pipelineCRD.getMetadata().getName());
                logger.info("\t > Tekton PipelineRun CRD: " + pipelineRunCRD.getMetadata().getName());
//                logger.info("\t > MicroService CRD: " + microServiceCRD.getMetadata().getName());
//                logger.info("\t > Registry CRD: " + registryCRD.getMetadata().getName());
//                logger.info("\t > Gateway CRD: " + gatewayCRD.getMetadata().getName());
                return true;
            } else {
                logger.error("> Custom CRDs required to work not found please check your installation!");
                logger.error("\t > Conference CRD: " + ((conferenceCRD == null) ? " NOT FOUND " : conferenceCRD.getMetadata().getName()));
                logger.error("\t > Tekton Pipeline CRD: " + ((pipelineCRD == null) ? " NOT FOUND " : pipelineCRD.getMetadata().getName()));
                logger.error("\t > Tekton PipelineRun CRD: " + ((pipelineRunCRD == null) ? " NOT FOUND " : pipelineRunCRD.getMetadata().getName()));
//                logger.error("\t > MicroService CRD: " + ((microServiceCRD == null) ? " NOT FOUND " : microServiceCRD.getMetadata().getName()));
//                logger.error("\t > Registry CRD: " + ((registryCRD == null) ? " NOT FOUND " : registryCRD.getMetadata().getName()));
//                logger.error("\t > Gateway CRD: " + ((gatewayCRD == null) ? " NOT FOUND " : gatewayCRD.getMetadata().getName()));
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
        logger.info("> JHipster K8s Operator is Starting!");
        // Creating CRDs Clients
        conferenceCRDClient = k8SCoreRuntime.customResourcesClient(conferenceCRD, Conference.class, ConferenceList.class, DoneableConference.class).inNamespace(k8SCoreRuntime.getNamespace());
        pipelineCRDClient = k8SCoreRuntime.customResourcesClient(pipelineCRD, Pipeline.class, PipelineList.class, DoneablePipeline.class).inNamespace(k8SCoreRuntime.getNamespace());
        pipelineRunCRDClient = k8SCoreRuntime.customResourcesClient(pipelineRunCRD, PipelineRun.class, PipelineRunList.class, DoneablePipelineRun.class).inNamespace(k8SCoreRuntime.getNamespace());
//        microServicesCRDClient = k8SCoreRuntime.customResourcesClient(microServiceCRD, MicroService.class, MicroServiceList.class, DoneableMicroService.class).inNamespace(k8SCoreRuntime.getNamespace());
//        gatewaysCRDClient = k8SCoreRuntime.customResourcesClient(gatewayCRD, Gateway.class, GatewayList.class, DoneableGateway.class).inNamespace(k8SCoreRuntime.getNamespace());
//        registriesCRDClient = k8SCoreRuntime.customResourcesClient(registryCRD, Registry.class, RegistryList.class, DoneableRegistry.class).inNamespace(k8SCoreRuntime.getNamespace());

        if (loadExistingResources() && watchOurCRDs()) {
            return true;
        }

        return false;

    }

    /*
     * Check that all the CRDs are found for this operator to work
     */
    private boolean allCRDsFound() {
//        if (microServiceCRD == null || conferenceCRD == null || gatewayCRD == null || registryCRD == null) {
        if (conferenceCRD == null || pipelineCRD == null || pipelineRunCRD == null) {
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
        if (conferenceWatchRegistered) {
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
            conferenceList.forEach(app -> {
                conferenceService.addConference(app.getMetadata().getName(), app);
                logger.info("> Conf " + app.getMetadata().getName() + " found.");
            });

        }

        List<Pipeline> pipelineList = pipelineCRDClient.list().getItems();
        for(Pipeline p : pipelineList){
            logger.info("> Pipeline: " + p);
        }

        List<PipelineRun> pipelineRunList = pipelineRunCRDClient.list().getItems();
        for(PipelineRun pr : pipelineRunList){
            logger.info("> PipelineRun: " + pr);
        }


//        // Load Existing Service As
//        List<MicroService> microServiceList = microServicesCRDClient.list().getItems();
//        if (!microServiceList.isEmpty()) {
//            microServicesResourceVersion = microServiceList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> MicroService Resource Version: " + microServicesResourceVersion);
//            microServiceList.forEach(microService -> {
//                // If it doesn't have owner references we need to set it up at load time
//                MicroService updatedMicroService = checkAndAddOwnerReferences(microService);
//                microServicesCRDClient.createOrReplace(updatedMicroService);
//            });
//        }
//        // Load Existing Gateways
//        List<Gateway> gatewayList = gatewaysCRDClient.list().getItems();
//        if (!gatewayList.isEmpty()) {
//            gatewaysResourceVersion = gatewayList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> Gateway Resource Version: " + gatewaysResourceVersion);
//            gatewayList.forEach(gateway -> {
//                // If it doesn't have owner references we need to set it up at load time
//                Gateway updatedGateway = checkAndAddOwnerReferences(gateway);
//                gatewaysCRDClient.createOrReplace(updatedGateway);
//
//            });
//
//        }
//        // Load Existing Registries
//        List<Registry> registriesList = registriesCRDClient.list().getItems();
//        if (!registriesList.isEmpty()) {
//            registriesResourceVersion = registriesList.get(0).getMetadata().getResourceVersion();
//            logger.info(">> Registry Resource Version: " + registriesResourceVersion);
//            registriesList.forEach(registry -> {
//                // If it doesn't have owner references we need to set it up at load time
//                Registry updatedRegistry = checkAndAddOwnerReferences(registry);
//                registriesCRDClient.createOrReplace(updatedRegistry);
//            });
//
//        }
        return true;
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
     * Register an Application Resource Watch
     *  - This watch is in charge of adding and removing apps to/from the In memory desired state
     */
    private void registerConferenceWatch() {
        logger.info("> Registering Application CRD Watch");
        conferenceCRDClient.withResourceVersion(conferencesResourceVersion).watch(new Watcher<Conference>() {
            @Override
            public void eventReceived(Watcher.Action action, Conference conference) {
                if (action.equals(Action.ADDED)) {
                    logger.info(">> Adding Conf: " + conference.getMetadata().getName());
                    conferenceService.addConference(conference.getMetadata().getName(), conference);

//                    linkAllApplicationResources(conference);

                }
                if (action.equals(Action.DELETED)) {
                    logger.info(">> Deleting Conf: " + conference.getMetadata().getName());
                    conferenceService.removeConference(conference.getMetadata().getName());
                }
                if (action.equals(Action.MODIFIED)) {
                    logger.info(">> Modifying App: " + conference.getMetadata().getName());
                    conferenceService.addConference(conference.getMetadata().getName(), conference);
//                    linkAllApplicationResources(conference);
                }

                if (conference.getSpec() == null) {
                    logger.info("No Spec for resource " + conference.getMetadata().getName());
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
            }
        });
        conferenceWatchRegistered = true;

    }

    private void linkAllApplicationResources(Conference conference) {
//        linkMicroServicesToApp(conference);
//
//        linkRegistryToApp(conference);
//
//        linkGatewayToApp(conference);
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
        conferenceService.getConferencesMap().keySet().forEach(appName ->
                {
                    Conference conference = conferenceService.getConference(appName);
                    linkAllApplicationResources(conference);
                    logger.info("> Conference Found: " + appName + ". Scanning ...");
                    // Is App Structure ok
                    if (isApplicationStructureOK(conference)) {

                        // Is the APP Healthy??
                        boolean appHealthy = false;
//                        if (config.isK8sServiceCheckEnabled()) {
//                            appHealthy = conferenceService.isConferenceHealthy(conference, true);
//                        } else {
                        // If we have K8s services disabled and the structure is ok we will set it as healthy
                        appHealthy = true;
//                        }
                        if (appHealthy) {
                            // YES: Change the state and provide a URL
//                            conference.getSpec().getMicroservices().forEach(m -> logger.info("\t> MicroService found: " + m));
                            conference.getSpec().setStatus("HEALTHY");
                            String url = conferenceService.createAndSetConferenceURL(conference.getMetadata().getName(), conference.getSpec().getVersion());
                            conference.getSpec().setUrl(url);
                            logger.info("\t> App: " + appName + ", status:  HEALTHY, URL: " + url + " \n");
                        } else {
                            // NO: Change the state and remove the URL
                            logger.error("\t > App Name: " + appName + " is down due missing services");
//                            if (conference.getSpec().getMicroservices() == null || conference.getSpec().getMicroservices().isEmpty()) {
//                                logger.info("\t>App: " + appName + ": No MicroService found. ");
//                            } else {
//                                conference.getSpec().getMicroservices().forEach(m -> logger.info("\t> MicroService found: " + m));
//                            }
                            conference.getSpec().setStatus("UNHEALTHY");
                            conference.getSpec().setUrl("N/A");
                            logger.info("\t> App: " + appName + ", status: UNHEALTHY. \n ");
                        }
                        // Notify K8s about the updates required
                        conferenceCRDClient.createOrReplace(conference);
                    } else {
                        logger.error("The application " + conference.getMetadata().getName() + " structure is not complete please check the resources required by this application");
                    }
                }
        );

    }

    private boolean isApplicationStructureOK(Conference conference) {

//        boolean isRegistryDefAvailable = false;
//        boolean isGatewayDefAvailable = false;
//        boolean areMicroServicesAvailable = false;
//
//        Set<MicroServiceDescr> microservices = conference.getSpec().getMicroservices();
//        if (microservices != null && !microservices.isEmpty()) {
//            boolean areMicroServicesDefAvailable[] = new boolean[microservices.size()];
//            int microservicesCount = 0;
//            for (MicroServiceDescr msd : microservices) {
//                MicroService microService = microServicesCRDClient.withName(msd.getName()).get();
//                if (microService != null) {
//                    areMicroServicesDefAvailable[microservicesCount] = true;
//                } else {
//                    logger.info("MicroService " + msd.getName() + " not found!");
//                }
//                microservicesCount++;
//            }
//            areMicroServicesAvailable = conferenceService.checkMicroServicesAvailability(microservices.size(), areMicroServicesDefAvailable);
//        }
//
//        String registryName = conference.getSpec().getRegistry();
//        Registry registry = registriesCRDClient.withName(registryName).get();
//        if (registry != null) {
//            isRegistryDefAvailable = true;
//        } else {
//            logger.info("Registry " + registryName + " not found!");
//        }
//
//        String gatewayName = conference.getSpec().getGateway();
//        Gateway gateway = gatewaysCRDClient.withName(gatewayName).get();
//        if (gateway != null) {
//            isGatewayDefAvailable = true;
//        } else {
//            logger.info("Gateway " + gatewayName + " not found!");
//        }
//
//        if (areMicroServicesAvailable && isGatewayDefAvailable && isRegistryDefAvailable) {
//            return true;
//        }


        return true;
    }


    /*
     * Delete a JHipster Application by name
     */
    public void deleteConference(String conferenceName) {
        Conference conference = conferenceService.getConference(conferenceName);
        //@TODO: delete by API doesn't cascade yet..
        conferenceCRDClient.delete(conference);
    }

//    public CustomResourceDefinition getMicroServiceCRD() {
//        return microServiceCRD;
//    }
//
//    public CustomResourceDefinition getGatewayCRD() {
//        return gatewayCRD;
//    }
//
//    public CustomResourceDefinition getRegistryCRD() {
//        return registryCRD;
//    }

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
