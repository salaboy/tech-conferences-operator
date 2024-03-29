package com.salaboy.conferences.core;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressRuleBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class K8SCoreRuntime {

    private Logger logger = LoggerFactory.getLogger(K8SCoreRuntime.class);

    @Autowired
    private KubernetesClient kubernetesClient;


    private String externalIP = "N/A";

    @PostConstruct
    public void init() {
        logger.error(">>> Current Namespace: " + kubernetesClient.getNamespace());
    }

    public String getNamespace() {
        return kubernetesClient.getNamespace();
    }

    public void registerCustomKind(String apiVersion, String kind, Class<? extends KubernetesResource> clazz) {
        KubernetesDeserializer.registerCustomKind(apiVersion, kind, clazz);
    }

    public boolean isServiceAvailable(String serviceName) {
        //@TODO: i should check that the k8s deployment exist before adding the microservice
        //@TODO: i should update the k8s deployment to make sure that services are configured for the app
        io.fabric8.kubernetes.api.model.Service service = kubernetesClient.services().withName(serviceName).get();
        if (service != null) {
            logger.debug(">> K8s Service " + serviceName + " found.");
            return true;
        }
        logger.error(">> K8s Service " + serviceName + " not found.");
        return false;

    }

    public CustomResourceDefinitionList getCustomResourceDefinitionList() {
        return kubernetesClient.customResourceDefinitions().list();
    }

    public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> customResourcesClient(CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass) {
        return kubernetesClient.customResources(crd, resourceType, listClass, doneClass);
    }

    public String findExternalIP() {

        if (externalIP.equals("N/A")) {
            externalIP = tryIstioGatewayApproach();
        }
        if (externalIP.equals("N/A")) {
            externalIP = tryLoadBalancerApproach();
        }
        return externalIP;
    }

    public String exposeConferenceSite(String confName, String serviceName, boolean exposed) {
        String ip = tryToResolveIngressControllerIP();
        String host = confName + ".jx-staging." + ip + ".nip.io";
        if (exposed) {
            IngressRule backend = new IngressRuleBuilder()
                    .withHost(host)
                    .withNewHttp()
                    .addNewPath()
                    .withNewBackend()
                    .withServiceName(serviceName)
                    .withNewServicePort().withIntVal(80).endServicePort()
                    .endBackend()
                    .endPath()
                    .endHttp()
                    .build();
            Ingress ingress = new IngressBuilder().
                    withNewMetadata()
                    .withName(confName)
//                    //@TODO:  CREATE OWNER reFERENCE FOR INGRESS AND THE CONFERENCE AS PARENT
//                    .withOwnerReferences(new OwnerReferenceBuilder().withUid().build())
                    .endMetadata()
                    .withNewSpec()
                    .withRules(backend)
                    .endSpec()
                    .build();
            kubernetesClient.extensions().ingresses().createOrReplace(ingress);
            logger.info("> Ingress: " + confName + " created! ");
        } else {
            Ingress ingress = kubernetesClient.extensions().ingresses().withName(confName).get();
            if (ingress != null) {
                kubernetesClient.extensions().ingresses().delete(ingress);
                logger.info("> Ingress: " + confName + " deleted! ");
            }
        }


        return host;


    }

    private String tryToResolveIngressControllerIP() {
        String ip = "N/A";
        io.fabric8.kubernetes.api.model.Service service = kubernetesClient.services().inNamespace("kube-system").withName("jxing-nginx-ingress-controller").get();
        if (service != null) {
            if (service.getSpec().getType().equals("LoadBalancer")) {
                if (!service.getStatus().getLoadBalancer().getIngress().isEmpty()) {
                    ip = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
                    logger.info("> IP resolved from Ingress Controller: " + ip);

                }
            }
        } else {
            logger.error("> Service: jxing-nginx-ingress-controller not found!");
        }
        return ip;
    }

    private String tryLoadBalancerApproach() {
        String loadBalancerIP = "N/A";
        ServiceList list = kubernetesClient.services().inNamespace(getNamespace()).list();
        for (io.fabric8.kubernetes.api.model.Service s : list.getItems()) {
            if (s.getMetadata().getName().equals("gateway")) {
                if (s.getSpec().getType().equals("LoadBalancer")) {
                    if (!s.getSpec().getExternalIPs().isEmpty()) {
                        loadBalancerIP = s.getSpec().getExternalIPs().get(0);
                    } else {
                        logger.error(">> LoadBalancer type service is being used, but there is no External IP available, " +
                                "you need to use port-forward:  'kubectl port-forward svc/jhipster-operator 8081:80 -n jhipster' " +
                                "and then access using http://localhost:8081/apps/");
                        loadBalancerIP = "localhost:8081";
                    }
                }
                if (s.getSpec().getType().equals("NodePort")) {
                    logger.error(">> NodePort type service is being used, you need to use port-forward:  'kubectl port-forward svc/jhipster-operator 8080:80 " +
                            "-n jhipster' and then access using http://localhost:8081/apps/");
                    loadBalancerIP = "localhost:8081";
                }

            } else {
                logger.error(">> Trying to resolve External IP from LoadBalancer service \"jhipster-operator\" failed. There will be no external IP for your apps.");
                logger.error(">> Trying to use port-forward:  'kubectl port-forward svc/jhipster-operator 8081:80 " +
                        "-n jhipster' and then access using http://localhost:8081/apps/");
            }
        }
        return loadBalancerIP;
    }

    private String tryIstioGatewayApproach() {
        String istioIP = "N/A";
        ServiceList list = kubernetesClient.services().inNamespace("istio-system").list();
        for (io.fabric8.kubernetes.api.model.Service s : list.getItems()) {
            if (s.getMetadata().getName().equals("istio-ingressgateway")) {
                List<LoadBalancerIngress> ingress = s.getStatus().getLoadBalancer().getIngress();
                if (ingress.size() == 1) {
                    istioIP = ingress.get(0).getIp();
                }
            } else {
                logger.error(">> Trying to resolve External IP from istio-ingressgateway failed. There will be no external IP for your apps.");
                logger.error(">> Trying to use port-forward:  'kubectl port-forward svc/jhipster-operator 8081:80 " +
                        "-n jhipster' and then access using http://localhost:8081/apps/");
            }
        }
        return istioIP;
    }

}
