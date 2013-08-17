package net.muttsworld.mumblechat.commands;

import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.FixedMetadataValue;

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

    @EventHandler
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
                    	 if (cc.usePrefix == true) {
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
            		  player.sendMessage("MumbleChat Version:" + plugin.getDescription());
            	  }
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
                        if (plugin.getMetadata(player, "listenchannel." + chname.getName(), plugin) == true) {
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
            	if(player.hasPermission("mumblechat.who"))
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
	
	                            if (!player.isPermissionSet(cinfo.getPermission())) {
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
        }

        return false;
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
}
