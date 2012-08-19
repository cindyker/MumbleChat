package net.muttsworld.mumblechat.commands;

import java.util.List;

import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class TellCommandExecutor implements CommandExecutor {

	private MumbleChat plugin;
	@SuppressWarnings("unused")
	private String name;
	ChatChannelInfo cc;
	
	public TellCommandExecutor(MumbleChat plugin, ChatChannelInfo _cc) {
		this.plugin = plugin;
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
		
		if(cmd.getName().equalsIgnoreCase("tell"))
		{
	
			//	plugin.getServer().getLogger().info("Got Tell!");	
				
				if (args.length == 0)
				{
	
					 return false;
				}
				
				 Player player = null;
				 player = sender.getServer().getPlayer(args[0]);
				 if(player == null)
				 {
					 //plugin.getServer().getLogger().info("Can't find Player "+ args[0] + ".");
					 if (!(admin==null))
					 {
						 admin.sendMessage(ChatColor.RED+"Can't find Player "+ args[0] + ".");
					 
					 }
					 
					 return false;
				 
				 }
				 
				 if(args.length >= 2)
					{
						String msg = "";
						if (args[1].length() > 0)
					 	{
						 
							//for(String s:args)
							for(int r = 1; r < args.length; r++)
								msg +=" " + args[r];
							
							String echo = "you tell " + player.getDisplayName() + ChatColor.GRAY + " " + msg;							
							msg = admin.getDisplayName()  +" tells you "+ ChatColor.GRAY + msg;
							
							msg = cc.FilterChat(msg);
							player.sendMessage(msg);
							admin.sendMessage(echo);
							//plugin.getServer().getLogger().info("Called Staff Chat... Commands!");
					 	}
						
					}
				 
				 //Start a Sticky Tell
				 if(args.length == 1)
				 {
					 if(args[0].length() > 0 )
					 {
						 plugin.getServer().getLogger().info("tell::" + args[0]);
						 admin.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin,args[0]));
						 admin.sendMessage("You are now chatting with " + args[0]);
					 }
				 
				 }
				
		 return true;
		 }
				 
				
			
		
		return false;
	}

}
