package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.util.MessageUtil;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.plizga.ticketplugin.TicketPluginBungee;
import io.github.plizga.ticketplugin.helpers.Comment;
import io.github.plizga.ticketplugin.helpers.OfflineStorage;
import io.github.plizga.ticketplugin.helpers.Ticket;
import io.github.plizga.ticketplugin.database.Database;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.List;


public abstract class BaseCommand extends CommandTemplate
{
    protected TicketPluginBungee plugin = TicketPluginBungee.getTicketPluginBungeeInstance();
    protected Database database = plugin.getDatabase();

    private static final String TICKET_BORDER = "~~~~~~~~~~~~~~~";
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
        int index = 1;

        for(Object o: ticketList)
        {
            Ticket ticket = (Ticket) o;
            ComponentBuilder componentBuilder = ticket.toBasicInfo();

            sender.sendMessage(componentBuilder.create());


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
            sender.sendMessage(ticket.toPlayerInfo().create());
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
            sender.sendMessage(t.toPlayerInfo().create());
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
        ProxiedPlayer player = plugin.getProxy().getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(new TextComponent(plugin.PREFIX + "Your ticket, with the description \"" + plugin.ALT_COLOR +
                                                 ticket.getInfo() + plugin.PREFIX + ",\" has been reassigned to the " + plugin.ALT_COLOR +
                                                 team + plugin.PREFIX + " team!"));
        }


    }

    void sendCompletedMessage(Ticket ticket)
    {
        ProxiedPlayer player = plugin.getProxy().getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(new TextComponent(plugin.PREFIX + "Your ticket, with the description \"" + plugin.ALT_COLOR +
                                                 ticket.getInfo() + plugin.PREFIX + "\" has been completed! Use " + plugin.ALT_COLOR + "\"/" +
                                                 plugin.COMMAND_START + " viewCompleted\"" + plugin.PREFIX + "to add a review!"));
        }
    }

    void sendCommentMessage(Ticket ticket)
    {
        ProxiedPlayer player = plugin.getProxy().getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(new TextComponent(plugin.PREFIX + "A comment has been added to your ticket, with the description \"" + plugin.ALT_COLOR +
                                                 ticket.getInfo() + plugin.PREFIX + "\" Use " + plugin.ALT_COLOR + "\"/" +
                                                 plugin.COMMAND_START + " view\"" + plugin.PREFIX + ", and click \"View Comments\" to view!"));
        }
    }

    void sendClaimedMessage(Ticket ticket)
    {
        ProxiedPlayer player = plugin.getProxy().getPlayer(ticket.getPlayerName());

        if(player != null)
        {
            player.sendMessage(new TextComponent(plugin.PREFIX + "Your ticket, with the description \"" + plugin.ALT_COLOR +
                                                 ticket.getInfo() + plugin.PREFIX + "\" has been claimed by " + plugin.ALT_COLOR + ticket.getAssignedModerator() +
                                                 "!"));
        }
    }


    /**
     * This function generates a WRITTEN book of comments for a given ticket.
     * @param sender    the (Player) receiving the book of comments
     * @param uuid  the ticket from which the comments are related to.
     */
    protected void makeCommentBook(CommandSender sender, String uuid)
    {
        if (sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            List<Comment> comments = database.getCommentsForPlayer(uuid);

            ServerInfo server = player.getServer().getInfo();
            if (server != null) {
                Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
                // perform a check to see if there are globally no players
                if (networkPlayers == null || networkPlayers.isEmpty())
                {
                    return;
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(plugin.COMMENT_SUB_CHANNEL);
                out.writeUTF(player.getUniqueId().toString());
                out.writeUTF(uuid);

                if (!comments.isEmpty())
                {
                    for (Comment c : comments)
                    {
                        out.writeUTF(c.toString());
                    }
                } else
                {
                    out.writeUTF("There are no comments for this ticket!");
                }

                server.sendData(plugin.CHANNEL, out.toByteArray());
            } else {
                msg(plugin.ERROR_COLOR + "Unable to find the server you're on. Are you still online?");
            }
        }
        else
        {
            msg("Only players may access and modify comments.");
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
            sender.sendMessage(new TextComponent(paginatedFirstLine));

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
                    sender.sendMessage(new TextComponent((String) object));
                }
            }

            //end of a line
        }
        else
        {
            sender.sendMessage(new TextComponent(plugin.PREFIX + "There are only " + plugin.ALT_COLOR + totalPageCount + plugin.PREFIX + "pages."));
        }




    }


}
