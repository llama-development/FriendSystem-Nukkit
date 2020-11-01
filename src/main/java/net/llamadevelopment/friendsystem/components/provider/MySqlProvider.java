package net.llamadevelopment.friendsystem.components.provider;

import cn.nukkit.Player;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.simplesqlclient.MySqlClient;
import net.llamadevelopment.friendsystem.components.simplesqlclient.objects.SqlColumn;
import net.llamadevelopment.friendsystem.components.simplesqlclient.objects.SqlDocument;
import net.llamadevelopment.friendsystem.components.simplesqlclient.objects.SqlDocumentSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MySqlProvider extends Provider {

    private MySqlClient client;

    @Override
    public void connect(FriendSystem server) {
        try {
            this.client = new MySqlClient(
                    server.getConfig().getString("MySql.Host"),
                    server.getConfig().getString("MySql.Port"),
                    server.getConfig().getString("MySql.User"),
                    server.getConfig().getString("MySql.Password"),
                    server.getConfig().getString("MySql.Database")
            );

            this.client.createTable("users", "uid",
                    new SqlColumn("uid", SqlColumn.Type.VARCHAR, 64)
                            .append("player", SqlColumn.Type.VARCHAR, 64));

            this.client.createTable("friends",
                    new SqlColumn("tuid", SqlColumn.Type.VARCHAR, 64)
                            .append("fid", SqlColumn.Type.VARCHAR, 64)
                            .append("puid", SqlColumn.Type.VARCHAR, 64));

            this.client.createTable("settings", "player",
                    new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                            .append("notifications", SqlColumn.Type.VARCHAR, 64)
                            .append("requests", SqlColumn.Type.VARCHAR, 64));

            this.client.createTable("requests",
                    new SqlColumn("rid", SqlColumn.Type.VARCHAR, 64)
                            .append("tuid", SqlColumn.Type.VARCHAR, 64)
                            .append("puid", SqlColumn.Type.VARCHAR, 64));

            server.getLogger().info("[MySqlClient] Connection opened.");
        } catch (Exception e) {
            server.getLogger().info("[MySqlClient] Failed to create connection.");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect(FriendSystem server) {
        try {
            server.getLogger().info("[MySqlClient] Connection closed.");
        } catch (Exception throwables) {
            server.getLogger().info("[MySqlClient] Failed to close connection.");
            throwables.printStackTrace();
        }
    }

    @Override
    public void createData(String player) {
        CompletableFuture.runAsync(() -> {
            this.client.insert("users", new SqlDocument("player", player)
                    .append("uid", FriendSystemAPI.getRandomID()));
            this.client.insert("settings", new SqlDocument("player", player)
                    .append("notifications", "true")
                    .append("requests", "true"));
        });
    }

    @Override
    public void userExists(String player, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("users", "player", player).first();
            exists.accept(document != null);
        });
    }

    @Override
    public void createFriendRequest(String player, String target) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> CompletableFuture.runAsync(() -> this.client.insert("requests", new SqlDocument("rid", FriendSystemAPI.getRandomID())
                .append("tuid", targetuid)
                .append("puid", playeruid)
        ))));
    }

    @Override
    public void removeFriendRequest(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            String rid = "null";

            SqlDocumentSet document = this.client.find("requests", "tuid", targetuid);
            if (document.first().getString("puid").equals(playeruid)) rid = document.first().getString("rid");

            this.client.delete("requests", "rid", rid);
        })));
    }

    @Override
    public void removeFriend(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            String fid1 = "null";
            String fid2 = "null";

            SqlDocumentSet document1 = this.client.find("friends", "puid", playeruid);
            if (document1.first().getString("tuid").equals(targetuid)) fid1 = document1.first().getString("fid");

            this.client.delete("friends", "fid", fid1);

            SqlDocumentSet document2 = this.client.find("friends", "puid", targetuid);
            if (document2.first().getString("tuid").equals(playeruid)) fid2 = document2.first().getString("fid");

            this.client.delete("friends", "fid", fid2);
        })));
    }

    @Override
    public void createFriendship(String player, String target) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            String rid = "null";

            this.client.insert("friends", new SqlDocument("fid", FriendSystemAPI.getRandomID())
                    .append("puid", playeruid)
                    .append("tuid", targetuid));

            this.client.insert("friends", new SqlDocument("fid", FriendSystemAPI.getRandomID())
                    .append("puid", targetuid)
                    .append("tuid", playeruid));

            SqlDocumentSet document = this.client.find("requests", "tuid", playeruid);
            if (document.first().getString("puid").equals(targetuid)) rid = document.first().getString("rid");

            this.client.delete("requests", "rid", rid);
        })));
    }

    @Override
    public void convertToID(String player, Consumer<String> consumer) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet document = this.client.find("users", "player", player);
            consumer.accept(document.first().getString("uid"));
        });
    }

    @Override
    public void convertToName(String uid, Consumer<String> consumer) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet document = this.client.find("users", "uid", uid);
            consumer.accept(document.first().getString("player"));
        });
    }

    @Override
    public void areFriends(String player, String target, Consumer<Boolean> areFriends) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            SqlDocument document = this.client.find("friends", "puid", playeruid).first();
            if (document == null) {
                areFriends.accept(false);
                return;
            }
            areFriends.accept(document.getString("tuid").equalsIgnoreCase(targetuid));
        })));
    }

    @Override
    public void requestExists(String player, String target, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            SqlDocument document = this.client.find("requests", "tuid", targetuid).first();
            if (document == null) {
                exists.accept(false);
                return;
            }
            exists.accept(document.getString("puid").equalsIgnoreCase(playeruid));
        })));
    }

    @Override
    public void toggleRequest(Player player) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("settings", "player", player.getName()).first();
            if (Boolean.parseBoolean(document.getString("requests"))) {
                this.client.update("settings", "player", player.getName(), new SqlDocument("requests", "false"));
                player.sendMessage(Language.get("requests-denied"));
            } else {
                this.client.update("settings", "player", player.getName(), new SqlDocument("requests", "true"));
                player.sendMessage(Language.get("requests-allowed"));
            }
        });
    }

    @Override
    public void toggleNotification(Player player) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("settings", "player", player.getName()).first();
            if (Boolean.parseBoolean(document.getString("notifications"))) {
                this.client.update("settings", "player", player.getName(), new SqlDocument("notifications", "false"));
                player.sendMessage(Language.get("notifications-denied"));
            } else {
                this.client.update("settings", "player", player.getName(), new SqlDocument("notifications", "true"));
                player.sendMessage(Language.get("notifications-allowed"));
            }
        });
    }


    @Override
    public void getFriendData(String player, Consumer<PlayerSettings> playerSettings) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("settings", "player", player).first();
            playerSettings.accept(new PlayerSettings(Boolean.parseBoolean(document.getString("requests")), Boolean.parseBoolean(document.getString("notifications"))));
        });
    }


    @Override
    public void getFriends(String player, Consumer<List<String>> friends) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> {
            List<String> list = new ArrayList<>();

            SqlDocumentSet documentSet = this.client.find("friends", "puid", playeruid);
            documentSet.getAll().forEach(document -> this.convertToName(document.getString("tuid"), list::add));

            friends.accept(list);
        }));
    }

    @Override
    public void getRequests(String player, Consumer<List<String>> requests) {
        CompletableFuture.runAsync(() -> this.convertToID(player, playeruid -> {
            List<String> list = new ArrayList<>();

            SqlDocumentSet documentSet = this.client.find("requests", "tuid", playeruid);
            documentSet.getAll().forEach(document -> {
                this.convertToName(document.getString("puid"), list::add);
            });

            requests.accept(list);
        }));
    }

    @Override
    public String getProvider() {
        return "MySql";
    }

}
