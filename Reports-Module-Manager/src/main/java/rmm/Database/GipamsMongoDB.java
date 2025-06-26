package rmm.Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.eq;

public class GipamsMongoDB {
    protected static final String COLLECTION_LOGS = "gipamsRMAdb_Collection_Logs";
    protected static final String COLLECTION_LOG_VARIABLES = "gipamsRMAdb_Collection_variables";
    protected static final String COLLECTION_DETECTION_PARAMS = "gipamsRMAdb_Collection_Detection_Params";
    protected static final String COLLECTION_IP_BLOCKING = "gipamsRMAdb_Collection_Ip_Blocking";


    protected String connectionString = "";
    protected String connectionDatabase = "";
    protected String connectionCollection = "";

    protected GipamsMongoDB(String collection) throws IOException {
        Properties props = new Properties();
        props.load(GipamsMongoDB.class.getClassLoader().getResourceAsStream("app.properties"));
        connectionString = props.getProperty("gipamsRMAdb_StringConnection");
        connectionDatabase = props.getProperty("gipamsRMAdb_Database");
        connectionCollection = props.getProperty(collection);
    }

    protected <T> T ExecuteQuery(Function<MongoCollection<Document>, T> mongoQuery) {
        try (MongoClient mongoClient = MongoClients.create(this.connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(this.connectionDatabase);
            MongoCollection<Document> collection = database.getCollection(this.connectionCollection);

            return (T) mongoQuery.apply(collection);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return null;

    }
}
