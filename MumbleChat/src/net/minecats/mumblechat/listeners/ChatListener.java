package net.minecats.mumblechat.listeners;

import java.util.Calendar;
import java.util.logging.Level;

//import net.sacredlabyrinth.phaed.simpleclans.Clan;
//import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import com.p000ison.dev.simpleclans2.api.clan.Clan;
import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.StringTokenizer;

import net.minecats.mumblechat.ChatChannel;
import net.minecats.mumblechat.ChatChannelInfo;
import net.minecats.mumblechat.MumbleChat;
import net.minecats.mumblechat.MumbleChat.LOG_LEVELS;

public class ChatListener implements Listener {
    MumbleChat plugin;
    // String[] Filters;
    List<String> filters;
    ChatChannelInfo cc;

    @SuppressWarnings("unchecked")
    public ChatListener(MumbleChat _plugin) {
        plugin = _plugin;
        //  int Count = 10;

        filters = (List<String>) plugin.getConfig().getList("filters");

    }

    @SuppressWarnings("unchecked")
    public ChatListener(MumbleChat _plugin, ChatChannelInfo _cc) {
        plugin = _plugin;
        cc = _cc;
        filters = (List<String>) plugin.getConfig().getList("filters");


    }


   void SendTell(Player p, String tellPlayer,String message)
    {
        if(p.hasPermission(plugin.getChatChannelInfo().tellpermissions))
        {

            //plugin.getServer().getLogger().info("tell to player" + tellPlayer);
            Player tp = plugin.getServer().getPlayer(tellPlayer);
            if (tp == null) {
                p.sendMessage(tellPlayer + " is not available");
                p.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin, ""));

            } else {
                //Check for Ignores....
                String playerignorelist = plugin.getMetadataString(tp, "MumbleChat.ignore", plugin);
                if (playerignorelist.length() > 0) {
                    String curplayer = "";
                    StringTokenizer st = new StringTokenizer(playerignorelist, ",");
                    while (st.hasMoreTokens()) {

                        curplayer = st.nextToken();
                        if (curplayer.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(ChatColor.YELLOW + tellPlayer + " is currently ignoring your tells.");

                            return;
                        }
                    }

                }
                String filtered = cc.FilterChat(message);
                String msg = p.getDisplayName() + " tells you: " + ChatColor.valueOf(cc.tellColor.toUpperCase()) + filtered;
                tp.sendMessage(msg);
                p.sendMessage("You tell " + tellPlayer + ": " + ChatColor.valueOf(cc.tellColor.toUpperCase()) + filtered);
                plugin.logme(LOG_LEVELS.INFO,"AsyncChat:Tell",p.getDisplayName() + " tells " + tp.getName() +": "+ message) ;

                return;
            }

