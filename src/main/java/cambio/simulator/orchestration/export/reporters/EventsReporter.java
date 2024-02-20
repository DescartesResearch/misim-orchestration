package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.entities.networking.NetworkRequestSendEvent;
import cambio.simulator.orchestration.events.*;
import cambio.simulator.orchestration.export.CSVBuilder;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsReporter {
    public static void generateEventsReport(Path csvPath) {
        // Headers
        List<String> headers = Arrays.asList("PodsRemovedFromNode", "ScaleEvent", "TryToRestartContainerEvent",
                "RestartStartContainerAndMicroserviceInstanceEvent", "StartContainerAndMicroserviceInstanceEvent",
                "StartPodEvent", "HealthCheckEvent", "NetworkRequestSendEvent", "OrchestrationEvents", "MiSimEvents",
                "Total");

        int sumOrchestrationEvents =
                ScaleEvent.counter + StartContainerAndMicroserviceInstanceEvent.counter + StartPodEvent.counter + HealthCheckEvent.counter;
        int sumMiSimEvents = (int) NetworkRequestSendEvent.getCounterSendEvents();
        int total = sumOrchestrationEvents + sumMiSimEvents;
        List<String> rowData = Arrays.asList(String.valueOf(ManagementPlane.getInstance().podsRemovedFromNode),
                String.valueOf(ScaleEvent.counter), String.valueOf(TryToRestartContainerEvent.counter),
                String.valueOf(RestartStartContainerAndMicroserviceInstanceEvent.counter),
                String.valueOf(StartContainerAndMicroserviceInstanceEvent.counter),
                String.valueOf(StartPodEvent.counter), String.valueOf(HealthCheckEvent.counter),
                String.valueOf(NetworkRequestSendEvent.getCounterSendEvents()),
                String.valueOf(sumOrchestrationEvents), String.valueOf(sumMiSimEvents), String.valueOf(total));

        CSVBuilder csvBuilder = new CSVBuilder().headers(headers).row(rowData);
        try {
            csvBuilder.build(csvPath);
        } catch (CSVBuilder.CSVBuilderException e) {
            System.err.println("Error building CSV: " + e.getMessage());
        }
    }
}

