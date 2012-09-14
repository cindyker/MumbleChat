package net.muttsworld.mumblechat.permissions;
import java.lang.reflect.*;

import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;
public class MumblePermissions {
	
	private MumbleChat plugin;
	@SuppressWarnings("unused")
	private String name;
	@SuppressWarnings("unused")
	private ChatChannelInfo cc;
	
	public MumblePermissions(MumbleChat plugin, ChatChannelInfo _cc) {
		this.plugin = plugin;
		name= plugin.getName();
		cc = _cc;
	}
	
	@SuppressWarnings("rawtypes")
	public
	boolean PermissionsExAvailable()
	{
		try {
	        Class c = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
	        Method m[] = c.getDeclaredMethods();
	        for (int i = 0; i < m.length; i++)
	        		plugin.logme(LOG_LEVELS.DEBUG, "PermissionsExAvailable", m[i].toString());
	        
	        return true;
	     }
	     catch (Throwable e) {
	    	 plugin.logme(LOG_LEVELS.ERROR, "PermissionsExAvailable", e.getMessage());
	     }
		
		
		return false;
	}
	 
	 
}
