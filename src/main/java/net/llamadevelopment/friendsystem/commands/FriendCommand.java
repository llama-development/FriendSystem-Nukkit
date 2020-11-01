package net.llamadevelopment.friendsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.forms.FormWindows;

public class FriendCommand extends PluginCommand<FriendSystem> {

    public FriendCommand(FriendSystem owner) {
        super(owner.getConfig().getString("Commands.Friend.Name"), owner);
        this.setDescription(owner.getConfig().getString("Commands.Friend.Description"));
        this.setAliases(owner.getConfig().getStringList("Commands.Friend.Aliases").toArray(new String[]{}));
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
