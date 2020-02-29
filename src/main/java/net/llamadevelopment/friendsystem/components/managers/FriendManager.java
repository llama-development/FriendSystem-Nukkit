package net.llamadevelopment.friendsystem.components.managers;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import com.mongodb.client.MongoCollection;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.friendsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.friendsystem.components.messaging.Messages;
import net.llamadevelopment.friendsystem.components.utils.PlayerUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Random;

public class FriendManager {

    private static FriendSystem instance = FriendSystem.getInstance();

    //Create user data
    public static void createData(String player) {
        if (instance.isMongodb()) {
            Document document = new Document("player", player)
                    .append("uid", getID());
            MongoDBProvider.getUserCollection().insertOne(document);
            Document document1 = new Document("player", player)
                    .append("notifications", true)
                    .append("requests", true);
            MongoDBProvider.getSettingsCollection().insertOne(document1);
        } else if (instance.isMysql()) {
            MySqlProvider.update("INSERT INTO users (PLAYER, UID) VALUES ('" + player + "', '" + getID() + "');");
            MySqlProvider.update("INSERT INTO settings (PLAYER, NOTIFICATIONS, REQUESTS) VALUES ('" + player + "', '" + true + "', '" + true + "');");
        } else if (instance.isYaml()) {
            String uid = getID();
            Config users = new Config(instance.getDataFolder() + "/data/user-data.yml", Config.YAML);
            users.set("Player." + player, uid);
            users.set("Uid." + uid, player);
            users.save();
            users.reload();
            Config settings = new Config(instance.getDataFolder() + "/data/settings-data.yml", Config.YAML);
            settings.set("Player." + player + ".Notifications", true);
            settings.set("Player." + player + ".Requests", true);
            settings.save();
            settings.reload();
        }
    }

