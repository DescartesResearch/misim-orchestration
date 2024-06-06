package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.entities.networking.NetworkRequestSendEvent;
import cambio.simulator.orchestration.events.*;
import cambio.simulator.orchestration.export.CSVBuilder;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.rest.EventsController;
import io.kubernetes.client.openapi.models.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EventsReporter {
    public static void generateInternalEventsReport(Path csvPath) {
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

    public static void generateK8sEventsApiEventsLog(Path csvPath) {
        try {
            EventsV1EventList k8sEventsListObj = EventsController.getEvents();
            List<EventsV1Event> eventsList = k8sEventsListObj.getItems();
            if (eventsList == null || eventsList.isEmpty()) {
                System.out.println("No kubernetes events API events found");
                return;
            }
            eventsList.sort(Comparator.comparing(EventsV1Event::getEventTime));
            List<String> headers = Arrays.asList("Event Time", "Action", "API Version", "Kind", "Note", "Reason",
                    "Regarding", "Related", "Reporting Controller", "Reporting Instance", "Series", "Type");

            CSVBuilder csvBuilder = new CSVBuilder().headers(headers);
            for (EventsV1Event event : eventsList) {
                List<String> rowData = Arrays.asList(event.getEventTime().toString(), event.getAction(),
                        event.getApiVersion(), event.getKind(), event.getNote(), event.getReason(),
                        event.getRegarding() == null ? "" : event.getRegarding().toString(),
                        event.getRelated() == null ? "" : event.getRelated().toString(),
                        event.getReportingController(), event.getReportingInstance(), event.getSeries() == null ? ""
                                : event.getSeries().toString(), event.getType());

                csvBuilder = csvBuilder.row(rowData);
                csvBuilder.build(csvPath);
            }

        } catch (Exception e) {
            System.err.println("Error building K8s events API events log: " + e.getMessage());
        }
    }

    public static void generateK8sCoreApiEventsLog(Path csvPath) {
        try {
            CoreV1EventList k8sEventsListObj = EventsController.getCoreApiEvents();
            List<CoreV1Event> eventsList = k8sEventsListObj.getItems();
            if (eventsList == null || eventsList.isEmpty()) {
                System.out.println("No kubernetes core API events found");
                return;
            }
            eventsList.sort(Comparator.comparing(CoreV1Event::getEventTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            List<String> headers = Arrays.asList("Event Time", "Action", "API Version", "Count", "First Timestamp",
                    "Involved Object", "Kind", "Last Timestamp", "Message", "Metadata", "Reason", "Related",
                    "Reporting Component", "Reporting Instance", "Series", "Source", "Type");

            CSVBuilder csvBuilder = new CSVBuilder().headers(headers);
            for (CoreV1Event event : eventsList) {
                List<String> rowData = Arrays.asList(event.getEventTime() == null ? "" :
                        event.getEventTime().toString(), event.getAction() == null ? "" : event.getAction(),
                        event.getApiVersion() == null ? "" : event.getApiVersion(), event.getCount() == null ? "" :
                                event.getCount().toString(), event.getFirstTimestamp() == null ? "" :
                                event.getFirstTimestamp().toString(), event.getInvolvedObject() == null ? "" :
                                event.getInvolvedObject().toString(), event.getKind() == null ? "" : event.getKind(),
                        event.getLastTimestamp() == null ? "" : event.getLastTimestamp().toString(),
                        event.getMessage() == null ? "" : event.getMessage(), event.getMetadata() == null ? "" :
                                event.getMetadata().toString(), event.getReason() == null ? "" : event.getReason(),
                        event.getRelated() == null ? "" : event.getRelated().toString(),
                        event.getReportingComponent() == null ? "" : event.getReportingComponent(),
                        event.getReportingInstance() == null ? "" : event.getReportingInstance(),
                        event.getSeries() == null ? "" : event.getSeries().toString(), event.getSource() == null ?
                                "" : event.getSource().toString(), event.getType() == null ? "" : event.getType());

                csvBuilder = csvBuilder.row(rowData);
            }
            csvBuilder.build(csvPath);

        } catch (Exception e) {
            System.err.println("Error building K8s core API events log: " + e.getMessage());
        }
    }

}

