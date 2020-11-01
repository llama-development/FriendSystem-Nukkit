package net.llamadevelopment.friendsystem.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Sound;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.provider.Provider;

import java.util.concurrent.atomic.AtomicInteger;

public class EventListener implements Listener {

    private final FriendSystem instance = FriendSystem.getInstance();
    private final Provider provider = FriendSystemAPI.getProvider();

    @EventHandler
    public void on(PlayerJoinEvent event) {
        this.provider.userExists(event.getPlayer().getName(), exists -> {
            if (!exists) {
                this.provider.createData(event.getPlayer().getName());
                return;
            }
            this.provider.getFriendData(event.getPlayer().getName(), playerSettings -> {
                AtomicInteger i = new AtomicInteger();
                for (Player player : this.instance.getServer().getOnlinePlayers().values()) {
                    this.provider.getFriendData(player.getName(), targetSettings -> this.provider.areFriends(event.getPlayer().getName(), player.getName(), areFriends -> {
                        i.getAndIncrement();
                        if (areFriends) {
                            if (targetSettings.isNotification()) {
                                player.sendMessage(Language.get("friend-joined", event.getPlayer().getName()));
                                FriendSystemAPI.playSound(player, Sound.NOTE_PLING);
                            }
                        }
                    }));
                }
                AtomicInteger finalI = new AtomicInteger();
                this.instance.getServer().getScheduler().scheduleDelayedTask(this.instance, () -> {
                    if (playerSettings.isNotification()) {
                        event.getPlayer().sendMessage(Language.get("join-info", finalI));
                        FriendSystemAPI.playSound(event.getPlayer(), Sound.NOTE_PLING);
                    }
                }, 70);
            });
        });
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        for (Player player : this.instance.getServer().getOnlinePlayers().values()) {
            this.provider.getFriendData(player.getName(), settings -> this.provider.areFriends(event.getPlayer().getName(), player.getName(), areFriends -> {
                if (areFriends) {
                    if (settings.isNotification()) {
                        player.sendMessage(Language.get("friend-left", event.getPlayer().getName()));
                        FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                    }
                }
            }));
        }
    }

}
