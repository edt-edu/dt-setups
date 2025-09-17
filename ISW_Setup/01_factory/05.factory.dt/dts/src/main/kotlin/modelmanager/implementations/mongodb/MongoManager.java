package modelmanager.implementations.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;

public class MongoManager {

    String uri = "mongodb://localhost:27017";
    MongoClient mongoClient = MongoClients.create(uri);

    public void connect(){
        MongoDatabase database = mongoClient.getDatabase("DB1");
        database.createCollection("test");

        MongoCollection<Document> collection = database.getCollection("movies");

        collection.insertOne(new Document()
                .append("_id", new ObjectId())
                .append("title", "Silly Video")
                .append("genres", Arrays.asList("Action", "Adventure")));
    }

    public static void main(String[] args) {
        MongoManager m = new MongoManager();
        m.connect();
    }
}
