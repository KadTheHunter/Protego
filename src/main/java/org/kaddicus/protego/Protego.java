package org.kaddicus.protego;

import org.kaddicus.protego.commands.EvanescoCommand;
import org.kaddicus.protego.commands.ProtegoCommand;
import org.kaddicus.protego.listeners.*;
import org.kaddicus.protego.managers.ConfigManager;
import org.kaddicus.protego.managers.EntityManager;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Protego extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfig();

        EntityManager entityManager = new EntityManager(configManager, getLogger());

        getServer().getPluginManager().registerEvents(new EntitySpawnListener(entityManager), this);
        getServer().getPluginManager().registerEvents(new VehicleCreateListener(entityManager), this);
        getServer().getPluginManager().registerEvents(new BeehiveListener(), this);
        getServer().getPluginManager().registerEvents(new HangingEntityListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(), this);

        if (getCommand("evanesco") != null) {
            Objects.requireNonNull(getCommand("evanesco")).setExecutor(new EvanescoCommand(this));
        }
        if (getCommand("protego") != null) {
            Objects.requireNonNull(getCommand("protego")).setExecutor(new ProtegoCommand(configManager));
        }

        getLogger().info("Protego enabled!");
    }
}