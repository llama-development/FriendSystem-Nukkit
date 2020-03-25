package net.llamadevelopment.friendsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import net.llamadevelopment.friendsystem.commands.FriendCommand;
import net.llamadevelopment.friendsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.friendsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.friendsystem.listeners.EventListener;

public class FriendSystem extends PluginBase {

    private static FriendSystem instance;
    private boolean mysql, mongodb, yaml = false;
    private int version = 1;

    @Override
    public void onEnable() {
        instance = this;
        System.out.println("");
        System.out.println("  ______    _                _  _____           _                 ");
        System.out.println(" |  ____|  (_)              | |/ ____|         | |                ");
        System.out.println(" | |__ _ __ _  ___ _ __   __| | (___  _   _ ___| |_ ___ _ __ ___  ");
        System.out.println(" |  __| '__| |/ _ \\ '_ \\ / _` |\\___ \\| | | / __| __/ _ \\ '_ ` _ \\ ");
        System.out.println(" | |  | |  | |  __/ | | | (_| |____) | |_| \\__ \\ ||  __/ | | | | |");
        System.out.println(" |_|  |_|  |_|\\___|_| |_|\\__,_|_____/ \\__, |___/\\__\\___|_| |_| |_|");
        System.out.println("                                       __/ |                      ");
        System.out.println("                                      |___/                       ");
        System.out.println("");
        getLogger().info("§aStarting and loading all components...");
        saveDefaultConfig();
        registerCommands();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getLogger().info("Components successfully loaded!");
        if (getConfig().getString("Provider").equalsIgnoreCase("MongoDB")) {
            mongodb = true;
            getLogger().info("Connecting to database...");
            MongoDBProvider.connect(this);
        } else if (getConfig().getString("Provider").equalsIgnoreCase("MySql")) {
            mysql = true;
            getLogger().info("Connecting to database...");
            MySqlProvider mySqlProvider = new MySqlProvider();
            mySqlProvider.createTables();
        } else if (getConfig().getString("Provider").equalsIgnoreCase("Yaml")) {
            yaml = true;
            getLogger().info("Using YAML as provider...");
            saveResource("data/friend-data.yml");
            saveResource("data/user-data.yml");
            saveResource("data/settings-data.yml");
            getLogger().info("§aPlugin successfully started.");
        } else {
            getLogger().warning("§4§lFailed to load! Please specify a valid provider: MySql, MongoDB, Yaml");
        }
        updateConfig(getConfig());
    }

    private void updateConfig(Config config) {
        if (!config.exists("ConfigVersion")) {
            config.set("ConfigVersion", 1);
            config.set("Messages.NotOnline", "&cThis player is not online.");
            config.set("Messages.MsgReceived", "&8[&a[0]&8] &e-> &8[&aMe&8] &8: &7[1]");
            config.set("Messages.MsgSent", "&8[&aMe&8] &e-> &8[&a[0]&8] &8: &7[1]");
            config.set("HelpFormat.Msg", "&e/friend msg <Player> <Message> &8- &7Send a private message to a friend.");
            config.save();
            config.reload();
        }
    }

    private void registerCommands() {
        Config config = getConfig();
        CommandMap map = getServer().getCommandMap();
        map.register(config.getString("Commands.Friend"), new FriendCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling FriendSystem...");
    }

    public static FriendSystem getInstance() {
        return instance;
    }

    public boolean isMongodb() {
        return mongodb;
    }

    public boolean isMysql() {
        return mysql;
    }

    public boolean isYaml() {
        return yaml;
    }
}
