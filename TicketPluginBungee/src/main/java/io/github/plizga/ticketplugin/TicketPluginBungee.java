package io.github.plizga.ticketplugin;

import co.lotc.core.bungee.command.BungeeCommandData;
import co.lotc.core.bungee.command.Commands;
import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.util.MessageUtil;
import io.github.plizga.ticketplugin.commands.UserCommands;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.enums.TicketViewOptions;
import io.github.plizga.ticketplugin.helpers.Staff;
import io.github.plizga.ticketplugin.helpers.Ticket;
import io.github.plizga.ticketplugin.listeners.TicketPlayerListener;
import io.github.plizga.ticketplugin.database.ConcreteDatabase;
import io.github.plizga.ticketplugin.database.Database;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * The main class for the Ticket Plugin. Handles enabling and disabling of the plugin as well as setting up permissions
 * and defining global variables.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public final class TicketPluginBungee extends Plugin
{
    /** The database used throughout the plugin. */
    private Database database;
    /** Defines the start of permissions used in this plugin. */
    public static final String PERMISSION_START = "ticketplugin";
    /** Defines the start of commands '/COMMAND_START create etc' */
    public final String COMMAND_START = "request";
    /** Defines the first common color used in the plugin. */
    public final ChatColor PREFIX = ChatColor.GRAY;
    /** Defines the second common color used in the plugin. */
    public final ChatColor ALT_COLOR = ChatColor.BLUE;
    /** Defines the common error color used in the plugin. */
    public final ChatColor ERROR_COLOR = ChatColor.DARK_RED;
    /** Represents an instance of the ticket plugin. */
    private static TicketPluginBungee ticketPluginBungeeInstance;
    /** Represents the config file. */
    public static Configuration config;
    /** The Bungee-Bukkit channels we use to communicate. */
    public final String CHANNEL = "lf:tickets";
    public final String TP_SUB_CHANNEL = "TicketsTeleport";
    public final String CREATE_SUB_CHANNEL = "TicketsCreate";
    public final String COMMENT_SUB_CHANNEL = "TicketsComment";
    /** The scheduled reminder task. */
    private static ScheduledTask reminderTask = null;
    /** The amount of minutes between reminders. */
    private static int reminderDelay = -1;

    /**
     * Static getter method that returns the {TicketPluginInstance}.
     * @return  The {TicketPluginInstance}
     */
    public static TicketPluginBungee getTicketPluginBungeeInstance()
    {
        return ticketPluginBungeeInstance;
    }

    /**
     * Handles the enabling of the plugin. Pretty self-explanatory. Establishes the instance, establishes connection to
     * the database, and registers all parameters and events listeners.
     */
    @Override
    public void onEnable()
    {
        ticketPluginBungeeInstance = this;
        getProxy().registerChannel(CHANNEL);
        getProxy().getConsole().sendMessage(new TextComponent("TicketPlugin V1 Enabled!"));
        loadConfig();

        establishDatabase();

        this.database.load();
        getProxy().getConsole().sendMessage(new TextComponent("TicketPlugin database established properly."));


        registerParameters();
        Commands.build(new BungeeCommandData(this, COMMAND_START), UserCommands::new);

        PluginManager pluginManager = getProxy().getPluginManager();
        TicketPlayerListener listener = new TicketPlayerListener(this);
        pluginManager.registerListener(this, listener);
        startReminder();
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
        // Save default config if config doesn't exist.
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Try to load our config
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getProxy().getConsole().sendMessage(new TextComponent("Failed to find config."));
            e.printStackTrace();
        }
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
     * Establishes the mySQL database connection through the use of the config file.
     * PRECONDITION: {loadConfig} must have been called prior.
     */
    private void establishDatabase()
    {
        String host = config.getString("mysql.host");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        String databaseName = config.getString("mysql.database");
        int port = config.getInt("mysql.port");

        this.database = new ConcreteDatabase(this, host, username, password, databaseName, port);
    }

    /**
     * Getter for the list of staff on duty.
     * @return the staff currently on duty in an ArrayList of Staff.
     */
    public List<Staff> getStaffOnDuty()
    {
        // Keeps track of any staff currently on duty. Will empty out upon plugin restart or shutdown.
        return database.getStaff();
    }

    /**
     * Getter for the list of staff on duty. (STRING)
     * @return the staff currently on duty in an ArrayList of Strings.
     */
    public ArrayList<String> getStaffUUIDsOnDuty()
    {
        ArrayList<String> staffUUIDsOnDuty = new ArrayList<String>();

        for(Staff s : getStaffOnDuty())
        {
            staffUUIDsOnDuty.add(s.getUuid().toString());
        }
        return staffUUIDsOnDuty;
    }

    /**
     * Handles notifying any on-duty staff about new tickets or ticket reassignments.
     * @param team  the team whose members need to be notified (if they are on-duty).
     */
    public void notifyOnDutyStaff(Team team)
    {
        List<UUID> broadcastedPlayers = new ArrayList<>();
        for(Staff staff : getStaffOnDuty())
        {
            ProxiedPlayer player = getProxy().getPlayer(UUID.fromString(staff.getUuid()));

            if (player != null && player.hasPermission(PERMISSION_START + team.permission) && !broadcastedPlayers.contains(player.getUniqueId()))
            {
                broadcastedPlayers.add(player.getUniqueId());

                TextComponent message = ChatBuilder.appendTextComponent(null, "A player ticket has been assigned to the ", PREFIX);
                ChatBuilder.appendTextComponent(message, team.name(), team.color);
                ChatBuilder.appendTextComponent(message, " team.", PREFIX);
                player.sendMessage(message);
            }
        }
    }

    /**
     * Gets a specific staff member of the staff-team from the database. Overloaded.
     * @return a {Staff} member who is currently on duty.
     */
    public Staff getStaffOnDuty(String uuid)
    {
        return database.getStaff(uuid);
    }

    /**
     * Removes a staff member from the on-duty list.
     * @param uuid  uuid in String form of the staff-member.
     */
    public void removeStaffOnDuty(String uuid)
    {
        database.updateStaffOnDuty(uuid, false);
    }

    /**
     * Start the reminder process that pings staff every 5 mins of open tickets.
     */
    public void startReminder() {
        if (reminderDelay < 0) {
            try {
                reminderDelay = config.getInt("reminder-delay");
            } catch (Exception e) {
                getLogger().warning("Unable to parse reminder delay. Outdated config?");
                e.printStackTrace();
                reminderDelay = 5;
            }
        }

        if (reminderTask != null) {
            reminderTask.cancel();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (Team team : Team.values()) {
                    List<Ticket> openTickets = database.getOpenTicketsByTeam(team.name());
                    if (openTickets.size() > 0) {
                        List<UUID> broadcastedPlayers = new ArrayList<>();

                        for(Staff staff : getStaffOnDuty()) {
                            ProxiedPlayer player = getProxy().getPlayer(UUID.fromString(staff.getUuid()));

                            if (player != null && player.hasPermission(PERMISSION_START + team.permission) && !broadcastedPlayers.contains(player.getUniqueId())) {
                                broadcastedPlayers.add(player.getUniqueId());

                                TextComponent message1 = ChatBuilder.appendTextComponent(null, "There's currently ", PREFIX);
                                ChatBuilder.appendTextComponent(message1, openTickets.size() + "", ALT_COLOR);
                                ChatBuilder.appendTextComponent(message1, " open ticket(s) for the ", PREFIX);
                                ChatBuilder.appendTextComponent(message1, team.name() + " Team", team.color);
                                ChatBuilder.appendTextComponent(message1, ".", PREFIX);

                                TextComponent message2 = ChatBuilder.appendTextComponent(null, "Use ", PREFIX);
                                ChatBuilder.appendTextComponent(message2, "/request staff view " + team.name(), ALT_COLOR,
                                                                false, false, false, false, false,
                                                                null,
                                                                MessageUtil.hoverEvent("Click Here!"),
                                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/request staff view " + team.name()));
                                ChatBuilder.appendTextComponent(message2, " to view them.", PREFIX);

                                player.sendMessage(message1);
                                player.sendMessage(message2);
                            }
                        }
                    }
                }
            }
        };
        reminderTask = getProxy().getScheduler().schedule(this, runnable, 0, reminderDelay, TimeUnit.MINUTES);
    }

    /**
     * Cancel the reminder process that pings staff every 5 mins of open tickets, if it exists.
     */
    public void stopReminder() {
        if (reminderTask != null) {
            reminderTask.cancel();
        }
    }
}
