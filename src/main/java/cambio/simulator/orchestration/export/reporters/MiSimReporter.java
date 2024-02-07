package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.export.CSVBuilder;
import cambio.simulator.orchestration.export.Stats;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MiSimReporter {
    public static void generateMiSimScalingReports(Path reportPath) {
        Map<Microservice, List<Stats.ScalingRecord>> microServiceRecordsMap =
                Stats.getInstance().getMicroServiceRecordsMap();

        for (Microservice microservice : microServiceRecordsMap.keySet()) {
            CSVBuilder csvBuilder = new CSVBuilder().delimiter(",");
            List<Stats.ScalingRecord> scalingRecords = microServiceRecordsMap.get(microservice);

            List<String> headers = new ArrayList<>();
            headers.add("Time");
            headers.add("AvgConsumption");
            headers.add("#Instances");
            headers.add("NetworkRequestTimeoutEvent_" + microservice.getPlainName());

            List<MicroserviceInstance> sortedInstances = new ArrayList<>(microservice.getInstancesSet());
            sortedInstances.sort(Comparator.comparingInt(m -> Integer.parseInt(m.getQuotedName().split("#")[1])));
            sortedInstances.forEach(instance -> headers.add(instance.getQuotedName()));
            csvBuilder.headers(headers);

            // Adding rows
            for (Stats.ScalingRecord scalingRecord : scalingRecords) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(scalingRecord.getTime()));
                row.add(String.valueOf(scalingRecord.getAvgConsumption()));
                row.add(String.valueOf(scalingRecord.getAmountPods()));

                for (MicroserviceInstance instance : sortedInstances) {
                    row.add(String.valueOf(scalingRecord.getMicroserviceInstanceDoubleHashMap().get(instance)));
                }
                csvBuilder.row(row);
            }
            Path csvPath = reportPath.resolve(microservice.getPlainName() + "_alt" + ".csv");
            try {
                csvBuilder.build(csvPath);
            } catch (CSVBuilder.CSVBuilderException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
