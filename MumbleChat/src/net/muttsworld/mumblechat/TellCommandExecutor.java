package net.muttsworld.mumblechat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class TellCommandExecutor implements CommandExecutor {

	@SuppressWarnings("unused")
	private MumbleChat plugin;
	@SuppressWarnings("unused")
	private String name;
	
	public TellCommandExecutor(MumbleChat plugin) {
		this.plugin = plugin;
		name= plugin.getName();
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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
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
				 
				 if(args.length >= 1)
					{
						String msg = "";
						if (args[1].length() > 0)
					 	{
						 
							//for(String s:args)
							for(int r = 1; r < args.length; r++)
								msg +=" " + args[r];
							
							String echo = "you tell " + player.getDisplayName() + ChatColor.GRAY + " " + msg;							
							msg = admin.getName()  +" tells you "+ ChatColor.GRAY + msg;
							
							player.sendMessage(msg);
							admin.sendMessage(echo);
							//plugin.getServer().getLogger().info("Called Staff Chat... Commands!");
					 	}
					}
				
		 return true;
		 }
				 
				
			
		
		return false;
	}

}
