package net.muttsworld.mumblechat;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class ChatChannelInfo {

    MumbleChat plugin;
    // String[] Filters;
    List<String> filters;
    List<String> filterexceptions;
    //List<chatChannel> cc;
    ChatChannel[] cc;
    public String mutepermissions;
    public String forcepermissions;
    public Boolean saveplayerdata;
    public Boolean usePrefix;
    public Boolean useSuffix;
    public String tellColor;
    public String defaultChannel; //There can be only one :)
    //Broadcast Variables
    public String broadcastCommand; //also only one
    public String broadcastColor; //white
    public String broadcastDisplayTag;
    public Boolean broadcastPlayer;
    public String broadcastPermissions;

    @SuppressWarnings("unchecked")
    ChatChannelInfo(MumbleChat _plugin) {
        plugin = _plugin;
        filters = (List<String>) plugin.getConfig().getList("filters");
        filterexceptions = (List<String>) plugin.getConfig().getList("filterexceptions");

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
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("channels");


        mutepermissions = plugin.getConfig().getString("mute.permissions", "");
        forcepermissions = plugin.getConfig().getString("force.permissions", "");
        //plugin.getServer().getLogger().info("["+plugin.getName()+"] " + mutepermissions);

        saveplayerdata = plugin.getConfig().getBoolean("saveplayerdata", true);
        
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


            broadcastPlayer = (plugin.getConfig().getBoolean("displayplayername", false));

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

    public List<String> getAutojoinList() {
        List<String> joinlist = new ArrayList<String>();
        ;

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

    public ChatChannel[] getChannelsInfo() {
        return cc;
    }

    public ChatChannel getChannelInfo(String ChannelName) {
        for (ChatChannel c : cc) {
            if (c.getName().equalsIgnoreCase(ChannelName)) {
                return c;
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
        allFormated = allFormated.replaceAll("%", "/%");

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
        return playerPrefix + playerDisplayName.trim() + playerSuffix;

    }

    List<String> getFilters() {
        return filters;
    }
}
