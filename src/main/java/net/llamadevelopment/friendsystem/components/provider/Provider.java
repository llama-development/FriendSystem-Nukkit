package net.llamadevelopment.friendsystem.components.provider;

import cn.nukkit.Player;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;

import java.util.List;
import java.util.function.Consumer;

public class Provider {

    public void connect(FriendSystem server) {

    }

    public void disconnect(FriendSystem server) {

    }

    public void createData(String player) {

    }

    public void userExists(String player, Consumer<Boolean> exists) {

    }

    public void createFriendRequest(String player, String target) {

    }

    public void removeFriendRequest(String player, String target) {

    }

    public void removeFriend(String player, String target) {

    }

    public void createFriendship(String player, String target) {

    }

    public void convertToID(String player, Consumer<String> consumer) {

    }

    public void convertToName(String uid, Consumer<String> consumer) {

    }

    public void areFriends(String player, String target, Consumer<Boolean> areFriends) {

    }

    public void requestExists(String player, String target, Consumer<Boolean> exists) {

    }

    public void toggleRequest(Player player) {

    }

    public void toggleNotification(Player player) {

    }

    public void getFriendData(String player, Consumer<PlayerSettings> playerSettings) {

    }

    public void getFriends(String player, Consumer<List<String>> friends) {

    }

    public void getRequests(String player, Consumer<List<String>> requests) {

    }

    public String getProvider() {
        return null;
    }

}
