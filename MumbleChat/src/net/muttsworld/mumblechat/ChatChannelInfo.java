package net.muttsworld.mumblechat;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.p000ison.dev.simpleclans2.api.clan.Clan;
import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

//import net.sacredlabyrinth.phaed.simpleclans.Clan;
//import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import com.p000ison.dev.simpleclans2.api.SCCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

//import net.sacredlabyrinth.phaed.simpleclans.api.events.SimpleClansClanCreateEvent;
//import net.sacredlabyrinth.phaed.simpleclans.SimpleClans.*;
//import com.p000ison.dev.simpleclans2.api.clan.Clan;
//import com.p000ison.dev.simpleclans2.api.clanplayer.ClanPlayer;

public class ChatChannelInfo {

    MumbleChat plugin;
    // String[] Filters;
    List<String> filters;
    List<String> filterexceptions;
    //List<chatChannel> cc;
    ChatChannel[] cc;
    public String mutepermissions;
    public String unmutepermissions;
    public String tellpermissions;
    public String forcepermissions;
    public String colorpermissions;
    public String whopermissions;
    public String lookuppermissions;
    public String filterpermissions;
    public boolean saveplayerdata;
    public boolean usePrefix;
    public boolean useSuffix;
    public String tellColor;
    public String defaultChannel; //There can be only one :)
    //Broadcast Variables
    public String broadcastCommand; //also only one
    public String broadcastColor; //white
    public String broadcastDisplayTag;
    public boolean broadcastPlayer;
    public String broadcastPermissions;
    public String lookupdatepermissions;
    
    public boolean bChannelInfront;
    public boolean bDisplayAlias;

    public Boolean bUseWho;
    ConfigurationSection cs;

   //@SuppressWarnings("unchecked")
    ChatChannelInfo(MumbleChat _plugin) {
        plugin = _plugin;
        filters = (List<String>) plugin.getConfig().getStringList("filters");
        filterexceptions = (List<String>) plugin.getConfig().getStringList("filterexceptions");

        String _color = "";
        String _name = "";
        String _permission = "";
        Boolean _muteable = false;
        Boolean _filter = false;
        Boolean _defaultchannel = false;
        String _alias = "";
        Double _distance = (double) 0;
        Boolean _autojoin = false;
        tellColor = "gray";
        cs = plugin.getConfig().getConfigurationSection("channels");


        mutepermissions = plugin.getConfig().getString("permissions.mute", "mumblechat.mute");
        unmutepermissions = plugin.getConfig().getString("permissions.unmute",mutepermissions);
        
        forcepermissions = plugin.getConfig().getString("permissions.force", "mumblechat.force");
        colorpermissions = plugin.getConfig().getString("permissions.color","mumblechat.color");

        //plugin.getServer().getLogger().info("["+plugin.getName()+"] " + mutepermissions);

        tellpermissions = plugin.getConfig().getString("permissions.tell","mumblechat.tell");

        whopermissions = plugin.getConfig().getString("permissions.who","mumblechat.who");
        lookuppermissions = plugin.getConfig().getString("permissions.lookup","mumblechat.lookup");
        lookupdatepermissions =  plugin.getConfig().getString("permissions.lookupDate","mumblechat.lookupDate");
        filterpermissions = plugin.getConfig().getString("permissions.filterlookup","mumblechat.filterlookup");

        saveplayerdata = plugin.getConfig().getBoolean("saveplayerdata", true);
        
        usePrefix = false;
        useSuffix = false;
        bChannelInfront = false;
        bDisplayAlias=false;
        bUseWho=false;

        bUseWho = plugin.getConfig().getBoolean("useWho",false);


        //Temporary support for backwards config compatability
        if(plugin.getConfig().getBoolean("usePexPrefix", false))
        {
        	usePrefix = plugin.getConfig().getBoolean("usePexPrefix", false);
        	useSuffix = plugin.getConfig().getBoolean("usePexPrefix", false);
        }

        if(plugin.getConfig().getBoolean("usePrefix", false))
        {
        	usePrefix = plugin.getConfig().getBoolean("usePrefix", false);
        	useSuffix = plugin.getConfig().getBoolean("usePrefix", false);
        }
        
        plugin.setLogLevel(plugin.getConfig().getString("loglevel", "INFO").toUpperCase());

        bChannelInfront = plugin.getConfig().getBoolean("channelInFront",false);
        tellColor = plugin.getConfig().getString("tellcolor", "gray");

        //Fill in Broadcase information
        getBroadcastInfo();

        int len = (cs.getKeys(false)).size();
        cc = new ChatChannel[len];

        int x = 0;
        for (String key : cs.getKeys(false)) {
            //	plugin.getServer().getLogger().info(key + ":" + (String)cs.getString(key+".color"));

            _color = (String) cs.getString(key + ".color", "white");
            //plugin.getServer().getLogger().info("Got Color:" + _color);

            if (!(isValidColor(_color))) {
                plugin.getServer().getLogger().info("[" + plugin.getName() + "] " + _color + " is not valid. Changing to white.");
                _color = "white";
            }

            _name = key;
            //plugin.getServer().getLogger().info("Got name:" +key);

            _permission = (String) cs.getString(key + ".permissions", "None");
            //plugin.getServer().getLogger().info("Got permission:" + _permission);

            _muteable = (Boolean) cs.getBoolean(key + ".muteable", false);
            //plugin.getServer().getLogger().info("Got muteable:" + _muteable);

            _filter = (Boolean) cs.getBoolean(key + ".filter", true);

            _defaultchannel = (Boolean) cs.getBoolean(key + ".default", false);
            if (_defaultchannel == true) {
                defaultChannel = _name;
            }
            //plugin.getServer().getLogger().info("Got defaultchannel:" + _defaultchannel);

            _alias = (String) cs.getString(key + ".alias", "None");

            _distance = (Double) cs.getDouble(key + ".distance", (double) 0);

            _autojoin = (Boolean) cs.getBoolean(key + ".autojoin", false);

            ChatChannel c =
                    new ChatChannel(_name, _color, _permission, _muteable, _filter, _defaultchannel, _alias, _distance, _autojoin);

            //	plugin.getServer().getLogger().info("new channel:" + c.getName());

            cc[x++] = c;
            //cc.add((chatChannel)c);

            //plugin.getServer().getLogger().info("saved channel:" + cc.length);
        }

        //plugin.getServer().getLogger().info("print list");
        //logChannelList();
    }

