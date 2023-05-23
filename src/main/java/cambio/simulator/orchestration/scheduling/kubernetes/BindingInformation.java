package cambio.simulator.orchestration.scheduling.kubernetes;

import com.google.gson.annotations.SerializedName;

public class BindingInformation {
    @SerializedName("Pod")
    private String pod;
    @SerializedName("Node")
    private String node;

    public BindingInformation() {
    }

    public String getPod() {
        return pod;
    }

    public String getNode() {
        return node;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public void setNode(String node) {
        this.node = node;
    }
}