    //Check if user exists
    public static boolean userExists(String player) {
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getUserCollection().find(new Document("player", player)).first();
            if (document != null) return true;
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM users WHERE PLAYER = ?");
                preparedStatement.setString(1, player);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config users = new Config(instance.getDataFolder() + "/data/user-data.yml", Config.YAML);
            if (users.exists("Player." + player)) return true;
        }
        return false;
    }

    //Create a friend request
    public static void createFriendRequest(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        if (instance.isMongodb()) {
            Document document = new Document("tuid", targetuid)
                    .append("puid", playeruid)
                    .append("rid", getID());
            MongoDBProvider.getRequestCollection().insertOne(document);
        } else if (instance.isMysql()) {
            MySqlProvider.update("INSERT INTO requests (RID, TUID, PUID) VALUES ('" + getID() + "','" + targetuid + "', '" + playeruid + "');");
        } else if (instance.isYaml()) {
            Config friends = new Config(instance.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            List<String> list = friends.getStringList("Requests." + targetuid);
            list.add(playeruid);
            friends.set("Requests." + targetuid, list);
            friends.save();
            friends.reload();
        }
    }

    //Remove a request
    public static void removeFriendRequest(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        String rid = "null";
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getRequestCollection().find(new Document("tuid", targetuid).append("puid", playeruid)).first();
            if (document != null) rid = document.getString("rid");
            MongoCollection<Document> collection = MongoDBProvider.getRequestCollection();
            collection.deleteOne(new Document("rid", rid));
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
                preparedStatement.setString(1, targetuid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (rs.getString("PUID").equalsIgnoreCase(playeruid)) rid = rs.getString("RID");
                }

                PreparedStatement preparedStatement1 = MySqlProvider.getConnection().prepareStatement("DELETE FROM requests WHERE RID = ?");
                preparedStatement1.setString(1, rid);
                preparedStatement1.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config friends = new Config(instance.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            List<String> list = friends.getStringList("Requests." + targetuid);
            list.remove(playeruid);
            friends.set("Requests." + targetuid, list);
            friends.save();
            friends.reload();
        }
    }

    //Remove a friend
    public static void removeFriend(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        String fid1 = "null";
        String fid2 = "null";
        if (instance.isMongodb()) {
            MongoCollection<Document> collection = MongoDBProvider.getFriendCollection();
            collection.deleteOne(new Document("puid", playeruid).append("tuid", targetuid));
            collection.deleteOne(new Document("puid", targetuid).append("tuid", playeruid));
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
                preparedStatement.setString(1, playeruid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (rs.getString("TUID").equalsIgnoreCase(targetuid)) fid1 = rs.getString("FID");
                }

                PreparedStatement preparedStatement1 = MySqlProvider.getConnection().prepareStatement("DELETE FROM friends WHERE FID = ?");
                preparedStatement1.setString(1, fid1);
                preparedStatement1.executeUpdate();

                PreparedStatement preparedStatement2 = MySqlProvider.getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
                preparedStatement2.setString(1, targetuid);
                ResultSet rs1 = preparedStatement2.executeQuery();
                if (rs1.next()) {
                    if (rs1.getString("TUID").equalsIgnoreCase(playeruid)) fid2 = rs1.getString("FID");
                }

                PreparedStatement preparedStatement3 = MySqlProvider.getConnection().prepareStatement("DELETE FROM friends WHERE FID = ?");
                preparedStatement3.setString(1, fid2);
                preparedStatement3.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config friends = new Config(instance.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            List<String> list1 = friends.getStringList("Friends." + targetuid);
            List<String> list2 = friends.getStringList("Friends." + playeruid);
            list1.remove(playeruid);
            list2.remove(targetuid);
            friends.set("Friends." + targetuid, list1);
            friends.set("Friends." + playeruid, list2);
            friends.save();
            friends.reload();
        }
    }

    //Add friend
    public static void createFriendship(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        String rid = "null";
        if (instance.isMongodb()) {
            Document document = new Document("puid", playeruid)
                    .append("tuid", targetuid);
            MongoDBProvider.getFriendCollection().insertOne(document);
            Document document1 = new Document("puid", targetuid)
                    .append("tuid", playeruid);
            MongoDBProvider.getFriendCollection().insertOne(document1);
            MongoCollection<Document> collection = MongoDBProvider.getRequestCollection();
            collection.deleteOne(new Document("tuid", targetuid).append("puid", playeruid));
        } else if (instance.isMysql()) {
            MySqlProvider.update("INSERT INTO friends (FID, PUID, TUID) VALUES ('" + getID() + "', '" + playeruid + "', '" + targetuid + "');");
            MySqlProvider.update("INSERT INTO friends (FID, PUID, TUID) VALUES ('" + getID() + "', '" + targetuid + "', '" + playeruid + "');");
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
                preparedStatement.setString(1, playeruid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (rs.getString("PUID").equalsIgnoreCase(targetuid)) rid = rs.getString("RID");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("DELETE FROM requests WHERE RID = ?");
                preparedStatement.setString(1, rid);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config friends = new Config(instance.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            List<String> list = friends.getStringList("Requests." + targetuid);
            List<String> list3 = friends.getStringList("Requests." + playeruid);
            List<String> list1 = friends.getStringList("Friends." + targetuid);
            List<String> list2 = friends.getStringList("Friends." + playeruid);
            list.remove(playeruid);
            list3.remove(targetuid);
            list1.add(playeruid);
            list2.add(targetuid);
            friends.set("Friends." + targetuid, list1);
            friends.set("Friends." + playeruid, list2);
            friends.set("Requests." + targetuid, list);
            friends.set("Requests." + playeruid, list3);
            friends.save();
            friends.reload();
        }
    }

    //Convert name to uid
    public static String convertToID(String player) {
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getUserCollection().find(new Document("player", player)).first();
            if (document != null) return document.getString("uid");
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM users WHERE PLAYER = ?");
                preparedStatement.setString(1, player);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    return rs.getString("UID");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config users = new Config(instance.getDataFolder() + "/data/user-data.yml", Config.YAML);
            if (users.exists("Player." + player)) return users.getString("Player." + player);
        }
        return "null";
    }

    //Convert uid to name
    public static String convertToName(String uid) {
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getUserCollection().find(new Document("uid", uid)).first();
            if (document != null) return document.getString("player");
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM users WHERE UID = ?");
                preparedStatement.setString(1, uid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    return rs.getString("PLAYER");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config users = new Config(instance.getDataFolder() + "/data/user-data.yml", Config.YAML);
            if (users.exists("Uid." + uid)) return users.getString("Uid." + uid);
        }
        return "null";
    }

    //Check if player and target are friends
    public static boolean areFriends(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getFriendCollection().find(new Document("puid", playeruid).append("tuid", targetuid)).first();
            if (document != null) return true;
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
                preparedStatement.setString(1, playeruid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) return rs.getString("TUID").equalsIgnoreCase(targetuid);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config friends = new Config(instance.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            List<String> list = friends.getStringList("Friends." + playeruid);
            return (list.contains(targetuid));
        }
        return false;
    }

    //Check if a request exists
    public static boolean requestExists(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getRequestCollection().find(new Document("tuid", targetuid).append("puid", playeruid)).first();
            if (document != null) return true;
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
                preparedStatement.setString(1, targetuid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) return rs.getString("PUID").equalsIgnoreCase(playeruid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config friends = new Config(instance.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            List<String> list = friends.getStringList("Requests." + targetuid);
            return (list.contains(playeruid));
        }
        return false;
    }

    //Toggle requests
    public static void toggleRequests(Player player) {
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getSettingsCollection().find(new Document("player", player.getName())).first();
            assert document != null;
            if (document.getBoolean("requests")) {
                Bson bson = new Document("requests", false);
                Bson bson1 = new Document("$set", bson);
                MongoDBProvider.getSettingsCollection().updateOne(document, bson1);
                player.sendMessage(Messages.getAndReplace("Messages.RequestFalse"));
            } else {
                Bson bson = new Document("requests", true);
                Bson bson1 = new Document("$set", bson);
                MongoDBProvider.getSettingsCollection().updateOne(document, bson1);
                player.sendMessage(Messages.getAndReplace("Messages.RequestTrue"));
            }
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM settings WHERE PLAYER = ?");
                preparedStatement.setString(1, player.getName());
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (rs.getBoolean("REQUESTS")) {
                        MySqlProvider.update("UPDATE settings SET REQUESTS= '" + false + "' WHERE PLAYER= '" + player.getName() + "';");
                        player.sendMessage(Messages.getAndReplace("Messages.RequestFalse"));
                    } else {
                        MySqlProvider.update("UPDATE settings SET REQUESTS= '" + true + "' WHERE PLAYER= '" + player.getName() + "';");
                        player.sendMessage(Messages.getAndReplace("Messages.RequestTrue"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config settings = new Config(instance.getDataFolder() + "/data/settings-data.yml", Config.YAML);
            if (settings.getBoolean("Player." + player.getName() + ".Requests")) {
                settings.set("Player." + player.getName() + ".Requests", false);
                player.sendMessage(Messages.getAndReplace("Messages.RequestFalse"));
            } else {
                settings.set("Player." + player.getName() + ".Requests", true);
                player.sendMessage(Messages.getAndReplace("Messages.RequestTrue"));
            }
            settings.save();
            settings.reload();
        }
    }

    //Toggle notifications
    public static void toggleNotifications(Player player) {
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getSettingsCollection().find(new Document("player", player.getName())).first();
            assert document != null;
            if (document.getBoolean("notifications")) {
                Bson bson = new Document("notifications", false);
                Bson bson1 = new Document("$set", bson);
                MongoDBProvider.getSettingsCollection().updateOne(document, bson1);
                player.sendMessage(Messages.getAndReplace("Messages.NotificationFalse"));
            } else {
                Bson bson = new Document("notifications", true);
                Bson bson1 = new Document("$set", bson);
                MongoDBProvider.getSettingsCollection().updateOne(document, bson1);
                player.sendMessage(Messages.getAndReplace("Messages.NotificationTrue"));
            }
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM settings WHERE PLAYER = ?");
                preparedStatement.setString(1, player.getName());
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (rs.getBoolean("NOTIFICATIONS")) {
                        MySqlProvider.update("UPDATE settings SET NOTIFICATIONS= '" + false + "' WHERE PLAYER= '" + player.getName() + "';");
                        player.sendMessage(Messages.getAndReplace("Messages.NotificationFalse"));
                    } else {
                        MySqlProvider.update("UPDATE settings SET NOTIFICATIONS= '" + true + "' WHERE PLAYER= '" + player.getName() + "';");
                        player.sendMessage(Messages.getAndReplace("Messages.NotificationTrue"));

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config settings = new Config(instance.getDataFolder() + "/data/settings-data.yml", Config.YAML);
            if (settings.getBoolean("Player." + player.getName() + ".Notifications")) {
                settings.set("Player." + player.getName() + ".Notifications", false);
                player.sendMessage(Messages.getAndReplace("Messages.NotificationFalse"));
            } else {
                settings.set("Player." + player.getName() + ".Notifications", true);
                player.sendMessage(Messages.getAndReplace("Messages.NotificationTrue"));
            }
            settings.save();
            settings.reload();
        }
    }

    //Get settings of a player
    public static PlayerUtil getPlayerSettings(String player) {
        boolean r = true;
        boolean n = true;
        if (instance.isMongodb()) {
            Document document = MongoDBProvider.getSettingsCollection().find(new Document("player", player)).first();
            assert document != null;
            r = document.getBoolean("requests");
            n = document.getBoolean("notifications");
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM settings WHERE PLAYER = ?");
                preparedStatement.setString(1, player);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    r = rs.getBoolean("REQUESTS");
                    n = rs.getBoolean("NOTIFICATIONS");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config settings = new Config(instance.getDataFolder() + "/data/settings-data.yml", Config.YAML);
            r = settings.getBoolean("Player." + player + ".Requests");
            n = settings.getBoolean("Player." + player + ".Notifications");
        }
        return new PlayerUtil(r, n);
    }

    public static String getID() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 15) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }
}
