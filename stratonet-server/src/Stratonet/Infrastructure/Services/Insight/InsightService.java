package Stratonet.Infrastructure.Services.Insight;

import Stratonet.Core.Helpers.StratonetLogger;
import Stratonet.Core.Models.PRE;
import Stratonet.Core.Models.SOL;
import Stratonet.Core.Services.Insight.IInsightService;
import Stratonet.Infrastructure.Utils.StringToMapConverter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Level;

public class InsightService implements IInsightService {
    private final String INSIGHT_API_ENDPOINT = "https://api.nasa.gov/insight_weather/?api_key=DEMO_KEY&feedtype=json&ver=1.0";
    private StratonetLogger logger;
    private HttpClient client;
    private StringToMapConverter mapper;

    public InsightService() {
        logger = StratonetLogger.getInstance();
        client = HttpClient.newHttpClient();
        mapper = new StringToMapConverter();
    }

    @Override
    public PRE GetRandomPRE() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(INSIGHT_API_ENDPOINT))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonFactory factory = new JsonFactory();

            ObjectMapper mapper = new ObjectMapper(factory);
            JsonNode rootNode = mapper.readTree(response.body());

            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
            List<String> solList = new ArrayList<>();
            int solCount = 0;
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsIterator.next();
                try {
                    Integer.parseInt(field.getKey());
                    solList.add(field.getValue().toString());
                    solCount++;
                } catch (NumberFormatException ex) {
                    break;
                }
            }

            Random rnd = new Random();
            int randomSolIndex = rnd.nextInt(solCount) + 1;

            String randomSolAsString = solList.get(randomSolIndex);

            ObjectMapper solMapper = new ObjectMapper();
            solMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            SOL sol = solMapper.readValue(randomSolAsString, SOL.class);

            return sol.PRE;

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception while fetching Insight API");
        }


        return null;
    }
}
