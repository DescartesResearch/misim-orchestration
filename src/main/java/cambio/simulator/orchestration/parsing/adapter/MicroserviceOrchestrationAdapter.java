package cambio.simulator.orchestration.parsing.adapter;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.patterns.InstanceOwnedPatternConfiguration;
import cambio.simulator.entities.patterns.LoadBalancer;
import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import cambio.simulator.parsing.adapter.architecture.*;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import desmoj.core.dist.ContDistNormal;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;

public class MicroserviceOrchestrationAdapter extends MicroserviceAdapter {
    public MicroserviceOrchestrationAdapter(MiSimModel baseModel, LinkedList<DependencyDescription> dependencies) {
        super(baseModel, dependencies);
    }

    @Override
    public Microservice read(JsonReader in) {
        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
        String microserviceName = root.get("name").getAsString();

        Gson gson = GsonHelper
                .getGsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Microservice.class, new OrchestrationMicroserviceInstanceCreator(model, microserviceName))
                .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(model))
                .registerTypeAdapter(LoadBalancer.class, new LoadBalancerAdapter(model))
                .registerTypeAdapter(Operation.class, new OperationAdapter(model, microserviceName, dependencies))
                .registerTypeAdapter(InstanceOwnedPatternConfiguration.class, new InstanceOwnedPatternConfigAdapter())
                .registerTypeAdapter(ServiceOwnedPattern.class, new ServiceOwnedPatternAdapter(model, microserviceName))
                .create();

        Microservice microservice = gson.fromJson(root, Microservice.class);

        //inject owning microservice into ownerMs field of operations
        try {
            Field ownerInjectionFieldOperation = Operation.class.getDeclaredField("ownerMS");
            ownerInjectionFieldOperation.setAccessible(true);
            for (Operation operation : microservice.getOperations()) {
                ownerInjectionFieldOperation.set(operation, microservice);
            }

            Field ownerInjectionFieldServicePattern = ServiceOwnedPattern.class.getDeclaredField("owner");
            Field serviceOwnedPatternsField = Microservice.class.getDeclaredField("serviceOwnedPatterns");
            ownerInjectionFieldServicePattern.setAccessible(true);
            serviceOwnedPatternsField.setAccessible(true);
            ServiceOwnedPattern[] serviceOwnedPatterns =
                    (ServiceOwnedPattern[]) serviceOwnedPatternsField.get(microservice);
            for (ServiceOwnedPattern pattern : serviceOwnedPatterns) {
                ownerInjectionFieldServicePattern.set(pattern, microservice);
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return microservice;
    }

    private static final class OrchestrationMicroserviceInstanceCreator
            implements InstanceCreator<Microservice> {
        private final MiSimOrchestrationModel baseModel;
        private final String microserviceName;

        public OrchestrationMicroserviceInstanceCreator(MiSimModel baseModel, String name) {
            if (!(baseModel instanceof MiSimOrchestrationModel)) {
                throw new IllegalArgumentException("Unexpected type: baseModel is not of type MiSimOrchestrationModel");
            }
            this.baseModel = (MiSimOrchestrationModel) baseModel;
            microserviceName = name;
        }

        @Override
        public Microservice createInstance(Type type) {
            if(baseModel.getOrchestrationConfig().isOrchestrate()){
                return new MicroserviceOrchestration(baseModel, microserviceName, true);
            } else {
                return new Microservice(baseModel, microserviceName, true);
            }
        }
    }

}
