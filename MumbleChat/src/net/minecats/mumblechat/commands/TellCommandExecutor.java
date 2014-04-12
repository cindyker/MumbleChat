package net.minecats.mumblechat.commands;

import java.util.StringTokenizer;

import net.minecats.mumblechat.ChatChannelInfo;
import net.minecats.mumblechat.MumbleChat;
import net.minecats.mumblechat.MumbleChat.LOG_LEVELS;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;


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


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		
		long ignorecount = 0;
		String ignorelist;
		//if not a player... we are done.
		if (!(sender instanceof Player)) return false; 
		
		Player admin = null;
		if ((sender instanceof Player))
		 {
		   admin = (Player)sender;
		 }
		
		if(cmd.getName().equalsIgnoreCase("reply"))
        {
            //Send Message to PLayer in Mumblechat.reply metadata.
            if(args.length == 0)
            {
                admin.sendMessage("[MumbleChat] Invalid Command Usage: /reply [message]");
                return true;
            }

            Player player = null;
            String playername;
            playername = plugin.getMetadataString(admin,"MumbleChat.reply",plugin)   ;
            player = sender.getServer().getPlayer(playername);
            if(player == null)
            {
                plugin.logme(LOG_LEVELS.DEBUG, "reply", "Can't find Player "+ playername + ".");

                if (admin!=null)
                {
                    admin.sendMessage(ChatColor.RED+"[MumbleChat] Can't reply to "+ playername + ". They are no longer online.");
                }

                plugin.logme(LOG_LEVELS.DEBUG, "rely Command:onCommand:reply", "Returned True");
                return true;

            }
            //Ok.. we have a player.. lets see if they are ignoring us...
            if(isIgnoring(admin,player))
            {
                admin.sendMessage(ChatColor.YELLOW +"[MumbleChat] "+ args[0] + " is currently ignoring your tells.");
                return true;
            }


            //Ok, they are not ignoring you....

            String msg = "";
            if (args[0].length() > 0)
            {

                //for(String s:args)
                for(int r = 0; r < args.length; r++)
                    msg +=" " + args[r];

                //ChatColor.GRAY
                String echo = "Reply to " + player.getDisplayName() + ChatColor.valueOf(cc.tellColor.toUpperCase())  + ": " + msg;
                msg = admin.getDisplayName()  +" tells you: "+ ChatColor.valueOf(cc.tellColor.toUpperCase())  + msg;
                msg = cc.FilterChat(msg);
                player.sendMessage(msg);
                admin.sendMessage(echo);

                return true;
                //plugin.getServer().getLogger().info("Called Staff Chat... Commands!");
            }


        }


		if(cmd.getName().equalsIgnoreCase("chignore"))
		{
			
			plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:ignore", "Entered");
			
			if(args.length == 0)
			{
				plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:ignore", "No Arguements");
				plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:ignore", "Returned False");

                admin.sendMessage(ChatColor.AQUA+"[MumbleChat]: Try /chignore ? for help.");
				return true;
			}
			 
			 if(args.length == 1)
			 {
				 String newignorelist = "";
				 	
				 	//Ignore help and listing..
				 	if(args[0].equalsIgnoreCase("?"))
				 	{
				 		 admin.sendMessage("\n"+ChatColor.AQUA+"-->Ignore Information<--");
				 		 admin.sendMessage(ChatColor.AQUA+"    /chignore [playername]");
						 admin.sendMessage(ChatColor.AQUA+"This will prevent players from sending you tells. ");
						 admin.sendMessage(ChatColor.AQUA+"You may only have 20 players ignored at a time");
						 admin.sendMessage(ChatColor.AQUA+"To remove an ignore; use the chignore command again.");
						 admin.sendMessage(ChatColor.AQUA+"------------------------------------------------");
						 
						 ignorelist = plugin.getMetadataString(admin, "MumbleChat.ignore", plugin);
						 
						 if(ignorelist.length() > 0)
						 {
							 //lets display a list of currently ignored players to the user
							 long linecount = plugin.getLineLength();
							 String curignores = "";
							 String nextpl ="";
			                 StringTokenizer st = new StringTokenizer(ignorelist, ",");
			                 if (st.countTokens() > 0)
			                 {
			                	 admin.sendMessage(ChatColor.GOLD+"You are currently ignoring these players:"+ChatColor.WHITE);
			                 }
							 while (st.hasMoreTokens()) {
			                        
								nextpl = st.nextToken();
			                    	
			                    if(curignores.length() + nextpl.length()> linecount)
			                    {
			                    	curignores += "\n" + nextpl;
			                    	linecount = linecount + plugin.getLineLength();	
			                    	//plugin.getServer().getLogger().info("Linecount = " + linecount + "nextpl:"+nextpl);
			                    }
			                    else
			                    	curignores += nextpl;
							  
			                  
			                    curignores += ChatColor.WHITE+", ";
			                 } //while tokens
							 
							//Remove the last trailing comma...
							 curignores = curignores.substring(0, curignores.length()-2);
							 //show player list.
							 admin.sendMessage(curignores);
						 }
						 
						 plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:ignore", "Returned True");
						 return true;
				 	}
				 
					if (args[0].length() > 0)
				 	{
					 
						ignorelist = plugin.getMetadataString(admin, "MumbleChat.ignore", plugin);
						Boolean bFoundRemove = false;
						//Check to see if we are removing player from ignore list first...
						// they don't need to be online for that...
						if(ignorelist.length() > 0)
						{		        
								String curplayer = "";
			                    StringTokenizer st = new StringTokenizer(ignorelist, ",");
			                    ignorecount = st.countTokens();
			                    while (st.hasMoreTokens()) {
			                    	 
			                    	curplayer=st.nextToken();
			                        if(curplayer.equalsIgnoreCase(args[0]))
			                        {
			                        	admin.sendMessage("You are no longer ignoring player: " + args[0]);
			                        	bFoundRemove = true;			                        	
			                        }
			                        else
			                        {
			                        	newignorelist += curplayer + "," ;
			                        				                        	
			                        }
			                    } //while tokens
			                    
			                    //strip off trailing comma
			                    if(newignorelist.length() > 0)			                   
			                    	newignorelist = newignorelist.substring(0, newignorelist.length()-1);
			                  
			                   //Save modified list
			                   admin.setMetadata("MumbleChat.ignore", new FixedMetadataValue(plugin,newignorelist));
			             }
						
						//Didn't find the name to remove from ignore.
						//lets check to see if the player is offline or online.
						if(bFoundRemove == false)
						{
							Player player = null;
							 player = sender.getServer().getPlayer(args[0]);
							 if(player == null)
							 {
								 if (!(admin==null))
								 {
									 admin.sendMessage(ChatColor.RED+"Wrong player name or player offline: "+ args[0] + ".");
								 
								 }
								 
								 return true;
							 }
							 
							 if (ignorecount >= 20)
							 {
								 admin.sendMessage(ChatColor.RED+"You cannot ignore more than 20 players at once");
								 admin.sendMessage(ChatColor.RED+"/chignore ignoredplayername  ");
								 admin.sendMessage(ChatColor.RED+"to remove one from existing list.");
								 admin.sendMessage(ChatColor.RED+" Use /chignore ? to see a list of ignored players.");
								 return true;
							 }
							 //looks like they are online... lets add em to the list then.
							 if(newignorelist.length() == 0)
								 newignorelist += args[0];
							 else
								 newignorelist += "," + args[0];
							 
							 admin.sendMessage("You are now ignoring player: " + args[0]);
							 
							 admin.setMetadata("MumbleChat.ignore", new FixedMetadataValue(plugin,newignorelist));
							 
						}
					
						plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:ignore", "Returned True");
						return true;  //removed them from ignore...
							
					}// name field has length
							
			 }// received a name argument
			 
			 plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:ignore", "Returned False");
			 return false;
			 
		}
		
		if(cmd.getName().equalsIgnoreCase("tell") || cmd.getName().equalsIgnoreCase("whisper"))
		{
			if(!admin.hasPermission(plugin.getChatChannelInfo().tellpermissions))
			{
				 admin.sendMessage(ChatColor.RED+"[MumbleChat] You do not have permission to send tells.");
				return true;
			}
	
			//	plugin.getServer().getLogger().info("Got Tell!");	
			plugin.logme(LOG_LEVELS.DEBUG, "tell", "Tell or Whisper command called: "+ cmd.getName() );
				
				if (args.length == 0)
				{
	
					 return false;
				}
				
				 Player player = null;
				 player = sender.getServer().getPlayer(args[0]);
				 if(player == null)
				 {
					plugin.logme(LOG_LEVELS.DEBUG, "tell", "Can't find Player "+ args[0] + ".");
					
					 if (admin!=null)
					 {
						 admin.sendMessage(ChatColor.RED+"[MumbleChat] Can't find Player "+ args[0] + ".");
					 
					 }
					 
					 plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:tell", "Returned True");
					 return true;
				 
				 }
				 
				 //Ok.. we have a player.. lets see if they are ignoring us...
				  if(isIgnoring(admin,player))
                  {
                    admin.sendMessage(ChatColor.YELLOW + args[0] + " is currently ignoring your tells.");
                    return true;
                  }


				 //Ok, they are not ignoring you....
				 if(args.length >= 2)
					{
						String msg = "";
						if (args[1].length() > 0)
					 	{
						 
							//for(String s:args)
							for(int r = 1; r < args.length; r++)
								msg +=" " + args[r];
							
							//ChatColor.GRAY
							String echo = "you tell " + player.getDisplayName() + ChatColor.valueOf(cc.tellColor.toUpperCase())  + ": " + msg;							
							msg = admin.getDisplayName()  +" tells you: "+ ChatColor.valueOf(cc.tellColor.toUpperCase())  + msg;
                            player.setMetadata("MumbleChat.reply", new FixedMetadataValue(plugin,admin.getPlayerListName()));
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
						 plugin.logme(LOG_LEVELS.DEBUG,"Sticky Tell", "tell::" + args[0]);
						 admin.setMetadata("MumbleChat.tell", new FixedMetadataValue(plugin,args[0]));
                         player.setMetadata("MumbleChat.reply", new FixedMetadataValue(plugin,admin.getPlayerListName()));
						 admin.sendMessage("You are now chatting with " + args[0]);
					 }
				 
				 }
		        
		  plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:tell", "Returned True");
		 return true;
		 }
		 
		 plugin.logme(LOG_LEVELS.DEBUG, "TellCommand:onCommand:tell", "Returned False");
		return false;
	}



    boolean isIgnoring(Player speaker, Player receiver)
    {
        int ignorecount;
        //Ok.. we have a player.. lets see if they are ignoring us...
        String playerignorelist = plugin.getMetadataString(receiver, "MumbleChat.ignore", plugin);
        if(playerignorelist.length() > 0)
        {
            String curplayer = "";
            StringTokenizer st = new StringTokenizer(playerignorelist, ",");
            ignorecount = st.countTokens();
            while (st.hasMoreTokens()) {

                curplayer=st.nextToken();
                if(curplayer.equalsIgnoreCase(speaker.getName()))
                    return true;
            }

        }

        return false;
    }

}
