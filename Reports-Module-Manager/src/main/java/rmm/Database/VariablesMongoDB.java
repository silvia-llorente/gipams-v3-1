package rmm.Database;

import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;

public class VariablesMongoDB extends GipamsMongoDB {

    private static VariablesMongoDB instance = null;

    public static VariablesMongoDB GetInstance() throws IOException {
        if (instance == null) instance = new VariablesMongoDB();
        return instance;
    }

    protected VariablesMongoDB() throws IOException {
        super(COLLECTION_LOG_VARIABLES);
    }

    public <T> T GetVariableByName(String name, Class<T> tclass) {

        T date = ExecuteQuery(collection -> {
            Document doc = collection.find(eq("name", name)).first();
            if (doc == null) {
                System.out.println("No variable found: " + name);
            } else {
                return doc.get("value", tclass);
            }
            return null;
        });

        return date;

    }

    public <T> boolean UpdateVariableByName(String name, T value) {
        Boolean done = ExecuteQuery(collection -> {
            Bson query = eq("name", name);

            Document doc = new Document();
            doc.put("name", name);
            doc.put("value", value);

            ReplaceOptions opts = new ReplaceOptions().upsert(true);
            UpdateResult result = collection.replaceOne(query, doc, opts);
            if (result.getModifiedCount() == 1)
                return true;
            return false;
        });

        if (done == null)
            done = false;

        return done;

    }
}
