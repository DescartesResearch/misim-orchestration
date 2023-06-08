package cambio.simulator.orchestration.parsing.kubernetes;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.orchestration.scaling.HorizontalPodAutoscaler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.parsing.ParsingException;
import desmoj.core.simulator.Model;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KubernetesParser {
    public static List<Node> importNodes(Model model, boolean trace, String dir) {
        List<Node> result = new ArrayList<>();
        try {
            Set<String> fileNames = Util.getInstance().listFilesUsingJavaIO(dir);
            for (String fileName : fileNames) {
                String filePath = dir + "/" + fileName;
                Node node = readNodeFromFile(model, trace, filePath);
                if (node != null) result.add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return result;
    }

    private static Node readNodeFromFile(Model model, boolean trace, String path) throws IOException {
        V1Node v1Node;
        try {
            v1Node = Yaml.loadAs(new File(path), V1Node.class);
        } catch (ConstructorException e) {
            return null;
        }
        Node node = new Node(model, v1Node.getMetadata().getName(), trace, v1Node.getStatus().getAllocatable().get("cpu").getNumber().intValue());
        node.setKubernetesRepresentation(v1Node);
        return node;
    }

    public static void initDeployments(String dir, ArchitectureModel architectureModel, OrchestrationConfig orchestrationConfig) {
        Set<String> fileNames;
        Set<V1Deployment> deployments = new HashSet<>();
        try {
            fileNames = Util.getInstance().listFilesUsingJavaIO(dir);
            Set<String> namesToRemove = new HashSet<>();
            for (String fileName : fileNames) {
                String filePath = dir + "/" + fileName;
                V1Deployment d = readDeploymentFromFile(filePath);
                if (d != null) {
                    deployments.add(d);
                    namesToRemove.add(fileName);
                }
            }
            fileNames.removeAll(namesToRemove);
            namesToRemove.clear();
            Set<Microservice> microservicesFromArchitecture = new HashSet<>(architectureModel.getMicroservices());
            Map<V1Deployment, Microservice> mapping = createMapping(microservicesFromArchitecture, deployments);

            // Create Deployments
            for (Map.Entry<V1Deployment, Microservice> entry : mapping.entrySet()) {
                Deployment d = createDeployment(entry.getKey(), entry.getValue());
                ManagementPlane.getInstance().getDeployments().add(d);
            }

            // Read other k8s objects that refer to deployments (e.g. HPA)
            for (String fileName : fileNames) {
                String filePath = dir + "/" + fileName;
                V1HorizontalPodAutoscaler hpa = readHPAFromFile(filePath);
                if (hpa != null) {
                    namesToRemove.add(filePath);
                    String targetDeploymentName = hpa.getSpec().getScaleTargetRef().getName();
                    Optional<Deployment> optionalDeployment = ManagementPlane.getInstance().getDeployments().stream()
                            .filter(deployment -> deployment.getPlainName().equals(targetDeploymentName))
                            .findFirst();
                    if (optionalDeployment.isPresent()) {
                        Deployment deployment = optionalDeployment.get();
                        deployment.setAutoScaler(new HorizontalPodAutoscaler());
                        int minReplicas = hpa.getSpec().getMinReplicas().intValue();
                        int maxReplicas = hpa.getSpec().getMaxReplicas().intValue();
                        int targetCPUUtilizationPercentage = hpa.getSpec().getTargetCPUUtilizationPercentage().intValue();
                        deployment.setMinReplicaCount(minReplicas);
                        deployment.setMaxReplicaCount(maxReplicas);
                        deployment.setAverageUtilization(targetCPUUtilizationPercentage / 100.0);
                    } else {
                        throw new ParsingException("Could not find an existing deployment object by the given name: " + targetDeploymentName);
                    }
                }
            }
            fileNames.removeAll(namesToRemove);
            namesToRemove.clear();

            //set AutoScaler Hold Times from configDto
            ManagementPlane.getInstance().getDeployments().stream().filter(deployment -> deployment.getAutoScaler() != null).forEach(deployment -> {
                        deployment.getAutoScaler().setHoldTimeUp(orchestrationConfig.getScaler().getHoldTimeUpScaler());
                        deployment.getAutoScaler().setHoldTimeDown(orchestrationConfig.getScaler().getHoldTimeDownScaler());
            });

            //Init only schedulers that are used
            ManagementPlane.getInstance().populateSchedulers();
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static V1Deployment readDeploymentFromFile(String path) throws IOException {
        V1Deployment v1Deployment;
        try {
            v1Deployment = Yaml.loadAs(new File(path), V1Deployment.class);
        } catch (ConstructorException e) {
            return null;
        }
        return v1Deployment;
    }

    private static Map<V1Deployment, Microservice> createMapping(Set<Microservice> microservices, Set<V1Deployment> deployments) {
        Map<V1Deployment, Microservice> map = new HashMap<>();
        for (V1Deployment d : deployments) {
            Optional<Microservice> optionalService = microservices.stream().filter(service -> service.getPlainName().equals(d.getMetadata().getName())).findFirst();
            if (optionalService.isPresent()) {
                MicroserviceOrchestration service = (MicroserviceOrchestration) optionalService.get();
                microservices.remove(service);
                map.put(d, service);
            } else {
                System.out.println("[WARNING]: The deployment " + d.getMetadata().getName() + " will be scheduled, but" +
                        " is not simulated because there is no corresponding microservice in the architecture file.");
            }
        }

        //create default deployments for remaining microservices from the architecture file
        for (Microservice microservice : microservices) {
            V1Deployment d = createDefaultDeployment(microservice);
            map.put(d, microservice);
        }

        return map;

    }

    private static V1Deployment createDefaultDeployment(Microservice microservice) {
        V1Deployment d = new V1Deployment();
        d.setMetadata(new V1ObjectMeta().name(microservice.getPlainName() + "-deployment"));
        System.out.println("[INFO]: Creating deployment " + d.getMetadata().getName() + " from architecture file only. There is no corresponding YAML file");
        return d;
    }

    private static Deployment createDeployment(V1Deployment v1Deployment, Microservice microservice) {
        final String deploymentName = v1Deployment.getMetadata().getName();
        MicroserviceOrchestration casted = (MicroserviceOrchestration) microservice;
        Util.getInstance().connectLoadBalancer(casted);
        if (casted.getStartingInstanceCount() != v1Deployment.getSpec().getReplicas().intValue()) {
            throw new ParsingException("Replica count for service " + casted.getPlainName() + " in architecture file does not match the replica count" +
                    "provided in the deployment file for " + deploymentName + " (" + casted.getStartingInstanceCount() + "/" + v1Deployment.getSpec().getReplicas().intValue() + ")");
        }
        SchedulerType schedulerType = Util.getInstance().getSchedulerTypeByNameOrStandard(v1Deployment.getSpec().getTemplate().getSpec().getSchedulerName(), v1Deployment.getMetadata().getName());
        Deployment deployment = new Deployment(ManagementPlane.getInstance().getModel(), deploymentName, ManagementPlane.getInstance().getModel().traceIsOn(), casted, v1Deployment.getSpec().getReplicas(), schedulerType);
        deployment.setKubernetesRepresentation(v1Deployment);
        return deployment;
    }

    private static V1HorizontalPodAutoscaler readHPAFromFile(String path) throws IOException {
        V1HorizontalPodAutoscaler v1HorizontalPodAutoscaler;
        try {
            v1HorizontalPodAutoscaler = Yaml.loadAs(new File(path), V1HorizontalPodAutoscaler.class);
        } catch (ConstructorException e) {
            return null;
        }
        return v1HorizontalPodAutoscaler;
    }

    private static V1Pod readPodFromFile(String path) throws IOException {
        V1Pod v1Pod;
        try {
            v1Pod = Yaml.loadAs(new File(path), V1Pod.class);
        } catch (ConstructorException e) {
            return null;
        }
        return v1Pod;
    }

}