package net.llamadevelopment.friendsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import net.llamadevelopment.friendsystem.commands.FriendCommand;
import net.llamadevelopment.friendsystem.components.api.FriendSystemAPI;
import net.llamadevelopment.friendsystem.components.forms.FormListener;
import net.llamadevelopment.friendsystem.components.language.Language;
import net.llamadevelopment.friendsystem.components.managers.MongoDBProvider;
import net.llamadevelopment.friendsystem.components.managers.MySqlProvider;
import net.llamadevelopment.friendsystem.components.managers.YamlProvider;
import net.llamadevelopment.friendsystem.components.managers.database.Provider;
import net.llamadevelopment.friendsystem.listeners.EventListener;

import java.util.HashMap;
import java.util.Map;

public class FriendSystem extends PluginBase {

    private static FriendSystem instance;
    public static Provider provider;
    private static final Map<String, Provider> providers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        registerProvider(new MongoDBProvider());
        registerProvider(new MySqlProvider());
        registerProvider(new YamlProvider());
        if (!providers.containsKey(getConfig().getString("Provider"))) {
            getLogger().error("§4Please specify a valid provider: Yaml, MySql, MongoDB");
            return;
        }
        provider = providers.get(getConfig().getString("Provider"));
        provider.connect(this);
        getLogger().info("§aSuccessfully loaded " + provider.getProvider() + " provider.");
        FriendSystemAPI.setProvider(provider);
        Language.initConfiguration();
        registerCommands();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        getLogger().info("§aPlugin successfully started.");
    }

    private void registerCommands() {
        Config config = getConfig();
        CommandMap map = getServer().getCommandMap();
        map.register(config.getString("Commands.Friend"), new FriendCommand(config.getString("Commands.Friend")));
    }

    @Override
    public void onDisable() {
        provider.disconnect(this);
    }

    private void registerProvider(Provider provider) {
        providers.put(provider.getProvider(), provider);
    }

    public static FriendSystem getInstance() {
        return instance;
    }

}