package net.llamadevelopment.friendsystem.components.managers;

import cn.nukkit.Player;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.managers.database.Provider;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBProvider extends Provider {

    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> userCollection, friendCollection, settingsCollection, requestCollection;

    @Override
    public void connect(FriendSystem server) {
        CompletableFuture.runAsync(() -> {
            try {
                MongoClientURI uri = new MongoClientURI(server.getConfig().getString("MongoDB.Uri"));
                mongoClient = new MongoClient(uri);
                mongoDatabase = mongoClient.getDatabase(server.getConfig().getString("MongoDB.Database"));
                userCollection = mongoDatabase.getCollection("users");
                friendCollection = mongoDatabase.getCollection("friends");
                settingsCollection = mongoDatabase.getCollection("settings");
                requestCollection = mongoDatabase.getCollection("requests");
                Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
                mongoLogger.setLevel(Level.OFF);
                server.getLogger().info("[MongoClient] Opened connection.");
            } catch (Exception e) {
                server.getLogger().info("[MongoClient] Failed to connect.");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void disconnect(FriendSystem server) {
        mongoClient.close();
        server.getLogger().info("[MongoClient] Closed connection.");
    }

    @Override
    public void createData(String player) {
        Document document = new Document("player", player)
                .append("uid", FriendSystemAPI.getRandomID());
        userCollection.insertOne(document);
        Document document1 = new Document("player", player)
                .append("notifications", true)
                .append("requests", true);
        settingsCollection.insertOne(document1);
    }

    @Override
    public boolean userExists(String player) {
        Document document = userCollection.find(new Document("player", player)).first();
        return document != null;
    }

    @Override
    public void createFriendRequest(String player, String target) {
        CompletableFuture.runAsync(() -> {
            String playeruid = convertToID(player);
            String targetuid = convertToID(target);
            Document document = new Document("tuid", targetuid)
                    .append("puid", playeruid)
                    .append("rid", FriendSystemAPI.getRandomID());
            requestCollection.insertOne(document);
        });
    }

    @Override
    public void removeFriendRequest(String player, String target) {
        CompletableFuture.runAsync(() -> {
            String playeruid = convertToID(player);
            String targetuid = convertToID(target);
            String rid = "null";
            Document document = requestCollection.find(new Document("tuid", targetuid).append("puid", playeruid)).first();
            if (document != null) rid = document.getString("rid");
            MongoCollection<Document> collection = requestCollection;
            collection.deleteOne(new Document("rid", rid));
        });
    }

    @Override
    public void removeFriend(String player, String target) {
        CompletableFuture.runAsync(() -> {
            String playeruid = convertToID(player);
            String targetuid = convertToID(target);
            MongoCollection<Document> collection = friendCollection;
            collection.deleteOne(new Document("puid", playeruid).append("tuid", targetuid));
            collection.deleteOne(new Document("puid", targetuid).append("tuid", playeruid));
        });
    }

    @Override
    public void createFriendship(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        Document document = new Document("puid", playeruid)
                .append("tuid", targetuid);
        friendCollection.insertOne(document);
        Document document1 = new Document("puid", targetuid)
                .append("tuid", playeruid);
        friendCollection.insertOne(document1);
        removeFriendRequest(target, player);
    }

    @Override
    public String convertToID(String player) {
        Document document = userCollection.find(new Document("player", player)).first();
        if (document != null) return document.getString("uid");
        return null;
    }

    @Override
    public String convertToName(String uid) {
        Document document = userCollection.find(new Document("uid", uid)).first();
        if (document != null) return document.getString("player");
        return null;
    }

    @Override
    public boolean areFriends(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        Document document = friendCollection.find(new Document("puid", playeruid).append("tuid", targetuid)).first();
        return document != null;
    }

    @Override
    public boolean requestExists(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        Document document = requestCollection.find(new Document("tuid", targetuid).append("puid", playeruid)).first();
        return document != null;
    }

    @Override
    public void toggleRequest(Player player) {
        CompletableFuture.runAsync(() -> {
            Document document = settingsCollection.find(new Document("player", player.getName())).first();
            assert document != null;
            if (document.getBoolean("requests")) {
                Bson bson = new Document("requests", false);
                Bson bson1 = new Document("$set", bson);
                settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("requests-denied"));
            } else {
                Bson bson = new Document("requests", true);
                Bson bson1 = new Document("$set", bson);
                settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("requests-allowed"));
            }
        });
    }

    @Override
    public void toggleNotification(Player player) {
        CompletableFuture.runAsync(() -> {
            Document document = settingsCollection.find(new Document("player", player.getName())).first();
            assert document != null;
            if (document.getBoolean("notifications")) {
                Bson bson = new Document("notifications", false);
                Bson bson1 = new Document("$set", bson);
                settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("notifications-denied"));
            } else {
                Bson bson = new Document("notifications", true);
                Bson bson1 = new Document("$set", bson);
                settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("notifications-allowed"));
            }
        });
    }

    @Override
    public PlayerSettings getFriendData(String player) {
        boolean r;
        boolean n;
        Document document = settingsCollection.find(new Document("player", player)).first();
        assert document != null;
        r = document.getBoolean("requests");
        n = document.getBoolean("notifications");
        return new PlayerSettings(r, n);
    }

    @Override
    public List<String> getFriends(String player) {
        List<String> list = new ArrayList<>();
        String playeruid = convertToID(player);
        friendCollection.find().forEach((Block<? super Document>) document -> {
            if (document.getString("puid").equals(playeruid)) list.add(convertToName(document.getString("tuid")));
        });
        return list;
    }

    @Override
    public List<String> getRequests(String player) {
        List<String> list = new ArrayList<>();
        String playeruid = convertToID(player);
        requestCollection.find().forEach((Block<? super Document>) document -> {
            if (document.getString("tuid").equals(playeruid)) list.add(convertToName(document.getString("puid")));
        });
        return list;
    }

    @Override
    public String getProvider() {
        return "MongoDB";
    }
}
