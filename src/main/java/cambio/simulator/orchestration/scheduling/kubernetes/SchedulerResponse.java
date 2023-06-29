package cambio.simulator.orchestration.scheduling.kubernetes;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.openapi.models.V1Node;

import java.util.List;

public class SchedulerResponse {
    @SerializedName("Binded")
    private List<BindingInformation> binded;
    @SerializedName("Failed")
    private List<BindingFailureInformation> failed;
    @SerializedName("NewNodes")
    private List<V1Node> newNodes;
    @SerializedName("DeletedNodes")
    private List<V1Node> deletedNodes;

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

    public List<V1Node> getNewNodes() {
        return newNodes;
    }

    public List<V1Node> getDeletedNodes() {
        return deletedNodes;
    }

    public void setDeletedNodes(List<V1Node> deletedNodes) {
        this.deletedNodes = deletedNodes;
    }

    public void setNewNodes(List<V1Node> newNodes) {
        this.newNodes = newNodes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (V1Node newNode : newNodes) {
            builder.append("Node ").append(newNode.getMetadata().getName()).append(" created\n");
        }
        for (V1Node deletedNode : deletedNodes) {
            builder.append("Node ").append(deletedNode.getMetadata().getName()).append(" deleted\n");
        }
        for (BindingInformation bi : binded) {
            builder.append("Pod ").append(bi.getPod()).append(" bound to node ").append(bi.getNode());
        }
        for (BindingFailureInformation bfi : failed) {
            builder.append("Pod ").append(bfi.getPod()).append(" cannot be scheduled, reason: ").append(bfi.getMessage());
        }
        return builder.toString();
    }
}
