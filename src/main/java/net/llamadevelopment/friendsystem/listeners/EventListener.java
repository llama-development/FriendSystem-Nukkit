package net.llamadevelopment.friendsystem.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Sound;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.managers.database.Provider;

public class EventListener implements Listener {

    private final FriendSystem instance = FriendSystem.getInstance();
    private final Provider provider = FriendSystemAPI.getProvider();

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        if (!provider.userExists(event.getPlayer().getName())) provider.createData(event.getPlayer().getName());
        final PlayerSettings settings1 = provider.getFriendData(event.getPlayer().getName());
        int e = 0;
        for (Player player : instance.getServer().getOnlinePlayers().values()) {
            PlayerSettings settings2 = provider.getFriendData(player.getName());
            if (provider.areFriends(event.getPlayer().getName(), player.getName())) {
                e++;
                if (settings2.isNotification()) {
                    player.sendMessage(Language.get("friend-joined", event.getPlayer().getName()));
                    FriendSystemAPI.playSound(player, Sound.NOTE_PLING);
                }
            }
        }
        final int finalE = e;
        instance.getServer().getScheduler().scheduleDelayedTask(instance, () -> {
            if (settings1.isNotification()) {
                event.getPlayer().sendMessage(Language.get("join-info", String.valueOf(finalE)));
                FriendSystemAPI.playSound(event.getPlayer(), Sound.NOTE_PLING);
            }
        }, 40);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        for (Player player : instance.getServer().getOnlinePlayers().values()) {
            PlayerSettings settings = provider.getFriendData(player.getName());
            if (provider.areFriends(event.getPlayer().getName(), player.getName())) {
                if (settings.isNotification()) {
                    player.sendMessage(Language.get("friend-left", event.getPlayer().getName()));
                    FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                }
            }
        }
    }
}
