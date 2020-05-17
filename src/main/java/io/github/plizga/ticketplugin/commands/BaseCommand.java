package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.util.MessageUtil;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.sqlite.Database;
import io.github.plizga.ticketplugin.sqlite.Ticket;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.List;


public abstract class BaseCommand extends CommandTemplate
{
    protected TicketPlugin plugin = TicketPlugin.getTicketPluginInstance();

    private final String TICKET_BORDER = "~~~~~~~~~~~~~~~";

    /**
     * Provides for a means of accessing tickets and gathering their basic info for an easily parsed through list.
     * This is for staff use.
     * Additional info: Creates a button which allows the staff to expand tickets.
     * @param sender    the command sender such as a player
     * @param ticketList    the ticket list being passed to print out
     */
    void readTicketsBasic(CommandSender sender, List ticketList)
    {
        int index = 1; //omg an index!!!!

        for(Object o : ticketList)
        {
            sender.sendMessage(plugin.PREFIX + "\nTicket " + index + ":");
            Ticket ticket = (Ticket) o;
            sender.sendMessage(plugin.PREFIX + ticket.toBasicInfo());
            BaseComponent cmdButton = MessageUtil.CommandButton("Expand This Ticket", "/request staff expandTicket " + ticket.getId());

            sender.spigot().sendMessage(cmdButton);

            sender.sendMessage(plugin.ALT_COLOR + TICKET_BORDER + "\n");
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
            sender.sendMessage("\nTicket " + index + ":");
            Ticket ticket = (Ticket) o;
            sender.sendMessage(ticket.toPlayerInfo());

            sender.sendMessage(plugin.PREFIX + TICKET_BORDER + "\n\n");
            index++;

        }

    }

    protected int getTotalPagesForPagination(int size, int contentPerPage)
    {
        int totalPageCount;
        if((size % contentPerPage) == 0)
        {
            totalPageCount = size / contentPerPage;
        }
        else
        {
            totalPageCount = (size / contentPerPage) + 1;
        }

        return totalPageCount;
    }

    /**
     * Helper method to assist in pagination of data given a list of values.
     *
     * PRECONDITION: the parameter list has a size > 0.
     *
     * @param sender    the person who the data is going to
     * @param list  the list being paginated
     * @param page  the page number
     * @param contentPerPage    amount of content to be shown per page.
     */
    protected void paginate(CommandSender sender, List list, int page, int totalPageCount, int contentPerPage)
    {

        if(page <= totalPageCount)
        {
            String paginatedFirstLine = String.valueOf(page) + "/" + String.valueOf(totalPageCount);
            sender.sendMessage(paginatedFirstLine);

            //begin a line
            int index = 0, subIndex = 0;
            page--;

            for(Object object : list)
            {
                index++;
                if((((page * contentPerPage) + subIndex + 1) == index) &&
                        (index != ((page * contentPerPage) + contentPerPage + 1)))
                {
                    subIndex++;
                    sender.sendMessage((String) object);
                }
            }

            //end of a line
        }
        else
        {
            sender.sendMessage(plugin.PREFIX + "There are only " + plugin.ALT_COLOR + totalPageCount + plugin.PREFIX + "pages.");
        }




    }


}
