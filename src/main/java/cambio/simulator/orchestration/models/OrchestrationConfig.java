package cambio.simulator.orchestration.models;

import com.google.gson.annotations.SerializedName;

public class OrchestrationConfig {

    @SerializedName(value = "orchestration_dir")
    private String orchestrationDirectory;

    @SerializedName(value = "orchestrate")
    private boolean orchestrated;

    public String getOrchestrationDirectory() {
        return orchestrationDirectory;
    }

    public void setOrchestrationDirectory(String orchestrationDirectory) {
        this.orchestrationDirectory = orchestrationDirectory;
    }

    public boolean isOrchestrated() {
        return orchestrated;
    }

    public void setOrchestrated(boolean orchestrated) {
        this.orchestrated = orchestrated;
    }
}
