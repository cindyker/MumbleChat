package net.minecats.mumblechat.listeners;

import net.minecats.mumblechat.ChatChannelInfo;
import net.minecats.mumblechat.MumbleChat;

import org.bukkit.event.Listener;

//import com.p000ison.dev.simpleclans2.api.events.ClanPlayerKillEvent;
//import com.p000ison.dev.simpleclans2.api.events.ClanPlayerCreateEvent;
//import com.p000ison.dev.simpleclans2.api.events.ClanCreateEvent;

public class SimpleClansListener implements Listener{
	ChatChannelInfo cc;
	 MumbleChat plugin;
	
	public SimpleClansListener(MumbleChat _plugin,ChatChannelInfo _cc)
	{
		cc = _cc;
		plugin = _plugin;
	}
	
//	    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//	    public void onClanPlayerKill(ClanPlayerKillEvent event) {
//
//	    }
//
//
//	    public void onClanPlayer(ClanPlayerCreateEvent event)
//	    {
//	    	plugin.logme(LOG_LEVELS.INFO, "SimpleClanListener", "ClanPlayerCreateEvent Setting Display Name");
//	    	cc.GetClanTag(event.getClanPlayer().getOnlineVersion().toPlayer());
//	    }
//
//	    public void onClanEvent(ClanCreateEvent event)
//	    {
//	    	plugin.logme(LOG_LEVELS.INFO, "SimpleClanListener", "ClanCreateEvent Setting Display Name: "+event.getClan().getTag());
//
//	    	//cc.GetClanTag(event.getClanPlayer().getOnlineVersion().toPlayer());
//	    }

}
