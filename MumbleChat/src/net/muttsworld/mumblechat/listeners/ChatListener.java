package net.muttsworld.mumblechat.listeners;

//import org.bukkit.ChatColor;
//import org.bukkit.Location;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

//import ru.tehkode.permissions.PermissionUser;
//import ru.tehkode.permissions.bukkit.PermissionsEx;
//import org.bukkit.permissions.PermissionAttachmentInfo;

//import java.lang.Math;
import java.util.List;
import java.util.StringTokenizer;
import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;

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

    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        // boolean globalmsg = false;


        String evMessage;
        if (event.isCancelled()) {
            return;
        }

        Player p = event.getPlayer();

        // this gets the player's prefix and suffix from PEx.. but with every chat
        // with the async chat, I am not sure if calling permissionsEx here is a good thing.
        // Next thing to look at is to move this to Login with a metatag added with this info for
        // use here. 
        //PermissionUser user = PermissionsEx.getUser(p);
        String pFormatted =""; //= cc.FormatPlayerName(user.getPrefix(),p.getPlayerListName(),user.getSuffix());
        if (cc.usePexPrefix == true )
        	pFormatted = getMetadataString(p,"chatnameformat",plugin);
       // p.setDisplayName(pFormatted);
         
        
        evMessage = event.getMessage();

        //mama.getServer().getLogger().info("Filter ok?");


        Location locreceip;
        Location locsender = p.getLocation();
        Location diff;
        String tempformat;
        Boolean filterthis = true;

        tempformat = getMetadataString(p, "format", plugin);

        String curChannel = "";

        //Check for Quick chat vs Sticky Chat
        String insertchannel = getMetadataString(p, "insertchannel", plugin);
        if (!p.hasMetadata("insertchannel")) {
            insertchannel = "NONE";
        }
        if ((insertchannel.equalsIgnoreCase("NONE"))) {
           //String curChannel = p.getMetadata("currentchannel").get(0).asString();
           // 	plugin.getServer().getLogger().info("Talking Sticky");
            curChannel = getMetadataString(p, "currentchannel", plugin);
        } else {
            //		mama.getServer().getLogger().info("Temp Talk");
            curChannel = insertchannel;
            p.setMetadata("insertchannel", new FixedMetadataValue(plugin, "NONE"));
            
        }

        if (curChannel.length() == 0) {
            // Talking local?
            p.sendMessage("please choose a channel! /ch [channelname]");
            return;

        }




        String listenChannel = "listenchannel." + curChannel;
        //mama.getServer().getLogger().info("Who's listening on:" +listenChannel);

        //if they are not muted and they want to talk on the channel...
        //they need to listen on the channel.
        if (getMetadata(p, "durpMute." + curChannel, plugin) == true) {
            p.sendMessage(ChatColor.DARK_PURPLE + "You are muted in this channel: " + curChannel);
            event.setCancelled(true);
            return;
        }
        else
        {
        	 p.setMetadata(listenChannel, new FixedMetadataValue(plugin, true));
        }

        Double chDistance = (double) 0;

        //Get Distance from Channel...
        for (ChatChannel ci : cc.getChannelsInfo()) {
            if (curChannel.equalsIgnoreCase(ci.getName())) {
                if (ci.hasPermission()) {
                    if (getMetadata(p, ci.getPermission(), plugin) == false) {
                        //	 mama.getServer().getLogger().info(ci.getPermission()+" <== you don't have this");
                        p.sendMessage(ChatColor.DARK_PURPLE + "You don't have permissions for this channel.");
                        event.setCancelled(true);
                        return;
                    }
                }
                if (ci.isDistance()) {
                    chDistance = ci.getDistance();
                }

                if ((insertchannel.equalsIgnoreCase("NONE")) || insertchannel.length() == 0) {

                    tempformat = ChatColor.valueOf(ci.getColor().toUpperCase()) + "[" + curChannel + "] ";
                }
                filterthis = ci.isFiltered();
            }
        }

        //mama.getServer().getLogger().info("chatting on:" + curChannel);

        /////////////////////////////////////////////////////
        //Apply the Filter is required
        int t = 0;
        if (filterthis) {
            for (String s : filters) {
                t = 0;
                String[] pparse = new String[2];
                pparse[0] = " ";
                pparse[1] = " ";
                StringTokenizer st = new StringTokenizer(s, ",");
                while (st.hasMoreTokens()) {
                    //mama.getServer().getLogger().info("chatting: " + st.toString() + " i:"+i);
                    if (t < 2) {
                        pparse[t++] = st.nextToken();
                    }
                }
                //	mama.getServer().getLogger().info(p[0]+":"+p[1] + " i:"+i);
                //String tempMessage = evMessage.toLowerCase()
                //StringTokenizer fe = new StringTokenizer()

                evMessage = evMessage.replaceAll("(?i)" + pparse[0], pparse[1]);

            }
        }

        //Add channel info
        evMessage = tempformat + evMessage;


        Player[] pl = event.getRecipients().toArray(new Player[0]);
        //Check each player to see who should receive message...
        for (Player rp : pl) {
            //mama.getServer().getLogger().info("["+mama.getName()+"] "+listenChannel);

            if (!(rp.hasMetadata(listenChannel))) {
                event.getRecipients().remove(rp);

            } else {
                if ((getMetadata(rp, listenChannel, plugin) == false)) {
                    event.getRecipients().remove(rp);
                }
            }

            //Player has listenChannel and its true...
            if (chDistance > (double) 0) {
                locreceip = rp.getLocation();
                if ((locreceip.getWorld() == p.getWorld())) {
                    diff = locreceip.subtract(locsender);
                    //  mama.getServer().getLogger().info("Looking for distance!:" + " X:" + diff.getX()+" Y:" + diff.getY() + " Z:" + diff.getZ());

                    if (Math.abs(diff.getX()) > chDistance || Math.abs(diff.getZ()) > chDistance) //diff.getY() > 100 && 
                    {
                        event.getRecipients().remove(rp);

                    }
                } else //Not on the same planet
                {
                    event.getRecipients().remove(rp);
                }

            }

        }

        if (event.getRecipients().size() == 1) {
            p.sendMessage(ChatColor.GOLD + "No one is listening to you");
        }

        if(cc.usePexPrefix == true)
        	event.setFormat(pFormatted+" "+evMessage);
        else
           event.setMessage(evMessage);
        // p.setMetadata("durpGlobal",new FixedMetadataValue(mama,false));
        return;

    }

    public HandlerList getHandlers() {
        // TODO Auto-generated method stub
        return null;
    }
}
