package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.orchestration.parsing.kubernetes.K8ObjectDto;
import cambio.simulator.parsing.ParsingException;

import java.util.Set;

public interface DtoToObjectMapper<T> {
    T buildScheme() throws ParsingException;

    void setMicroservices(Set<Microservice> microservices);

    void setK8ObjectDto(K8ObjectDto k8ObjectDto);
}
