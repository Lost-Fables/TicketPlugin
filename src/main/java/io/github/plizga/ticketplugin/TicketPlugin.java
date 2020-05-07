package io.github.plizga.ticketplugin;

import co.lotc.core.bukkit.command.Commands;
import io.github.plizga.ticketplugin.commands.UserCommands;
import io.github.plizga.ticketplugin.enums.Team;
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


        registerParameters();
        Commands.build(getCommand("request"), UserCommands::new);
        //this.getCommand("ticket").setExecutor(new UserCommands(this));


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

    private void registerParameters()
    {
        Commands.defineArgumentType(Team.class)
                .defaultName("Team")
                .completer((s,$) ->  Team.getAvailable(s))
                .mapperWithSender(((sender, type) -> Team.getByName(type)))
                .register();
    }
}
