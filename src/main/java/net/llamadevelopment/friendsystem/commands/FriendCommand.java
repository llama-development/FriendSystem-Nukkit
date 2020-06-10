package net.llamadevelopment.friendsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.friendsystem.components.forms.FormWindows;

public class FriendCommand extends Command {

    public FriendCommand(String name) {
        super(name, "Friend menu");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            FormWindows.openFriendMenu(player);
        }
        return false;
    }
}
