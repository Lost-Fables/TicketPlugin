package io.github.plizga.ticketplugin;

import co.lotc.core.bukkit.command.Commands;
import io.github.plizga.ticketplugin.commands.TicketCommand;
import io.github.plizga.ticketplugin.sqlite.ConcreteDatabase;
import io.github.plizga.ticketplugin.sqlite.Database;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public final class TicketPlugin extends JavaPlugin
{
    private Database database;

    /** all of these static final strings be defining stuff.*/
    public static final String PERMISSION_START = "req";
    public final String PREFIX = ChatColor.DARK_AQUA + "";
    public final String ALT_COLOR = ChatColor.DARK_PURPLE + "";
    public final String ERROR_COLOR = ChatColor.DARK_RED + "";

    private static TicketPlugin ticketPluginInstance;

    public static TicketPlugin getTicketPluginInstance()
    {
        return ticketPluginInstance;
    }

    @Override
    public void onEnable()
    {
        ticketPluginInstance = this;
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Ayyy lmao, TicketPlugin V1 Enabled!");
        loadConfig();

        this.database = new ConcreteDatabase(this);
        this.database.load();
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "TicketPlugin database established properly.");

        Commands.build(getCommand("ticket"), TicketCommand::new);
        //this.getCommand("ticket").setExecutor(new TicketCommand(this));

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
