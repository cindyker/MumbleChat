package net.muttsworld.mumblechat;

import java.util.List;
import java.util.logging.Level;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import net.muttsworld.mumblechat.listeners.LoginListener;
import net.muttsworld.mumblechat.listeners.ChatListener;
import net.muttsworld.mumblechat.permissions.MumblePermissions;
import net.muttsworld.mumblechat.commands.MuteCommandExecutor;
import net.muttsworld.mumblechat.commands.TellCommandExecutor;
import net.muttsworld.mumblechat.commands.ChatCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

//TODO: Add SQL so player data can be stored there
@SuppressWarnings("unused")
public class MumbleChat extends JavaPlugin {

    //  public static final String LOG_LEVEL = null;
    public ChatListener cl;
    private ChatCommand myExecutor;
    private MuteCommandExecutor muteExecutor;
    //private unmuteCommandExecutor muteExecutor;
    private TellCommandExecutor tellExecutor;
    public LoginListener login;
    private ChatChannelInfo cci;
    FileConfiguration fc;
    public static Permission permission = null;
    public static Chat chat = null;
    MumblePermissions mp;

    public enum LOG_LEVELS {
        DEBUG, INFO, WARNING, ERROR
    }
    private LOG_LEVELS curLogLevel;
    public long LINELENGTH = 40;

    @Override
    public void onEnable() {
        getLogger().info("Initializing MumbleChat.");

        getLogger().info("Checking for Vault...");

        

        getLogger().info("MumbleChat has been enabled.");
        fc = getConfig();
        if (fc.getList("filters") == null) {
            saveDefaultConfig();
        }
        saveConfig();

        //class with channel information
        cci = new ChatChannelInfo(this);

        cl = new ChatListener(this, cci);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(cl, this);

        login = new LoginListener(this, cci);
        pm.registerEvents(login, this);

        //Future enhancement testing...
        //mp = new MumblePermissions(this,cci);
        //mp.PermissionsExAvailable();

        myExecutor = new ChatCommand(this, cci);
        pm.registerEvents(myExecutor, this);

        muteExecutor = new MuteCommandExecutor(this, cci);
        tellExecutor = new TellCommandExecutor(this, cci);

        getCommand("tell").setExecutor(tellExecutor);
        getCommand("ignore").setExecutor(tellExecutor);
        getCommand("whisper").setExecutor(tellExecutor);

        getCommand("channel").setExecutor(myExecutor);
        getCommand("leave").setExecutor(myExecutor);
        getCommand("join").setExecutor(myExecutor);
        getCommand("chlist").setExecutor(myExecutor);
        getCommand("chwho").setExecutor(myExecutor);

        getCommand("mute").setExecutor(muteExecutor);
        getCommand("unmute").setExecutor(muteExecutor);
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    public long getLineLength() {
        return LINELENGTH;
    }

    @Override
    public void onDisable() {
        //getLogger().info("Your plugin has been disabled!");
        login.SaveItToDisk();
        //System.out.println("Temp Chat Disabled");
        getLogger().info("MumbleChat has been disabled.");
    }

    //Utiliy metadata functions... 
    public String getMetadataString(Player player, String key, MumbleChat plugin) {
        List<MetadataValue> values = player.getMetadata(key);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())) {
                return value.asString(); //value();
            }
        }
        return "";
    }

    public boolean getMetadata(Player player, String key, MumbleChat plugin) {
        List<MetadataValue> values = player.getMetadata(key);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())) {
                return value.asBoolean(); //value();
            }
        }
        return false;
    }

    public void setLogLevel(String loglevel) {
        if (LOG_LEVELS.valueOf(loglevel) != null) {
            curLogLevel = LOG_LEVELS.valueOf(loglevel);
        } else {
            curLogLevel = LOG_LEVELS.INFO;
        }
    }

    public void logme(LOG_LEVELS level, String location, String logline) {
        //Get LogLevel from Config...
        //if no loglevel exist assume Warning... less spam that way
        if (level.ordinal() >= curLogLevel.ordinal()) {
            getLogger().log(Level.INFO, ":{0}:{1} : {2}", new Object[]{level.toString(), location, logline});
        }

    }
}
