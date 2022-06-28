package cambio.simulator.orchestration.models;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.parsing.OrchestrationModelLoader;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.orchestration.entities.Cluster;
import cambio.simulator.orchestration.entities.Node;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.management.ScaleTaskExecutor;
import cambio.simulator.orchestration.export.StatsTasksExecutor;
import cambio.simulator.orchestration.parsing.ConfigDto;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.orchestration.parsing.kubernetes.YAMLParser;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import cambio.simulator.parsing.ModelLoader;
import desmoj.core.simulator.TimeInstant;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MiSimOrchestrationModel extends MiSimModel {

    private final OrchestrationConfig orchestrationConfig;

    public MiSimOrchestrationModel(File architectureModelLocation, File experimentModelOrScenarioLocation, File orchestrationConfigLocation) {
        super(architectureModelLocation, experimentModelOrScenarioLocation);
        this.orchestrationConfig = OrchestrationModelLoader.loadOrchestrationConfig(orchestrationConfigLocation);;
    }

    public OrchestrationConfig getOrchestrationConfig() {
        return orchestrationConfig;
    }

    @Override
    public void init() {
        this.architectureModel = OrchestrationModelLoader.loadArchitectureModel(this);
        this.experimentModel = ModelLoader.loadExperimentModel(this);
        this.experimentMetaData.setStartDate(LocalDateTime.now());
    }

    @Override
    public void doInitialSchedules() {
        if (!orchestrationConfig.isOrchestrated()) {
            super.doInitialSchedules();
        } else {
            this.experimentMetaData.markStartOfExperiment(System.nanoTime());
            initOrchestration();
            for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEntities()) {
                selfScheduledEvent.doInitialSelfSchedule();
            }
            SimulationEndEvent simulationEndEvent =
                    new SimulationEndEvent(this, SimulationEndEvent.class.getSimpleName(), true);
            simulationEndEvent.schedule(new TimeInstant(this.experimentMetaData.getDuration()));
        }
    }

    private void initOrchestration() {
        System.out.println();
        System.out.println("### Initializing Container Orchestration ###");
        String targetDir = orchestrationConfig.getOrchestrationDirectory() + "/k8_files";
        ConfigDto configDto = null;
        try {
            configDto = YAMLParser.parseConfigFile(orchestrationConfig.getOrchestrationDirectory() + "/environment/config.yaml");
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Cluster cluster = new Cluster(createNodesFromConfigDto(configDto));
        ManagementPlane.getInstance().setModel(this);
        ManagementPlane.getInstance().setCluster(cluster);

        try {
            assignPriosToSchedulers(configDto);
            assignStartUpTimesToInstances(configDto);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        final YAMLParser yamlParser = YAMLParser.getInstance();
        yamlParser.setArchitectureModel(architectureModel);
        yamlParser.setConfigDto(configDto);
        try {
            yamlParser.initDeploymentsFromArchitectureAndYAMLFiles(targetDir);
        } catch (ParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final ScaleTaskExecutor scaleTaskExecutor = new ScaleTaskExecutor(getModel(), "MasterTaskExecutor", getModel().traceIsOn(), configDto.getScalingInterval());
        scaleTaskExecutor.doInitialSelfSchedule();
        System.out.println("[INFO]: Orchestration Report will be created afterwards\n");
        final StatsTasksExecutor statsTasksExecutor = new StatsTasksExecutor(getModel(), "StatsExecutor", getModel().traceIsOn());
        statsTasksExecutor.doInitialSelfSchedule();
        System.out.println("### Initialization of Container Orchestration finished ###");
        System.out.println();

    }

    private List<Node> createNodesFromConfigDto(ConfigDto configDto) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < configDto.getNodes().getAmount(); i++) {
            nodes.add(new Node(getModel(), "Node" + i, traceIsOn(), configDto.getNodes().getCpu()));
        }

        if (configDto.getCustomNodes() != null) {
            for (ConfigDto.CustomNodes customNode : configDto.getCustomNodes()) {
                nodes.add(new Node(getModel(), customNode.getName(), traceIsOn(), customNode.getCpu()));
            }
        }


        return nodes;
    }

    private void assignPriosToSchedulers(ConfigDto configDto) {
        if (configDto.getSchedulerPrio() != null) {
            for (ConfigDto.SchedulerPrio schedulerPrio : configDto.getSchedulerPrio()) {
                String name = schedulerPrio.getName();
                SchedulerType schedulerType1 = SchedulerType.fromString(name);
                Scheduler schedulerInstanceByType = Util.getInstance().getSchedulerInstanceByType(schedulerType1);
                schedulerInstanceByType.setPRIO(schedulerPrio.getPrio());
            }
        }
    }

    private void assignStartUpTimesToInstances(ConfigDto configDto) {
        if (configDto.getStartUpTimeContainer() != null) {
            for (ConfigDto.StartUpTimeContainer startUpTimeContainer : configDto.getStartUpTimeContainer()) {
                String name = startUpTimeContainer.getName();
                Microservice service = architectureModel.getMicroservices().stream().filter(microservice -> microservice.getPlainName().equals(name)).findAny().orElse(null);
                if (service != null) {
                    ((MicroserviceOrchestration) service).setStartTime(startUpTimeContainer.getTime());
                }
            }
        }
    }
}
