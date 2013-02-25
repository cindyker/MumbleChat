package net.muttsworld.mumblechat.listeners;

import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.p000ison.dev.simpleclans2.api.events.ClanPlayerKillEvent;
import com.p000ison.dev.simpleclans2.api.events.ClanPlayerCreateEvent;

public class SimpleClansListener implements Listener{
	ChatChannelInfo cc;
	 MumbleChat plugin;
	
	public SimpleClansListener(MumbleChat _plugin,ChatChannelInfo _cc)
	{
		cc = _cc;
		plugin = _plugin;
	}
	
	    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	    public void onClanPlayerKill(ClanPlayerKillEvent event) {
	    	
	    }
	    

	    public void onClanPlayer(ClanPlayerCreateEvent event)
	    {
	    	plugin.logme(LOG_LEVELS.INFO, "SimpleClanListener", "Setting Display Name");
	    	cc.GetClanTag(event.getClanPlayer().getOnlineVersion().toPlayer());
	    }
	

}
