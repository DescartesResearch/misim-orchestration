package cambio.simulator.orchestration.scheduling.kubernetes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SchedulerResponse {
    @SerializedName("Binded")
    private List<BindingInformation> binded;
    @SerializedName("Failed")
    private List<BindingFailureInformation> failed;

    public SchedulerResponse() {
    }

    public List<BindingFailureInformation> getFailed() {
        return failed;
    }

    public List<BindingInformation> getBinded() {
        return binded;
    }

    public void setBinded(List<BindingInformation> binded) {
        this.binded = binded;
    }

    public void setFailed(List<BindingFailureInformation> failed) {
        this.failed = failed;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (BindingInformation bi : binded) {
            builder.append("Pod ").append(bi.getPod()).append(" bound to node ").append(bi.getNode());
        }
        for (BindingFailureInformation bfi : failed) {
            builder.append("Pod ").append(bfi.getPod()).append(" cannot be scheduled, reason: ").append(bfi.getMessage());
        }
        return builder.toString();
    }
}
