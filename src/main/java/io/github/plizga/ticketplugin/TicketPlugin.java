package io.github.plizga.ticketplugin;

import io.github.plizga.ticketplugin.sqlite.ConcreteDatabase;
import io.github.plizga.ticketplugin.sqlite.Database;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class TicketPlugin extends JavaPlugin
{
    private Database database;

    @Override
    public void onEnable()
    {
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Ayyy lmao, TicketPlugin V1 Enabled!");
        loadConfig();
        this.database = new ConcreteDatabase(this);
        this.database.load();
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "TicketPlugin database established properly.");
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }

    public void loadConfig()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public Database getDatabase()
    {
        return this.database;
    }
}
