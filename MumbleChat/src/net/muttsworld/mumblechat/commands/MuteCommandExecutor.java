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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;


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
 
	public boolean getMetadata(Player player, String key, MumbleChat plugin){
		  List<MetadataValue> values = player.getMetadata(key);  
		  for(MetadataValue value : values){
		     if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){
		        return value.asBoolean(); //value();
		     }
		  }
		  return false;
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
		
		//TODO: Fix this section... needs command changed, now has both mute and unmute
		//Need to check for permission before checking for command
		if(cmd.getName().equalsIgnoreCase("durpmute"))
		{
			if(getMetadata(admin, "durpchat.canmute", plugin)==true)
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
						 if(c.getName().equalsIgnoreCase(args[1])|| c.getAlias().equalsIgnoreCase(args[1]))
						 {
							 player.setMetadata("durpMute."+c.getName(),new FixedMetadataValue(plugin,true));
							 admin.sendMessage(ChatColor.RED+"Muted Player "+ args[0] + " in " + c.getName());
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
		
		if(cmd.getName().equalsIgnoreCase("durpunmute"))
		{
			if(getMetadata(admin, "durpchat.canmute", plugin)==true)
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
							 player.setMetadata("durpMute."+c.getName(),new FixedMetadataValue(plugin,false));
							 admin.sendMessage(ChatColor.RED+"unMuted Player "+ args[0] + " in " + c.getName());
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
