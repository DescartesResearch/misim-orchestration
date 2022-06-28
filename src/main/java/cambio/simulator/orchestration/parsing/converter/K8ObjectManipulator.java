package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.orchestration.parsing.kubernetes.K8ObjectDto;
import cambio.simulator.parsing.ParsingException;

public interface K8ObjectManipulator {

    void manipulate() throws ParsingException;

    void setK8ObjectDto(K8ObjectDto k8ObjectDto);
}
