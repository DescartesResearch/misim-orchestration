package cambio.simulator.orchestration.models;

import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.export.MiSimReporters;
import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.entities.Cluster;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.export.StatsTasksExecutor;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.management.ScaleTaskExecutor;
import cambio.simulator.orchestration.parsing.OrchestrationModelLoader;
import cambio.simulator.orchestration.parsing.kubernetes.KubernetesParser;
import cambio.simulator.orchestration.scaling.FakeAutoscaler;
import cambio.simulator.orchestration.scaling.HorizontalPodAutoscaler;
import cambio.simulator.orchestration.scaling.SimpleReactiveAutoscaler;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.adapter.scenario.ScenarioDescriptionAdapter;
import com.google.gson.TypeAdapter;
import desmoj.core.simulator.TimeInstant;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cambio.simulator.parsing.ModelLoader.loadModel;


@Getter
public class MiSimOrchestrationModel extends MiSimModel {
    private OrchestrationConfig orchestrationConfig;

    public MiSimOrchestrationModel(File architectureModelLocation, File experimentModelOrScenarioLocation,
                                   String orchestrationConfigLocation) {
        super(architectureModelLocation, experimentModelOrScenarioLocation);
        try {
            this.orchestrationConfig = OrchestrationModelLoader.loadOrchestrationConfig(orchestrationConfigLocation);
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void init() {
        this.architectureModel = OrchestrationModelLoader.loadArchitectureModel(this);
        this.experimentModel = loadOrchestrationExperimentModel(this);
        this.experimentMetaData.setStartDate(LocalDateTime.now());
        MiSimReporters.initializeStaticReporters(this);
    }

    @Override
    public void doInitialSchedules() {
        if (!orchestrationConfig.isOrchestrate()) {
            super.doInitialSchedules();
        } else {
            this.experimentMetaData.markStartOfExperiment(System.nanoTime());
            initOrchestration();
            for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEntities()) {
                selfScheduledEvent.doInitialSelfSchedule();
            }
            SimulationEndEvent simulationEndEvent = new SimulationEndEvent(this,
                    SimulationEndEvent.class.getSimpleName(), true);
            simulationEndEvent.schedule(new TimeInstant(this.experimentMetaData.getDuration()));
        }
    }

    public static ExperimentModel loadOrchestrationExperimentModel(MiSimModel baseModel) {
        File modelLocation = baseModel.getExperimentMetaData().getExperimentDescriptionLocation();
        Function<TypeAdapter<ExperimentModel>, ExperimentModel> loadFunction =
                (adapter) -> (ExperimentModel) loadModel(modelLocation, ExperimentModel.class, adapter);
        try {
            return loadFunction.apply(new ScenarioDescriptionAdapter(baseModel));
        } catch (ParsingException var4) {
            System.out.printf("[Info] Could not detect %s as scenario. Trying to detect an experiment.%n",
                    modelLocation);
            return loadFunction.apply(new OrchestrationExperimentModelAdapter(baseModel));
        }
    }

    private void initOrchestration() {
        ManagementPlane.getInstance().setModel(this);

        System.out.println();
        System.out.println("### Initializing Container Orchestration ###");
        String targetDir = orchestrationConfig.getOrchestrationDir();
        Cluster cluster;
        Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> delayMap = null;
        if (orchestrationConfig.getNetworkDelays() != null && orchestrationConfig.getNetworkDelays().isEnabled()) {
            delayMap = orchestrationConfig.getNetworkDelays().getDelayMap();
        }
        if (orchestrationConfig.isImportNodes()) {
            cluster = new Cluster(KubernetesParser.importNodes(getModel(), traceIsOn(), targetDir), delayMap);
        } else {
            cluster = new Cluster(createNodesFromConfigDto(orchestrationConfig), delayMap);
        }
        if (orchestrationConfig.isUseClusterAutoscaler()) {
            cluster.setMachineSets(KubernetesParser.parseGenericKubernetesFiles(orchestrationConfig.getOrchestrationDir(), "MachineSet"));
            cluster.setMachines(KubernetesParser.parseGenericKubernetesFiles(orchestrationConfig.getOrchestrationDir(), "Machine"));
        }

        ManagementPlane.getInstance().setCluster(cluster);

        try {
            assignPriosToSchedulers(orchestrationConfig);
            assignStartUpTimesToInstances(orchestrationConfig);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        KubernetesParser.initDeployments(targetDir, architectureModel, orchestrationConfig);

        // Init scalers if not done
        if (!orchestrationConfig.getScaler().isImportScaler()) {
            List<Deployment> deployments = ManagementPlane.getInstance().getDeployments();
            for (OrchestrationConfig.ScalerSpec spec : orchestrationConfig.getScaler().getScalerList()) {
                Deployment referencedDeployment = null;
                for (Deployment d : deployments) {
                    if (d.getPlainName().equals(spec.getService())) {
                        referencedDeployment = d;
                        break;
                    }
                }
                if (referencedDeployment == null) {
                    System.out.println("[WARNING] Could not find deployment for scaler with service " + spec.getService());
                    continue;
                }
                switch (spec.getScalerType()) {
                    case "HPA":
                        referencedDeployment.setAutoScaler(new HorizontalPodAutoscaler(spec.getTargetUtilization(),
                                spec.getMinReplicas(), spec.getMaxReplicas()));
                        break;
                    case "Fake":
                        referencedDeployment.setAutoScaler(new FakeAutoscaler(spec.getMaxReplicas(),
                                spec.getIncrement(), spec.getDecrement()));
                        break;
                    case "SimpleReactive":
                        referencedDeployment.setAutoScaler(new SimpleReactiveAutoscaler(spec.getLowerBound(),
                                spec.getUpperBound(), spec.getHoldTime()));
                        break;
                    default:
                        System.out.println("[WARNING] Unknown scaler type " + spec.getScalerType());
                        break;
                }
            }
        }

        final ScaleTaskExecutor scaleTaskExecutor = new ScaleTaskExecutor(getModel(), "MasterTaskExecutor",
                getModel().traceIsOn(), orchestrationConfig.getScalingInterval());
        scaleTaskExecutor.doInitialSelfSchedule();
        System.out.println("[INFO]: Orchestration Report will be created afterwards\n");
        final StatsTasksExecutor statsTasksExecutor = new StatsTasksExecutor(getModel(), "StatsExecutor",
                getModel().traceIsOn());
        statsTasksExecutor.doInitialSelfSchedule();
        System.out.println("### Initialization of Container Orchestration finished ###");
        System.out.println();

    }

    private List<Node> createNodesFromConfigDto(OrchestrationConfig configDto) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < configDto.getNodes().getAmount(); i++) {
            nodes.add(new Node(getModel(), "Node" + i, traceIsOn(), configDto.getNodes().getCpu()));
        }

        if (configDto.getCustomNodes() != null) {
            for (OrchestrationConfig.CustomNodes customNode : configDto.getCustomNodes()) {
                nodes.add(new Node(getModel(), customNode.getName(), traceIsOn(), customNode.getCpu()));
            }
        }


        return nodes;
    }

    private void assignPriosToSchedulers(OrchestrationConfig configDto) {
        if (configDto.getSchedulerPrio() != null) {
            for (OrchestrationConfig.SchedulerPrio schedulerPrio : configDto.getSchedulerPrio()) {
                String name = schedulerPrio.getName();
                SchedulerType schedulerType1 = SchedulerType.fromString(name);
                Scheduler schedulerInstanceByType = Util.getSchedulerInstanceByType(schedulerType1);
                schedulerInstanceByType.setPRIO(schedulerPrio.getPrio());
            }
        }
    }

    private void assignStartUpTimesToInstances(OrchestrationConfig orchestrationConfig) {
        if (orchestrationConfig.getStartUpTimeContainer() != null) {
            for (OrchestrationConfig.StartUpTimeContainer startUpTimeContainer :
                    orchestrationConfig.getStartUpTimeContainer()) {
                String name = startUpTimeContainer.getName();
                architectureModel.getMicroservices().stream().filter(microservice -> microservice.getPlainName().equals(name)).findAny().ifPresent(service -> ((MicroserviceOrchestration) service).setStartTime(startUpTimeContainer.getTime()));
            }
        }
    }
}
