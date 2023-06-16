package cambio.simulator.orchestration.parsing.kubernetes;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getSpec() {
        return spec;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setSpec(Map<String, Object> spec) {
        this.spec = spec;
    }

    public Map<String, Object> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Object> status) {
        this.status = status;
    }
}
