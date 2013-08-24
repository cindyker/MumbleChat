package net.muttsworld.mumblechat.commands;

import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.*;

public class ChatCommand implements CommandExecutor, Listener {

    private MumbleChat plugin;
    @SuppressWarnings("unused")
    private String name;
    private ChatChannelInfo cc;

    public enum Mumblings {

        channel,
        leave
    }

    public ChatCommand(MumbleChat plugin, ChatChannelInfo chatchannel) {
        this.plugin = plugin;
        name = plugin.getName();
        cc = chatchannel;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        String brformat = "";

        //Alert Alert Alert
        if (e.getMessage().toLowerCase().startsWith("/" + cc.getBroadcastCommand() + " ")) {
            if (cc.isBroadcastAvailable()) {
                //Implement Alerts here... ?

                if (cc.broadcastPermissions.equalsIgnoreCase("none")
                        || player.hasPermission(cc.broadcastPermissions)) {

                    String newMsg = e.getMessage();
                  
                    //remove command
                    newMsg = newMsg.replaceFirst("/" + cc.getBroadcastCommand() + " ", "");
                    
                    //Format it for Rainbow Skittle colors - Your Welcome Double.
                    newMsg = cc.broadcastDisplayTag +" " + cc.FormatString(newMsg);
                    
                    if (cc.broadcastPlayer) {
                    	String pName= "";
                    	 if (cc.usePrefix) {
                             pName = String.format(plugin.getMetadataString(player, "chatnameformat", plugin),player.getDisplayName());
                         }
                    	 else
                    		 pName = player.getDisplayName();

                        brformat =  pName  + ChatColor.valueOf(cc.broadcastColor.toUpperCase()) +  " " + newMsg;

                    } else {
                        brformat = ChatColor.valueOf(cc.broadcastColor.toUpperCase()) +" " + newMsg;
                    }

                    plugin.getServer().broadcastMessage(brformat);
                    //put in Chat listener
                    //player.chat(newMsg);

                    //cancel command
                    e.setCancelled(true);
                    return;
                }


            }
        }
        
        if(e.getMessage().toLowerCase().startsWith("/chname"))
        {
        	  cc.SetPlayerDisplayName(player);  //This Should Re GET the player's tag Each time they talk. 
        	  player.sendMessage("Display name has been updated.");
        	  e.setCancelled(true);
        	  
              return;
        }

        if(plugin.simplelclans)
        {
        	if(e.getMessage().toLowerCase().startsWith("/clan modtag"))
        	{
        		player.sendMessage("/chname to refresh your tag after you have changed it.");
        		return;
        	}

            if(e.getMessage().toLowerCase().startsWith("/chtag"))
            {
                String newMsg = e.getMessage();
                newMsg = newMsg.replaceFirst("/chtag ", "");

                switch( newMsg)
                {
                    case "off":
                        player.setMetadata("MumbleChat.ClanTag", new FixedMetadataValue(plugin, false));
                        break;
                    case "on":
                        player.setMetadata("MumbleChat.ClanTag", new FixedMetadataValue(plugin, true));
                        break;

                    default:
                        player.sendMessage("Invalid command: /chtag off  or  /chtag on");
                }

            }
        }

        for (ChatChannel ci : cc.getChannelsInfo()) {

            if (e.getMessage().toLowerCase().startsWith("/" + ci.getAlias() + " ")) {
                String newMsg = e.getMessage();
                newMsg = newMsg.replaceFirst("/", "/ch ");

                //	plugin.getServer().getLogger().info("["+plugin.getName() +"]"+ "Channel "+ci.getName());	

                player.setMetadata("insertchannel", new FixedMetadataValue(plugin, ci.getName()));
                player.setMetadata("listenchannel." + ci.getName(), new FixedMetadataValue(plugin, true));
                //If they were chatting with another player, its time to stop.

                String tellchat = plugin.getMetadataString(player, "MumbleChat.tell", plugin);
                if (tellchat.length() > 0) {
                    player.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin, ""));
                    player.sendMessage("You are no longer in private conversation with " + tellchat);
                }

                //	 plugin.getServer().getLogger().info("["+plugin.getName() +"]"+ "New Message to "+newMsg);	
                e.setMessage(newMsg);

                //e.setCancelled(true);
            }

        }
        /*if (e.getMessage().toLowerCase().startsWith("/doh "))
        {
        //sendToChannel(); // handle it
        e.setCancelled(true); // if you don't want another plugin to try to process the same command
        }*/
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public boolean onConsoleCommand(ServerCommandEvent sce)
    {

    //    plugin.getLogger().info("Get Command: "+sce.getCommand());


        switch(sce.getCommand())
        {
            case "who":
            case "online":
              //  ShowWhoInfo((Player)sce.getSender());
                Player[] plist = plugin.getServer().getOnlinePlayers();
                String players = "Online Players: ";
                for(Player p:plist)
                {
                    players+= p.getPlayerListName()+", ";
                }

                sce.getSender().sendMessage(players);
                return true;

            case "chversion":

                    PluginDescriptionFile pdf = plugin.getDescription(); //Gets plugin.yml
                    //Gets the version
                    sce.getSender().sendMessage("MumbleChat Version: " + pdf.getVersion());
                return true;


        }

        return false;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        //if not a player... we are done.
        if (!(sender instanceof Player)) {
            return false;
        }

        //OK.. we are a player.. lets play	
        Player player = (Player) sender;

        String comm = cmd.getName().toLowerCase();

        //player.sendMessage(comm + " we sent ");
        if (args == null) {
            return false;
        }


        //plugin.getServer().getLogger().info("["+plugin.getName() +"]"+ "Argument: " +args.length + " command: "+ comm);

        if ((comm.equalsIgnoreCase("join") || comm.equalsIgnoreCase("channel")) && args.length == 0) {

            player.sendMessage("Error in command: /" + comm + " [name] [msg]");
            //	plugin.getServer().getLogger().info("["+plugin.getName() +"]"+ "Returning to player");

            return false;
        }

        if (comm.equalsIgnoreCase("leave") && args.length == 0) {
            player.sendMessage("Error in command: /leave [name]");
            return false;
        }

        if (comm.equalsIgnoreCase("chwho") && args.length == 0) {
            player.sendMessage("Error in command: /chwho [channelname]");
            return false;
        }

        switch (comm) {
            case "chversion":
            {
            	  if(player.isOp())
            	  {
                      PluginDescriptionFile pdf = plugin.getDescription(); //Gets plugin.yml
                      //Gets the version
            		  player.sendMessage("MumbleChat Version: " +  pdf.getVersion());
            	  }
                return true;

            }
            case "join":
            case "channel": {
                if (args[0].length() > 0)
                {
            	       //	player.sendMessage("channel: " + args[0]);

                	ChatChannel chname;
                	
                  
                    
                	if(plugin.simplelclans)
                	{
                		
	                	if(args[0].equalsIgnoreCase("ally") || args[0].equalsIgnoreCase("clan"))
	                	{
	                		//CLAN CHAT  
	                		//They can hear it
	                		player.setMetadata("listenchannel." + args[0].toLowerCase(), new FixedMetadataValue(plugin, true));
	                		//They will talk on it when no commands are added. 
	                		player.setMetadata("currentchannel", new FixedMetadataValue(plugin, args[0].toLowerCase()));
	                		 player.sendMessage("Channel Set: " + args[0]);
	                		return true;
	                	}	                	
	                
                	}
                	
                	chname = cc.getChannelInfo(args[0]);
                	if(chname == null)
                	{
                		 player.sendMessage("Invalid channel name");
                		 return false;
                	}
                	
                	
                	
                	
                    //for (ChatChannel chname : cc.getChannelsInfo()) {
                        //player.sendMessage("channels: " + chname.getName()+ "=" + args[0]);
                    if (chname.hasPermission()) {
                        if (!player.isPermissionSet(chname.getPermission())) {
                            //Command pre processor may have added listener, need to turn it off.
                            player.setMetadata("listenchannel." + chname.getName(), new FixedMetadataValue(plugin, false));
                            //	plugin.getServer().getLogger().info("["+plugin.getName() +"]"+ " Permission missing: "+chname.getPermission());	
                            player.sendMessage(ChatColor.DARK_PURPLE + "You do not have permission for this channel");
                            return true;
                        }
                    }

                    //	plugin.getServer().getLogger().info("["+plugin.getName() +"]"+ "set Current Channel to "+chname.getName());	

                    if ((plugin.getMetadataString(player, "insertchannel", plugin).equalsIgnoreCase("NONE"))) {

                        //	plugin.getServer().getLogger().info("Change Sticky Channel to "+chname.getName());	
                        player.setMetadata("currentchannel", new FixedMetadataValue(plugin, chname.getName()));
                    }

                    //Stop chatting with another player..
                    String tellchat = plugin.getMetadataString(player, "MumbleChat.tell", plugin);
                    if (tellchat.length() > 0) {
                        player.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin, ""));
                        player.sendMessage("You are no longer in private conversation with " + tellchat);
                    }

                    player.setMetadata("listenchannel." + chname.getName(), new FixedMetadataValue(plugin, true));
                    String msg = "";

                    String format = ChatColor.valueOf(chname.getColor().toUpperCase()) + "[" + chname.getName() + "] ";
                    player.setMetadata("format", new FixedMetadataValue(plugin, format));

                    if (args.length > 1) 
                    {
                          //msg = ChatColor.valueOf(chname.getColor().toUpperCase()) + "["+chname.getName()+"]";
                            for (int x = 1; x < args.length; x++) 
                            {
                            	 if (args[x].length() > 0) //
                                       msg += " " + args[x];
                            }

                            player.chat(msg);
                            //	 plugin.getServer().getLogger().info("Called Chat... Commands!");
                            return true;
                       
                    }
                    else
                    {
                        player.sendMessage("Channel Set: " + format);
                        return true;
                    }
                }
                  
                   
               
            }
            break;

            case "leave": {
                int listenchannelcount = 0;

                if (args[0].length() > 0) {
                	

                	if(plugin.simplelclans)
                	{
                		
	                	if(args[0].equalsIgnoreCase("ally") || args[0].equalsIgnoreCase("clan"))
	                	{
	                		//CLAN CHAT  
	                		player.setMetadata("listenchannel." + args[0].toLowerCase(), new FixedMetadataValue(plugin, false));
	                		  
	                	    
	                	    if (!cc.getAutojoinList().isEmpty())
	                	    {
	                	    	player.setMetadata("listenchannel." + cc.getAutojoinList().get(0), new FixedMetadataValue(plugin, true));
	                	    	player.setMetadata("currentchannel", new FixedMetadataValue(plugin, cc.getAutojoinList().get(0)));
	                	    	player.sendMessage("Leaving "+args[0]+" Channel - Changing to: " +  cc.getAutojoinList().get(0));
	                	    }
	                	    else
	                	    {
	                	    	player.setMetadata("listenchannel." + cc.defaultChannel, new FixedMetadataValue(plugin, true));
	                	    	player.sendMessage("Leaving "+args[0]+" Channel - Changing to: " + cc.defaultChannel);
	                	    	player.setMetadata("currentchannel", new FixedMetadataValue(plugin, cc.defaultChannel));
	                	    }
	                	   
	                		return true;
	                	}	                   	                	
	                
                	}
                	
                	
                    for (ChatChannel chname : cc.getChannelsInfo()) {

                        if (chname.getName().equalsIgnoreCase(args[0]) || chname.getAlias().equalsIgnoreCase(args[0])) {

                            player.setMetadata("listenchannel." + chname.getName(), new FixedMetadataValue(plugin, false));

                            String format = ChatColor.valueOf(chname.getColor().toUpperCase()) + "[" + chname.getName() + "]";
                            player.sendMessage("Leaving channel: " + format);

                            //	plugin.getServer().getLogger().info("Leaving Channel:" +chname.getName());

                        }

                        //Figure out how many channels the player is listening too..
                        //they need at least one.
                        if (plugin.getMetadata(player, "listenchannel." + chname.getName(), plugin)) {
                            listenchannelcount++;
                        }
                    }

                    if (listenchannelcount == 0) {
                        player.setMetadata("listenchannel." + cc.defaultChannel, new FixedMetadataValue(plugin, true));
                        player.sendMessage("You need to be listening on at least one channel.");
                        player.sendMessage("Setting to listening to: " + cc.defaultChannel);
                    }
                    return true;

                } else {
                    player.sendMessage("Please include channel name");
                }
            }
            break;

            case "chlist": {
                player.sendMessage(ChatColor.GOLD + "Channel list:  alias");
                for (ChatChannel chname : cc.getChannelsInfo()) {

                    if (chname.hasPermission()) {
                        if (player.isPermissionSet(chname.getPermission())) {
                            player.sendMessage(ChatColor.valueOf(chname.getColor().toUpperCase()) + chname.getName() + " : " + chname.getAlias() + " - required permission");
                        }
                    } else {
                        player.sendMessage(ChatColor.valueOf(chname.getColor().toUpperCase()) + chname.getName() + " : " + chname.getAlias());
                    }
                }
                
                if(plugin.simplelclans)
            	{
                	player.sendMessage(ChatColor.AQUA+ "ally : Need to be in a clan");
                	player.sendMessage(ChatColor.AQUA+ "clan : Need to be in a clan");
                	
                	
            	}
                return true;
            }

            case "chwho": {
            	if(player.hasPermission(plugin.getChatChannelInfo().whopermissions))
            	{
	                String lstchan = "listenchannel.";
	                String playerlist = "";
	
	                //did they provide a channel
	                if (args[0].length() > 0) {
	
	                    //is it a valid channel
	                    ChatChannel cinfo = cc.getChannelInfo(args[0]);
	
	                    if (cinfo != null) {
	
	                        //Does this channel have permissions?
	                        if (cinfo.hasPermission()) {
	
	                            if (!player.hasPermission(cinfo.getPermission())) {
	                                //Command pre processor may have added listener, need to turn it off.
	                                player.setMetadata("listenchannel." + cinfo.getName(), new FixedMetadataValue(plugin, false));
	                                player.sendMessage(ChatColor.DARK_PURPLE + "You do not have permission to look at this channel");
	                                return true;
	                            }
	                        }
	
	                        lstchan += cinfo.getName();
	                        Player pl[] = plugin.getServer().getOnlinePlayers();
	
	                        plugin.logme(LOG_LEVELS.DEBUG, "ChWho","Count of player:" + pl.length);
	
	
	                        long linecount = plugin.getLineLength();
	                        for (Player p : pl) {
	                            plugin.logme(LOG_LEVELS.DEBUG, "ChWho:", "player:" + p.getDisplayName() + " " + p.isOnline());
	                            if (plugin.getMetadata(p, lstchan, plugin)) {
	
	
	                                if (cinfo.isDistance()) {
	                                    if (!isPlayerWithinDistance(player, p, cinfo.getDistance())) {
	                                        continue;
	                                    }
	                                }
	
	                                //Wrapping the text on the screen...
	                                if ((playerlist.length() + p.getName().length() > linecount)) {
	                                	 plugin.logme(LOG_LEVELS.DEBUG, "ChWho","linecount:" + linecount + "listlength:" + playerlist.length());
	                                    playerlist += "\n";
	                                    linecount = linecount + plugin.getLineLength();
	                                }
	
	
	                                if (!plugin.getMetadata(p, "MumbleMute." + cinfo.getName(), plugin)) {
	                                    playerlist += ChatColor.WHITE + p.getName();
	                                } else {
	                                    playerlist += ChatColor.RED + p.getName();
	                                }
	
	                                //Add commas between names
	                                playerlist += ChatColor.WHITE + ", ";
	                            }
	                        }
	
	                        //Remove the last trailing comma...
	                        if (playerlist.length() > 2) {
	                            playerlist = playerlist.substring(0, playerlist.length() - 2);
	                        }
	                        //show list to player who asked...
	                        player.sendMessage(ChatColor.AQUA + "Players in Channel : " + ChatColor.valueOf(cinfo.getColor().toUpperCase()) + cinfo.getName());
	                        player.sendMessage(playerlist);
	
	                        return true;
	                    } else {
	                        player.sendMessage("Please enter a valid channel name");
	                        return true;
	                    }
	
	                } else {
	                    player.sendMessage("/chwho [Channel]");
	                    return true;
	                }
            	}//Permissions
            	else
            	{
            		player.sendMessage("You do not have permissions to use this command.");
            		return true;
            	}
            } //end of chwho

            case "chhelp":
            {
                ShowHelpInfo(player);
                return true;
            }

            case "who":
            case "online":
            {
                ShowWhoInfo(player);
                return true;
            }
            case "chlookup":
            {
                if(plugin.CheckPermission(player,cc.lookuppermissions))
               // if(player.hasPermission(plugin.getChatChannelInfo().lookuppermissions))
                 {
                    if(!(args[0].length() > 1))
                    {
                      player.sendMessage("/chlookup [playername]");
                      return true;
                    }

                    GetPlayerInfo(player,args[0]);
                 }
                return true;
            }

        }

        return false;
    }

    //////////////////////////////////////////
    // Implementing my on /Who for Groups
    // Commandbook doesn't support Vault Groups
    // and the /who was bugged.
    //
    // However I did borrow just modify their function here
    // so thank you sk89q team.
    // https://github.com/sk89q/commandbook/blob/master/src/main/java/com/sk89q/commandbook/OnlineListComponent.java
    ///////////////////////////////////////////
    void ShowWhoInfo(Player player)
    {
        StringBuilder out = new StringBuilder();

        Player[] plist = plugin.getServer().getOnlinePlayers();

        if(plist.length==0){
            player.sendMessage("0 players are online.");
            return;
        }

        int onlineCount = plist.length;
        out.append(ChatColor.GRAY + "Online (");
        for (Player pl : plist) {
             if (!(player.canSee(pl))) {
                    onlineCount--;

            }
        }
        out.append(onlineCount);

        out.append("/");
        out.append(plugin.getServer().getMaxPlayers());

        out.append("): ");
        out.append(ChatColor.WHITE);

        //Groups
        Map<String, List<Player>> groups = new HashMap<String, List<Player>>();

        for (Player pl : plist) {

                if (!(player.canSee(pl))) {
                    continue;
                }

            String playerGroup = MumbleChat.permission.getPrimaryGroup( pl);

            String group = playerGroup.length() > 0 ? playerGroup : "Default";

            if (groups.containsKey(group)) {
                groups.get(group).add(pl);
            } else {
                List<Player> list = new ArrayList<Player>();
                list.add(pl);
                groups.put(group, list);
            }
        }

        for (Map.Entry<String, List<Player>> entry : groups.entrySet()) {
            out.append("\n");
            out.append(ChatColor.WHITE).append(entry.getKey());
            out.append(": ");

            // To keep track of commas
            boolean first = true;

            for (Player pl : entry.getValue()) {
                if (!first) {
                    out.append(", ");
                }
                out.append(pl.getDisplayName()).append(ChatColor.WHITE);
                first = false;
            }
        }

        String[] lines = out.toString().split("\n");

        for (String line : lines) {
            player.sendMessage(line);
        }

    }

    Boolean isPlayerWithinDistance(Player p1, Player p2, double Distance) {

        Double chDistance = Distance;
        Location locreceip;
        Location locsender = p1.getLocation();
        Location diff;


        //Player has listenChannel and its true...
        if (chDistance > (double) 0) {
            locreceip = p2.getLocation();
            if ((locreceip.getWorld() == p1.getWorld())) {
                diff = locreceip.subtract(locsender);
                //  mama.getServer().getLogger().info("Looking for distance!:" + " X:" + diff.getX()+" Y:" + diff.getY() + " Z:" + diff.getZ());

                if (Math.abs(diff.getX()) > chDistance || Math.abs(diff.getZ()) > chDistance) //diff.getY() > 100 && 
                {
                    return false;

                }
            } else { //Not on the same planet

                return false;
            }

        }

        return true;
    }


    void  ShowHelpInfo(Player p)
    {
        p.sendMessage(ChatColor.AQUA+"MumbleChat Quick Help");
        p.sendMessage(ChatColor.AQUA+"-- Commands --");
        p.sendMessage(ChatColor.AQUA+"/chlist    : "+ ChatColor.WHITE +" Provides a list of Available Channels");
        p.sendMessage(ChatColor.AQUA+"/join [channel]"+ ChatColor.WHITE +" Join a channel to listen and talk in it");
        p.sendMessage(ChatColor.AQUA+"/lev [channel]"+ ChatColor.WHITE +" Leave a channel if you no longer want to see it");
        p.sendMessage(ChatColor.AQUA+"/[channel] [Message]" + ChatColor.WHITE +" To talk in a channel.");


        if(p.hasPermission(plugin.getChatChannelInfo().whopermissions))
            p.sendMessage(ChatColor.AQUA+"/chwho [channel]"+ ChatColor.WHITE +" See who is in the channel (Red names are muted)");

        if(p.hasPermission(plugin.getChatChannelInfo().tellpermissions))
            p.sendMessage(ChatColor.AQUA+"/tell [player] [message]"+ ChatColor.WHITE +" Send a private message to a player");

        if(p.hasPermission(plugin.getChatChannelInfo().mutepermissions))
            p.sendMessage(ChatColor.AQUA + "/chmute [player] [channel]" + ChatColor.WHITE + " Mute player in the channel, so they can't talk");

        if(p.hasPermission(plugin.getChatChannelInfo().unmutepermissions))
            p.sendMessage(ChatColor.AQUA+"/chunmute [player] [channel]"+ ChatColor.WHITE +" Unmute a player in the channel, so they may talk again");

    }

    void GetPlayerInfo(Player requestor,String playerName)
    {
        StringBuilder out = new StringBuilder();

        FileConfiguration customConfig = null;

        if(plugin.CheckPermission(requestor,cc.lookuppermissions))
        {
             requestor.sendMessage(ChatColor.AQUA+"Player: "+ChatColor.GOLD + playerName );

            customConfig = plugin.playerdata.getCustomConfig();

            ConfigurationSection cs = customConfig.getConfigurationSection("players." + (playerName.toLowerCase()));

            if (cs != null) {

                String muteChannels = cs.getString("mutes", "");

                //If player is not Online get mutes from Config Data....
                Player p = plugin.getServer().getPlayer(playerName) ;

                out.append(ChatColor.AQUA+" Mutes: ");
                if( p  == null)
                {

                    if (muteChannels.length() > 0) {
                        out.append(ChatColor.RED + muteChannels+ "\n");
                    }
                }
                else
                {
                    //get Mutes from MetaData
                     for(ChatChannel ci: cc.getChannelsInfo())
                     {
                        if(plugin.getMetadata(p, "MumbleMute." + ci.getName(), plugin))
                        {
                            out.append(ChatColor.RED + ci.getName()+ ",");
                        }
                     }
                    out.append(ChatColor.RED + "\n");
                }


                if(plugin.CheckPermission(requestor,cc.lookupdatepermissions))
                {
                    String strDate = cs.getString("date","");
                    out.append(ChatColor.AQUA+"Last Seen: "+ChatColor.GOLD + strDate);
                }

                requestor.sendMessage(out.toString());
            }
            else
            {
                requestor.sendMessage("Player does not exist in the data.");
            }
        }
       else
        {
            requestor.sendMessage("You do not have permission for this command.");
        }

        //Open Player Config

        //Find PLayer Info

        //Return Mutes

        //return when last online.


    }


}
