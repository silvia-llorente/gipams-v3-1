package rmm.Database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import rmm.Models.LogMeasurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class MetricsMongoDB extends GipamsMongoDB {

    private static MetricsMongoDB instance = null;

    public static MetricsMongoDB GetInstance() throws IOException {
        if (instance == null) instance = new MetricsMongoDB();
        return instance;
    }

    protected MetricsMongoDB() throws IOException {
        super(COLLECTION_LOGS);
    }

    public ArrayList<String> GetAllIPs() {

        ArrayList<String> IPsResult = ExecuteQuery(collection -> {
            ArrayList<String> IPs = new ArrayList<>();

            Bson projectionFields = Projections.fields(
                    Projections.include("ip"),
                    Projections.excludeId());

            for (Document doc : collection.find()
                    .projection(projectionFields)) {
                String ip = (String) doc.get("ip");
                IPs.add(ip);
            }
            return IPs;
        });

        if (IPsResult == null)
            IPsResult = new ArrayList<>();

        return IPsResult;

    }

    public LogMeasurement GetMeasurementByIP(String ip) {

        return ExecuteQuery(collection -> {
            Document doc = collection.find(eq("ip", ip)).first();
            if (doc == null) {
                System.out.println("No results found.");
            } else {
                String json = doc.toJson();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue(json, LogMeasurement.class);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            return null;
        });

    }

    public boolean InsertMeasurement(LogMeasurement measurement) {

        Boolean done = ExecuteQuery(collection -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(measurement);
                Document doc = Document.parse(json);
                InsertOneResult result = collection.insertOne(doc);

                BsonObjectId id = Objects.requireNonNull(result.getInsertedId()).asObjectId();
                return true;
            } catch (NullPointerException | JsonProcessingException ex) {
                System.out.println(ex.getMessage());
                return false;
            }
        });

        if (done == null)
            done = false;

        return done;

    }

    public boolean UpdateMeasurement(LogMeasurement measurement) {

        Boolean done = ExecuteQuery(collection -> {
            try {
                Bson query = eq("_id", new ObjectId(measurement._id));

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(measurement);
                Document doc = Document.parse(json);
                doc.remove("_id");

                ReplaceOptions opts = new ReplaceOptions().upsert(true);
                UpdateResult result = collection.replaceOne(query, doc, opts);
                if (result.getModifiedCount() == 1)
                    return true;
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
            }
            return false;
        });

        if (done == null)
            done = false;

        return done;

    }

}