    //***************************************
    // Fill in the config for Alert Broadcasts...
    void getBroadcastInfo() {
        if (plugin.getConfig().isConfigurationSection("broadcast")) {
            broadcastCommand = plugin.getConfig().getString("broadcast.command", "");
            if (broadcastCommand.equalsIgnoreCase("")) {
                //If they didn't set a command, then no broadcast for you!
                broadcastCommand = null;
                return;
            }


            broadcastPlayer = (plugin.getConfig().getBoolean("broadcast.displayplayername", false));

            broadcastColor = plugin.getConfig().getString("broadcast.color", "white");
            if (!(isValidColor(broadcastColor))) {
                broadcastColor = "white";
                plugin.getServer().getLogger().info("[" + plugin.getName() + "] Broadcast Color: " + broadcastColor + " is not valid. Changing to white.");
            }

            broadcastDisplayTag = plugin.getConfig().getString("broadcast.displaytag", "");
            broadcastPermissions = plugin.getConfig().getString("broadcast.permissions", "none");
        } else {
            broadcastCommand = null;
        }

    }

    public void SaveConfig()
    {
        plugin.saveDefaultConfig();

    }
    public List<String> getAutojoinList() {
        List<String> joinlist = new ArrayList<String>();

        for (ChatChannel c : cc) {
            if (c.getAutojoin()) {
                joinlist.add(c.getName());
            }
        }

        return joinlist;
    }

    int getChannelCount() {
        return cc.length;
    }

