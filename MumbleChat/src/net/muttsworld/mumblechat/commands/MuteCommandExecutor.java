package net.muttsworld.mumblechat.commands;

import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class MuteCommandExecutor implements CommandExecutor {

	private MumbleChat plugin;
	@SuppressWarnings("unused")
	private String name;
	private ChatChannelInfo cc;
	
	public MuteCommandExecutor(MumbleChat _plugin, ChatChannelInfo _cc) {
		this.plugin = _plugin;
		name= plugin.getName();
		cc = _cc;
	}
 
		

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		
		//if not a player... we are done.
		if (!(sender instanceof Player)) return false; 
		
		Player admin = null;
		if ((sender instanceof Player))
		 {
		   admin = (Player)sender;
		 }

        if(admin==null)
        {
            //This shouldn't happen, but if it does we need to bail.
            return false;
        }




// Future Development!
// Want to be able force in and force out
		if(cmd.getName().equalsIgnoreCase("chforce"))
		{


            if(plugin.CheckPermission((Player)admin,cc.forcepermissions))
            {


					if (args.length != 3)
					{
                         admin.sendMessage("/chforce [out/in] player channel");
						 return true;
					}

                    Player player = plugin.getServer().getPlayer(args [1]);

                    if(player != null){
                        String command = args[0];
                        String channelName = args[2];
                        ChatChannel ci =  cc.getChannelInfo(channelName);
                        if(ci==null)
                        {
                            admin.sendMessage("Can't Force to Invalid Channel: "+ channelName);
                            return true;
                        }

                        ///FORCE IN......
                        if(command.compareToIgnoreCase("in")==0)
                        {

                            player.setMetadata("listenchannel."+ci.getName(),new FixedMetadataValue(plugin,true));
                            player.setMetadata("currentchannel",new FixedMetadataValue(plugin,ci.getName()));

                            admin.sendMessage("Forcing player "+player.getPlayerListName()+" into "+ ci.getName());
                            player.sendMessage("You have been added to "+ ci.getName());
                            return true;

                        }

                        //FORCE OUT......
                        if(command.compareToIgnoreCase("out")==0)
                        {
                            int listenchannelcount = 0;

                            if (channelName.length() > 0) {


                                if(plugin.simplelclans)
                                {

                                    if(channelName.equalsIgnoreCase("ally") || channelName.equalsIgnoreCase("clan"))
                                    {
                                        //CLAN CHAT
                                        player.setMetadata("listenchannel." + channelName.toLowerCase(), new FixedMetadataValue(plugin, false));


                                        if (!cc.getAutojoinList().isEmpty())
                                        {
                                            player.setMetadata("listenchannel." + cc.getAutojoinList().get(0), new FixedMetadataValue(plugin, true));
                                            player.setMetadata("currentchannel", new FixedMetadataValue(plugin, cc.getAutojoinList().get(0)));
                                            player.sendMessage("Leaving "+channelName+" Channel - Changing to: " +  cc.getAutojoinList().get(0));
                                        }
                                        else
                                        {
                                            player.setMetadata("listenchannel." + cc.defaultChannel, new FixedMetadataValue(plugin, true));
                                            player.sendMessage("Leaving "+channelName+" Channel - Changing to: " + cc.defaultChannel);
                                            player.setMetadata("currentchannel", new FixedMetadataValue(plugin, cc.defaultChannel));
                                        }

                                        return true;
                                    }

                                }


                                for (ChatChannel chname : cc.getChannelsInfo()) {

                                    if (chname.getName().equalsIgnoreCase(channelName) || chname.getAlias().equalsIgnoreCase(channelName)) {

                                        player.setMetadata("listenchannel." + chname.getName(), new FixedMetadataValue(plugin, false));

                                        String format = ChatColor.valueOf(chname.getColor().toUpperCase()) + "[" + chname.getName() + "]";
                                        player.sendMessage("Leaving channel: " + format);

                                        plugin.getServer().getLogger().info("Forcing Channel:" +chname.getName());

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

                            admin.sendMessage("Forcing player "+player.getPlayerListName()+" out of "+ ci.getName());
                            return true;
                        }

                    }
                    else
                    {
                        admin.sendMessage("Player "+ args[1]+" is not available.");
                        return true;
                    }

                    return false;
                }

            }
            else
            {
                admin.sendMessage("You do not have permissions to run this command.");
                return true;
            }

		
		

		if( (cmd.getName().equalsIgnoreCase("mute")) || (cmd.getName().equalsIgnoreCase("chmute")))
		{
            if (args.length < 2) {
                admin.sendMessage(ChatColor.WHITE+"Invalid command. Try: /chmute [player] [channel]");
                return true;
            }
            Player player = null;
            player = sender.getServer().getPlayerExact(args[0]);
            String channel = args[1];
            return HandleMute(admin,player,channel);
        }
		
		if((cmd.getName().equalsIgnoreCase("unmute")) ||(cmd.getName().equalsIgnoreCase("chunmute")) )
		{
            if (args.length < 2)
            {
                admin.sendMessage(ChatColor.WHITE+"Invalid command. Try: /chunmute [player] [channel]");
                return false;
            }
            Player player = null;
            player = sender.getServer().getPlayerExact(args[0]);
            String channel = args[1];
            return HandleUnMute(admin,player,channel);
		}
		
		return false;
	}

        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Handle Mute Function
    /////////////////////////////////////////////////////////////////////////////////////
    boolean HandleMute(Player admin, Player player, String channel)
    {
        if(admin.isPermissionSet(plugin.getChatChannelInfo().mutepermissions))
        {
            String playername = "";

            if(player == null) {

                admin.sendMessage(ChatColor.RED+"Can't mute. Player "+ playername + " doesn't exist.");

                return true;
            }
            if(player.getPlayerListName()!= null)
                  playername = player.getPlayerListName();
            //Check for Channels
            for(ChatChannel c:cc.getChannelsInfo())
            {
                //Channel Name or Alias is OK
                if(c.getName().equalsIgnoreCase(channel)|| c.getAlias().equalsIgnoreCase(channel) )
                {
                    if(c.isMuteable()) {
                        plugin.logme(LOG_LEVELS.INFO ,"Muting Player", " In Channel : " + c.getName() + " Player Name: " + playername );
                        player.setMetadata("MumbleMute."+c.getName(),new FixedMetadataValue(plugin,true));
                        admin.sendMessage(ChatColor.RED + "Muted player: "+ChatColor.WHITE+" "+ playername + ChatColor.RED + " in: " +  ChatColor.valueOf(c.getColor().toUpperCase())  + c.getName());
                        player.sendMessage(ChatColor.RED+"You have just been muted in " +  ChatColor.valueOf(c.getColor().toUpperCase())  +c.getName());
                    }
                    else
                        admin.sendMessage(ChatColor.RED + "You cannot mute players in this channel: "  + c.getName());

                    return true;
                }
            }

            //If we left the channel check without finding a channel, then it doesn't exist.
            admin.sendMessage(ChatColor.RED+"Can't mute. Channel "+ channel+ " doesn't exist.");
            return true;

        }//Has permissions...
        else{
            admin.sendMessage(ChatColor.DARK_PURPLE+"You do not have permission for this command.");
            return true;
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Handle UnMute Function
    /////////////////////////////////////////////////////////////////////////////////////
    boolean HandleUnMute(Player admin, Player player, String channel)
    {
        if(admin.isPermissionSet(plugin.getChatChannelInfo().unmutepermissions))
        {

            String playername = "";

            if(player == null) {

                admin.sendMessage(ChatColor.RED+"Can't unmute. Player "+ playername + " doesn't exist.");

                return true;
            }

            if(player.getPlayerListName()!= null)
                playername = player.getPlayerListName();
            else
                playername = "";
            for(ChatChannel c:cc.getChannelsInfo())
            {
                if(c.getName().equalsIgnoreCase(channel)|| c.getAlias().equalsIgnoreCase(channel))
                {
                    plugin.logme(LOG_LEVELS.INFO ,"Unmuting Player", " In Channel : " + c.getName() + " Player Name: " +playername );
                    player.setMetadata("MumbleMute."+c.getName(),new FixedMetadataValue(plugin,false));
                    admin.sendMessage(ChatColor.RED+"unMuted Player "+playername + " in "+  ChatColor.valueOf(c.getColor().toUpperCase())  +c.getName());
                    player.sendMessage(ChatColor.RED+"You have just been unmuted in " +  ChatColor.valueOf(c.getColor().toUpperCase())  + c.getName());
                    return true;
                }
            }

            admin.sendMessage(ChatColor.RED+"Can't unmute. Channel "+ channel + " doesn't exist.");
            return true;

        }//Has permissions...
        else
        {
            admin.sendMessage(ChatColor.DARK_PURPLE+"You do not have permission for this command.");
            return true;
        }

    }

}
