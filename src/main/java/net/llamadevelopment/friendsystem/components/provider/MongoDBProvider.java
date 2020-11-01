package net.llamadevelopment.friendsystem.components.provider;

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
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBProvider extends Provider {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> userCollection, friendCollection, settingsCollection, requestCollection;

    @Override
    public void connect(FriendSystem server) {
        CompletableFuture.runAsync(() -> {
            try {
                MongoClientURI uri = new MongoClientURI(server.getConfig().getString("MongoDB.Uri"));
                this.mongoClient = new MongoClient(uri);
                this.mongoDatabase = this.mongoClient.getDatabase(server.getConfig().getString("MongoDB.Database"));
                this.userCollection = this.mongoDatabase.getCollection("users");
                this.friendCollection = this.mongoDatabase.getCollection("friends");
                this.settingsCollection = this.mongoDatabase.getCollection("settings");
                this.requestCollection = this.mongoDatabase.getCollection("requests");
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
        this.mongoClient.close();
        server.getLogger().info("[MongoClient] Closed connection.");
    }

    @Override
    public void createData(String player) {
        CompletableFuture.runAsync(() -> {
            Document document = new Document("player", player)
                    .append("uid", FriendSystemAPI.getRandomID());
            this.userCollection.insertOne(document);
            Document document1 = new Document("player", player)
                    .append("notifications", true)
                    .append("requests", true);
            this.settingsCollection.insertOne(document1);
        });
    }

    @Override
    public void userExists(String player, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            Document document = this.userCollection.find(new Document("player", player)).first();
            exists.accept(document != null);
        });
    }

    @Override
    public void createFriendRequest(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            Document document = new Document("tuid", targetuid)
                    .append("puid", playeruid)
                    .append("rid", FriendSystemAPI.getRandomID());
            this.requestCollection.insertOne(document);
        })));
    }

    @Override
    public void removeFriendRequest(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            String rid = "null";
            Document document = this.requestCollection.find(new Document("tuid", targetuid).append("puid", playeruid)).first();
            if (document != null) rid = document.getString("rid");
            MongoCollection<Document> collection = requestCollection;
            collection.deleteOne(new Document("rid", rid));
        })));
    }

    @Override
    public void removeFriend(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            MongoCollection<Document> collection = this.friendCollection;
            collection.deleteOne(new Document("puid", playeruid).append("tuid", targetuid));
            collection.deleteOne(new Document("puid", targetuid).append("tuid", playeruid));
        })));
    }

    @Override
    public void createFriendship(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            Document document = new Document("puid", playeruid)
                    .append("tuid", targetuid);
            this.friendCollection.insertOne(document);
            Document document1 = new Document("puid", targetuid)
                    .append("tuid", playeruid);
            this.friendCollection.insertOne(document1);
            this.removeFriendRequest(target, player);
        })));
    }

    @Override
    public void convertToID(String player, Consumer<String> consumer) {
        CompletableFuture.runAsync(() -> {
            Document document = this.userCollection.find(new Document("player", player)).first();
            if (document != null) consumer.accept(document.getString("uid"));
        });
    }

    @Override
    public void convertToName(String uid, Consumer<String> consumer) {
        CompletableFuture.runAsync(() -> {
            Document document = this.userCollection.find(new Document("uid", uid)).first();
            if (document != null) consumer.accept(document.getString("player"));
        });
    }

    @Override
    public void areFriends(String player, String target, Consumer<Boolean> areFriends) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            Document document = this.friendCollection.find(new Document("puid", playeruid).append("tuid", targetuid)).first();
            areFriends.accept(document != null);
        })));
    }

    @Override
    public void requestExists(String player, String target, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            Document document = this.requestCollection.find(new Document("tuid", targetuid).append("puid", playeruid)).first();
            exists.accept(document != null);
        })));
    }

    @Override
    public void toggleRequest(Player player) {
        CompletableFuture.runAsync(() -> {
            Document document = this.settingsCollection.find(new Document("player", player.getName())).first();
            assert document != null;
            if (document.getBoolean("requests")) {
                Bson bson = new Document("requests", false);
                Bson bson1 = new Document("$set", bson);
                this.settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("requests-denied"));
            } else {
                Bson bson = new Document("requests", true);
                Bson bson1 = new Document("$set", bson);
                this.settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("requests-allowed"));
            }
        });
    }

    @Override
    public void toggleNotification(Player player) {
        CompletableFuture.runAsync(() -> {
            Document document = this.settingsCollection.find(new Document("player", player.getName())).first();
            assert document != null;
            if (document.getBoolean("notifications")) {
                Bson bson = new Document("notifications", false);
                Bson bson1 = new Document("$set", bson);
                this.settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("notifications-denied"));
            } else {
                Bson bson = new Document("notifications", true);
                Bson bson1 = new Document("$set", bson);
                this.settingsCollection.updateOne(document, bson1);
                player.sendMessage(Language.get("notifications-allowed"));
            }
        });
    }


    @Override
    public void getFriendData(String player, Consumer<PlayerSettings> playerSettings) {
        CompletableFuture.runAsync(() -> {
            boolean r;
            boolean n;
            Document document = this.settingsCollection.find(new Document("player", player)).first();
            assert document != null;
            r = document.getBoolean("requests");
            n = document.getBoolean("notifications");
            playerSettings.accept(new PlayerSettings(r, n));
        });
    }


    @Override
    public void getFriends(String player, Consumer<List<String>> friends) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> {
            List<String> list = new ArrayList<>();
            this.friendCollection.find().forEach((Block<? super Document>) document -> this.convertToName(document.getString("tuid"), targetuid -> {
                if (document.getString("puid").equals(playeruid)) list.add(targetuid);
            }));
            friends.accept(list);
        }));
    }

    @Override
    public void getRequests(String player, Consumer<List<String>> requests) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> {
            List<String> list = new ArrayList<>();
            this.requestCollection.find().forEach((Block<? super Document>) document -> this.convertToName(document.getString("puid"), targetuid -> {
                if (document.getString("tuid").equals(playeruid)) list.add(targetuid);
            }));
            requests.accept(list);
        }));
    }

    @Override
    public String getProvider() {
        return "MongoDB";
    }

}
