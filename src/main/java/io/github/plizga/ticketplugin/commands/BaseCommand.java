package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.CommandTemplate;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.sqlite.Ticket;
import org.bukkit.command.CommandSender;

import java.util.List;


public abstract class BaseCommand extends CommandTemplate
{
    protected TicketPlugin plugin = TicketPlugin.getTicketPluginInstance();

    /**
     * Provides for a means of accessing tickets and gathering their basic info for an easily parsed through list.
     * @param sender    the command sender such as a player
     * @param ticketList    the ticket list being passed to print out
     */
    void readTicketsBasic(CommandSender sender, List ticketList)
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

    /**
     * Helper method that reads and prints the tickets in a ticket list back out to the sender in BASIC FORM
     * @param sender    the {CommandSender} who wants those motherfuckin tickets!!!!
     * @param ticketList    the motherfuckin ticket list!!!!!!!!!!
     */
    void readPlayerTickets(CommandSender sender, List ticketList)
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
