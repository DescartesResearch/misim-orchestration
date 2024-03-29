package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.export.Stats;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsMiSimEvent extends NamedExternalEvent {


    public StatsMiSimEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        createScalingStats();
    }


    public void createScalingStats() {
        int time = (int) getModel().presentTime().getTimeAsDouble();

        List<Microservice> microservices = new ArrayList<>(((MiSimModel) getModel()).getArchitectureModel().getMicroservices());
        for (Microservice microservice : microservices) {
            Stats.ScalingRecord scalingRecord = new Stats.ScalingRecord();
            scalingRecord.setTime(time);

            double avg = microservice.getAverageRelativeUtilization();
            scalingRecord.setAvgConsumption(avg);
            scalingRecord.setAmountPods(microservice.getInstancesCount());


//            //add event info
//            Integer integer = NetworkRequestTimeoutEvent.getMicroserviceTimeoutMap().get(microservice);
//            if (integer != null) {
//                scalingRecord.getMicroservicetimoutmap().put(microservice, Integer.valueOf(integer));
//            } else {
//                scalingRecord.getMicroservicetimoutmap().put(microservice, 0);
//            }

            for(MicroserviceInstance microserviceInstance : microservice.getInstancesSet()){
                scalingRecord.getMicroserviceInstanceDoubleHashMap().put(microserviceInstance, microserviceInstance.getRelativeWorkDemand());
            }


            List<Stats.ScalingRecord> scalingRecords = Stats.getInstance().getMicroServiceRecordsMap().get(microservice);
            if (scalingRecords != null) {
                scalingRecords.add(scalingRecord);
            } else {
                ArrayList<Stats.ScalingRecord> scalingRecordList = new ArrayList<>();
                scalingRecordList.add(scalingRecord);
                Stats.getInstance().getMicroServiceRecordsMap().put(microservice, scalingRecordList);
            }
        }
    }
}
