package net.llamadevelopment.friendsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import lombok.Getter;
import net.llamadevelopment.friendsystem.commands.FriendCommand;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.forms.FormListener;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.provider.MongoDBProvider;
import net.llamadevelopment.friendsystem.components.provider.MySqlProvider;
import net.llamadevelopment.friendsystem.components.provider.YamlProvider;
import net.llamadevelopment.friendsystem.components.provider.Provider;
import net.llamadevelopment.friendsystem.listeners.EventListener;

import java.util.HashMap;
import java.util.Map;

public class FriendSystem extends PluginBase {

    public static Provider provider;
    private static final Map<String, Provider> providers = new HashMap<>();

    @Getter
    private static FriendSystem instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.saveDefaultConfig();
            this.registerProvider(new MongoDBProvider());
            this.registerProvider(new MySqlProvider());
            this.registerProvider(new YamlProvider());
            if (!providers.containsKey(this.getConfig().getString("Provider"))) {
                this.getLogger().error("§4Please specify a valid provider: Yaml, MySql, MongoDB");
                return;
            }
            provider = providers.get(this.getConfig().getString("Provider"));
            provider.connect(this);
            this.getLogger().info("§aSuccessfully loaded " + provider.getProvider() + " provider.");
            FriendSystemAPI.setProvider(provider);
            Language.init();
            this.registerCommands();
            this.getServer().getPluginManager().registerEvents(new EventListener(), this);
            this.getServer().getPluginManager().registerEvents(new FormListener(), this);
            this.getLogger().info("§aFriendSystem successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().error("§4Failed to load FriendSystem.");
        }
    }

    private void registerCommands() {
        Config config = this.getConfig();
        CommandMap map = this.getServer().getCommandMap();
        map.register(config.getString("Commands.Friend"), new FriendCommand(this));
    }

    @Override
    public void onDisable() {
        provider.disconnect(this);
    }

    private void registerProvider(Provider provider) {
        providers.put(provider.getProvider(), provider);
    }

}
