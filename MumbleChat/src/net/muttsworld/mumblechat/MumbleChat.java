package net.muttsworld.mumblechat;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import net.muttsworld.mumblechat.listeners.LoginListener;
import net.muttsworld.mumblechat.listeners.ChatListener;
import net.muttsworld.mumblechat.listeners.SimpleClansListener;
import net.muttsworld.mumblechat.permissions.MumblePermissions;
import net.muttsworld.mumblechat.commands.MuteCommandExecutor;
import net.muttsworld.mumblechat.commands.TellCommandExecutor;
import net.muttsworld.mumblechat.commands.ChatCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.p000ison.dev.simpleclans2.SimpleClans;

//TODO: Add SQL so player data can be stored there
@SuppressWarnings("unused")
public class MumbleChat extends JavaPlugin {

    //  public static final String LOG_LEVEL = null;
    // Listeners --------------------------------
    public ChatListener chatListener;
    public LoginListener loginListener;
    public SimpleClans sc;

    // Executors --------------------------------
    private ChatCommand chatExecutor;
    private MuteCommandExecutor muteExecutor;
    //private unmuteCommandExecutor muteExecutor;
    private TellCommandExecutor tellExecutor;

    // Misc --------------------------------
    private ChatChannelInfo ccInfo;
    FileConfiguration fc;
    MumblePermissions mp;
    public boolean simplelclans=false;
    private static final Logger log = Logger.getLogger("Minecraft");

    // Vault --------------------------------
    public static Permission permission = null;
    public static Chat chat = null;

    public enum LOG_LEVELS {
        DEBUG, INFO, WARNING, ERROR
    }
    private LOG_LEVELS curLogLevel;
    public long LINELENGTH = 40;

    @Override
    public void onEnable() {
        log.info(String.format("[%s] - Initializing...", getDescription().getName()));

        log.info(String.format("[%s] - Checking for Vault...", getDescription().getName()));
        
        // Set up Vault
        if(!setupPermissions()) {
            log.info(String.format("[%s] - Could not find Vault dependency, disabling.", getDescription().getName()));
        }
        
        log.info(String.format("[%s] - Checking for SimpleClans...", getDescription().getName()));
        
        Plugin plug = getServer().getPluginManager().getPlugin("SimpleClans2");
       
        // Set up simpleclans
        if(plug==null) {
            log.info(String.format("[%s] - Could not find Simpleclans dependency, disabling.", getDescription().getName()));
        }
        else
        {
        	simplelclans=true;
        	sc = ((SimpleClans) plug);
        	
        
        }
        
        setupChat();

        
        // Log completion of initialization
        getLogger().info(String.format("[%s] - Enabled with version %s", getDescription().getName(), getDescription().getVersion()));
        
        // Get config and handle
        
     // Configuration
     		try {
     			
     			fc= getConfig(); 					
     			//saveConfig();
     			if(fc.getList("channels") == null)
     			{
     				getConfig().options().copyDefaults(true);
     				saveDefaultConfig();
     				reloadConfig();
     			}
     		
     		} catch (Exception ex) {
     			getLogger().log(Level.SEVERE,
     					"[MumbleChat] Could not load configuration!", ex);
     		}
     	   

        log.info(String.format("[%s] - Registering Listeners", getDescription().getName()));

        // Channel information reference
        ccInfo = new ChatChannelInfo(this);
        
        if(ccInfo == null)
        	 log.info(String.format("[%s] - Configuration is BAD!", getDescription().getName()));

        
        if(simplelclans)
        	super.getServer().getPluginManager().registerEvents(new SimpleClansListener(this, ccInfo), this);
        
        chatListener = new ChatListener(this, ccInfo);
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(chatListener, this);
       

        loginListener = new LoginListener(this, ccInfo);
        pluginManager.registerEvents(loginListener, this);
        
       

        //Future enhancement testing...
        //mp = new MumblePermissions(this,cci);
        //mp.PermissionsExAvailable();

        chatExecutor = new ChatCommand(this, ccInfo);
        pluginManager.registerEvents(chatExecutor, this);
        
        log.info(String.format("[%s] - Attaching to Executors", getDescription().getName()));
        
        muteExecutor = new MuteCommandExecutor(this, ccInfo);
        tellExecutor = new TellCommandExecutor(this, ccInfo);

        getCommand("tell").setExecutor(tellExecutor);
        getCommand("ignore").setExecutor(tellExecutor);
        getCommand("whisper").setExecutor(tellExecutor);

        getCommand("channel").setExecutor(chatExecutor);
        getCommand("leave").setExecutor(chatExecutor);
        getCommand("join").setExecutor(chatExecutor);
        getCommand("chlist").setExecutor(chatExecutor);
        getCommand("chwho").setExecutor(chatExecutor);
        getCommand("chversion").setExecutor(chatExecutor);
        getCommand("chhelp").setExecutor(chatExecutor);
        
        getCommand("chmute").setExecutor(muteExecutor);        
        getCommand("chunmute").setExecutor(muteExecutor);
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
        loginListener.SaveItToDisk();
        //System.out.println("Temp Chat Disabled");
        log.info("MumbleChat has been disabled.");
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

    public ChatChannelInfo getChatChannelInfo()
    {
        return ccInfo;
    }

    public void logme(LOG_LEVELS level, String location, String logline) {
        //Get LogLevel from Config...
        //if no loglevel exist assume Warning... less spam that way
        if (level.ordinal() >= curLogLevel.ordinal()) {
            log.log(Level.INFO, "[MumbleChat]: {0}:{1} : {2}", new Object[]{level.toString(), location, logline});
        }

    }
}
