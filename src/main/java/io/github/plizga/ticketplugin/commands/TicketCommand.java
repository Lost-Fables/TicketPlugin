package io.github.plizga.ticketplugin.commands;

import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.util.MessageUtil;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.sqlite.Database;
import io.github.plizga.ticketplugin.sqlite.Ticket;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class TicketCommand extends BaseCommand
{

    private Database database;


    public TicketCommand()
    {
        this.database = plugin.getDatabase();
    }
    @Cmd(value="Create a new ticket.", permission=TicketPlugin.PERMISSION_START + ".create")
    public void create(CommandSender sender, @Arg(value = "team", description = "the team to be assigned.") String team,
                       @Arg(value="info", description="The description for the ticket being created.") String[] info)
    {

        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            String infoMessage = String.join(" ", info);
            if(Team.isTeam(team))
            {
                database.createNewTicket(player, Status.OPEN, Team.valueOf(team.toUpperCase()), infoMessage, false);
            }
            else
            {
                infoMessage = String.join(" ", team, infoMessage);
                database.createNewTicket(player, Status.OPEN, Team.NONE, infoMessage, false);
            }

            sender.sendMessage(plugin.PREFIX + "Your ticket, with the description: " +  plugin.ALT_COLOR +
                    infoMessage + plugin.PREFIX + " has been created!");

        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may create a ticket.");
        }
    }

    @Cmd(value="look at the open tickets you have created", permission=TicketPlugin.PERMISSION_START + ".viewMyTickets")
    public void viewMyTickets(CommandSender sender)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            List openTickets = database.getPlayerOpenTickets(player.getName().toLowerCase());

            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "No open tickets to view!");
                return;
            }

            readPlayerTickets(sender, openTickets);
            sender.sendMessage(plugin.PREFIX + "To delete a ticket, use the command " + plugin.ALT_COLOR +
                    " /ticket cancel <number> " + plugin.PREFIX + ",   where <number> is the ticket number as it appears" +
                    " on this list.");
        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may get a list of their own tickets.");
        }

    }

    @Cmd(value="allows a player to cancel their own tickets", permission=TicketPlugin.PERMISSION_START + ".cancel")
    public void cancel(CommandSender sender, @Arg(value = "number", description = "number of ticket to delete") int num)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            List openTickets = database.getPlayerOpenTickets(player.getName().toLowerCase());
            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "No open tickets to cancel!");
                return;
            }

            try
            {
                Ticket ticket = (Ticket) openTickets.get(num - 1);
                database.removeTicket(ticket.getId());
                sender.sendMessage(plugin.PREFIX + "Ticket " + plugin.ALT_COLOR + num + plugin.PREFIX + " has been deleted.");
            }
            catch(IndexOutOfBoundsException e)
            {
                sender.sendMessage(plugin.ERROR_COLOR + "Ticket " + plugin.ALT_COLOR + num + plugin.ERROR_COLOR +
                        " does not exist. Please enter a valid ticket number to cancel.");
            }







        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may remove their own tickets.");
        }
    }



    @Cmd(value="look at the list of tickets", permission=TicketPlugin.PERMISSION_START + ".viewOpenTickets")
    public void viewOpenTickets(CommandSender sender)
    {


        List openTickets = database.getOpenTickets();

        if(openTickets.size() == 0)
        {
            sender.sendMessage(plugin.PREFIX + "No open tickets to view!");
            return;
        }
        readTicketsBasic(sender, openTickets);
    }

    /**
     * Helper method that reads and prints the tickets in a ticket list back out to the sender in BASIC FORM
     * @param sender    the {CommandSender} who wants those motherfuckin tickets!!!!
     * @param ticketList    the motherfuckin ticket list!!!!!!!!!!
     */
    private void readTicketsBasic(CommandSender sender, List ticketList)
    {
        int index = 1; //omg an index!!!!
        for(Object o : ticketList)
        {
            sender.sendMessage("Ticket " + index + ":");
            Ticket ticket = (Ticket) o;
            sender.sendMessage(plugin.PREFIX + ticket.toBasicInfo());
            sender.sendMessage("\n\n");
            index++;

        }
    }

    private void readPlayerTickets(CommandSender sender, List ticketList)
    {
        int index = 1; //omg an index!!!!
        for(Object o : ticketList)
        {
            sender.sendMessage("Ticket " + index + ":");
            Ticket ticket = (Ticket) o;
            sender.sendMessage(plugin.PREFIX + ticket.toPlayerInfo());
            sender.sendMessage("\n\n");
            index++;

        }
    }



}
