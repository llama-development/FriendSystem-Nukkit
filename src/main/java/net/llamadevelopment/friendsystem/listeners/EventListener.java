package net.llamadevelopment.friendsystem.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.managers.FriendManager;
import net.llamadevelopment.friendsystem.components.messaging.Messages;
import net.llamadevelopment.friendsystem.components.utils.PlayerUtil;

public class EventListener implements Listener {

    private FriendSystem instance = FriendSystem.getInstance();

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        if (!FriendManager.userExists(event.getPlayer().getName())) FriendManager.createData(event.getPlayer().getName());
        final PlayerUtil settings1 = FriendManager.getPlayerSettings(event.getPlayer().getName());
        int e = 0;
        for (Player player : instance.getServer().getOnlinePlayers().values()) {
            PlayerUtil settings2 = FriendManager.getPlayerSettings(player.getName());
            if (FriendManager.areFriends(event.getPlayer().getName(), player.getName())) {
                e++;
                if (settings2.isNotifications()) {
                    player.sendMessage(Messages.getAndReplace("Messages.FriendJoined", event.getPlayer().getName()));
                }
            }
        }
        final int finalE = e;
        instance.getServer().getScheduler().scheduleDelayedTask(instance, new Runnable() {
            public void run() {
                if (settings1.isNotifications()) {
                    event.getPlayer().sendMessage(Messages.getAndReplace("Messages.JoinNotification", String.valueOf(finalE)));
                }
            }
        }, 40);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        for (Player player : instance.getServer().getOnlinePlayers().values()) {
            PlayerUtil settings = FriendManager.getPlayerSettings(player.getName());
            if (FriendManager.areFriends(event.getPlayer().getName(), player.getName())) {
                if (settings.isNotifications()) {
                    player.sendMessage(Messages.getAndReplace("Messages.FriendLeft", event.getPlayer().getName()));
                }
            }
        }
    }
}
