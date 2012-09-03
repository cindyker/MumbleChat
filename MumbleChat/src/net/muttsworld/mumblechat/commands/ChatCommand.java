package net.muttsworld.mumblechat.commands;

import java.util.List;

import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;


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

    public boolean getMetadata(Player player, String key, MumbleChat plugin) {
        List<MetadataValue> values = player.getMetadata(key);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())) {
                return value.asBoolean(); //value();
            }
        }
        return false;
    }

    public String getMetadataString(Player player, String key, MumbleChat plugin) {
        List<MetadataValue> values = player.getMetadata(key);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())) {
                return value.asString(); //value();
            }
        }
        return "";
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        String brformat ="";
       
        //Alert Alert Alert
        	if (e.getMessage().toLowerCase().startsWith("/" + cc.getBroadcastCommand() + " "))
        	{
        		if( cc.isBroadcastAvailable())
        		{
	        		//Implement Alerts here... ?
        			
	        		if(cc.broadcastPermissions.equalsIgnoreCase("none")
	        				||
	        			player.hasPermission(cc.broadcastPermissions))
	        		{
	        		
		        		 String newMsg = e.getMessage();
		        		 
		        		 //remove command
		        		 newMsg = newMsg.replaceFirst("/" + cc.getBroadcastCommand() + " ", "");
		    	        
		        		 if (cc.broadcastPlayer){
		        			 brformat =   ChatColor.valueOf(cc.broadcastColor.toUpperCase())+ " <" +player.getDisplayName()+ ">"+ cc.broadcastDisplayTag +" " + newMsg;
		        			 
		        		 }
		        		 else
		        		      brformat =   ChatColor.valueOf(cc.broadcastColor.toUpperCase())+  cc.broadcastDisplayTag +" " + newMsg;
		        		 
		        		 plugin.getServer().broadcastMessage(brformat);
		        		 //put in Chat listener
		        		 //player.chat(newMsg);
		        		        		 
		        		 //cancel command
		        		 e.setCancelled(true);
		        		 return;
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
                
                String tellchat = getMetadataString(player,"MumbleChat.tell",plugin);
                if(tellchat.length()>0)
                {
                	player.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin, ""));
                	player.sendMessage("You are no longer in private conversation with "+tellchat);
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
            
        	player.sendMessage("Error in command: /"+comm+" [name] [msg]");
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
        	case "join":
            case "channel": {
                if (args[0].length() > 0) {
                    //	player.sendMessage("channel: " + args[0]);

                    for (ChatChannel chname : cc.getChannelsInfo()) {
                        //player.sendMessage("channels: " + chname.getName()+ "=" + args[0]);

                        if (chname.getName().equalsIgnoreCase(args[0]) || chname.getAlias().equalsIgnoreCase(args[0])) {
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

                            if ((getMetadataString(player, "insertchannel", plugin).equalsIgnoreCase("NONE"))) {

                                //	plugin.getServer().getLogger().info("Change Sticky Channel to "+chname.getName());	
                                player.setMetadata("currentchannel", new FixedMetadataValue(plugin, chname.getName()));
                            }

                            //Stop chatting with another player..
                            String tellchat = getMetadataString(player,"MumbleChat.tell",plugin);
                            if(tellchat.length()>0)
                            {
                            	player.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin, ""));
                            	player.sendMessage("You are no longer in private conversation with "+tellchat);
                            }
                        	
                            player.setMetadata("listenchannel." + chname.getName(), new FixedMetadataValue(plugin, true));
                            String msg = "";

                            String format = ChatColor.valueOf(chname.getColor().toUpperCase()) + "[" + chname.getName() + "] ";
                            player.setMetadata("format", new FixedMetadataValue(plugin, format));

                            if (args.length > 1) {
                                if (args[1].length() > 0) {
                                    //msg = ChatColor.valueOf(chname.getColor().toUpperCase()) + "["+chname.getName()+"]";
                                    for (int x = 1; x < args.length; x++) {
                                        msg += " " + args[x];
                                    }

                                    player.chat(msg);
                                    //	 plugin.getServer().getLogger().info("Called Chat... Commands!");
                                    return true;
                                }
                            } else {
                                player.sendMessage("Channel Set: " + format);
                                return true;
                            }
                        }


                    }

                    player.sendMessage("Invalid channel name");
                }

            }
            break;

            case "leave": {
            	
            	int listenchannelcount = 0;
            	
                if (args[0].length() > 0) {
                    for (ChatChannel chname : cc.getChannelsInfo()) {
                    	                   	
                        if (chname.getName().equalsIgnoreCase(args[0]) || chname.getAlias().equalsIgnoreCase(args[0])) {
                        	
                            player.setMetadata("listenchannel." + chname.getName(), new FixedMetadataValue(plugin, false));

                            String format = ChatColor.valueOf(chname.getColor().toUpperCase()) + "[" + chname.getName() + "]";
                            player.sendMessage("Leaving channel: " + format);

                            //	plugin.getServer().getLogger().info("Leaving Channel:" +chname.getName());
                            
                        }
                        
                        //Figure out how many channels the player is listening too..
                        	//they need at least one.
                    	if(getMetadata(player,"listenchannel."+chname.getName(),plugin) == true)
                        		listenchannelcount++;
                    }
                    
                    if(listenchannelcount == 0)
                    {
                    	player.setMetadata("listenchannel."+cc.defaultChannel,new FixedMetadataValue(plugin, true));
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
                return true;

            }

            case "chwho": {
            	
            	String lstchan = "listenchannel.";
              	String playerlist = "";
            	
              	//did they provide a channel
                if (args[0].length() > 0) {
                	
                	//is it a valid channel
                	ChatChannel cinfo = cc.getChannelInfo(args[0]);
                	
                	if (cinfo != null)
                	{
                		if(cinfo.isDistance())
                		{
                			player.sendMessage("Sorry no who information for " + args[0] +" chat right now...");
                			return true;
                		}
	                	lstchan += cinfo.getName();
		               	Player pl[] = plugin.getServer().getOnlinePlayers();
		               	
		               	plugin.getServer().getLogger().info("Count of player:" +pl.length);
		            	
		            
		               	long linecount = 30;
		               	for(Player p:pl)
		            	{
		               	   	plugin.getServer().getLogger().info("player:"+p.getDisplayName() +" " +p.isOnline());
		            		if( getMetadata(p,lstchan,  plugin))
		            		{
		            		//Wrapping the text on the screen...	
		            		  if((playerlist.length()+p.getName().length() > linecount ))
		            		  {
		            			 plugin.getServer().getLogger().info("linecount:"+ linecount + "listlength:" + playerlist.length());
		            			  playerlist += "\n";
		            			  linecount= linecount*2;
		            		  }
		            		  
		            		  		            		  
		            		  if( ! getMetadata(p,"MumbleMute."+cinfo.getName(),plugin))
		            		  {
		            			   playerlist += ChatColor.WHITE+p.getName() ;
		            		  }
		            		  else
		            		  {
		            			    playerlist += ChatColor.RED+p.getName() ;
		            		  }
		            			
		            		  //Add commas between names
		            		  playerlist += ChatColor.WHITE+", ";
		            		  
		            		}
		            		
		            		
		            		
		            	}
		               	               
		            	//Remove the last trailing comma...
		            	playerlist = playerlist.substring(0, playerlist.length()-2);
		            	//show list to player who asked...
		            	player.sendMessage(ChatColor.AQUA+ "Players in Channel : " +ChatColor.valueOf(cinfo.getColor().toUpperCase())+ cinfo.getName());
		            	player.sendMessage(playerlist);

		            	
	            	    return true;
	                } 
                	else
                	{
                		player.sendMessage("Please enter a valid channel name");
                		return true;
                	}
	                  
                
                }
                else
                {
                	player.sendMessage("/chwho [Channel]");
                	return true;
                }
                
            	
            } //end of chwho
            


        }




        return false;
    }
}
