package net.minecats.mumblechat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by cindy on 5/14/14.
 */
public class Filters {



    static FileConfiguration customConfig = null;
    File customConfigFile = null;
    MumbleChat plugin;

    Filters(MumbleChat plugin)
    {
        this.plugin = plugin;
    }

    public void reloadCustomConfig() {

        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder().getAbsolutePath(), "resources/filters.yml");
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


}
