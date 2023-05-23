package cambio.simulator.orchestration.scheduling.kubernetes;

import com.google.gson.annotations.SerializedName;

public class BindingFailureInformation {
    @SerializedName("Pod")
    private String pod;
    @SerializedName("Message")
    private String message;

    public BindingFailureInformation() {
    }

    public String getMessage() {
        return message;
    }

    public String getPod() {
        return pod;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }
}
