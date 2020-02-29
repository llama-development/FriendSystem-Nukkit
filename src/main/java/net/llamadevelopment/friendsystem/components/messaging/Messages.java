package net.llamadevelopment.friendsystem.components.messaging;

import net.llamadevelopment.friendsystem.FriendSystem;

public class Messages {

    public static String getAndReplace(String path, String... replacements) {
        String message = FriendSystem.getInstance().getConfig().getString(path);
        int i = 0;
        for (String replacement : replacements) {
            message = message.replace("[" + i + "]", replacement);
            i++;
        }
        return FriendSystem.getInstance().getConfig().getString("Messages.Prefix").replace("&", "§") + message.replace("&", "§");
    }

}
