package io.github.plizga.ticketplugin;

import co.lotc.core.bukkit.command.Commands;
import io.github.plizga.ticketplugin.commands.UserCommands;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.enums.TicketViewOptions;
import io.github.plizga.ticketplugin.listeners.TicketPlayerListener;
import io.github.plizga.ticketplugin.database.ConcreteDatabase;
import io.github.plizga.ticketplugin.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;


/**
 * The main class for the Ticket Plugin. Handles enabling and disabling of the plugin as well as setting up permissions
 * and defining global variables.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public final class TicketPlugin extends JavaPlugin
{
    /** The database used throughout the plugin. */
    private Database database;
    /** Defines the start of permissions used in this plugin. */
    public static final String PERMISSION_START = "ticketplugin";
    /** Defines the start of commands '/COMMAND_START create etc' */
    public final String COMMAND_START = "request";
    /** Defines the first common color used in the plugin. */
    public final String PREFIX = ChatColor.DARK_AQUA + "";
    /** Defines the second common color used in the plugin. */
    public final String ALT_COLOR = ChatColor.DARK_PURPLE + "";
    /** Defines the common error color used in the plugin. */
    public final String ERROR_COLOR = ChatColor.DARK_RED + "";
    /** Keeps track of any staff currently on duty. Will empty out upon plugin restart or shutdown. */
    private ArrayList<String> staffOnDuty = new ArrayList<String>();
    /**Represents an instance of the ticket plugin. */
    private static TicketPlugin ticketPluginInstance;

    /**
     * Static getter method that returns the {TicketPluginInstance}.
     * @return  The {TicketPluginInstance}
     */
    public static TicketPlugin getTicketPluginInstance()
    {
        return ticketPluginInstance;
    }

    /**
     * Handles the enabling of the plugin. Pretty self-explanatory. Establishes the instance, establishes connection to
     * the database, and registers all parameters and events listeners.
     */
    @Override
    public void onEnable()
    {
        ticketPluginInstance = this;
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "TicketPlugin V1 Enabled!");
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


    /**
     * Stub method that handles disable logic.
     */
    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }

    /**
     * Loads the config.
     */
    public void loadConfig()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }


    /**
     * Returns a reference to the database.
     * @return
     */
    public Database getDatabase()
    {
        return this.database;
    }

    /**
     * Handles notifying any on-duty staff about new tickets or ticket reassignments.
     * @param team  the team whose members need to be notified (if they are on-duty).
     */
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


    /**
     * Registers enum parameters in order to be used with the commands found in the commands package.
     * @see io.github.plizga.ticketplugin.commands
     */
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

    /**
     * Getter for the list of staff on duty in order to encapsulate and protect data.
     * @return the staff currently on duty in an ArrayList of Strings.
     */
    public ArrayList<String> getStaffOnDuty()
    {
        return staffOnDuty;
    }
}
