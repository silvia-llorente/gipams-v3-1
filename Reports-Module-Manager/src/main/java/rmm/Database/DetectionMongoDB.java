package rmm.Database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import rmm.Models.DetectionParam;
import rmm.Models.DetectionParamCollection;

import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;

public class DetectionMongoDB extends GipamsMongoDB {


    private static DetectionMongoDB instance = null;

    public static DetectionMongoDB GetInstance() throws IOException {
        if (instance == null) instance = new DetectionMongoDB();
        return instance;
    }

    protected DetectionMongoDB() throws IOException {
        super(COLLECTION_DETECTION_PARAMS);
    }

    public DetectionParamCollection GetAllParams() {

        DetectionParamCollection paramsResult = ExecuteQuery(collection -> {
            DetectionParamCollection params = new DetectionParamCollection();

            for (Document doc : collection.find()) {
                String json = doc.toJson();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    params.add(mapper.readValue(json, DetectionParam.class));
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            return params;
        });

        if (paramsResult == null)
            paramsResult = new DetectionParamCollection();

        return paramsResult;

    }

    public boolean UpdateParams(DetectionParamCollection detectionParams) {
        Boolean ok = ExecuteQuery(collection -> {
            int okCount = 0;
            for (DetectionParam dv : detectionParams) {
                Bson query = eq("_id", new ObjectId(dv._id));

                Bson updates = Updates.set("value", dv.value);

                UpdateOptions opts = new UpdateOptions().upsert(false);
                UpdateResult result = collection.updateOne(query, updates, opts);
                if (result.getMatchedCount() == 1)
                    okCount++;
            }

            if (okCount == detectionParams.size())
                return true;

            return false;
        });

        if (ok == null)
            ok = false;

        return ok;

    }

}
