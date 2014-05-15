package net.minecats.mumblechat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: cindy
 * Date: 8/19/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerData {


    static FileConfiguration customConfig = null;
    File customConfigFile = null;
    MumbleChat plugin;

    PlayerData(MumbleChat plugin)
    {
       this.plugin = plugin;
    }

    public void reloadCustomConfig() {

        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder().getAbsolutePath(), "resources/PlayerData.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        /*  // Look for defaults in the jar
        InputStream defConfigStream = mama.getResource("PlayerData.yml");
        if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        customConfig.setDefaults(defConfig);
        }*/
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            this.reloadCustomConfig();
        }
        return customConfig;
    }


    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

    public void SaveItToDisk() {
        //saveCustomConfig();
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, e);
            //	  logger.severe(PREFIX + " error writting configurations");
            e.printStackTrace();
        }
    }

    public void OneTimeFileCleanUp()
    {
        //FileConfiguration curconfig;
       // curconfig = getCustomConfig();

        //If there is old file.. BLOW it away. UUID's are here.. sorry charlie.
        Boolean cleaned = customConfig.getBoolean("Cleaned",false);

        if(!cleaned)
        {
                plugin.getLogger().log(Level.INFO, "No clean up flag found. Rewriting Config!");
                ConfigurationSection main = customConfig.getConfigurationSection("");
                ConfigurationSection cs = customConfig.getConfigurationSection("players");
                ConfigurationSection ns;  //New Player Section

                if(cs!=null)
                {
                    Set<String> keys = cs.getKeys(false);//Get All id's for items under Warps
                    for(String key : keys){

                        plugin.getLogger().log(Level.INFO, "Cleaning: " + key);
                        String PlayerString =  key.toLowerCase();// Get the string with the location data
                        ConfigurationSection playersection = cs.getConfigurationSection(key);

                        //THIS REMOVES THE NODE
                        main.set("players."+key,null);
//
//                        ns = main.createSection("players." +PlayerString);
//
//                        for (String pKey:playersection.getKeys(false))
//                        {
//                          //  plugin.getLogger().log(Level.INFO, "New Key: " + pKey);
//                            ns.set(pKey,playersection.getString(pKey)) ;
//                        }



                    }
                }
                main.set("Cleaned",true);

                SaveItToDisk();
        }
    }
}
