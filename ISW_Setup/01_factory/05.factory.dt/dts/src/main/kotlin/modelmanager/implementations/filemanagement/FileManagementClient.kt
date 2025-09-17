package dts.modelmanager.implementations.filemanagement

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import dts.modelmanager.components.IClient
import dts.modelmanager.components.IQuery
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import kotlin.reflect.jvm.internal.impl.builtins.StandardNames.FqNames.collection


class FileManagementClient(override var iQuery: IQuery) : IClient {
    /*
    init {
        this.iQuery = FileManagementQuery();
    }
    String uri = "mongodb+srv://user:password@cluster.example.mongodb.net/";
    MongoClient mongoClient = MongoClients.create(uri)
    var database: MongoDatabase = mongoClient.getDatabase("sample_mflix")

    fun create(path: String){
        database.createCollection("movies");
    }

    fun insert(content: String){
        val database: MongoDatabase = mongoClient.getDatabase("sample_mflix")
        val collection: MongoCollection<Document> = database.getCollection("movies")

        collection.insertOne(
            Document()
                .append("_id", ObjectId())
                .append("title", "Silly Video")
                .append("genres", Arrays.asList("Action", "Adventure"))
        )
    }

    fun update(path:String, content: String){
        Document updateQuery = new Document().append("title", "Silly Video");

        Bson updates = Updates.combine(
                Updates.set("runtime", 99),
        Updates.addToSet("genres", "Comedy")
        Updates.currentTimestamp("lastUpdated"));

        collection.updateOne(updateQuery, updates);
    }

    fun updateMany(pathes:String, contents:String){
        val query: Bson = gt("num_mflix_comments", 50)

        val updates = Updates.combine(
            Updates.addToSet("genres", "Frequently Discussed"),
            Updates.currentTimestamp("lastUpdated")
        )

        collection.updateMany(query, updates)
    }

    fun Unit? read(path: String){
        collection.find(eq("title", "The Great Train Robbery"))
            .first();
        return null;
    }

    fun Unit? read(id: String){
        val projectionFields = Projections.fields(
            Projections.include("title", "genres"),
            Projections.excludeId()
        )

        collection.find(eq("title", "The Great Train Robbery"))
            .projection(projectionFields)
            .sort(Sorts.ascending("year"))
            .first()
        return null
    }

    fun delete(id: String){
        val deleteQuery: Bson = eq("title", "Silly Video")

        collection.deleteOne(deleteQuery)
    }*/


}