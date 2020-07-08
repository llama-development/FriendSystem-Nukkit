package net.llamadevelopment.friendsystem.components.managers;

import cn.nukkit.Player;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.managers.database.Provider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlProvider extends Provider {

    private Connection connection;

    @Override
    public void connect(FriendSystem server) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + server.getConfig().getString("MySql.Host") + ":" + server.getConfig().getString("MySql.Port") + "/" + server.getConfig().getString("MySql.Database") + "?autoReconnect=true", server.getConfig().getString("MySql.User"), server.getConfig().getString("MySql.Password"));
            update("CREATE TABLE IF NOT EXISTS users(player VARCHAR(255), uid VARCHAR(255), PRIMARY KEY (uid));");
            update("CREATE TABLE IF NOT EXISTS friends(fid VARCHAR(255), puid VARCHAR(255), tuid VARCHAR(255));");
            update("CREATE TABLE IF NOT EXISTS settings(player VARCHAR(255), notifications VARCHAR(255), requests VARCHAR(255), PRIMARY KEY (player));");
            update("CREATE TABLE IF NOT EXISTS requests(rid VARCHAR(255), tuid VARCHAR(255), puid VARCHAR(255));");
            server.getLogger().info("[MySqlClient] Connection opened.");
        } catch (Exception e) {
            server.getLogger().info("[MySqlClient] Failed to create connection.");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void update(String qry) {
        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(qry);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect(FriendSystem server) {
        if (connection != null) {
            try {
                connection.close();
                server.getLogger().info("[MySqlClient] Connection closed.");
            } catch (SQLException throwables) {
                server.getLogger().info("[MySqlClient] Failed to close connection.");
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void createData(String player) {
        update("INSERT INTO users (PLAYER, UID) VALUES ('" + player + "', '" + FriendSystemAPI.getRandomID() + "');");
        update("INSERT INTO settings (PLAYER, NOTIFICATIONS, REQUESTS) VALUES ('" + player + "', '" + true + "', '" + true + "');");
    }

    @Override
    public boolean userExists(String player) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM users WHERE PLAYER = ?");
            preparedStatement.setString(1, player);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) return true;
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void createFriendRequest(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        update("INSERT INTO requests (RID, TUID, PUID) VALUES ('" + FriendSystemAPI.getRandomID() + "','" + targetuid + "', '" + playeruid + "');");
    }

    @Override
    public void removeFriendRequest(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        String rid = "null";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
            preparedStatement.setString(1, targetuid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) if (rs.getString("PUID").equalsIgnoreCase(playeruid)) rid = rs.getString("RID");
            PreparedStatement preparedStatement1 = getConnection().prepareStatement("DELETE FROM requests WHERE RID = ?");
            preparedStatement1.setString(1, rid);
            preparedStatement1.executeUpdate();
            preparedStatement1.close();
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFriend(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        String fid1 = "null";
        String fid2 = "null";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
            preparedStatement.setString(1, playeruid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) if (rs.getString("TUID").equalsIgnoreCase(targetuid)) fid1 = rs.getString("FID");
            PreparedStatement preparedStatement1 = getConnection().prepareStatement("DELETE FROM friends WHERE FID = ?");
            preparedStatement1.setString(1, fid1);
            preparedStatement1.executeUpdate();
            PreparedStatement preparedStatement2 = getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
            preparedStatement2.setString(1, targetuid);
            ResultSet rs1 = preparedStatement2.executeQuery();
            if (rs1.next()) if (rs1.getString("TUID").equalsIgnoreCase(playeruid)) fid2 = rs1.getString("FID");
            PreparedStatement preparedStatement3 = getConnection().prepareStatement("DELETE FROM friends WHERE FID = ?");
            preparedStatement3.setString(1, fid2);
            preparedStatement3.executeUpdate();
            preparedStatement.close();
            preparedStatement1.close();
            preparedStatement2.close();
            preparedStatement3.close();
            rs.close();
            rs1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createFriendship(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        String rid = "null";
        update("INSERT INTO friends (FID, PUID, TUID) VALUES ('" + FriendSystemAPI.getRandomID() + "', '" + playeruid + "', '" + targetuid + "');");
        update("INSERT INTO friends (FID, PUID, TUID) VALUES ('" + FriendSystemAPI.getRandomID() + "', '" + targetuid + "', '" + playeruid + "');");
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
            preparedStatement.setString(1, playeruid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) if (rs.getString("PUID").equalsIgnoreCase(targetuid)) rid = rs.getString("RID");
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM requests WHERE RID = ?");
            preparedStatement.setString(1, rid);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String convertToID(String player) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM users WHERE PLAYER = ?");
            preparedStatement.setString(1, player);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) return rs.getString("UID");
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String convertToName(String uid) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM users WHERE UID = ?");
            preparedStatement.setString(1, uid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) return rs.getString("PLAYER");
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean areFriends(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
            preparedStatement.setString(1, playeruid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) return rs.getString("TUID").equalsIgnoreCase(targetuid);
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean requestExists(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
            preparedStatement.setString(1, targetuid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) return rs.getString("PUID").equalsIgnoreCase(playeruid);
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void toggleRequest(Player player) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM settings WHERE PLAYER = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean("REQUESTS")) {
                    update("UPDATE settings SET REQUESTS= '" + false + "' WHERE PLAYER= '" + player.getName() + "';");
                    player.sendMessage(Language.get("requests-denied"));
                } else {
                    update("UPDATE settings SET REQUESTS= '" + true + "' WHERE PLAYER= '" + player.getName() + "';");
                    player.sendMessage(Language.get("requests-allowed"));
                }
            }
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toggleNotification(Player player) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM settings WHERE PLAYER = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean("NOTIFICATIONS")) {
                    update("UPDATE settings SET NOTIFICATIONS= '" + false + "' WHERE PLAYER= '" + player.getName() + "';");
                    player.sendMessage(Language.get("notifications-denied"));
                } else {
                    update("UPDATE settings SET NOTIFICATIONS= '" + true + "' WHERE PLAYER= '" + player.getName() + "';");
                    player.sendMessage(Language.get("notifications-allowed"));
                }
            }
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerSettings getFriendData(String player) {
        boolean r = true;
        boolean n = true;
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM settings WHERE PLAYER = ?");
            preparedStatement.setString(1, player);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                r = rs.getBoolean("REQUESTS");
                n = rs.getBoolean("NOTIFICATIONS");
            }
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PlayerSettings(r, n);
    }

    @Override
    public List<String> getFriends(String player) {
        List<String> list = new ArrayList<>();
        String playeruid = convertToID(player);
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
            preparedStatement.setString(1, playeruid);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) list.add(convertToName(rs.getString("TUID")));
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> getRequests(String player) {
        List<String> list = new ArrayList<>();
        String playeruid = convertToID(player);
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM requests");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) if (rs.getString("TUID").equals(playeruid)) list.add(convertToName(rs.getString("PUID")));
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String getProvider() {
        return "MySql";
    }
}