            return;
        }
        else
        {
            p.sendMessage(ChatColor.YELLOW +"You do not have permission to send tells.");

            return;
        }

    }




    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        // boolean globalmsg = false;


        String evMessage;

        if (event.isCancelled()) {
            plugin.logme(LOG_LEVELS.INFO,"AsyncChat:Cancelled",event.getMessage() );
            return;
        }

        Player p = event.getPlayer();


        ////////////////////////////////////////////////////////////////////////
        // if sticky tell this becomes quick...
        //
        String tellPlayer = plugin.getMetadataString(p, "MumbleChat.tell", plugin);

        if (tellPlayer.length() > 0)
        {
            SendTell(p,tellPlayer,event.getMessage());
            event.setCancelled(true);
            return;
         }
         /////////////////////////////////////////////////////////////////////



        String pFormatted = "";

        cc.SetPlayerDisplayName(p);  //This Should get the player's tag Each time they talk.
        pFormatted = plugin.getMetadataString(p, "chatnameformat", plugin);

        evMessage = event.getMessage();


        ////// SPAM FILTER //////
        // Disable is maxSpamScore is 0
        // SpamScore
        // SpamTime
        // TempMute
        // TempExpire
        // LastMessage
        //////////////////////////
        if(plugin.getChatChannelInfo().maxSpamScore > 0)
        {
            if(p.hasMetadata("MumbleChat.LastMessage"))
            {
                String lastmessage = plugin.getMetadataString(p,"MumbleChat.LastMessage",plugin);

                if(p.hasMetadata("MumbleChat.SpamTime"))
                {
                    String Time = plugin.getMetadataString(p,"MumbleChat.SpamTime",plugin);
                    long lTime = Long.parseLong(Time);
                    Calendar expireca = Calendar.getInstance();
                    expireca.setTimeInMillis(lTime);

                    Calendar rightnow = Calendar.getInstance();

                    plugin.getLogger().info("Has SpamTime - until " + lTime + " Currently : " + rightnow.getTimeInMillis());

                    if(lTime < rightnow.getTimeInMillis())
                    {
                        p.removeMetadata("MumbleChat.SpamTime",plugin);
                    }
                    else
                    {
                        plugin.getLogger().info("Player is running from the guards, cancelling teleport!");
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "You can not escape the guards so easily!");
                        event.setCancelled(true);
                    }
                }


                if(evMessage.compareToIgnoreCase(lastmessage)==0)
                {
                    //DAT SPAM...
                    if(p.hasMetadata("MumbleChat.SpamScore"))
                    {
                        //how old is the SCORE?

                        int spamscore = Integer.parseInt(plugin.getMetadataString(p, "MumbleChat.SpamScore", plugin));
                        if(spamscore > plugin.getChatChannelInfo().maxSpamScore)
                        {
                            //Mute this player

                        }
                    }
                }

            }
        }


        Boolean filterthis = true;
        String curChannel = "";

        //Check for Quick chat vs Sticky Chat
        String insertchannel = plugin.getMetadataString(p, "insertchannel", plugin);
        if (!p.hasMetadata("insertchannel")) {
            insertchannel = "NONE";
        }

        if ((insertchannel.equalsIgnoreCase("NONE"))) {
            //String curChannel = p.getMetadata("currentchannel").get(0).asString();
            //	plugin.getServer().getLogger().info("Talking Sticky");
            curChannel = plugin.getMetadataString(p, "currentchannel", plugin);
        } else {
            curChannel = insertchannel;
            p.setMetadata("insertchannel", new FixedMetadataValue(plugin, "NONE"));

        }

        if (curChannel.length() == 0) {
            // Talking local?
            p.sendMessage("please choose a channel! /ch [channelname] - /chlist for channel list");
            event.setCancelled(true);
            return;

        }




        String listenChannel = "listenchannel." + curChannel;
        // plugin.getServer().getLogger().info("Who's listening on:" +listenChannel);

        //---------------SIMPLE CLANS CHAT ------------------------------
        if(plugin.simplelclans)
        {
	        if(curChannel.equalsIgnoreCase("ally")||curChannel.equals("clan"))
	        {
	        	ClanChat(curChannel,listenChannel,p,event.getRecipients().toArray(new Player[0]),cc.FilterChat(evMessage));
	        	event.setCancelled(true);
	        	return;
	        }
        }
              
        
        //if they are not muted and they want to talk on the channel...
        //they need to listen on the channel.
        if (plugin.getMetadata(p, "MumbleMute." + curChannel, plugin)) {
            p.sendMessage(ChatColor.DARK_PURPLE + "You are muted in this channel: " + curChannel);
            event.setCancelled(true);
            return;
        } else {
            if(cc.getChannelInfo(curChannel).hasPermission())
            {
                if(p.hasPermission(cc.getChannelInfo(curChannel).getPermission()))
                {
                    plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent","Set listen channel");
                     p.setMetadata(listenChannel, new FixedMetadataValue(plugin, true));
                }
                else
                {
                    p.sendMessage(ChatColor.DARK_PURPLE + "You don't have permissions for this channel..");
                    event.setCancelled(true);
                    return;
                }
            }
            else
            {

                p.setMetadata(listenChannel, new FixedMetadataValue(plugin, true));
            }
        }

        Double chDistance = (double) 0;

        String Channelformat;
        String ChannelColor = "WHITE";
        Channelformat = plugin.getMetadataString(p, "format", plugin);

        ////////////////////////////////////////////
        //Get Channel Information
        ////////////////////////////////////////////
        for (ChatChannel ci : cc.getChannelsInfo()) {
            if (curChannel.equalsIgnoreCase(ci.getName())) {
            	
            	ChannelColor = ci.getColor().toUpperCase();
            	
                if (ci.hasPermission()) {
                    //if (plugin.getMetadata(p, ci.getPermission(), plugin) == false) {
                    if(!p.isPermissionSet(ci.getPermission()))
                    {
                        p.sendMessage(ChatColor.DARK_PURPLE + "You don't have permissions for this channel..");
                        event.setCancelled(true);
                        return;
                    }
                }
                if (ci.isDistance()) {
                    chDistance = ci.getDistance();
                }

                if ((insertchannel.equalsIgnoreCase("NONE")) || insertchannel.length() == 0) {

                    Channelformat = ChatColor.valueOf(ci.getColor().toUpperCase()) + "[" + curChannel + "] ";
                }
                filterthis = ci.isFiltered();
            }
        }

        /////////////////////////////////////////////////////
        //Apply the Filter is required
        if (filterthis) {
            evMessage = cc.FilterChat(evMessage);

        }

        //Player has listenChannel and its true...
        //Getting Speakers Location..
        Location locreceip;
        Location locsender = p.getLocation();
        Location diff;

        Player[] pl = event.getRecipients().toArray(new Player[0]);
        //Check each player to see who should receive message...
        for (Player rp : pl) {
            //mama.getServer().getLogger().info("["+mama.getName()+"] "+listenChannel);

            //Are they Listening?
            if (!(rp.hasMetadata(listenChannel))) {

                event.getRecipients().remove(rp);
                continue;
            }
            else{
                if ((!plugin.getMetadata(rp, listenChannel, plugin))) {
                    plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent",rp.getPlayerListName() + " Removed from Channel "+listenChannel +" -  Not Listening");
                    event.getRecipients().remove(rp);
                    continue;
            }
                
                /// Prevent Easedroppers...
                //////////////////////////////////////////////////////////////////////////////
                 ChatChannel cci = cc.getChannelInfo(curChannel);
                 if(cci == null)
                 {
                	  p.sendMessage("UKNOWN CHANNEL " + curChannel + " Please choose a channel! /ch [channelname] - /chlist for channel list");
                      event.setCancelled(true);
                      return;
                 }
                if( cci.hasPermission() )
                {
                	//if (plugin.getMetadata(p, cci.getPermission(), plugin) == false) {
                    if(!rp.isPermissionSet(cci.getPermission()))
                    {
                        plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent",rp.getPlayerListName() + " Removed from Channel "+cci.getAlias() +" -  No Permissions");
                		 rp.removeMetadata("listenchannel." + listenChannel,plugin);
                		 event.getRecipients().remove(rp);

                         //If this is the Sender, then they need to be kicked out and told.
                         if(rp.getPlayerListName().compareToIgnoreCase(p.getPlayerListName())==0)
                         {
                             p.sendMessage(ChatColor.DARK_PURPLE + "You don't have permissions for this channel...");
                         }
                		 continue;
                	}
                }
                ///////////////////////////////////////////////////////////////////////////////////
            }

            //Distance Channel. Check to make sure on the Same World and inside the Box.
            if (chDistance > (double) 0) {
                locreceip = rp.getLocation();
                if ((locreceip.getWorld() == p.getWorld())) {
                    diff = locreceip.subtract(locsender);
                    //  mama.getServer().getLogger().info("Looking for distance!:" + " X:" + diff.getX()+" Y:" + diff.getY() + " Z:" + diff.getZ());

                    if (Math.abs(diff.getX()) > chDistance || Math.abs(diff.getZ()) > chDistance) //diff.getY() > 100 && 
                    {
                        event.getRecipients().remove(rp);

                    }
                } else { //Not on the same planet

                    event.getRecipients().remove(rp);
                }

            }

        }

          try {
              if (p.isPermissionSet(plugin.getChatChannelInfo().colorpermissions)) {
                  //Rainbow Colored Skittles here... :)
                  evMessage = cc.FormatString(evMessage);
              }

               event.setMessage(evMessage);



                  if(cc.bChannelInfront)
                      event.setFormat(Channelformat + pFormatted  + ChatColor.valueOf(ChannelColor)+ ": " + "%s"  ); //+" ");
                  else
                      event.setFormat(pFormatted + " " + Channelformat + "%s"); //+" ");


                  plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent", String.format("Format:%s::%s:%s",pFormatted, Channelformat,evMessage));
              } catch (IllegalFormatException ex) {
                  plugin.getLogger().log(Level.INFO, "Message Format issue: {0}:{1}", new Object[]{ex.getMessage(), evMessage});
                  event.setMessage(Channelformat + evMessage);
              }

              event.setMessage( evMessage);

        if (event.getRecipients().size() == 1) {
//             String fullMessage = String.format(event.getFormat(),p.getDisplayName(),evMessage);
//            p.sendMessage(fullMessage);
            plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent", "In One! " + event.getMessage());
            if(event.isCancelled())
            {
                plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent", "One and Event Cancelled...");
                String fullMessage = String.format(event.getFormat(),p.getDisplayName(),evMessage);
                p.sendMessage(fullMessage);
            }
            plugin.logme(LOG_LEVELS.DEBUG, "AsyncChatEvent", "ONE PERSON: "+ evMessage);
            p.sendMessage(ChatColor.GOLD + "There is no one in this channel to hear you.");
        }

        if(event.getRecipients().size() == 0 )
        {
            //somehow I remove EVERYONE!
            String fullMessage = String.format(event.getFormat(),p.getDisplayName(),evMessage);
            p.sendMessage(fullMessage);
            plugin.logme(LOG_LEVELS.INFO, "AsyncChatEvent", "Format: "+ fullMessage);
            p.sendMessage(ChatColor.GOLD + "There is no one in this channel to hear you.");
        }


    }

    public void ClanChat(String currentChannel, String listenChannel,Player pl,Player[] receivers,String Message)
    {
    	// String format;
     //    Player player = event.getPlayer();
    	
         ClanPlayer clanPlayer = plugin.sc.getClanPlayerManager().getClanPlayer(pl);
         if(clanPlayer!=null)
         {
	         String tag = clanPlayer.getClan().getTag();
	         
	         for(Player pr: receivers)
	         {
	        	 	
	             if (!(pr.hasMetadata(listenChannel))) {
	                 continue;
	
	             } else {
	                 if ((!plugin.getMetadata(pr, listenChannel, plugin))) {
	                     continue;
	                 }
	             }
	
	        	 if(pr.hasMetadata(listenChannel)) //If they aren't listening to clan chat, skip them.
	
	        	 {
		        	 ClanPlayer clanreceiver = plugin.sc.getClanPlayerManager().getClanPlayer(pr);
		        	 if(clanreceiver==null)
		        	 {
		        		 //player is no longer in a clan.
		        		 continue;
		        	 }
		        //	 if(currentChannel.equalsIgnoreCase("clan"))
		        	// {
		        		
		        		 if(clanreceiver.getClan().getTag().equalsIgnoreCase(tag))
		        		 {
		        			pr.sendMessage(ChatColor.AQUA+"["+currentChannel+"]"+pl.getDisplayName()+": "+ChatColor.GRAY+Message); 
		        		 }
		        //	 }
		        	 if(currentChannel.equalsIgnoreCase("ally"))
		        	 {
		        		// plugin.getLogger().log(Level.INFO, "Ally Chat");
		        		 for(Clan A:clanreceiver.getClan().getAllies())
		        		 {
                          //  Clan Ally = plugin.sc.getClanManager().getClan(A);
                            if(tag.equalsIgnoreCase(A.getTag()))
		        			{
		        				 //plugin.getLogger().log(Level..DEBUG, "Ally Found them");
		        				 pr.sendMessage(ChatColor.AQUA+"["+currentChannel+"]"+pl.getDisplayName()+": "+ChatColor.GRAY+Message); 
		        				 break;
		        			}
		        		 }
		        		         		 
		        	 }
	        	 }
	        	        	 
	         }
         }
         else
         {
        	 pl.sendMessage("You are not in a clan");
        		//CLAN CHAT  
     	
     	    
     	    if (!cc.getAutojoinList().isEmpty())
     	    {
     	    	pl.setMetadata("listenchannel." + cc.getAutojoinList().get(0), new FixedMetadataValue(plugin, true));
     	    	pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, cc.getAutojoinList().get(0)));
     	    	pl.sendMessage("Changing to: " +  cc.getAutojoinList().get(0));
     	    }
     	    else
     	    {
     	    	pl.setMetadata("listenchannel." + cc.defaultChannel, new FixedMetadataValue(plugin, true));
     	    	pl.sendMessage("Changing to: " + cc.defaultChannel);
     	    	pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, cc.defaultChannel));
     	    }
        	 
         }


    	
    }
    
    
    
    public HandlerList getHandlers() {
        // TODO Auto-generated method stub
        return null;
    }
}
