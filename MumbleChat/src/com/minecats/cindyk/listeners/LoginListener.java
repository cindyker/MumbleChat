package com.minecats.cindyk.listeners;

//import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.minecats.cindyk.MumbleChat;
import com.minecats.cindyk.ChatChannel;
import com.minecats.cindyk.ChatChannelInfo;

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

//import com.p000ison.dev.simpleclans2.api.clan.Clan;
//import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;

public class LoginListener implements Listener {

    MumbleChat plugin;
    ChatChannelInfo cc;
    String defaultChannel;
    String defaultColor;
    FileConfiguration customConfig = null;
    File customConfigFile = null;

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

    	plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Logout", "Function Start");

    
        //String curChannel;
        customConfig = getCustomConfig();
        Player pl = pp;
        
        //look up player in config
        ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
        
        //If the player doesn't have a section already, then we want to do this...
        if (cs == null) {
        Boolean nothingspecial = true;
        for (ChatChannel c : cc.getChannelsInfo()) {
            
        	if (plugin.getMetadata(pl, "listenchannel." + c.getName(), plugin)) {

                //if they have more than the default
                if (    !c.isDefaultchannel() 
                		&& !c.getAutojoin())
                {
                    nothingspecial = false;
                }  
              
            } 
            
            //or if they are muted...
            if (plugin.getMetadata(pl, "MumbleMute." + c.getName(), plugin))
            {                
            	nothingspecial = false;
            }
            
            //once we set nothingspecial to false, no point continuing.
            if(!nothingspecial)
            	break;
            

        }
        if (!plugin.getMetadataString(pl, "MumbleChat.ignore", plugin).isEmpty()) {
            nothingspecial = false;
        }


        //If they are only listening to the default and autojoin channels no point in saving them.
        if (nothingspecial) {

            plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Logoff", "No special chat stuff.. not savings them");
            return;
        }

     
         ConfigurationSection ps = customConfig.getConfigurationSection("players");
            if (ps == null) {
                cs = customConfig.createSection("players");
            }
            cs = customConfig.createSection("players." + pl.getPlayerListName());

        }

        //Otherwise lets always save these people, they are in there already. 
        cs.set("default", plugin.getMetadataString(pl, "currentchannel", plugin));
        //Save the Ignores list...
        cs.set("ignores", plugin.getMetadataString(pl, "MumbleChat.ignore", plugin));
        

        String strListening = "";
        String strMutes = "";
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (plugin.getMetadata(pl, "listenchannel." + c.getName(), plugin)) {
                strListening += c.getName() + ",";
            }

