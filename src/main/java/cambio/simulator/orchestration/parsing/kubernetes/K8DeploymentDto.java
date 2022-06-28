package cambio.simulator.orchestration.parsing.kubernetes;

public class K8DeploymentDto extends K8ObjectDto{
    private SpecDeploymentDto spec;

    public SpecDeploymentDto getSpec() {
        return spec;
    }

    public void setSpec(SpecDeploymentDto spec) {
        this.spec = spec;
    }
}
