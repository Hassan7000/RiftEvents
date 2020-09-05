package org.hassan.riftevents.rifteventsrecoded;

import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hassan.riftevents.rifteventsrecoded.commands.EventCommand;
import org.hassan.riftevents.rifteventsrecoded.games.Spleef;
import org.hassan.riftevents.rifteventsrecoded.games.setupgame.SetupGame;
import org.hassan.riftevents.rifteventsrecoded.utils.ConfigHandler;
import org.hassan.riftevents.rifteventsrecoded.utils.FastBlockUpdate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class RiftEvents extends JavaPlugin {

    private static RiftEvents instance;
    private ArrayList<UUID> setupState = new ArrayList<>();
    public HashMap<UUID, List<Location>> getStateBlocks = new HashMap<>();
    public FastBlockUpdate fastBlockUpdate = new FastBlockUpdate(this,50);
    private CommandManager commandManager;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        new ConfigHandler(this);
        commandManager = new CommandManager(this);
        commandManager.register(new EventCommand());
        getServer().getPluginManager().registerEvents(new SetupGame(), this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RiftEvents getInstance(){
        return instance;
    }

    public ArrayList<UUID> getSetupState(){
        return setupState;
    }

    public FileConfiguration getArenas() {
        return ConfigHandler.Configs.ARENAS.getConfig();
    }
}