            if (plugin.getMetadata(pl, "MumbleMute." + c.getName(), plugin)) {
            	plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player leaving", "Mutes");
                strMutes += c.getName() + ",";
            }

        }
        if(plugin.simplelclans)
        {
        	if(plugin.getMetadata(pl, "listenchannel.ally" , plugin))
        	{
        		strListening += "ally"+",";
        				
        	}
        	
        	if(plugin.getMetadata(pl, "listenchannel.clan" , plugin))
        	{
        		strListening += "clan"+",";
        				
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

        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Logout", "before save: "+ dateNow);
        cs.set("date", dateNow);

        //Do we want this Disk IO on every logout..or do we
        //just want to wait for server stop.
        // Lets just do it on server stop.
       //  saveCustomConfig();
       //  reloadCustomConfig();
    }

    ///////////////////////////////////////////
    //Function:
    //   onPlayerLogin
    //
    //  Responds to Player Login event.
    ///////////////////////////////////////////
    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerLogin(PlayerLoginEvent plog) {
        String curChannel;
       // String pFormatted = "";
        Player pl = plog.getPlayer();
        
        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Got Player");
             
        cc.SetPlayerDisplayName(pl);
      
//        if (cc.usePrefix == true) {
//        	
//        	   plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Got Prefix");
//        	 
//            //http://www.minecraftwiki.net/wiki/Classic_server_protocol#Color_Codes
//            pFormatted = cc.FormatPlayerName(MumbleChat.chat.getPlayerPrefix(pl),
//                    "%s", MumbleChat.chat.getPlayerSuffix(pl)+cc.GetClanTag(pl));
//            
//           // plugin.logme(LOG_LEVELS.ERROR, "Player Format:", pFormatted);
//            //pl.sendMessage(pFormatted);
//            //pl.getPlayerListName()
//            //So it shows when you login.
//            //However this is bad.. as it makes who impossible....
//            //pl.setDisplayName(pFormatted);
//
//            //put player tag in metadata... this way we don't keep calling permissionex in chatlistener.
//            pl.setMetadata("chatnameformat", new FixedMetadataValue(plugin, pFormatted));
//        }
//        else
//        {
//        	  pFormatted = "%s"; 
//        	  pl.setMetadata("chatnameformat", new FixedMetadataValue(plugin, pFormatted));
//        }
        
        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "After Format");
   
        //Set up Player Permissions FIRST
        for (ChatChannel c : cc.getChannelsInfo()) {
            //	mama.getServer().getLogger().info("Find Stuff  " + c.getName() + " " + c.getPermission());
            if (c.hasPermission()) {
                //	mama.getServer().getLogger().info("Perms Exist!" + c.getPermission());

                if (MumbleChat.permission.has(pl,c.getPermission())){//(pl.isPermissionSet(c.getPermission())) {
                    //mama.getServer().getLogger().info("And I can use them!!!" + c.getPermission());
                    pl.setMetadata(c.getPermission(), new FixedMetadataValue(plugin, true));
                } else {
                    pl.setMetadata(c.getPermission(), new FixedMetadataValue(plugin, false));
                }
            }
        }
        
                
        if (cc.saveplayerdata) {
            customConfig = getCustomConfig();
            plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "We have saved player data");

            //mama.getServer().getLogger().info("before Listen");
            ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
            if (cs != null) {
                plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Player's data has been found");
             
                curChannel = cs.getString("default", defaultChannel);
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, curChannel));
                if(curChannel.length()<2) //If for some reason an empty string is here. 
                {
                	pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
                }
                

                //Get the Ignore list.. if they have one.
                String ignores = cs.getString("ignores", "");
                pl.setMetadata("MumbleChat.ignore", new FixedMetadataValue(plugin, ignores));

                plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Check for listen channels");
                //check for channels to listen too...
                String listenChannels = cs.getString("listen", "");

                plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Listenchannels:" + listenChannels);
                if (listenChannels.length() > 0) {
                    //String[] pparse = new String[2];

                    StringTokenizer st = new StringTokenizer(listenChannels, ",");
                    while (st.hasMoreTokens()) {

                        String chname = st.nextToken();
                        ChatChannel c = cc.getChannelInfo(chname);
                        if(c != null)
                        {
	                        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Check for each channel:" + c.getName());
	                        //Check for Channel Permission before allowing player to use channel.
	                        //Incase their permissions change.
	                        if (c.hasPermission()) {
	                            plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Channel has permissions");
	                            if (MumbleChat.permission.has(pl,c.getPermission())){//(pl.isPermissionSet(c.getPermission())) {
	                                pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, true));
	                            }
                                else
                                {
                                    pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, false));
                                }
	                        }
	                        else {
	                            pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, true));
	                        }
                        }
                        else
                        { //Check for Clan listen channels.
                           if(plugin.simplelclans){
                        	   		if(chname.equalsIgnoreCase("ally")||chname.equalsIgnoreCase("clan"))
                        	   		{
                        	   			pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, true));
                        	   		}
                               }
                        }
                    }
                } else //if no channel is available to listen on... set it to default... they should listen on something.
                {
                    pl.sendMessage("You have no channels to listen to... setting listen to " + defaultChannel);
                    pl.sendMessage("Check /chlist for a list of available channels.");
                    pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
                }
                //	mama.getServer().getLogger().info("before Mutes");
                String muteChannels = cs.getString("mutes", "");
                if (muteChannels.length() > 0) {
                    StringTokenizer st = new StringTokenizer(muteChannels, ",");
                    while (st.hasMoreTokens()) {
                        pl.setMetadata("MumbleMute." + st.nextToken(), new FixedMetadataValue(plugin, true));

                    }
                }




            } else {
                plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login","No Player Data Found");
                curChannel = defaultChannel;
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
                pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
            }

        } else {
            curChannel = defaultChannel;
            pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
            pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
        }

        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "After Channels");
        
        //reset quick talk
        pl.setMetadata("insertchannel", new FixedMetadataValue(plugin, "NONE"));

        //=========================================================
        // AUTO JOIN 
        //Set AutoJoins up.. just make sure they are listening
        //=========================================================
        List<String> autolist = cc.getAutojoinList();
        if (autolist.size() > 0) {
            for (String s : autolist) {
            	
            	ChatChannel c = cc.getChannelInfo(s);
            	 //Incase their permissions change.
                if (c.hasPermission()) {
                    plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "Channel has permissions");
                    if (MumbleChat.permission.has(pl,c.getPermission())){// (pl.isPermissionSet(c.getPermission())) {
                        pl.setMetadata("listenchannel." + s, new FixedMetadataValue(plugin, true));
                    }
                    else
                    {
                        pl.setMetadata("listenchannel." + s, new FixedMetadataValue(plugin, false));
                    }
                } else {
                    pl.setMetadata("listenchannel." + s, new FixedMetadataValue(plugin, true));
                }
            }
                          	
        }

        
        //=========================================================
        // Set up Current Channel Format 
        //=========================================================
        String curColor = defaultColor;
    	String format = "[Unknown]";
        ChatChannel cf = cc.getChannelInfo(curChannel);
        if(cf != null)
        {
        	curColor = cf.getColor();
        	format = ChatColor.valueOf(curColor.toUpperCase()) + "[" + curChannel + "]";
        	
        }
    
        pl.setMetadata("format", new FixedMetadataValue(plugin, format));
        
        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "After format");
        

        //====================================================================================
        // ---  Get Permissions for Special Commands here -----------------------------------
        //====================================================================================
        if (MumbleChat.permission.has(pl,cc.mutepermissions))//(pl.isPermissionSet(cc.mutepermissions)) //pl.hasPermission(cc.mutepermissions))
        {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "] can Mute permissions given...");
            pl.setMetadata("cindyk.canmute", new FixedMetadataValue(plugin, true));
        }
        else  //MetaData does not clear on logoff / logons
        {
            pl.setMetadata("cindyk.canmute", new FixedMetadataValue(plugin, false));
        }
        
        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "After mute permissions");
        
        //------------------------------------------------------------------------------
        // --  Get Color Text Permissions
        //------------------------------------------------------------------------------
        if(MumbleChat.permission.has(pl,cc.colorpermissions))// (pl.isPermissionSet(cc.colorpermissions)) //pl.hasPermission(cc.mutepermissions))
        {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "] chat color permissions given...");
            pl.setMetadata("cindyk.cancolor", new FixedMetadataValue(plugin, true));
        }
        else
        {
            pl.setMetadata("cindyk.cancolor", new FixedMetadataValue(plugin, false));
        }



        ////////////////////////////////////////////////////////////////////////////////////////
        //FUTURE FORCE CHANNEL CODE
        ///////////////////////////////////////////////////////////////////////////////////////
        if (pl.isPermissionSet(cc.forcepermissions)) {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "] can Force permissions given...");
            pl.setMetadata("cindyk.canforce", new FixedMetadataValue(plugin, true));
        }

       

        plugin.logme(MumbleChat.LOG_LEVELS.DEBUG, "Player Login", "After forcechannel");
        

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
