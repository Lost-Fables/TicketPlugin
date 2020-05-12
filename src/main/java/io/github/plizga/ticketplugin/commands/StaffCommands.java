package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.annotate.Cmd;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.sqlite.Database;
import io.github.plizga.ticketplugin.sqlite.Ticket;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffCommands extends BaseCommand
{
    private Database database;
    public StaffCommands()
    {
        this.database = plugin.getDatabase();
    }

    @Cmd(value="look at the list of tickets for a specific team")
    public void view(CommandSender sender, Team team)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(team)) ||
            player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Admin)))
            {
                List openTickets = database.getTicketsByTeam(team.name());

                if(openTickets.size() == 0)
                {
                    sender.sendMessage(plugin.PREFIX + "There are no open tickets for the " + plugin.ALT_COLOR + team.name() +
                            plugin.PREFIX + " team.");
                    return;
                }
                sender.sendMessage(plugin.PREFIX + "Viewing tickets for the " + plugin.ALT_COLOR + team.name() +
                        plugin.PREFIX + " team:");
                readTicketsBasic(sender, openTickets);
            }
            else
            {
                sender.sendMessage(plugin.ERROR_COLOR + "You do not have permission to view that team's tickets!");
            }
        }
        else
        {
            List openTickets = database.getTicketsByTeam(team.name());

            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "There are no open tickets for the " + plugin.ALT_COLOR + team.name() +
                        plugin.PREFIX + " team.");
                return;
            }
            sender.sendMessage(plugin.PREFIX + "Viewing tickets for the " + plugin.ALT_COLOR + team.name() +
                    plugin.PREFIX + " team:");
            readTicketsBasic(sender, openTickets);
        }

    }

    @Cmd(value="look at the list of all available tickets for the staff member initiating the command.")
    public void viewAll(CommandSender sender)
    {
       ArrayList<Ticket> openTickets = new ArrayList<>();

        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Admin)))
            {
                sender.sendMessage("DEV: Player has admin permission.");

                openTickets.addAll(database.getTicketsByTeam(Team.Admin.name()));

                openTickets.addAll(database.getTicketsByTeam(Team.Tech.name()));

                openTickets.addAll(database.getTicketsByTeam(Team.Build.name()));

                openTickets.addAll(database.getTicketsByTeam(Team.Design.name()));

                openTickets.addAll(database.getTicketsByTeam(Team.Event.name()));

                openTickets.addAll(database.getTicketsByTeam(Team.Lore.name()));

                openTickets.addAll(database.getTicketsByTeam(Team.Moderator.name()));
            }
            else
            {
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Tech)))
                {
                    openTickets.addAll(database.getTicketsByTeam(Team.Tech.name()));
                    sender.sendMessage("DEV Player has dev permission.");
                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Moderator)))
                {
                    openTickets.addAll(database.getTicketsByTeam(Team.Moderator.name()));
                    sender.sendMessage("DEV Player has mod permission.");
                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Event)))
                {
                    openTickets.addAll(database.getTicketsByTeam(Team.Event.name()));
                    sender.sendMessage("DEV Player has event permission.");
                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Lore)))
                {
                    openTickets.addAll(database.getTicketsByTeam(Team.Lore.name()));
                    sender.sendMessage("DEV Player has lore permission.");
                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Build)))
                {
                    openTickets.addAll(database.getTicketsByTeam(Team.Build.name()));
                    sender.sendMessage("DEV Player has build permission.");
                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Design)))
                {
                    openTickets.addAll(database.getTicketsByTeam(Team.Design.name()));
                    sender.sendMessage("DEV Player has design permission.");
                }


                openTickets.addAll(database.getTicketsByTeam(Team.Global.name()));


            }

            Collections.sort(openTickets);

            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "There are no open tickets for any teams you are a part of. Pretty cool!");
                return;
            }
            sender.sendMessage(plugin.PREFIX + "Viewing tickets for your teams:");
            readTicketsBasic(sender, openTickets);

        }

    }

    @Cmd(value="expands a ticket from a list of tickets. You could theoretically enter the entire uuid but this is called from the expand buttons.")
    public void expandTicket(CommandSender sender, String uuid)
    {
        try
        {
            Database database = plugin.getDatabase();
            Ticket ticket =  database.getTicketByUUID(uuid);
            sender.sendMessage(plugin.PREFIX + "Ticket Information: \n" + ticket.toString());
            sender.sendMessage("\n");
        }
        catch(NullPointerException e)
        {
            sender.sendMessage(plugin.ERROR_COLOR + "Ticket not found! Contact a developer if this continues to occur.");
        }

    }

}
