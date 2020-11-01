package net.llamadevelopment.friendsystem.components.forms;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.*;
import cn.nukkit.level.Sound;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.forms.custom.CustomForm;
import net.llamadevelopment.friendsystem.components.forms.modal.ModalForm;
import net.llamadevelopment.friendsystem.components.forms.simple.SimpleForm;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.provider.Provider;

import java.util.List;

public class FormWindows {

    private static final Provider api = FriendSystemAPI.getProvider();

    public static void openFriendMenu(Player player) {
        api.getFriends(player.getName(), friends -> api.getRequests(player.getName(), requests -> {
            SimpleForm form = new SimpleForm.Builder(Language.getNP("friend-menu-title"), Language.getNP("friend-menu-content"))
                    .addButton(new ElementButton(Language.getNP("friend-menu-addfriend"),
                            new ElementButtonImageData("url", "http://system01.lldv.net:3000/img/friendsystem-addfriend-icon.png")), FormWindows::openAddFriend)
                    .addButton(new ElementButton(Language.getNP("friend-menu-requests", requests.size()),
                            new ElementButtonImageData("url", "http://system01.lldv.net:3000/img/friendsystem-requests-icon.png")), e -> openFriendRequests(player, requests))
                    .addButton(new ElementButton(Language.getNP("friend-menu-friendlist", friends.size()),
                            new ElementButtonImageData("url", "http://system01.lldv.net:3000/img/friendsystem-friendlist-icon.png")), e -> openFriendList(player, friends))
                    .addButton(new ElementButton(Language.getNP("friend-menu-settings"),
                            new ElementButtonImageData("url", "http://system01.lldv.net:3000/img/friendsystem-settings-icon.png")), FormWindows::openPlayerSettings)
                    .addButton(new ElementButton(Language.getNP("friend-menu-message"),
                            new ElementButtonImageData("url", "http://system01.lldv.net:3000/img/friendsystem-message-icon.png")), e -> openMessageMenu(player, friends))
                    .build();
            form.send(player);
        }));
    }

    public static void openMessageMenu(Player player, List<String> friends) {
        CustomForm form = new CustomForm.Builder(Language.getNP("friend-message-title"))
                .addElement(new ElementDropdown(Language.getNP("friend-message-select"), friends, 0))
                .addElement(new ElementInput(Language.getNP("friend-message-message"), "Text"))
                .onSubmit((e, r) -> {
                    String f = r.getDropdownResponse(0).getElementContent();
                    Player friend = Server.getInstance().getPlayer(f);
                    if (friend != null) {
                        String m = r.getInputResponse(1);
                        friend.sendMessage(Language.get("msg-received", player.getName(), m));
                        player.sendMessage(Language.get("msg-sent", friend.getName(), m));
                    } else player.sendMessage(Language.get("friend-not-online", f));
                })
                .build();
        form.send(player);
    }

    public static void openAddFriend(Player player) {
        CustomForm form = new CustomForm.Builder(Language.getNP("friend-add-title"))
                .addElement(new ElementInput(Language.getNP("friend-add-content"), Language.getNP("friend-add-input")))
                .onSubmit(((executor, response) -> {
                    String input = response.getInputResponse(0);
                    if (input.isEmpty() || input.equals(player.getName())) {
                        player.sendMessage(Language.get("invalid-input"));
                        FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                        return;
                    }
                    api.userExists(input, exists -> {
                        if (exists) {
                            api.getFriendData(input, settings -> {
                                if (settings.isRequest()) {
                                    api.areFriends(player.getName(), input, areFriends -> {
                                        if (!areFriends) {
                                            api.requestExists(player.getName(), input, requestExists -> {
                                                if (!requestExists) {
                                                    api.createFriendRequest(player.getName(), input);
                                                    player.sendMessage(Language.get("request-sent", input));
                                                    FriendSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                                                    Player player1 = Server.getInstance().getPlayer(input);
                                                    if (player1 != null) {
                                                        player1.sendMessage(Language.get("request-received", player.getName()));
                                                        FriendSystemAPI.playSound(player1, Sound.NOTE_PLING);
                                                    }
                                                } else {
                                                    player.sendMessage(Language.get("request-already-sent", input));
                                                    FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                                                }
                                            });
                                        } else {
                                            player.sendMessage(Language.get("already-friends", input));
                                            FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                                        }
                                    });
                                } else {
                                    player.sendMessage(Language.get("request-not-allowed", input));
                                    FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                                }
                            });
                        } else {
                            player.sendMessage(Language.get("user-not-found", input));
                            FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                        }
                    });
                }))
                .build();
        form.send(player);
    }

