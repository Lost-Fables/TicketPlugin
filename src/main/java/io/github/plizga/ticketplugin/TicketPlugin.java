package io.github.plizga.ticketplugin;

import co.lotc.core.bukkit.command.Commands;
import io.github.plizga.ticketplugin.commands.UserCommands;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.enums.TicketViewOptions;
import io.github.plizga.ticketplugin.helpers.Ticket;
import io.github.plizga.ticketplugin.listeners.TicketPlayerListener;
import io.github.plizga.ticketplugin.sqlite.ConcreteDatabase;
import io.github.plizga.ticketplugin.sqlite.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public final class TicketPlugin extends JavaPlugin
{
    private Database database;

    /** all of these static final strings be defining stuff.*/
    public static final String PERMISSION_START = "ticketplugin";
    public final String COMMAND_START = "request";
    public final String PREFIX = ChatColor.DARK_AQUA + "";
    public final String ALT_COLOR = ChatColor.DARK_PURPLE + "";
    public final String ERROR_COLOR = ChatColor.DARK_RED + "";


    private ArrayList<String> staffOnDuty = new ArrayList<String>();




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
        Commands.build(getCommand(COMMAND_START), UserCommands::new);

        PluginManager pluginManager = getServer().getPluginManager();
        TicketPlayerListener listener = new TicketPlayerListener(this);
        pluginManager.registerEvents(listener, this);


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

    public void notifyOnDutyStaff(Team team)
    {
        for(String uuid : staffOnDuty)
        {
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));


            if(player != null && player.hasPermission(PERMISSION_START + Team.getPermission(team)))
            {
                player.sendMessage(PREFIX + "A player ticket has been assigned to the " +
                        ALT_COLOR + team.name() + PREFIX + " team.");

            }
        }
    }



    private void registerParameters()
    {
        Commands.defineArgumentType(Team.class)
                .defaultName("Team")
                .completer((s,$) ->  Team.getAvailable(s))
                .mapperWithSender(((sender, type) -> Team.getByName(type)))
                .register();

        Commands.defineArgumentType(TicketViewOptions.class)
                .defaultName("Ticket_Options")
                .completer((s,$) -> TicketViewOptions.getAvailable(s))
                .mapperWithSender(((sender, type) -> TicketViewOptions.getByName(type)))
                .register();
    }

    public ArrayList<String> getStaffOnDuty()
    {
        return staffOnDuty;
    }
}
