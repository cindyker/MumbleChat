package net.muttsworld.mumblechat.listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class LoginListener implements Listener {

    MumbleChat plugin;
    ChatChannelInfo cc;
    String defaultChannel;
    String defaultColor;
    FileConfiguration customConfig = null;
    File customConfigFile = null;

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

    public void SaveItToDisk() {
        //saveCustomConfig();
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, e);
            //	  logger.severe(PREFIX + " error writting configurations");
            e.printStackTrace();
        }
    }

    public LoginListener(MumbleChat _plugin, ChatChannelInfo _cc) {
        plugin = _plugin;
        cc = _cc;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (c.isDefaultchannel()) {
                defaultChannel = c.getName();
                defaultColor = c.getColor();
            }
        }
        reloadCustomConfig();
    }

    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerKick(PlayerKickEvent plog) {
        if (cc.saveplayerdata) {
            PlayerLeaving(plog.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerQuit(PlayerQuitEvent plog) {
        if (cc.saveplayerdata) {
            PlayerLeaving(plog.getPlayer());
        }
    }

    void PlayerLeaving(Player pp) {

        //mama.getServer().getLogger().info("Logout.. ");

        //String curChannel;
        customConfig = getCustomConfig();
        Player pl = pp;

        Boolean listendefault = false;
        int listencount = 0;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (getMetadata(pl, "listenchannel." + c.getName(), plugin)) {
                listencount++;
                //if they are only listening to the default and only talking on the default....
                if (c.isDefaultchannel() && getMetadataString(pl, "currentchannel", plugin).equalsIgnoreCase(c.getName())) 
                {                	
                    listendefault = true;
                }
            }
            
        }

        
        //If they are only listening to the default channel no point in saving them.
        if (listencount == 1 && listendefault == true ) {
        	
            return;
        }

        //if(getMetadataString(p,"listenchannel".+))

        ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
        if (cs == null) {
            //	mama.getServer().getLogger().info("Logout.. No Player Found");
            //cs = new ConfigurationSection();
            //cs = customConfig.createSection("players");
            ConfigurationSection ps = customConfig.getConfigurationSection("players");
            if (ps == null) {
                cs = customConfig.createSection("players");
            }
            cs = customConfig.createSection("players." + pl.getPlayerListName());

        }

        cs.set("default", getMetadataString(pl, "currentchannel", plugin));

        //	mama.getServer().getLogger().info("After Section.... ");

        String strListening = "";
        String strMutes = "";
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (getMetadata(pl, "listenchannel." + c.getName(), plugin)) {
                strListening += c.getName() + ",";
            }

            if (getMetadata(pl, "durpMute." + c.getName(), plugin)) {
                strMutes += c.getName() + ",";
            }

        }

        //	mama.getServer().getLogger().info("After Section....2 ");

        if (strListening.length() > 0) {
            strListening = strListening.substring(0, strListening.length() - 1);
        }

        //	mama.getServer().getLogger().info("After Section....2 " + strListening);

        cs.set("listen", strListening);

        if (strMutes.length() > 0) {
            strMutes = strMutes.substring(0, strMutes.length() - 1);
        }
        //	mama.getServer().getLogger().info("After Section.. " + strMutes);;

        cs.set("mutes", strMutes);

        //	mama.getServer().getLogger().info("After Section....3 ");


        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
        String dateNow = formatter.format(currentDate.getTime());

        //	   mama.getServer().getLogger().info("Before Save:" + dateNow);
        cs.set("date", dateNow);

        //Do we want this Disk IO on every logout..or do we
        //just want to wait for server stop.
        //lets wait until server stops...
     //   saveCustomConfig();
       // reloadCustomConfig();
    }

    
    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerLogin(PlayerLoginEvent plog) {
        String curChannel;
        Player pl = plog.getPlayer();

        PermissionUser user = PermissionsEx.getUser(pl);
        //http://www.minecraftwiki.net/wiki/Classic_server_protocol#Color_Codes
        String pFormatted = cc.FormatPlayerName(user.getPrefix(),pl.getPlayerListName(),user.getSuffix());
       //So it shows when you login.
        pl.setDisplayName(pFormatted);

         //put player tag in metadata... this way we don't keep calling permissionex in chatlistener.
         pl.setMetadata("chatnameformat",new FixedMetadataValue(plugin,pFormatted));

       
        if (cc.saveplayerdata) {
            customConfig = getCustomConfig();

            ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
            if (cs != null) {
                //mama.getServer().getLogger().info("Player Found");

                curChannel = cs.getString("default", defaultChannel);
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, curChannel));

                //mama.getServer().getLogger().info("before Listen");
                
                //check for channels to listen too...
                String listenChannels = cs.getString("listen", "");
                if (listenChannels.length() > 0) {
                    //String[] pparse = new String[2];
                    StringTokenizer st = new StringTokenizer(listenChannels, ",");
                    while (st.hasMoreTokens()) {
                        //mama.getServer().getLogger().info("chatting: " + st.toString() + " i:"+i);
                        pl.setMetadata("listenchannel." + st.nextToken(), new FixedMetadataValue(plugin, true));
                    }
                }
                else //if no channel is available to listen on... set it to default... they should listen on something.
                {
                	pl.sendMessage("You have no channels to listen to... setting listen to "+ defaultChannel);
                	pl.sendMessage("Check /chlist for a list of available channels.");
                	pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
                }
                //	mama.getServer().getLogger().info("before Mutes");
                String muteChannels = cs.getString("mutes", "");
                if (muteChannels.length() > 0) {
                    StringTokenizer st = new StringTokenizer(muteChannels, ",");
                    while (st.hasMoreTokens()) {
                        pl.setMetadata("durpMute." + st.nextToken(), new FixedMetadataValue(plugin, true));

                    }
                }

            } else {
                //mama.getServer().getLogger().info("No Player Found");
                curChannel = defaultChannel;
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
                pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
            }

        } else {
            curChannel = defaultChannel;
            pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
            pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
        }
        pl.setMetadata("insertchannel", new FixedMetadataValue(plugin, "NONE"));

        String curColor = defaultColor;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (c.getName().equalsIgnoreCase(curChannel)) {
                curColor = c.getColor();
            }
        }

        String format = ChatColor.valueOf(curColor.toUpperCase()) + "[" + curChannel + "]";
        pl.setMetadata("format", new FixedMetadataValue(plugin, format));



        if (pl.isPermissionSet(cc.mutepermissions)) //pl.hasPermission(cc.mutepermissions))
        {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "] Can Mute Permissions given...");
            pl.setMetadata("durpchat.canmute", new FixedMetadataValue(plugin, true));
        }

        for (ChatChannel c : cc.getChannelsInfo()) {
            //	mama.getServer().getLogger().info("Find Stuff  " + c.getName() + " " + c.getPermission());
            if (c.hasPermission()) {
                //	mama.getServer().getLogger().info("Perms Exist!" + c.getPermission());

                if (pl.isPermissionSet(c.getPermission())) {
                    //mama.getServer().getLogger().info("And I can use them!!!" + c.getPermission());
                    pl.setMetadata(c.getPermission(), new FixedMetadataValue(plugin, true));
                } else {
                    pl.setMetadata(c.getPermission(), new FixedMetadataValue(plugin, false));
                }
            }
        }

        //mama.getServer().getLogger().info("End the Login Event");
    }

    public void reloadCustomConfig() {

        //TODO: Consider that this is a hardcoded filename... ick.
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder().getAbsolutePath(), "PlayerData.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        /*  // Look for defaults in the jar
        InputStream defConfigStream = mama.getResource("PlayerData.yml");
        if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        customConfig.setDefaults(defConfig);
        }*/
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            this.reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }
}
