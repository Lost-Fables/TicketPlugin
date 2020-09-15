package io.github.plizga.ticketplugin.commands;


import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;

import co.lotc.core.command.annotate.Range;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.plizga.ticketplugin.TicketPluginBungee;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Ticket;

import io.github.plizga.ticketplugin.listeners.TicketPlayerListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


public class UserCommands extends BaseCommand
{
    private StaffCommands staffCommands;

    public UserCommands()
    {
        this.database = plugin.getDatabase();
        this.staffCommands = new StaffCommands();
    }

    @Cmd(value="Moderator access to tickets.", permission= TicketPluginBungee.PERMISSION_START + ".staff")
    public BaseCommand staff()
    {
        return staffCommands;
    }

    @Cmd(value="Allows access to reviews.")
    public void review(CommandSender sender, String ticketUUID, @Range(min=1, max=5)int rating) {
        database.createNewReview(ticketUUID, rating);
        sender.sendMessage(ChatBuilder.appendTextComponent(null, "Thank you for submitting a review!", plugin.PREFIX));
    }

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

                UUID uuid = UUID.randomUUID();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(plugin.CREATE_SUB_CHANNEL);
                out.writeUTF(uuid.toString());
                out.writeUTF(player.getUniqueId().toString());
                out.writeUTF(team.name());
                out.writeUTF(String.join(" ", info));

                if (!TicketPlayerListener.waiting.contains(uuid)) {
                    TicketPlayerListener.waiting.add(uuid);
                }
                server.sendData(plugin.CHANNEL, out.toByteArray());
            } else {
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "Unable to find the server you're on. Are you still logged in?", plugin.ERROR_COLOR));
            }
        } else {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players may create a ticket.", plugin.ERROR_COLOR));
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
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "No open tickets to view!", plugin.PREFIX));
                return;
            }

            readPlayerTickets(sender, openTickets);

            TextComponent message = ChatBuilder.appendTextComponent(null, "To delete a ticket, use the command ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, " /request cancel <number> ", plugin.ALT_COLOR);
            ChatBuilder.appendTextComponent(message, ", where <number> is the ticket number as it appears on this list. \n Alternatively, use ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, "/request cancel all", plugin.ALT_COLOR);
            ChatBuilder.appendTextComponent(message, " to cancel all of your current tickets.", plugin.PREFIX);
            sender.sendMessage(message);
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players may get a list of their own tickets.", plugin.ERROR_COLOR));
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
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "No open tickets to cancel!", plugin.PREFIX));
                return;
            }

            try
            {
                if(num.equalsIgnoreCase("all"))
                {
                    database.cancelTicketByPlayer(player.getName());
                    sender.sendMessage(ChatBuilder.appendTextComponent(null, "All of your open tickets have been cancelled.", plugin.PREFIX));
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
                        TextComponent message = ChatBuilder.appendTextComponent(null, "Failed to execute command. Use 'all' or a number instead of ", plugin.ERROR_COLOR);
                        ChatBuilder.appendTextComponent(message, num, plugin.ALT_COLOR);
                        ChatBuilder.appendTextComponent(message, ".", plugin.ERROR_COLOR);
                        sender.sendMessage(message);
                        return;
                    }
                    Ticket ticket = (Ticket) openTickets.get(numAsInt);
                    database.cancelTicketByUUID(ticket.getId());

                    TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket ", plugin.PREFIX);
                    ChatBuilder.appendTextComponent(message, num, plugin.ALT_COLOR);
                    ChatBuilder.appendTextComponent(message, " has been deleted.", plugin.PREFIX);
                    sender.sendMessage(message);
                }

            }
            catch(IndexOutOfBoundsException e)
            {
                TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket ", plugin.ERROR_COLOR);
                ChatBuilder.appendTextComponent(message, num, plugin.ALT_COLOR);
                ChatBuilder.appendTextComponent(message, " does not exist. Please enter a valid ticket number to cancel.", plugin.ERROR_COLOR);
                sender.sendMessage(message);
            }
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players may remove their own tickets.", plugin.ERROR_COLOR));
        }
    }


    /**
     * This method shares a similar function to the "comment" function of StaffCommands, and the majority of their code
     * has been moved to BaseCommand's "makeCommentBook" function, where a book of ticket comments is force opened.
     * @param sender Prerequisite: Must be Player to succeed
     * @param uuid  the uuid of the ticket
     */
    @Cmd(value="access the comments section of a ticket.")
    public void comment(CommandSender sender, String uuid)
    {
        makeCommentBook(sender, uuid);
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
                TextComponent message = new TextComponent("No completed tickets to view!");
                message.setColor(plugin.PREFIX);
                sender.sendMessage(message);
                return;
            }

            readPlayerCompletedTickets(sender, completedTickets);
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players may view their completed tickets.", plugin.ERROR_COLOR));
        }
    }

    @Cmd(value="[Button] Allows a player to write a review for a ticket.")
    public void addReview(CommandSender sender, String ticketUUID)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            msg(plugin.PREFIX + "Please leave a review below, where 1 is the worst, and 5 is the best.\n");
            TextComponent oneStarButton = new TextComponent("[ ☆ ");
            oneStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review " + ticketUUID + " 1"));
            oneStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("1")));

            TextComponent twoStarButton = new TextComponent("☆ ");
            twoStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review " + ticketUUID + " 2"));
            twoStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("2")));

            TextComponent threeStarButton = new TextComponent("☆ ");
            threeStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review " + ticketUUID + " 3"));
            threeStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("3")));

            TextComponent fourStarButton = new TextComponent("☆ ");
            fourStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review " + ticketUUID + " 4"));
            fourStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("4")));

            TextComponent fiveStarButton = new TextComponent("☆ ]");
            fiveStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review " + ticketUUID + " 5"));
            fiveStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("5")));
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Review Rating:", plugin.PREFIX));
            ComponentBuilder componentBuilder = new ComponentBuilder("").append(oneStarButton).append(twoStarButton).append(threeStarButton).append(fourStarButton).append(fiveStarButton);
            sender.sendMessage(componentBuilder.create());
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players may leave reviews on their tickets.", plugin.ERROR_COLOR));
        }
    }

    @Cmd(value="List the available teams.")
    public void teams(CommandSender sender) {
        for (Team team : Team.values()) {
            TextComponent message = ChatBuilder.appendTextComponent(null, "Team '", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, team.name(), team.color);
            ChatBuilder.appendTextComponent(message, "' with the permission ending '" + team.permission + "'", plugin.PREFIX);
            sender.sendMessage(message);
        }
    }
}
