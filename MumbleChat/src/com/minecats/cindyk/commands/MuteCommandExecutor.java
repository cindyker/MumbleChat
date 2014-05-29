package com.minecats.cindyk.commands;

import com.minecats.cindyk.ChatChannel;
import com.minecats.cindyk.ChatChannelInfo;
import com.minecats.cindyk.MumbleChat;
import com.minecats.cindyk.MumbleChat.LOG_LEVELS;

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
	
// Future Development!
// Want to be able force in and force out
//		if(cmd.getName().equalsIgnoreCase("force"))
//		{
//		
//		
//			if(plugin.getMetadata(admin, "cindyk.canmute", plugin)==true)
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
		
		
		//Need to check for permission before checking for command
		if(cmd.getName().equalsIgnoreCase("mute"))
		{
			if(plugin.getMetadata(admin, "mumblechat.canmute", plugin))
			{
				//	plugin.getServer().getLogger().info("Got Mute!");	
				
					if (args.length < 2)
					{
		//				 plugin.getServer().getLogger().info("Command is /derpmute [player] [channel]");
		//				 if (!(admin==null))
		//				 {
		//					 admin.sendMessage("Command is /derpmute [player] [channel]");
		//				 
		//				 }
						admin.sendMessage(ChatColor.WHITE+"Invalid command: /mute [player] [channel]");
						 return false;
					}
				
					 Player player = null;
					 player = sender.getServer().getPlayerExact(args[0]);
					 if(player == null)
					 {
				//		 plugin.getServer().getLogger().info("Can't mute. Player "+ args[0] + " doesn't exist.");
						 if (!(admin==null))
						 {
							 admin.sendMessage(ChatColor.RED+"Can't mute. Player "+ args[0] + " doesn't exist.");
						 
						 }
						 return false;
					 
					 }
					 
					 for(ChatChannel c:cc.getChannelsInfo())
					 {
						 if(c.getName().equalsIgnoreCase(args[1])|| c.getAlias().equalsIgnoreCase(args[1]) )
						 {
							 if(c.isMuteable())
							 {
								 plugin.logme(LOG_LEVELS.INFO ,"Muting Player", " In Channel : " + c.getName() + " Player Name: " + args[0] );
								 player.setMetadata("MumbleMute."+c.getName(),new FixedMetadataValue(plugin,true));
								 admin.sendMessage(ChatColor.RED + "Muted player: "+ChatColor.WHITE+" "+ args[0] + ChatColor.RED + " in: " +  ChatColor.valueOf(c.getColor().toUpperCase())  + c.getName());
								 player.sendMessage(ChatColor.RED+"You have just been muted in " +  ChatColor.valueOf(c.getColor().toUpperCase())  + c.getName());
								
							 }
							 else
							   admin.sendMessage(ChatColor.RED + "You cannot mute players in this channel: "  + c.getName());
														 
							 return true;
						 }
						 
							 
					 }
					 
				
				 
				
			//	 plugin.getServer().getLogger().info("Can't mute. Channel "+ args[1] + " doesn't exist. global or local");
				 if (!(admin==null))
				 {
					 admin.sendMessage(ChatColor.RED+"Can't mute. Channel "+ args[1] + " doesn't exist.");
				 
				 }
				return false;
			}//Has permissions...
			else
			{
				admin.sendMessage(ChatColor.DARK_PURPLE+"You do not have permission for this command.");
				return true;
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("unmute"))
		{
			if(plugin.getMetadata(admin, "mumblechat.canmute", plugin))
			{
				   plugin.logme(LOG_LEVELS.DEBUG ,"Unmute command", "Got Command..." );
				
									
					if (args.length < 2)
					{
						 return false;
					}
				
					 Player player = null;
					 player = sender.getServer().getPlayerExact(args[0]);
					 if(player == null)
					 {
				//		 plugin.getServer().getLogger().info("Can't mute. Player "+ args[0] + " doesn't exist.");
						 if (!(admin==null))
						 {
							 admin.sendMessage(ChatColor.RED+"Can't unmute. Player "+ args[0] + " doesn't exist.");
						 
						 }
						 return false;
					 
					 }
					 
					 for(ChatChannel c:cc.getChannelsInfo())
					 {
						 if(c.getName().equalsIgnoreCase(args[1])|| c.getAlias().equalsIgnoreCase(args[1]))
						 { 
							 plugin.logme(LOG_LEVELS.INFO ,"Unmuting Player", " In Channel : " + c.getName() + " Player Name: " + args[0] );
							 player.setMetadata("MumbleMute."+c.getName(),new FixedMetadataValue(plugin,false));
							 admin.sendMessage(ChatColor.RED+"unMuted Player "+ args[0] + " in "+  ChatColor.valueOf(c.getColor().toUpperCase())  +c.getName());
							 player.sendMessage(ChatColor.RED+"You have just been unmuted in " +  ChatColor.valueOf(c.getColor().toUpperCase())  + c.getName());
							 return true;
						 }
					 }
					 
				
				 
				
			//	 plugin.getServer().getLogger().info("Can't mute. Channel "+ args[1] + " doesn't exist. global or local");
				 if (!(admin==null))
				 {
					 admin.sendMessage(ChatColor.RED+"Can't unmute. Channel "+ args[1] + " doesn't exist.");
				 
				 }
				return false;
			}//Has permissions...
			else
			{
				admin.sendMessage(ChatColor.DARK_PURPLE+"You do not have permission for this command.");
				return true;
			}
		}
		
		return false;
	}

}
