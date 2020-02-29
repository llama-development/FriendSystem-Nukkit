package net.llamadevelopment.friendsystem.components.managers.database;

import net.llamadevelopment.friendsystem.FriendSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MySqlProvider {

    private static Connection connection;

    private void connect(FriendSystem instance) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + instance.getConfig().getString("MySql.Host") + ":" + instance.getConfig().getString("MySql.Port") + "/" + instance.getConfig().getString("MySql.Database") + "?autoReconnect=true", instance.getConfig().getString("MySql.User"), instance.getConfig().getString("MySql.Password"));
            instance.getLogger().info("§aConnected successfully to database!");
        } catch (Exception e) {
            instance.getLogger().error("§4Failed to connect to database.");
            instance.getLogger().error("§4Please check your details in the config.yml.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public void createTables() {
        this.connect(FriendSystem.getInstance());
        update("CREATE TABLE IF NOT EXISTS users(player VARCHAR(255), uid VARCHAR(255), PRIMARY KEY (uid));");
        update("CREATE TABLE IF NOT EXISTS friends(fid VARCHAR(255), puid VARCHAR(255), tuid VARCHAR(255));");
        update("CREATE TABLE IF NOT EXISTS settings(player VARCHAR(255), notifications VARCHAR(255), requests VARCHAR(255), PRIMARY KEY (player));");
        update("CREATE TABLE IF NOT EXISTS requests(rid VARCHAR(255), tuid VARCHAR(255), puid VARCHAR(255));");
    }

    public static void update(String qry) {
        if (connection != null) {
            try {
                PreparedStatement ps = connection.prepareStatement(qry);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
