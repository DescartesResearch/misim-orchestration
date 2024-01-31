package cambio.simulator.orchestration.scheduling.kubernetes;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindingFailureInformation {
    @SerializedName("Pod")
    private String pod;
    @SerializedName("Message")
    private String message;

    public BindingFailureInformation() {
    }
}
