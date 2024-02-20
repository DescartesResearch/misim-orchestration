package cambio.simulator.orchestration.parsing.kubernetes;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
public class KubernetesObjectWithMetadataSpec {
    @SerializedName("apiVersion")
    private String apiVersion;
    @SerializedName("kind")
    private String kind;
    @SerializedName("metadata")
    private Map<String, Object> metadata;
    @SerializedName("spec")
    private Map<String, Object> spec;
    @SerializedName("status")
    private Map<String, Object> status;
}