    public static void openFriendRequests(Player player, List<String> requests) {
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("friend-request-menu-title"), Language.getNP("friend-request-menu-content"));
        requests.forEach(request -> form.addButton(new ElementButton(Language.getNP("friend-request-menu-button", request)), executor -> {
            ModalForm modalForm = new ModalForm.Builder(Language.getNP("friend-request-accept-title"), Language.getNP("friend-request-accept-content", request),
                    Language.getNP("friend-request-accept-accept"), Language.getNP("friend-request-accept-deny"))
                    .onYes(e -> {
                        api.createFriendship(player.getName(), request);
                        api.removeFriendRequest(player.getName(), request);
                        FriendSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                        player.sendMessage(Language.get("request-accepted", request));
                        Player player1 = Server.getInstance().getPlayer(request);
                        if (player1 != null) {
                            player1.sendMessage(Language.get("request-accepted-other", player.getName()));
                            FriendSystemAPI.playSound(player1, Sound.RANDOM_LEVELUP);
                        }
                    })
                    .onNo(e -> {
                        api.removeFriendRequest(request, player.getName());
                        FriendSystemAPI.playSound(player, Sound.MOB_GUARDIAN_DEATH);
                        player.sendMessage(Language.get("request-denied", request));
                        Player player1 = Server.getInstance().getPlayer(request);
                        if (player1 != null) {
                            player1.sendMessage(Language.get("request-denied-other", player.getName()));
                            FriendSystemAPI.playSound(player1, Sound.MOB_GUARDIAN_DEATH);
                        }
                    })
                    .build();
            modalForm.send(player);
        }));
        form.addButton(new ElementButton(Language.getNP("back")), FormWindows::openFriendMenu);
        SimpleForm finalForm = form.build();
        finalForm.send(player);
    }

    public static void openFriendList(Player player, List<String> friends) {
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("friend-list-menu-title"), Language.getNP("friend-list-menu-content"));
        friends.forEach(friend -> {
            String status;
            Player friendP = Server.getInstance().getPlayer(friend);
            if (friendP == null) status = Language.getNP("offline");
            else status = Language.getNP("online");
            form.addButton(new ElementButton(Language.getNP("friend-list-menu-button", friend, status)), executor -> {
                SimpleForm friendForm = new SimpleForm.Builder(Language.getNP("friend-friend-menu-title"), Language.getNP("friend-friend-menu-content", friend))
                        .addButton(new ElementButton(Language.getNP("friend-friend-remove"),
                                new ElementButtonImageData("url", "http://system01.lldv.net:3000/img/friendsystem-removefriend-icon.png")), e -> {
                            api.removeFriend(player.getName(), friend);
                            player.sendMessage(Language.get("friend-removed", friend));
                            FriendSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                            Player player1 = Server.getInstance().getPlayer(friend);
                            if (player1 != null) {
                                player1.sendMessage(Language.get("friend-removed-other", player.getName()));
                                FriendSystemAPI.playSound(player1, Sound.MOB_GUARDIAN_DEATH);
                            }
                        })
                        .addButton(new ElementButton(Language.getNP("back")), e -> openFriendList(player, friends))
                        .build();
                friendForm.send(player);
            });
        });
        form.addButton(new ElementButton(Language.getNP("back")), FormWindows::openFriendMenu);
        SimpleForm finalForm = form.build();
        finalForm.send(player);
    }

    public static void openPlayerSettings(Player player) {
        api.getFriendData(player.getName(), settings -> {
            CustomForm form = new CustomForm.Builder(Language.getNP("friend-settings-menu-title"))
                    .addElement(new ElementToggle(Language.getNP("friend-settings-menu-request"), settings.isRequest()))
                    .addElement(new ElementToggle(Language.getNP("friend-settings-menu-notification"), settings.isNotification()))
                    .onSubmit((executor, response) -> {
                        boolean request = response.getToggleResponse(0);
                        boolean notification = response.getToggleResponse(1);
                        if (!request && settings.isRequest() || request && !settings.isRequest()) api.toggleRequest(player);
                        if (!notification && settings.isNotification() || notification && !settings.isNotification()) api.toggleNotification(player);
                    })
                    .build();
            form.send(player);
        });
    }

}
