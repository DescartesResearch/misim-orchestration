package cambio.simulator.orchestration.rest;

import cambio.simulator.orchestration.rest.dto.SchedulerResponse;
import cambio.simulator.orchestration.rest.dto.UpdateNodesRequest;
import cambio.simulator.orchestration.rest.dto.UpdatePodsRequest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class KubeSchedulerController {
    static String API_URL = "http://127.0.0.1:8000/";
    static String PATH_PODS = "updatePods";
    static String PATH_NODES = "updateNodes";

    public static void updateNodes(UpdateNodesRequest nodeList) throws IOException {
        String json = new Gson().toJson(nodeList);
        post(json, PATH_NODES);
    }

    public static SchedulerResponse updatePods(UpdatePodsRequest upr) throws IOException {
        String json = new Gson().toJson(upr);
        String response = post(json, PATH_PODS);
        return new Gson().fromJson(response, SchedulerResponse.class);
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

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),
                StandardCharsets.UTF_8))) {
            for (String responseLine; (responseLine = br.readLine()) != null; ) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }
}
