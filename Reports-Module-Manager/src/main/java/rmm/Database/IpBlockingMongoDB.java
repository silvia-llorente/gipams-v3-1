package rmm.Database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import rmm.Models.*;

import java.io.IOException;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class IpBlockingMongoDB extends GipamsMongoDB {


    private static IpBlockingMongoDB instance = null;

    public static IpBlockingMongoDB GetInstance() throws IOException {
        if (instance == null) instance = new IpBlockingMongoDB();
        return instance;
    }

    protected IpBlockingMongoDB() throws IOException {
        super(COLLECTION_IP_BLOCKING);
    }

    public IpBlocking GetBlockingByIP(String ip) {

        return ExecuteQuery(collection -> {
            Document doc = collection.find(eq("ip", ip)).first();
            if (doc == null) {
                System.out.println("No results found.");
            } else {
                String json = doc.toJson();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue(json, IpBlocking.class);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            return null;
        });

    }

    public boolean InsertBlocking(IpBlocking ipBlocking) {

        Boolean done = ExecuteQuery(collection -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(ipBlocking);
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

    public boolean UpdateBlocking(IpBlocking ipBlocking) {

        Boolean done = ExecuteQuery(collection -> {
            try {
                Bson query = eq("_id", new ObjectId(ipBlocking._id));

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(ipBlocking);
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

    public IpBlockingCollection GetAllBlockedIPs() {
        IpBlockingCollection ipsResult = ExecuteQuery(collection -> {
            IpBlockingCollection ips = new IpBlockingCollection();


            for (Document doc : collection.find(eq("blocked", true))) {
                String json = doc.toJson();
                System.out.println(json);

                ObjectMapper mapper = new ObjectMapper();
                try {
                    ips.add(mapper.readValue(json, IpBlocking.class));
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            return ips;
        });

        if (ipsResult == null)
            ipsResult = new IpBlockingCollection();

        return ipsResult;
    }
}
