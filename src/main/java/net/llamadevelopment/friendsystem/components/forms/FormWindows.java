package net.llamadevelopment.friendsystem.components.forms;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.level.Sound;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.data.PlayerSettings;
import net.llamadevelopment.friendsystem.components.forms.custom.CustomForm;
import net.llamadevelopment.friendsystem.components.forms.modal.ModalForm;
import net.llamadevelopment.friendsystem.components.forms.simple.SimpleForm;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.managers.database.Provider;

public class FormWindows {

    private static final Provider api = FriendSystemAPI.getProvider();

    public static void openFriendMenu(Player player) {
        SimpleForm form = new SimpleForm.Builder(Language.getAndReplaceNP("friend-menu-title"), Language.getAndReplaceNP("friend-menu-content"))
                .addButton(new ElementButton(Language.getAndReplaceNP("friend-menu-addfriend"),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("friend-menu-addfriend-image"))), FormWindows::openAddFriend)
                .addButton(new ElementButton(Language.getAndReplaceNP("friend-menu-requests", api.getRequests(player.getName()).size()),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("friend-menu-requests-image"))), FormWindows::openFriendRequests)
                .addButton(new ElementButton(Language.getAndReplaceNP("friend-menu-friendlist", api.getFriends(player.getName()).size()),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("friend-menu-friendlist-image"))), FormWindows::openFriendList)
                .addButton(new ElementButton(Language.getAndReplaceNP("friend-menu-settings"),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("friend-menu-settings-image"))), FormWindows::openPlayerSettings)
                .build();
        form.send(player);
    }

    public static void openAddFriend(Player player) {
        CustomForm form = new CustomForm.Builder(Language.getAndReplaceNP("friend-add-title"))
                .addElement(new ElementInput(Language.getAndReplaceNP("friend-add-content"), Language.getAndReplaceNP("friend-add-input")))
                .onSubmit(((executor, response) -> {
                    String input = response.getInputResponse(0);
                    if (input.isEmpty() || input.equals(player.getName())) {
                        player.sendMessage(Language.getAndReplace("invalid-input"));
                        FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                        return;
                    }
                    if (api.userExists(input)) {
                        PlayerSettings settings = api.getFriendData(input);
                        if (settings.isRequest()) {
                            if (!api.areFriends(player.getName(), input)) {
                                if (!api.requestExists(player.getName(), input)) {
                                    api.createFriendRequest(player.getName(), input);
                                    player.sendMessage(Language.getAndReplace("request-sent", input));
                                    FriendSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                                    Player player1 = Server.getInstance().getPlayer(input);
                                    if (player1 != null) {
                                        player1.sendMessage(Language.getAndReplace("request-received", player.getName()));
                                        FriendSystemAPI.playSound(player1, Sound.NOTE_PLING);
                                    }
                                } else {
                                    player.sendMessage(Language.getAndReplace("request-already-sent", input));
                                    FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                                }
                            } else {
                                player.sendMessage(Language.getAndReplace("already-friends", input));
                                FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                            }
                        } else {
                            player.sendMessage(Language.getAndReplace("request-not-allowed", input));
                            FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                        }
                    } else {
                        player.sendMessage(Language.getAndReplace("user-not-found", input));
                        FriendSystemAPI.playSound(player, Sound.NOTE_BASS);
                    }
                }))
                .build();
        form.send(player);
    }

    public static void openFriendRequests(Player player) {
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getAndReplaceNP("friend-request-menu-title"), Language.getAndReplaceNP("friend-request-menu-content"));
        api.getRequests(player.getName()).forEach(request -> form.addButton(new ElementButton(Language.getAndReplaceNP("friend-request-menu-button", request)), executor -> {
            ModalForm modalForm = new ModalForm.Builder(Language.getAndReplaceNP("friend-request-accept-title"), Language.getAndReplaceNP("friend-request-accept-content", request),
                    Language.getAndReplaceNP("friend-request-accept-accept"), Language.getAndReplaceNP("friend-request-accept-deny"))
                    .onYes(e -> {
                        api.createFriendship(player.getName(), request);
                        api.removeFriendRequest(player.getName(), request);
                        FriendSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                        player.sendMessage(Language.getAndReplace("request-accepted", request));
                        Player player1 = Server.getInstance().getPlayer(request);
                        if (player1 != null) {
                            player1.sendMessage(Language.getAndReplace("request-accepted-other", player.getName()));
                            FriendSystemAPI.playSound(player1, Sound.RANDOM_LEVELUP);
                        }
                    })
                    .onNo(e -> {
                        api.removeFriendRequest(request, player.getName());
                        FriendSystemAPI.playSound(player, Sound.MOB_GUARDIAN_DEATH);
                        player.sendMessage(Language.getAndReplace("request-denied", request));
                        Player player1 = Server.getInstance().getPlayer(request);
                        if (player1 != null) {
                            player1.sendMessage(Language.getAndReplace("request-denied-other", player.getName()));
                            FriendSystemAPI.playSound(player1, Sound.MOB_GUARDIAN_DEATH);
                        }
                    })
                    .build();
            modalForm.send(player);
        }));
        form.addButton(new ElementButton(Language.getAndReplaceNP("back")), FormWindows::openFriendMenu);
        SimpleForm finalForm = form.build();
        finalForm.send(player);
    }

    public static void openFriendList(Player player) {
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getAndReplaceNP("friend-list-menu-title"), Language.getAndReplaceNP("friend-list-menu-content"));
        api.getFriends(player.getName()).forEach(friend -> {
            String status;
            Player friendP = Server.getInstance().getPlayer(friend);
            if (friendP == null) status = Language.getAndReplaceNP("offline");
            else status = Language.getAndReplaceNP("online");
            form.addButton(new ElementButton(Language.getAndReplaceNP("friend-list-menu-button", friend, status)), executor -> {
                SimpleForm friendForm = new SimpleForm.Builder(Language.getAndReplaceNP("friend-friend-menu-title"), Language.getAndReplaceNP("friend-friend-menu-content", friend))
                        .addButton(new ElementButton(Language.getAndReplaceNP("friend-friend-remove"),
                                new ElementButtonImageData("url", Language.getAndReplaceNP("friend-friend-remove-image"))), e -> {
                            api.removeFriend(player.getName(), friend);
                            player.sendMessage(Language.getAndReplace("friend-removed", friend));
                            FriendSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                            Player player1 = Server.getInstance().getPlayer(friend);
                            if (player1 != null) {
                                player1.sendMessage(Language.getAndReplace("friend-removed-other", player.getName()));
                                FriendSystemAPI.playSound(player1, Sound.MOB_GUARDIAN_DEATH);
                            }
                        })
                        .addButton(new ElementButton(Language.getAndReplaceNP("back")), FormWindows::openFriendList)
                        .build();
                friendForm.send(player);
            });
        });
        form.addButton(new ElementButton(Language.getAndReplaceNP("back")), FormWindows::openFriendMenu);
        SimpleForm finalForm = form.build();
        finalForm.send(player);
    }

    public static void openPlayerSettings(Player player) {
        PlayerSettings settings = api.getFriendData(player.getName());
        CustomForm form = new CustomForm.Builder(Language.getAndReplaceNP("friend-settings-menu-title"))
                .addElement(new ElementToggle(Language.getAndReplaceNP("friend-settings-menu-request"), settings.isRequest()))
                .addElement(new ElementToggle(Language.getAndReplaceNP("friend-settings-menu-notification"), settings.isNotification()))
                .onSubmit((executor, response) -> {
                    boolean request = response.getToggleResponse(0);
                    boolean notification = response.getToggleResponse(1);
                    if (!request && settings.isRequest() || request && !settings.isRequest()) api.toggleRequest(player);
                    if (!notification && settings.isNotification() || notification && !settings.isNotification()) api.toggleNotification(player);
                })
                .build();
        form.send(player);
    }
}
