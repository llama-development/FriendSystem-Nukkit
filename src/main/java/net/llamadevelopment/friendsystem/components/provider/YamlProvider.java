package net.llamadevelopment.friendsystem.components.provider;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.language.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YamlProvider extends Provider {

    private Config friends, users, settings;

    @Override
    public void connect(FriendSystem server) {
        server.saveResource("data/friend-data.yml");
        server.saveResource("data/user-data.yml");
        server.saveResource("data/settings-data.yml");
        this.friends = new Config(server.getDataFolder() + "/data/friend-data.yml", Config.YAML);
        this.users = new Config(server.getDataFolder() + "/data/user-data.yml", Config.YAML);
        this.settings = new Config(server.getDataFolder() + "/data/settings-data.yml", Config.YAML);
    }

    @Override
    public void createData(String player) {
        String uid = FriendSystemAPI.getRandomID();
        System.out.println(uid);
        this.users.set("Player." + player, uid);
        this.users.set("Uid." + uid, player);
        this.users.save();
        this.users.reload();
        this.settings.set("Player." + player + ".Notifications", true);
        this.settings.set("Player." + player + ".Requests", true);
        this.settings.save();
        this.settings.reload();
    }


    @Override
    public void userExists(String player, Consumer<Boolean> exists) {
        exists.accept(this.users.exists("Player." + player));
    }

    @Override
    public void createFriendRequest(String player, String target) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            List<String> list = this.friends.getStringList("Requests." + targetuid);
            list.add(playeruid);
            this.friends.set("Requests." + targetuid, list);
            this.friends.save();
            this.friends.reload();
        }));
    }

    @Override
    public void removeFriendRequest(String player, String target) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            List<String> list = this.friends.getStringList("Requests." + targetuid);
            list.remove(playeruid);
            this.friends.set("Requests." + targetuid, list);
            this.friends.save();
            this.friends.reload();
        }));
    }

    @Override
    public void removeFriend(String player, String target) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            List<String> list1 = this.friends.getStringList("Friends." + targetuid);
            List<String> list2 = this.friends.getStringList("Friends." + playeruid);
            list1.remove(playeruid);
            list2.remove(targetuid);
            this.friends.set("Friends." + targetuid, list1);
            this.friends.set("Friends." + playeruid, list2);
            this.friends.save();
            this.friends.reload();
        }));
    }

    @Override
    public void createFriendship(String player, String target) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            List<String> list = this.friends.getStringList("Requests." + targetuid);
            List<String> list3 = this.friends.getStringList("Requests." + playeruid);
            List<String> list1 = this.friends.getStringList("Friends." + targetuid);
            List<String> list2 = this.friends.getStringList("Friends." + playeruid);
            list.remove(playeruid);
            list3.remove(targetuid);
            list1.add(playeruid);
            list2.add(targetuid);
            this.friends.set("Friends." + targetuid, list1);
            this.friends.set("Friends." + playeruid, list2);
            this.friends.set("Requests." + targetuid, list);
            this.friends.set("Requests." + playeruid, list3);
            this.friends.save();
            this.friends.reload();
        }));
    }

    @Override
    public void convertToID(String player, Consumer<String> consumer) {
        if (this.users.exists("Player." + player)) consumer.accept(this.users.getString("Player." + player));
    }

    @Override
    public void convertToName(String uid, Consumer<String> consumer) {
        if (this.users.exists("Uid." + uid)) consumer.accept(this.users.getString("Uid." + uid));
    }

    @Override
    public void areFriends(String player, String target, Consumer<Boolean> areFriends) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            List<String> list = this.friends.getStringList("Friends." + playeruid);
            areFriends.accept(list.contains(targetuid));
        }));
    }

    @Override
    public void requestExists(String player, String target, Consumer<Boolean> exists) {
        this.convertToID(player, playeruid -> this.convertToID(target, targetuid -> {
            List<String> list = this.friends.getStringList("Requests." + targetuid);
            exists.accept(list.contains(playeruid));
        }));
    }

    @Override
    public void toggleRequest(Player player) {
        if (this.settings.getBoolean("Player." + player.getName() + ".Requests")) {
            this.settings.set("Player." + player.getName() + ".Requests", false);
            player.sendMessage(Language.get("requests-denied"));
        } else {
            this.settings.set("Player." + player.getName() + ".Requests", true);
            player.sendMessage(Language.get("requests-allowed"));
        }
        this.settings.save();
        this.settings.reload();
    }

    @Override
    public void toggleNotification(Player player) {
        if (this.settings.getBoolean("Player." + player.getName() + ".Notifications")) {
            this.settings.set("Player." + player.getName() + ".Notifications", false);
            player.sendMessage(Language.get("notifications-denied"));
        } else {
            this.settings.set("Player." + player.getName() + ".Notifications", true);
            player.sendMessage(Language.get("notifications-allowed"));
        }
        this.settings.save();
        this.settings.reload();
    }

    @Override
    public void getFriendData(String player, Consumer<PlayerSettings> playerSettings) {
        boolean r;
        boolean n;
        r = this.settings.getBoolean("Player." + player + ".Requests");
        n = this.settings.getBoolean("Player." + player + ".Notifications");
        playerSettings.accept(new PlayerSettings(r, n));
    }

    @Override
    public void getFriends(String player, Consumer<List<String>> consumer) {
        this.convertToID(player, playeruid -> {
            List<String> list = new ArrayList<>();
            this.friends.getStringList("Friends." + playeruid).forEach(entry -> this.convertToName(entry, list::add));
            consumer.accept(list);
        });
    }

    @Override
    public void getRequests(String player, Consumer<List<String>> requests) {
        List<String> list = new ArrayList<>();
        this.friends.getSection("Requests").getAll().getKeys(false).forEach(entry -> this.convertToName(entry, target -> {
            if (target.equals(player)) this.friends.getStringList("Requests." + entry).forEach(request -> this.convertToName(request, list::add));
        }));
        requests.accept(list);
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }
}
