package cambio.simulator.orchestration.rest;

import cambio.simulator.orchestration.rest.dto.NodeFailureRequest;
import cambio.simulator.orchestration.rest.dto.NodeFailureResponse;
import cambio.simulator.orchestration.rest.dto.NodeNoExecuteRequest;
import cambio.simulator.orchestration.rest.dto.NodeNoExecuteResponse;
import cambio.simulator.orchestration.rest.dto.NodeNotReadyRequest;
import cambio.simulator.orchestration.rest.dto.NodeNotReadyResponse;
import cambio.simulator.orchestration.rest.dto.PodFailureRequest;
import cambio.simulator.orchestration.rest.dto.SchedulerResponse;
import cambio.simulator.orchestration.rest.dto.UpdateNodesRequest;
import cambio.simulator.orchestration.rest.dto.UpdatePodsRequest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class KubeSchedulerController {
    static String API_URL = "http://127.0.0.1:8000/";
    static String PATH_PODS = "updatePods";
    static String PATH_NODES = "updateNodes";
    static String PATH_FAIL_POD = "failPod";
    static String PATH_FAILED_NODES = "failNodes";
    static String PATH_MARK_NODES_NOT_READY = "markNodesNotReady";
    static String PATH_ADD_NO_EXECUTE_TAINT = "addNoExecuteTaint";
    static Gson gson = new Gson();

    public static void updateNodes(UpdateNodesRequest nodeList) throws IOException {
        String json = gson.toJson(nodeList);
        post(json, PATH_NODES);
    }

    public static SchedulerResponse updatePods(UpdatePodsRequest upr) throws IOException {
        String json = gson.toJson(upr);
        String response = post(json, PATH_PODS);
        return gson.fromJson(response, SchedulerResponse.class);
    }

    public static NodeFailureResponse failNodes(NodeFailureRequest request) throws IOException {
        String json = gson.toJson(request);
        String response = post(json, PATH_FAILED_NODES);
        return gson.fromJson(response, NodeFailureResponse.class);
    }

    public static void failPod(PodFailureRequest request) throws IOException {
        String json = gson.toJson(request);
        post(json, PATH_FAIL_POD);
    }

    public static NodeNotReadyResponse markNodesNotReady(NodeNotReadyRequest request) throws IOException {
        String json = gson.toJson(request);
        String response = post(json, PATH_MARK_NODES_NOT_READY);
        return gson.fromJson(response, NodeNotReadyResponse.class);
    }

    public static NodeNoExecuteResponse addNoExecuteTaint(NodeNoExecuteRequest request) throws IOException {
        String json = gson.toJson(request);
        String response = post(json, PATH_ADD_NO_EXECUTE_TAINT);
        return gson.fromJson(response, NodeNoExecuteResponse.class);
    }

    private static String post(String content, String path) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = content.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int status = con.getResponseCode();
        InputStreamReader streamReader;
        System.err.println("Received HTTP status code " + status + " from k8s adapter");
        if (status >= 200 && status < 300) {
            streamReader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
        } else {
            InputStream err = con.getErrorStream();
            if (err != null) {
                streamReader = new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8);
            } else {
                Object resContent = con.getContent();
                throw new IOException("Scheduler: HTTP " + status + ": " + resContent.toString());
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(streamReader)) {
            for (String responseLine; (responseLine = br.readLine()) != null;) {
                response.append(responseLine.trim());
            }
        }

        if (status >= 400) {
            throw new IOException("Scheduler: HTTP " + status + ": " + response);
        }
        return response.toString();
    }
}
