package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.orchestration.util.FileOps;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static cambio.simulator.orchestration.export.reporters.EventsReporter.*;
import static cambio.simulator.orchestration.export.reporters.KubernetesReporter.generateKubernetesReports;
import static cambio.simulator.orchestration.export.reporters.MiSimReporter.generateMiSimScalingReports;
import static cambio.simulator.orchestration.export.reporters.PerformanceReporter.generatePerformanceReport;

public class MainReporter {
    final static String ORCHESTRATION_MAIN_REPORTS_DIR = "orchestration_reports";
    final static String MISIM_STANDARD_SUBDIR = "misim_standard";
    final static String MISIM_SCALING_SUBDIR = "misim_scaling";
    final static String SIMULATION_SUBDIR = "simulation";
    final static String ORCHESTRATION_SUBDIR = "orchestration";
    final static String PERFORMANCE_RESULTS_FILE = "performance_results.csv";
    final static String INTERNAL_EVENTS_FILE = "internal_events.csv";
    final static String K8S_EVENTS_API_EVENTS_LOG_FILE = "k8s_events_api_events_log.csv";

    final static String K8S_CORE_API_EVENTS_LOG_FILE = "k8s_core_api_events_log.csv";


    public static void generateReports(String currentRunName, MiSimModel model) {
        Path workingDirectoryPath = FileSystems.getDefault().getPath("").toAbsolutePath();
        Path orchestrationMainReportsPath = workingDirectoryPath.resolve(ORCHESTRATION_MAIN_REPORTS_DIR);
        Path runSpecificReportsPath = orchestrationMainReportsPath.resolve(currentRunName);

        Path miSimReportsPath = runSpecificReportsPath.resolve(MISIM_STANDARD_SUBDIR);
        Path miSimScalingPath = runSpecificReportsPath.resolve(MISIM_SCALING_SUBDIR);
        Path simulationPath = runSpecificReportsPath.resolve(SIMULATION_SUBDIR);
        Path orchestrationPath = runSpecificReportsPath.resolve(ORCHESTRATION_SUBDIR);

        try {
            FileOps.copyDirectory(model.getExperimentMetaData().getReportLocation(), miSimReportsPath);
        } catch (IOException e) {
            System.err.println("Could not copy MiSim reports to report directory.\n" + e.getMessage());
        }

        generatePerformanceReport(model, simulationPath.resolve(PERFORMANCE_RESULTS_FILE));
        generateInternalEventsReport(simulationPath.resolve(INTERNAL_EVENTS_FILE));
        generateK8sEventsApiEventsLog(orchestrationPath.resolve(K8S_EVENTS_API_EVENTS_LOG_FILE));
        generateK8sCoreApiEventsLog(orchestrationPath.resolve(K8S_CORE_API_EVENTS_LOG_FILE));
        boolean isOrchestrationRun =
                model instanceof MiSimOrchestrationModel && ((MiSimOrchestrationModel) model).getOrchestrationConfig().isOrchestrate();
        if (isOrchestrationRun) {
            generateKubernetesReports(orchestrationPath);
        }
        generateMiSimScalingReports(miSimScalingPath);
    }


}
