package net.llamadevelopment.friendsystem.components.managers;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.managers.database.Provider;

import java.util.ArrayList;
import java.util.List;

public class YamlProvider extends Provider {

    Config friends, users, settings;

    @Override
    public void connect(FriendSystem server) {
        server.saveResource("data/friend-data.yml");
        server.saveResource("data/user-data.yml");
        server.saveResource("data/settings-data.yml");
        this.friends = new Config(server.getDataFolder() + "/data/friend-data.yml");
        this.users = new Config(server.getDataFolder() + "/data/user-data.yml");
        this.settings = new Config(server.getDataFolder() + "/data/settings-data.yml");
    }

    @Override
    public void createData(String player) {
        String uid = FriendSystemAPI.getRandomID();
        users.set("Player." + player, uid);
        users.set("Uid." + uid, player);
        users.save();
        users.reload();
        settings.set("Player." + player + ".Notifications", true);
        settings.set("Player." + player + ".Requests", true);
        settings.save();
        settings.reload();
    }

    @Override
    public boolean userExists(String player) {
        return users.exists("Player." + player);
    }

    @Override
    public void createFriendRequest(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        List<String> list = friends.getStringList("Requests." + targetuid);
        list.add(playeruid);
        friends.set("Requests." + targetuid, list);
        friends.save();
        friends.reload();
    }

    @Override
    public void removeFriendRequest(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        List<String> list = friends.getStringList("Requests." + targetuid);
        list.remove(playeruid);
        friends.set("Requests." + targetuid, list);
        friends.save();
        friends.reload();
    }

    @Override
    public void removeFriend(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        List<String> list1 = friends.getStringList("Friends." + targetuid);
        List<String> list2 = friends.getStringList("Friends." + playeruid);
        list1.remove(playeruid);
        list2.remove(targetuid);
        friends.set("Friends." + targetuid, list1);
        friends.set("Friends." + playeruid, list2);
        friends.save();
        friends.reload();
    }

    @Override
    public void createFriendship(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
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

    @Override
    public String convertToID(String player) {
        if (users.exists("Player." + player)) return users.getString("Player." + player);
        return null;
    }

    @Override
    public String convertToName(String uid) {
        if (users.exists("Uid." + uid)) return users.getString("Uid." + uid);
        return null;
    }

    @Override
    public boolean areFriends(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        List<String> list = friends.getStringList("Friends." + playeruid);
        return (list.contains(targetuid));
    }

    @Override
    public boolean requestExists(String player, String target) {
        String playeruid = convertToID(player);
        String targetuid = convertToID(target);
        List<String> list = friends.getStringList("Requests." + targetuid);
        return list.contains(playeruid);
    }

    @Override
    public void toggleRequest(Player player) {
        if (settings.getBoolean("Player." + player.getName() + ".Requests")) {
            settings.set("Player." + player.getName() + ".Requests", false);
            player.sendMessage(Language.get("requests-denied"));
        } else {
            settings.set("Player." + player.getName() + ".Requests", true);
            player.sendMessage(Language.get("requests-allowed"));
        }
        settings.save();
        settings.reload();
    }

    @Override
    public void toggleNotification(Player player) {
        if (settings.getBoolean("Player." + player.getName() + ".Notifications")) {
            settings.set("Player." + player.getName() + ".Notifications", false);
            player.sendMessage(Language.get("notifications-denied"));
        } else {
            settings.set("Player." + player.getName() + ".Notifications", true);
            player.sendMessage(Language.get("notifications-allowed"));
        }
        settings.save();
        settings.reload();
    }

    @Override
    public PlayerSettings getFriendData(String player) {
        boolean r;
        boolean n;
        r = settings.getBoolean("Player." + player + ".Requests");
        n = settings.getBoolean("Player." + player + ".Notifications");
        return new PlayerSettings(r, n);
    }

    @Override
    public List<String> getFriends(String player) {
        List<String> list = new ArrayList<>();
        String playeruid = convertToID(player);
        friends.getStringList("Friends." + playeruid).forEach(entry -> list.add(convertToName(entry)));
        return list;
    }

    @Override
    public List<String> getRequests(String player) {
        List<String> list = new ArrayList<>();
        friends.getSection("Requests").getAll().getKeys(false).forEach(entry -> {
            if (convertToName(entry).equals(player)) friends.getStringList("Requests." + entry).forEach(request -> list.add(convertToName(request)));
        });
        return list;
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }
}
