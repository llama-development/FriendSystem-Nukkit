package net.llamadevelopment.friendsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.managers.FriendManager;
import net.llamadevelopment.friendsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.friendsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.friendsystem.components.messaging.Messages;
import net.llamadevelopment.friendsystem.components.utils.PlayerUtil;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FriendCommand extends CommandManager {

    private FriendSystem plugin;

    public FriendCommand(FriendSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Friend"), "Friend management commands.", "/friend");
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 2) {
                String target = args[1];
                if (args[0].equalsIgnoreCase("add")) {
                    if (player.getName().equalsIgnoreCase(target)) {
                        player.sendMessage(Messages.getAndReplace("Messages.FriendYourself"));
                        return true;
                    } else if (FriendManager.userExists(target)) {
                        PlayerUtil settings = FriendManager.getPlayerSettings(target);
                        if (settings.isRequests()) {
                            if (!FriendManager.areFriends(player.getName(), target)) {
                                if (!FriendManager.requestExists(player.getName(), target)) {
                                    FriendManager.createFriendRequest(player.getName(), target);
                                    player.sendMessage(Messages.getAndReplace("Messages.SendRequest", target));
                                    Player player1 = plugin.getServer().getPlayer(target);
                                    if (player1 != null) player1.sendMessage(Messages.getAndReplace("Messages.RequestReceived", player.getName()));
                                } else player.sendMessage(Messages.getAndReplace("Messages.AlreadySendRequest", target));
                            } else player.sendMessage(Messages.getAndReplace("Messages.AlreadyFriends", target));
                        } else player.sendMessage(Messages.getAndReplace("Messages.CannotRequest"));
                    } else player.sendMessage(Messages.getAndReplace("Messages.UserDoesNotExist"));
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (FriendManager.areFriends(player.getName(), target)) {
                        FriendManager.removeFriend(player.getName(), target);
                        player.sendMessage(Messages.getAndReplace("Messages.FriendRemoved", target));
                        Player player1 = plugin.getServer().getPlayer(target);
                        if (player1 != null) player1.sendMessage(Messages.getAndReplace("Messages.FriendRemoved", player.getName()));
                    } else player.sendMessage(Messages.getAndReplace("Messages.NotOnFriendList", target));
                } else if (args[0].equalsIgnoreCase("accept")) {
                    if (FriendManager.requestExists(target, player.getName())) {
                        if (!FriendManager.areFriends(player.getName(), target)) {
                            FriendManager.createFriendship(player.getName(), target);
                            if (plugin.isMongodb()) FriendManager.removeFriendRequest(target, player.getName());
                            else FriendManager.removeFriendRequest(player.getName(), target);
                            player.sendMessage(Messages.getAndReplace("Messages.FriendAccepted", target));
                            Player player1 = plugin.getServer().getPlayer(target);
                            if (player1 != null) player1.sendMessage(Messages.getAndReplace("Messages.RequestAccepted", player.getName()));
                        } else player.sendMessage(Messages.getAndReplace("Messages.AlreadyFriends", target));
                    } else player.sendMessage(Messages.getAndReplace("Messages.RequestNotExists"));
                } else if (args[0].equalsIgnoreCase("deny")) {
                    if (FriendManager.requestExists(target, player.getName())) {
                        FriendManager.removeFriendRequest(target, player.getName());
                        player.sendMessage(Messages.getAndReplace("Messages.FriendDenied", target));
                        Player player1 = plugin.getServer().getPlayer(target);
                        if (player1 != null) player1.sendMessage(Messages.getAndReplace("Messages.RequestDenied", player.getName()));
                    } else player.sendMessage(Messages.getAndReplace("Messages.RequestNotExists"));
                } else sendHelp(player);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    sendHelp(player);
                } else if (args[0].equalsIgnoreCase("list")) {
                    sendFriendList(player);
                } else if (args[0].equalsIgnoreCase("requests")) {
                    sendRequestsList(player);
                } else if (args[0].equalsIgnoreCase("togglenotify")) {
                    FriendManager.toggleNotifications(player);
                } else if (args[0].equalsIgnoreCase("togglerequests")) {
                    FriendManager.toggleRequests(player);
                } else sendHelp(player);
            } else if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("msg")) {
                    Player friend = plugin.getServer().getPlayer(args[1]);
                    String message = "";
                    for (int i = 2; i < args.length; ++i) message = message + args[i] + " ";
                    if (friend != null) {
                        if (FriendManager.areFriends(player.getName(), friend.getName())) {
                            friend.sendMessage(Messages.getAndReplace("Messages.MsgReceived", player.getName(), message));
                            player.sendMessage(Messages.getAndReplace("Messages.MsgSent", friend.getName(), message));
                        } else player.sendMessage(Messages.getAndReplace("Messages.NotOnFriendList", friend.getName()));
                    } else player.sendMessage(Messages.getAndReplace("Messages.NotOnline"));
                } else sendHelp(player);
            } else sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Messages.getAndReplace("HelpFormat.Header"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Help"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Add"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Remove"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Accept"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Deny"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.List"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Requests"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Msg"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Togglenotify"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Togglerequests"));
        player.sendMessage(Messages.getAndReplace("HelpFormat.Footer"));
    }

    private void sendFriendList(Player player) {
        String uid = FriendManager.convertToID(player.getName());
        int a = 0;
        player.sendMessage(Messages.getAndReplace("Messages.FriendslistInfo"));
        if (plugin.isMongodb()) {
            for (Document doc : MongoDBProvider.getFriendCollection().find(new Document("puid", uid))) {
                if (a > 30) return;
                player.sendMessage(Messages.getAndReplace("Messages.FriendlistFormat", FriendManager.convertToName(doc.getString("tuid"))));
                a++;
            }
        } else if (plugin.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM friends WHERE PUID = ?");
                preparedStatement.setString(1, uid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (a > 30) return;
                    player.sendMessage(Messages.getAndReplace("Messages.FriendlistFormat", FriendManager.convertToName(rs.getString("TUID"))));
                    a++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (plugin.isYaml()) {
            Config friends = new Config(plugin.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            for (String s : friends.getStringList("Friends." + uid)) {
                if (a > 30) return;
                player.sendMessage(Messages.getAndReplace("Messages.FriendlistFormat", FriendManager.convertToName(s)));
                a++;
            }
        }
        if (a == 0) player.sendMessage(Messages.getAndReplace("Messages.NoFriends"));
    }

    private void sendRequestsList(Player player) {
        String uid = FriendManager.convertToID(player.getName());
        int a = 0;
        player.sendMessage(Messages.getAndReplace("Messages.RequestlistInfo"));
        if (plugin.isMongodb()) {
            for (Document doc : MongoDBProvider.getRequestCollection().find(new Document("tuid", uid))) {
                if (a > 30) return;
                player.sendMessage(Messages.getAndReplace("Messages.RequestlistFormat", FriendManager.convertToName(doc.getString("puid"))));
                a++;
            }
        } else if (plugin.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM requests WHERE TUID = ?");
                preparedStatement.setString(1, uid);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    if (a > 30) return;
                    player.sendMessage(Messages.getAndReplace("Messages.RequestlistFormat", FriendManager.convertToName(rs.getString("PUID"))));
                    a++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (plugin.isYaml()) {
            Config friends = new Config(plugin.getDataFolder() + "/data/friend-data.yml", Config.YAML);
            for (String s : friends.getStringList("Requests." + uid)) {
                if (a > 30) return;
                player.sendMessage(Messages.getAndReplace("Messages.RequestlistFormat", FriendManager.convertToName(s)));
                a++;
            }
        }
        if (a == 0) player.sendMessage(Messages.getAndReplace("Messages.NoRequests"));
    }
}
