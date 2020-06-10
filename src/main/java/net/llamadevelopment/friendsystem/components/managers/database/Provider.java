package net.llamadevelopment.friendsystem.components.managers.database;

import cn.nukkit.Player;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;

import java.util.HashMap;
import java.util.List;

public class Provider {

    public static HashMap<String, PlayerSettings> playerSettings = new HashMap<>();

    public void connect(FriendSystem server) {

    }

    public void disconnect(FriendSystem server) {

    }

    public void createData(String player) {

    }

    public boolean userExists(String player) {
        return false;
    }

    public void createFriendRequest(String player, String target) {

    }

    public void removeFriendRequest(String player, String target) {

    }

    public void removeFriend(String player, String target) {

    }

    public void createFriendship(String player, String target) {

    }

    public String convertToID(String player) {
        return null;
    }

    public String convertToName(String uid) {
        return null;
    }

    public boolean areFriends(String player, String target) {
        return false;
    }

    public boolean requestExists(String player, String target) {
        return false;
    }

    public void toggleRequest(Player player) {

    }

    public void toggleNotification(Player player) {

    }

    public PlayerSettings getFriendData(String player) {
        return null;
    }

    public List<String> getFriends(String player) {
        return null;
    }

    public List<String> getRequests(String player) {
        return null;
    }

    public String getProvider() {
        return null;
    }

}
