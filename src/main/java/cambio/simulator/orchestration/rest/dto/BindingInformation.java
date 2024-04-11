package cambio.simulator.orchestration.rest.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindingInformation {

    @SerializedName("Pod")
    private String pod;

    @SerializedName("Node")
    private String node;

    public BindingInformation() {
    }

}
