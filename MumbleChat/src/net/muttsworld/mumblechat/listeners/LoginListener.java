package net.muttsworld.mumblechat.listeners;

//import java.awt.Color;
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
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

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




    public LoginListener(MumbleChat _plugin, ChatChannelInfo _cc) {
        plugin = _plugin;
        cc = _cc;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (c.isDefaultchannel()) {
                defaultChannel = c.getName();
                defaultColor = c.getColor();
            }
        }

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

        FileConfiguration customConfig = null;

    	plugin.logme(LOG_LEVELS.DEBUG, "Player Logout", "Function Start");

    
        //String curChannel;
        customConfig = plugin.playerdata.getCustomConfig();
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

            plugin.logme(LOG_LEVELS.DEBUG, "Player Logoff", "No special chat stuff.. not savings them");
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
            	plugin.logme(LOG_LEVELS.DEBUG, "Player leaving", "Mutes");
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

            cs.set("tag", plugin.getMetadata(pl, "MumbleChat.ClanTag", plugin));


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

        plugin.logme(LOG_LEVELS.DEBUG, "Player Logout", "before save: "+ dateNow);
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
        FileConfiguration customConfig = null;
       // String pFormatted = "";
        Player pl = plog.getPlayer();
        
        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Got Player");
             
        cc.SetPlayerDisplayName(pl);
        
        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "After Format");

        if (cc.saveplayerdata) {
            customConfig = plugin.playerdata.getCustomConfig();
            plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "We have saved player data");

            //mama.getServer().getLogger().info("before Listen");
            ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
            if (cs != null) {
                plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Player's data has been found");

                //=========SET PLAYER's Current CHANNEL
                curChannel = cs.getString("default", defaultChannel);
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, curChannel));
                if(curChannel.length()<2) //If for some reason an empty string is here. 
                {
                	pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
                }

                //========SET PLAYER's Ignore List
                //Get the Ignore list.. if they have one.
                String ignores = cs.getString("ignores", "");
                pl.setMetadata("MumbleChat.ignore", new FixedMetadataValue(plugin, ignores));

                //=======Set PLAYER's Listening Channels
                plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Check for listen channels");
                //check for channels to listen too...
                String listenChannels = cs.getString("listen", "");
                SetPlayerListenChannels( pl, listenChannels);

                //========Set PLAYER's Mute Channels
                //	mama.getServer().getLogger().info("before Mutes");
                String muteChannels = cs.getString("mutes", "");
                if (muteChannels.length() > 0) {
                    StringTokenizer st = new StringTokenizer(muteChannels, ",");
                    while (st.hasMoreTokens()) {
                        pl.setMetadata("MumbleMute." + st.nextToken(), new FixedMetadataValue(plugin, true));

                    }
                }


            } else {
                plugin.logme(LOG_LEVELS.DEBUG, "Player Login","No Player Data Found");
                curChannel = defaultChannel;
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
                pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
            }

            if(plugin.simplelclans){
                pl.setMetadata("MumbleChat.ClanTag", new FixedMetadataValue(plugin,cs.getBoolean("tag",true )));
            }

        } else {
            curChannel = defaultChannel;
            pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
            pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
        }

        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "After Channels");
        
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
                    plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Channel has permissions");
                    if (pl.isPermissionSet(c.getPermission())) {
                        pl.setMetadata("listenchannel." + s, new FixedMetadataValue(plugin, true));
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
        
        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "After format");
        

    }


    public void SetPlayerListenChannels(Player pl, String ListenChannelsList)
    {
        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Listenchannels:" + ListenChannelsList);
        if (ListenChannelsList.length() > 0) {

            StringTokenizer st = new StringTokenizer(ListenChannelsList, ",");
            while (st.hasMoreTokens()) {

                String chname = st.nextToken();
                ChatChannel c = cc.getChannelInfo(chname);
                if(c != null)
                {
                    plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Check for each channel:" + c.getName());
                    //Check for Channel Permission before allowing player to use channel.
                    //Incase their permissions change.
                    if (c.hasPermission()) {
                        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Channel has permissions");
                        if (pl.isPermissionSet(c.getPermission())) {
                            pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, true));
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
    }



}
