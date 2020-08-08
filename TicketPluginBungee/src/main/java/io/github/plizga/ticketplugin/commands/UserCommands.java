package io.github.plizga.ticketplugin.commands;


import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.plizga.ticketplugin.TicketPluginBungee;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Ticket;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.List;


public class UserCommands extends BaseCommand
{
    private StaffCommands staffCommands;
    private ReviewCommands reviewCommands;


    public UserCommands()
    {
        this.database = plugin.getDatabase();
        this.staffCommands = new StaffCommands();
        this.reviewCommands = new ReviewCommands();
    }

    @Cmd(value="Moderator access to tickets.", permission= TicketPluginBungee.PERMISSION_START + ".staff")
    public BaseCommand staff()
    {
        return staffCommands;
    }

    @Cmd(value="Allows access to reviews.")
    public BaseCommand review() {return reviewCommands;}

    @Cmd(value="Create a new ticket.", permission= TicketPluginBungee.PERMISSION_START + ".create")
    public void create(CommandSender sender, Team team,
                       @Arg(value="info", description="The description for the ticket being created.") String[] info)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            ServerInfo server = player.getServer().getInfo();
            if (server != null) {
                if (!player.getServer().getInfo().equals(server)) {
                    player.connect(server);
                }

                Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
                // perform a check to see if globally are no players
                if ( networkPlayers == null || networkPlayers.isEmpty() )
                {
                    return;
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(plugin.CREATE_SUB_CHANNEL);
                out.writeUTF(player.getUniqueId().toString());
                out.writeUTF(team.name());
                out.writeUTF(String.join(" ", info));

                server.sendData(plugin.CHANNEL, out.toByteArray());
            } else {
                msg(plugin.ERROR_COLOR + "Unable to find the server you're on. Are you still logged in?");
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "Only players may create a ticket."));
        }
    }

    @Cmd(value="look at the open tickets you have created", permission= TicketPluginBungee.PERMISSION_START + ".view")
    public void view(CommandSender sender)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            List<Ticket> openTickets = database.getPlayerOpenTickets(player.getName());

            if(openTickets.size() == 0)
            {
                sender.sendMessage(new TextComponent(plugin.PREFIX + "No open tickets to view!"));
                return;
            }

            readPlayerTickets(sender, openTickets);
            sender.sendMessage(new TextComponent(plugin.PREFIX + "To delete a ticket, use the command " + plugin.ALT_COLOR +
                                                 " /request cancel <number> " + plugin.PREFIX + ", where <number> is the ticket number as it appears" +
                                                 " on this list. \n Alternatively, use " + plugin.ALT_COLOR + "/request cancel all" +
                                                 plugin.PREFIX + " to cancel all of your current tickets."));
        }
        else
        {
            sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "Only players may get a list of their own tickets."));
        }

    }

    @Cmd(value="allows a player to cancel their own tickets", permission= TicketPluginBungee.PERMISSION_START + ".cancel")
    public void cancel(CommandSender sender, @Arg(value = "number (or 'all')", description = "number of ticket to delete") String num)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            List<Ticket> openTickets = database.getPlayerOpenTickets(player.getName());
            if(openTickets.size() == 0)
            {
                sender.sendMessage(new TextComponent(plugin.PREFIX + "No open tickets to cancel!"));
                return;
            }

            try
            {
                if(num.equals("all"))
                {
                    database.cancelTicketByPlayer(player.getName());
                    sender.sendMessage(new TextComponent(plugin.PREFIX +  "All of your open tickets have been cancelled."));
                }
                else
                {
                    //need try in case string is not a num
                    int numAsInt;
                    try
                    {
                        numAsInt = Integer.parseInt(num) - 1;
                    }
                    catch(NumberFormatException e)
                    {
                        sender.sendMessage(new TextComponent(plugin.ERROR_COLOR + "Failed to execute command. Use 'all' or a number" +
                                                             "instead of " + plugin.ALT_COLOR + num + plugin.ERROR_COLOR + "."));
                        return;
                    }
                    Ticket ticket = (Ticket) openTickets.get(numAsInt);
                    database.cancelTicketByUUID(ticket.getId());
                    sender.sendMessage(new TextComponent(plugin.PREFIX + "Ticket " + plugin.ALT_COLOR + num + plugin.PREFIX + " has been deleted."));
                }

            }
            catch(IndexOutOfBoundsException e)
            {
                sender.sendMessage(new TextComponent(plugin.ERROR_COLOR + "Ticket " + plugin.ALT_COLOR + num + plugin.ERROR_COLOR +
                                                     " does not exist. Please enter a valid ticket number to cancel."));
            }







        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may remove their own tickets.");
        }
    }


    /**
     * This method shares a similar function to the "comment" function of StaffCommands, and the majority of their code
     * has been moved to BaseCommand's "makeCommentBook" function, where a book of ticket comments is entered into a
     * book and handed to the player.
     * @param sender Prerequisite: Must be Player to succeed
     * @param uuid  the uuid of the ticket
     */
    @Cmd(value="access the comments section of a ticket.")
    public void comment(CommandSender sender, String uuid)
    {
        //TODO Update this when we update the make comment book on the Bukkit/Spigot/Paper side.
        //makeCommentBook(sender, uuid);
    }

    @Cmd(value="Allows a plyer to view their completed tickets.")
    public void viewCompleted(CommandSender sender)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            List<Ticket> completedTickets = database.getCompletedPlayerTickets(player.getUniqueId().toString());

            if(completedTickets.size() == 0)
            {
                sender.sendMessage(new TextComponent(plugin.PREFIX + "No completed tickets to view!"));
                return;
            }

            readPlayerCompletedTickets(sender, completedTickets);
        }
        else
        {
            msg(plugin.ERROR_COLOR + "Only players may view their completed tickets.");
        }
    }

    @Cmd(value="[Button] Allows a player to write a review for a ticket.")
    public void addReview(CommandSender sender, String ticketUUID)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            msg(plugin.PREFIX + "Please leave a review below, where 1 is the worst, and 5 is the best.\n");
            TextComponent oneStarButton = new TextComponent("[ * ");
            oneStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review one " + ticketUUID));
            oneStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("1")));

            TextComponent twoStarButton = new TextComponent("* ");
            twoStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review two " + ticketUUID));
            twoStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("2")));

            TextComponent threeStarButton = new TextComponent("* ");
            threeStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review three " + ticketUUID));
            threeStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("3")));

            TextComponent fourStarButton = new TextComponent("* ");
            fourStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review four " + ticketUUID));
            fourStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("4")));

            TextComponent fiveStarButton = new TextComponent("* ]");
            fiveStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review five " + ticketUUID));
            fiveStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("5")));
            msg(plugin.PREFIX + "Review Rating: ");
            ComponentBuilder componentBuilder = new ComponentBuilder("").append(oneStarButton).append(twoStarButton).append(threeStarButton).append(fourStarButton).append(fiveStarButton);
            sender.sendMessage(componentBuilder.create());
        }
        else
        {
            msg(plugin.ERROR_COLOR + "Only players may leave reviews on their tickets.");
        }
    }
}
