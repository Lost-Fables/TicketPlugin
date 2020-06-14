package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.util.MessageUtil;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.helpers.OfflineStorage;
import io.github.plizga.ticketplugin.helpers.Ticket;
import io.github.plizga.ticketplugin.database.Database;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.List;


public abstract class BaseCommand extends CommandTemplate
{
    protected TicketPlugin plugin = TicketPlugin.getTicketPluginInstance();

    private final String TICKET_BORDER = "~~~~~~~~~~~~~~~";
//todo here
    private OfflineStorage offlineStorage = new OfflineStorage(plugin);

    /**
     * Provides for a means of accessing tickets and gathering their basic info for an easily parsed through list.
     * This is for staff use.
     * Additional info: Creates a button which allows the staff to expand tickets.
     * @param sender    the command sender such as a player
     * @param ticketList    the ticket list being passed to print out
     */
    void readTicketsBasic(CommandSender sender, List ticketList)
    {
        //int index = 1; //omg an index!!!!
        /*
        Player player = null;
        if(sender instanceof  Player)
        {
            player = (Player) sender;
        }

        for(Object o : ticketList)
        {
            //msg(plugin.PREFIX + "\nTicket " + index + ":");
            Ticket ticket = (Ticket) o;
            if(ticket.getAssignedModerator().equals("None"))
            {
                msg(plugin.PREFIX + ticket.toBasicInfo());
            }
            else
            {
                msg(plugin.PREFIX + ticket.toBasicInfoClaimed());

                if(player != null && ticket.getAssignedModerator().equals(player.getName()))
                {
                    BaseComponent cmdButton = MessageUtil.CommandButton("Close this Ticket", "/" + plugin.COMMAND_START + " staff closeTicket " + ticket.getId());
                    msg(cmdButton);
                }

            }
            BaseComponent cmdButton = MessageUtil.CommandButton("Expand This Ticket", "/" + plugin.COMMAND_START + " staff expandTicket " + ticket.getId());

            msg(cmdButton);

            msg(plugin.ALT_COLOR + TICKET_BORDER + "\n");
            //index++;*/

        int index = 1;

        for(Object o: ticketList)
        {
            Ticket ticket = (Ticket) o;
            ComponentBuilder componentBuilder = ticket.toBasicInfo();

            sender.spigot().sendMessage(componentBuilder.create());


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
            msg("\nTicket " + index + ":");
            Ticket ticket = (Ticket) o;
            sender.spigot().sendMessage(ticket.toPlayerInfo().create());
            BaseComponent cmdButton = MessageUtil.CommandButton("View Comments", "/" + plugin.COMMAND_START + " comment " + ticket.getId());
            msg(cmdButton);
            msg(plugin.PREFIX + TICKET_BORDER + "\n");
            index++;

        }

    }

    void readPlayerCompletedTickets(CommandSender sender, List<Ticket> completedTickets)
    {
        int index = 1;

        for(Ticket t : completedTickets)
        {
            msg("\nTicket " + index + ":");
            sender.spigot().sendMessage(t.toPlayerInfo().create());
            BaseComponent cmdButton = MessageUtil.CommandButton("View Comments", "/" + plugin.COMMAND_START + " comment " + t.getId());
            msg(cmdButton);

            Database database = plugin.getDatabase();

            if(database.getReview(t.getId()) == null)
            {
                BaseComponent cmdButton2 = MessageUtil.CommandButton("Add Review", "/" + plugin.COMMAND_START + " addReview " + t.getId());
                msg(cmdButton2);
            }

            index++;

        }
    }

    void sendReassignMessage(Ticket ticket, String team)
    {
        Player player = Bukkit.getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(plugin.PREFIX + "Your ticket, with the description \"" + plugin.ALT_COLOR +
                    ticket.getInfo() + plugin.PREFIX + ",\" has been reassigned to the " + plugin.ALT_COLOR +
                    team + plugin.PREFIX + " team!");
        }


    }

    void sendCompletedMessage(Ticket ticket)
    {
        Player player = Bukkit.getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(plugin.PREFIX + "Your ticket, with the description \"" + plugin.ALT_COLOR +
                    ticket.getInfo() + plugin.PREFIX + "\" has been completed! Use " + plugin.ALT_COLOR + "\"/" +
                    plugin.COMMAND_START + " viewCompleted\"" + plugin.PREFIX + "to add a review!");
        }
    }

    void sendCommentMessage(Ticket ticket)
    {
        Player player = Bukkit.getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(plugin.PREFIX + "A comment has been added to your ticket, with the description \"" + plugin.ALT_COLOR +
                    ticket.getInfo() + plugin.PREFIX + "\" Use " + plugin.ALT_COLOR + "\"/" +
                    plugin.COMMAND_START + " view\"" + plugin.PREFIX + ", and click \"View Comments\" to view!");
        }
    }

    void sendClaimedMessage(Ticket ticket)
    {
        Player player = Bukkit.getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(plugin.PREFIX + "Your ticket, with the description \"" + plugin.ALT_COLOR +
                    ticket.getInfo() + plugin.PREFIX + "\" has been claimed by " + plugin.ALT_COLOR + ticket.getAssignedModerator() +
                    "!");
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
