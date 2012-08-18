package net.muttsworld.mumblechat;

import net.muttsworld.mumblechat.listeners.LoginListener;
import net.muttsworld.mumblechat.listeners.ChatListener;
import net.muttsworld.mumblechat.commands.MuteCommandExecutor;
import net.muttsworld.mumblechat.commands.TellCommandExecutor;
import net.muttsworld.mumblechat.commands.ChatCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

//TODO: Add Filter White List
//TODO: Make channel stick configurable
//TODO: Add SQL so player data can be stored there
//TODO: Implement /chwho [channel] to show online players in a channel
@SuppressWarnings("unused")
public class MumbleChat extends JavaPlugin {

    public ChatListener cl;
    private ChatCommand myExecutor;
    private MuteCommandExecutor muteExecutor;
    //private unmuteCommandExecutor muteExecutor;
    private TellCommandExecutor tellExecutor;
    public LoginListener login;
    private ChatChannelInfo cci;
    FileConfiguration fc;

    public void onEnable() {
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


        myExecutor = new ChatCommand(this, cci);
        pm.registerEvents(myExecutor, this);

        muteExecutor = new MuteCommandExecutor(this, cci);
        //unmuteExecutor = new unmuteCommandExecutor(this,cci);
        tellExecutor = new TellCommandExecutor(this);

        getCommand("tell").setExecutor(tellExecutor);
        getCommand("channel").setExecutor(myExecutor);
        getCommand("leave").setExecutor(myExecutor);
        getCommand("chlist").setExecutor(myExecutor);

        getCommand("durpmute").setExecutor(muteExecutor);
        getCommand("durpunmute").setExecutor(muteExecutor);

    }

    public void onDisable() {
        //getLogger().info("Your plugin has been disabled!");
        login.SaveItToDisk();
        System.out.println("Temp Chat Disabled");
        getLogger().info("MumbleChat has been disabled.");
    }
}
