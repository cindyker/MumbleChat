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
//		if(cmd.getName().equalsIgnoreCase("force"))
//		{
//		
//		
//			if(plugin.getMetadata(admin, "mumblechat.canmute", plugin)==true)
//			{
//				
//				
//					if (args.length < 2)
//					{
//	
//						 return false;
//					}
//					
//					if(args.length == 2)
//					{
//						Player player = plugin.getServer().getPlayer(args [0]);
//						if(player != null){
//							for(ChatChannel chname:cc.getChannelsInfo())
//							{
//								
//								
//								player.setMetadata("listenchannel."+chname.getName(),new FixedMetadataValue(plugin,true));
//								
//								player.setMetadata("currentchannel",new FixedMetadataValue(plugin,chname.getName()));
//								return true;
//							}
//						}
//					}
//			}
//				
//		}
		
		

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