    void logChannelList() {
        for (ChatChannel p : cc) {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "]" + p.getName() + ":" + p.getColor() + ":" + p.getPermission() + ":" + p.isMuteable() + ":" + p.isFiltered() + ":" + p.isDefaultchannel());
        }
    }

    public Boolean isValidColor(String _color) {
        //Check for valid color... Default it to white if wrong.
        Boolean bFound = false;
        for (ChatColor bkColors : ChatColor.values()) {
            //plugin.getServer().getLogger().info(_color+" : "+bkColors.name());
            if (_color.equalsIgnoreCase(bkColors.name())) {
                bFound = true;
            }
        }

        return bFound;
    }

    public String getBroadcastCommand() {
        return broadcastCommand;
    }

    public boolean isBroadcastAvailable() {
        if (broadcastCommand == null) {
            return false;
        } else {
            return true;
        }
    }

    public String getChannelDisplayFormat(String ChannelName)
    {
    	 for (ChatChannel c : cc) {
             if (c.getName().equalsIgnoreCase(ChannelName) || c.getAlias().equalsIgnoreCase(ChannelName)) {
                
            	 if (bDisplayAlias)
			    	return c.getAlias();			    				    	
            	 else
            		 return c.getName();
             }
         }
    	
    	 return null;
    }
    
    public ChatChannel[] getChannelsInfo() {
        return cc;
    }

    public ChatChannel getChannelInfo(String ChannelName) {
        for (ChatChannel c : cc) {
            if (c.getName().equalsIgnoreCase(ChannelName) || c.getAlias().equalsIgnoreCase(ChannelName)) {
                return c;
            }
        }

        return null;
    }

    //=======================================================
    //function: saveFilterWord
    //
    // Parameters: String filter
    //             String badWord
    //-------------------------------------------------------
    public boolean saveFilterWord(String filter, String badWord)
    {
        String currentFilter = getFilterWord(badWord);

        if(currentFilter != null)
        {
           for(int x=0;x<filters.size();x++)
           {
               String s =filters.get(x) ;
               if(s.compareToIgnoreCase(currentFilter)==0)
               {
                  filters.set(x,badWord+","+filter);
               }
           }
        }
        else
        {
           filters.add(filter+","+badWord);
        }

        plugin.getConfig().set("Filters",filters);
        return true;
    }

    public String getFilterWord(String badWord)
    {
        int t;
        for(String s : filters)
        {
            t = 0;

            String[] pparse = new String[2];
            pparse[0] = " ";
            pparse[1] = " ";
            StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) {
                if (t < 2) {
                    pparse[t++] = st.nextToken();
                }
            }

            if(pparse[1].compareToIgnoreCase(badWord)==0)
            {
               return pparse[0];
            }
        }

        return null;
    }


    public String FilterChat(String msg) {
        /////////////////////////////////////////////////////
        //Apply the Filter is required
        int t = 0;

        for (String s : filters) {
            t = 0;
            String[] pparse = new String[2];
            pparse[0] = " ";
            pparse[1] = " ";
            StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) {
                if (t < 2) {
                    pparse[t++] = st.nextToken();
                }
            }

            msg = msg.replaceAll("(?i)" + pparse[0], pparse[1]);

        }

        return msg;
    }
    //This will fix the color on a player's name with PEX format.
    // it has to be put into the SetFormat method on the chat event.
    protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
    protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
    protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
    protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
    protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
    protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
    protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");

    public String FormatString(String tobeformatted) {
        String allFormated = tobeformatted;

        allFormated = chatColorPattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = chatMagicPattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = chatBoldPattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = chatStrikethroughPattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = chatUnderlinePattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = chatItalicPattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = chatResetPattern.matcher(allFormated).replaceAll("\u00A7$1");
        allFormated = allFormated.replaceAll("%", "\\%");

        return allFormated;

    }

    public String FormatPlayerName(String playerPrefix, String playerDisplayName, String playerSuffix) {
        if (usePrefix) {

            playerPrefix = chatColorPattern.matcher(playerPrefix).replaceAll("\u00A7$1");
            playerPrefix = chatMagicPattern.matcher(playerPrefix).replaceAll("\u00A7$1");
            playerPrefix = chatBoldPattern.matcher(playerPrefix).replaceAll("\u00A7$1");
            playerPrefix = chatStrikethroughPattern.matcher(playerPrefix).replaceAll("\u00A7$1");
            playerPrefix = chatUnderlinePattern.matcher(playerPrefix).replaceAll("\u00A7$1");
            playerPrefix = chatItalicPattern.matcher(playerPrefix).replaceAll("\u00A7$1");
            playerPrefix = chatResetPattern.matcher(playerPrefix).replaceAll("\u00A7$1");

        }
        if (useSuffix) {

            playerSuffix = chatColorPattern.matcher(playerSuffix).replaceAll("\u00A7$1");
            playerSuffix = chatMagicPattern.matcher(playerSuffix).replaceAll("\u00A7$1");
            playerSuffix = chatBoldPattern.matcher(playerSuffix).replaceAll("\u00A7$1");
            playerSuffix = chatStrikethroughPattern.matcher(playerSuffix).replaceAll("\u00A7$1");
            playerSuffix = chatUnderlinePattern.matcher(playerSuffix).replaceAll("\u00A7$1");
            playerSuffix = chatItalicPattern.matcher(playerSuffix).replaceAll("\u00A7$1");
            playerSuffix = chatResetPattern.matcher(playerSuffix).replaceAll("\u00A7$1");
        }
        return playerPrefix +playerSuffix +playerDisplayName.trim() ;

    }



    
    public String GetClanTag(Player pl)
    {
    	String strclantag = ""; 
	    if(plugin.simplelclans)
	    {
             plugin.getLogger().info("GetClanTag: simpleclans = true");
            //If they have turned the clan tag off, don't show it.
            if( pl.hasMetadata("MumbleChat.ClanTag"))
            {
               if(!plugin.getMetadata(pl, "MumbleChat.ClanTag", plugin))
                   return strclantag;
            }
            plugin.getLogger().info("GetClanTag: Player has ClanTag metadata");

	    	 plugin.logme(LOG_LEVELS.DEBUG, "GetClanTag", "Simple Clans");
	    	 ClanPlayer cp = plugin.sc.getClanPlayerManager().getClanPlayer(pl.getName());
	    	//Incase they don't have a clan, we have to put it back.
	    	  pl.setDisplayName(pl.getPlayerListName());
	         if (cp != null)
	         {
	        	
	             Clan clan = cp.getClan();
	            // pl.setPlayerListName(clan.getTag()+pl.getPlayerListName());               
	             //plugin.logme(LOG_LEVELS.INFO, "Player Login", "Set ListName to:" + pl.getPlayerListName());
	             strclantag = clan.getTag()+ChatColor.WHITE+".";
	             pl.setDisplayName(strclantag+pl.getPlayerListName());
	             plugin.logme(LOG_LEVELS.DEBUG, "GetClanTag", "Set DisplayName to:"+strclantag + pl.getDisplayName());
	         }
	        
	        
	     }
	    return strclantag;
    }
    
    public void SetPlayerDisplayName(Player pl)
    {
    	 String pFormatted = "";
    	 
    	   GetClanTag(pl);
		    if (usePrefix) {
		    	
		 	   plugin.logme(LOG_LEVELS.DEBUG, "SetPlayerDisplayName", "Got Prefix");

               plugin.logme(LOG_LEVELS.DEBUG,"SetPlayerDisplayName","Vault Player Prefix: " + MumbleChat.chat.getPlayerPrefix(pl));

		     //http://www.minecraftwiki.net/wiki/Classic_server_protocol#Color_Codes
		     pFormatted = FormatPlayerName(MumbleChat.chat.getPlayerPrefix(pl),
		             "%s", MumbleChat.chat.getPlayerSuffix(pl));
		     
		    // plugin.logme(LOG_LEVELS.ERROR, "Player Format:", pFormatted);
		     //pl.sendMessage(pFormatted);
		     //pl.getPlayerListName()
		     //So it shows when you login.
		     //However this is bad.. as it makes who impossible....
		     //pl.setDisplayName(pFormatted);
		
		     //put player tag in metadata... this way we don't keep calling permissionex in chatlistener.
		     pl.setMetadata("chatnameformat", new FixedMetadataValue(plugin, pFormatted));
		 }
		 else
		 {
		 	  pFormatted = "%s"; 
		 	  pl.setMetadata("chatnameformat", new FixedMetadataValue(plugin, pFormatted));
		 }
    
    }
    
    

    List<String> getFilters() {
        return filters;
    }
}
