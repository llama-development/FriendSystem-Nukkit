package net.llamadevelopment.friendsystem.components.managers.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.friendsystem.FriendSystem;
import org.bson.Document;

public class MongoDBProvider {

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static MongoCollection<Document> userCollection, friendCollection, settingsCollection, requestCollection;

    public static void connect(FriendSystem instance) {
        try {
            MongoClientURI uri = new MongoClientURI(instance.getConfig().getString("MongoDB.Uri"));
            mongoClient = new MongoClient(uri);
            mongoDatabase = mongoClient.getDatabase(instance.getConfig().getString("MongoDB.Database"));
            userCollection = mongoDatabase.getCollection("users");
            friendCollection = mongoDatabase.getCollection("friends");
            settingsCollection = mongoDatabase.getCollection("settings");
            requestCollection = mongoDatabase.getCollection("requests");
            instance.getLogger().info("§aConnected successfully to database!");
        } catch (Exception e) {
            instance.getLogger().error("§4Failed to connect to database.");
            instance.getLogger().error("§4Please check your details in the config.yml.");
            e.printStackTrace();
        }
    }

    public static MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static MongoCollection<Document> getFriendCollection() {
        return friendCollection;
    }

    public static MongoCollection<Document> getSettingsCollection() {
        return settingsCollection;
    }

    public static MongoCollection<Document> getUserCollection() {
        return userCollection;
    }

    public static MongoCollection<Document> getRequestCollection() {
        return requestCollection;
    }
}
