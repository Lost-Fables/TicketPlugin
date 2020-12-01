package io.github.plizga.ticketplugin.commands;

import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.util.MessageUtil;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.plizga.ticketplugin.TicketPluginBungee;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.database.Database;
import io.github.plizga.ticketplugin.helpers.Staff;
import io.github.plizga.ticketplugin.helpers.Ticket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StaffCommands extends BaseCommand
{
    public StaffCommands()
    {
        this.database = plugin.getDatabase();
    }

    @Cmd(value ="Internally used to reassign tickets to various teams.")
    public void reassign(CommandSender sender, Team team, String uuid)
    {
        database.setTeam(uuid, team.name());

        if(!team.equals(Team.Global))
        {
            msg(TicketPluginBungee.PREFIX + "Ticket has been reassigned to the " + TicketPluginBungee.ALT_COLOR + team.name() +
                TicketPluginBungee.PREFIX + " team.");
        }
        else
        {msg(TicketPluginBungee.PREFIX + "Ticket has been reassigned to " + TicketPluginBungee.ALT_COLOR + team.name() +
             TicketPluginBungee.PREFIX + ".");

        }

        Ticket ticket = database.getTicketByUUID(uuid);
        sendReassignMessage(ticket, team);
        plugin.notifyOnDutyStaff(team);
    }

    @Cmd(value="look at the list of tickets for a specific team")
    public void view(CommandSender sender, Team team)
    {
        ProxiedPlayer player = null;
        if(sender instanceof ProxiedPlayer)
        {
            player = (ProxiedPlayer) sender;
        }

        if(player == null ||
           player.hasPermission(TicketPluginBungee.PERMISSION_START + team.permission) ||
           player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Admin.permission)) {
            List<Ticket> openTickets = database.getOpenTicketsByTeam(team.name());

            if (openTickets.size() == 0) {
                TextComponent message = ChatBuilder.appendTextComponent(null, "There are no open tickets for the ", TicketPluginBungee.PREFIX);
                ChatBuilder.appendTextComponent(message, team.name(), team.color);
                ChatBuilder.appendTextComponent(message, " team.", TicketPluginBungee.PREFIX);
                sender.sendMessage(message);
                return;
            }
            TextComponent message = ChatBuilder.appendTextComponent(null, "Viewing tickets for the ", TicketPluginBungee.PREFIX);
            ChatBuilder.appendTextComponent(message, team.name(), team.color);
            ChatBuilder.appendTextComponent(message, " team:", TicketPluginBungee.PREFIX);
            sender.sendMessage(message);

            readTicketsBasic(sender, openTickets);
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "You do not have permission to view that team's tickets!", TicketPluginBungee.ERROR_COLOR));
        }
    }

    @Cmd(value="look at the list of all available tickets for the staff member initiating the command.")
    public void viewAll(CommandSender sender)
    {
       List<Ticket> openTickets = new ArrayList<>();

        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Admin.permission))
            {
                openTickets.addAll(database.getAllOpenTickets());
            }
            else
            {
                if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Tech.permission))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Tech.name()));
                }
                if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Moderator.permission))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Moderator.name()));
                }
                if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Event.permission))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Event.name()));
                }
                if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Lore.permission))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Lore.name()));
                }
                if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Build.permission))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Build.name()));
                }
                /*if(player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Design.permission))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Design.name()));
                }*/


                openTickets.addAll(database.getOpenTicketsByTeam(Team.Global.name()));
            }


            if(openTickets.size() == 0)
            {
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "There are no open tickets for any teams you are a part of. Pretty cool!", TicketPluginBungee.PREFIX));
                return;
            }
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Viewing tickets for your teams:", TicketPluginBungee.PREFIX));

            readTicketsBasic(sender, openTickets);

        }

    }

    @Cmd(value="allows a staff member to claim or unclaim a ticket, based upon whether they have currently claimed it or not.")
    public void claim(CommandSender sender, String uuid)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            try
            {
                Database database = plugin.getDatabase();
                Ticket ticket = database.getTicketByUUID(uuid);

                String assignedModerator = ticket.getAssignedStaff();

                if(ticket.getStatus() == Status.OPEN)
                {
                    database.claimTicket(ticket.getId(), player.getName());
                    TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket has been ", TicketPluginBungee.PREFIX);
                    ChatBuilder.appendTextComponent(message, "claimed", TicketPluginBungee.ALT_COLOR);
                    ChatBuilder.appendTextComponent(message, ".", TicketPluginBungee.PREFIX);
                    sender.sendMessage(message);
                    sendClaimedMessage(database.getTicketByUUID(uuid));
                }
                else if(assignedModerator.equalsIgnoreCase(player.getName()))
                {
                    database.unClaimTicket(uuid);
                    TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket has been ", TicketPluginBungee.PREFIX);
                    ChatBuilder.appendTextComponent(message, "unclaimed", TicketPluginBungee.ALT_COLOR);
                    ChatBuilder.appendTextComponent(message, ".", TicketPluginBungee.PREFIX);
                    sender.sendMessage(message);
                }
                else if(player.hasPermission(TicketPluginBungee.PERMISSION_START + "." + ticket.getTeam().name() + ".manager"))
                {
                    database.unClaimTicket(uuid);
                    database.claimTicket(uuid, player.getName());
                    sender.sendMessage(ChatBuilder.appendTextComponent(null, "You have used your manager permissions to claim this ticket.", TicketPluginBungee.PREFIX));
                    sendClaimedMessage(database.getTicketByUUID(uuid));
                }
                else
                {
                    TextComponent message = ChatBuilder.appendTextComponent(null, "This ticket has been claimed by ", TicketPluginBungee.PREFIX);
                    ChatBuilder.appendTextComponent(message, ticket.getAssignedStaff(), TicketPluginBungee.ALT_COLOR);
                    ChatBuilder.appendTextComponent(message, ".", TicketPluginBungee.PREFIX);
                    sender.sendMessage(message);
                }
            }
            catch(NullPointerException e)
            {
                TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket not found! Contact a developer if this continues to occur.", TicketPluginBungee.ERROR_COLOR);
                ChatBuilder.appendTextComponent(message, " Method: claim", TicketPluginBungee.ALT_COLOR);
                sender.sendMessage(message);
            }
        }
        else
        {
            sender.sendMessage(new TextComponent("Only players may claim tickets dude. " +
                                                 "If a console claimed a ticket, what do you think would happen? Seriously, what would happen? " +
                                                 "Tell me. I'd like to know."));
        }

    }


    @Cmd(value="Allows staff members to view claimed tickets for a specified team.")
    public void viewClaimed(CommandSender sender, Team team)
    {
        ProxiedPlayer player = null;
        if(sender instanceof ProxiedPlayer)
        {
            player = (ProxiedPlayer) sender;
        }

        if(player == null ||
           player.hasPermission(TicketPluginBungee.PERMISSION_START + team.permission) ||
           player.hasPermission(TicketPluginBungee.PERMISSION_START + Team.Admin.permission)) {
            List<Ticket> openTickets = database.getTeamClaimedTickets(team.name());

            if (openTickets.size() == 0) {
                TextComponent message = ChatBuilder.appendTextComponent(null, "There are no claimed tickets for the ", TicketPluginBungee.PREFIX);
                ChatBuilder.appendTextComponent(message, team.name(), team.color);
                ChatBuilder.appendTextComponent(message, " team.", TicketPluginBungee.PREFIX);
                sender.sendMessage(message);
                return;
            }
            TextComponent message = ChatBuilder.appendTextComponent(null, "Viewing claimed tickets for the ", TicketPluginBungee.PREFIX);
            ChatBuilder.appendTextComponent(message, team.name(), team.color);
            ChatBuilder.appendTextComponent(message, " team:", TicketPluginBungee.PREFIX);
            sender.sendMessage(message);

            readTicketsBasic(sender, openTickets);
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "You do not have permission to view that team's claimed tickets!", TicketPluginBungee.ERROR_COLOR));
        }
    }

    @Cmd(value="Allows staff members to view all of their currently claimed tickets.")
    public void viewMyClaimed(CommandSender sender)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            try
            {
                Database database = plugin.getDatabase();
                List<Ticket> claimedTickets = database.getClaimedTickets(player.getName());

                Collections.sort(claimedTickets);

                if(claimedTickets.size() == 0)
                {
                    sender.sendMessage(ChatBuilder.appendTextComponent(null, "You have no currently claimed tickets!", TicketPluginBungee.PREFIX));
                    return;
                }

                sender.sendMessage(ChatBuilder.appendTextComponent(null, "Viewing your claimed tickets:", TicketPluginBungee.PREFIX));
                readTicketsBasic(sender, claimedTickets);
            }
            catch(NullPointerException e)
            {
                TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket not found! Contact a developer if this continues to occur.", TicketPluginBungee.ERROR_COLOR);
                ChatBuilder.appendTextComponent(message, " Method: viewMyClaimed", TicketPluginBungee.ALT_COLOR);
                sender.sendMessage(message);
            }
        }
        else
        {
            sender.sendMessage(new TextComponent("Only players may view their claimed tickets because you can't claim tickets dude you're a console."));
        }
    }

    @Cmd(value="Allows for tickets to be reassigned to other teams.")
    public void reassignTicket(CommandSender sender, String uuid)
    {
        sender.sendMessage(ChatBuilder.appendTextComponent(null, "Choose a team to reassign to:", TicketPluginBungee.PREFIX));
        for(Team t: Team.values())
        {
            BaseComponent cmdButton = MessageUtil.CommandButton("Reassign to " + t.name(),
                    "/" + TicketPluginBungee.COMMAND_START +" staff reassign " + t.name().toLowerCase() + " " + uuid, t.color, TicketPluginBungee.ALT_COLOR);
            msg(cmdButton);
        }
    }

    @Cmd(value="Allows for the expansion of tickets. Called from an 'expand ticket' button.")
    public void expandTicket(CommandSender sender, String uuid)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            try
            {
                Database database = plugin.getDatabase();
                Ticket ticket =  database.getTicketByUUID(uuid);
                msg(TicketPluginBungee.PREFIX + "Ticket Information: ");
                List <BaseComponent> ticketParts = ticket.toExpandedInfo();

                HoverEvent basicHover = MessageUtil.hoverEvent("Click Here!");

                TextComponent addCommentsButton = ChatBuilder.appendTextComponent(null, "[", TicketPluginBungee.ALT_COLOR);
                ChatBuilder.appendTextComponent(addCommentsButton, "Add Staff Comment", TicketPluginBungee.PREFIX);
                ChatBuilder.appendTextComponent(addCommentsButton, "]", TicketPluginBungee.ALT_COLOR);
                addCommentsButton.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + TicketPluginBungee.COMMAND_START +" staff addComment " + ticket.getId() + " true "));
                addCommentsButton.setHoverEvent(basicHover);

                TextComponent addCommentsButton2 = ChatBuilder.appendTextComponent(null, "[", TicketPluginBungee.ALT_COLOR);
                ChatBuilder.appendTextComponent(addCommentsButton2, "Add Player and Staff Comment", TicketPluginBungee.PREFIX);
                ChatBuilder.appendTextComponent(addCommentsButton2, "]", TicketPluginBungee.ALT_COLOR);
                addCommentsButton2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + TicketPluginBungee.COMMAND_START +" staff addComment " + ticket.getId() + " false "));
                addCommentsButton2.setHoverEvent(basicHover);

                TextComponent viewCommentsButton = ChatBuilder.appendTextComponent(null, "[", TicketPluginBungee.ALT_COLOR);
                ChatBuilder.appendTextComponent(viewCommentsButton, "View Comments", TicketPluginBungee.PREFIX);
                ChatBuilder.appendTextComponent(viewCommentsButton, "]", TicketPluginBungee.ALT_COLOR);
                viewCommentsButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + TicketPluginBungee.COMMAND_START +" staff comment " + ticket.getId()));
                viewCommentsButton.setHoverEvent(basicHover);

                ticketParts.add(addCommentsButton);
                ticketParts.add(addCommentsButton2);
                ticketParts.add(viewCommentsButton);

                if(ticket.getAssignedStaff().equals(player.getName()))
                {
                    BaseComponent closeButton = MessageUtil.CommandButton("Close this Ticket", "/" + TicketPluginBungee.COMMAND_START + " staff closeTicket " + ticket.getId());
                    ticketParts.add(closeButton);
                }

                for (BaseComponent msg : ticketParts) {
                    sender.sendMessage(msg);
                }

            }
            catch(NullPointerException e)
            {
                TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket not found! Contact a developer if this continues to occur.", TicketPluginBungee.ERROR_COLOR);
                ChatBuilder.appendTextComponent(message, "Method: claim", TicketPluginBungee.ALT_COLOR);
                sender.sendMessage(message);
            }
        }
        else
        {
            sender.sendMessage(new TextComponent("Only players may expand tickets dude."));
        }


    }

    @Cmd(value="access the comments section of a ticket.")
    public void comment(CommandSender sender, String uuid)
    {
        makeCommentBook(sender, uuid);
    }

    @Cmd(value="add a comment to a ticket.")
    public void addComment(CommandSender sender, String uuid, boolean staffComment, String[] info) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            String comment = String.join(" ", info);
            if (comment.length() <= 150) {
                database.createNewComment(player.getName(), comment, uuid, staffComment);
                sendCommentMessage(database.getTicketByUUID(uuid));
            } else {
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "Please limit your comment to 150 characters.", TicketPluginBungee.ERROR_COLOR));
            }
        } else {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players may access and modify comments.", TicketPluginBungee.ERROR_COLOR));
        }
    }

    @Cmd(value="Allows a staff member to close a given ticket.")
    public void closeTicket(CommandSender sender, String ticketUUID)
    {
        if(sender instanceof ProxiedPlayer)
        {
            try
            {
                database.closeTicket(ticketUUID);

                sendCompletedMessage(database.getTicketByUUID(ticketUUID));

                sender.sendMessage(ChatBuilder.appendTextComponent(null, "Ticket has been closed.", TicketPluginBungee.PREFIX));
            }
            catch(NullPointerException e)
            {
                TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket not found! Contact a developer if this continues to occur.", TicketPluginBungee.ERROR_COLOR);
                ChatBuilder.appendTextComponent(message, "Method: closeTicket", TicketPluginBungee.ALT_COLOR);
                sender.sendMessage(message);
            }
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players can close tickets.", TicketPluginBungee.ERROR_COLOR));
        }
    }

    @Cmd(value="Sets a staff-member off-duty.")
    public void offDuty(CommandSender sender)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            String playerUUID = player.getUniqueId().toString();
            if(plugin.getStaffUUIDsOnDuty().contains(playerUUID))
            {
                plugin.getDatabase().removeStaffFromOnDuty(playerUUID);

                TextComponent message = ChatBuilder.appendTextComponent(null, "You are now ", TicketPluginBungee.PREFIX);
                ChatBuilder.appendTextComponent(message, "off-duty ", ChatColor.RED);
                ChatBuilder.appendTextComponent(message, "and will no longer receive notifications regarding new tickets.", TicketPluginBungee.PREFIX);
                sender.sendMessage(message);
            }
            else
            {
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "You are already off-duty!", TicketPluginBungee.ERROR_COLOR));
            }
        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players can go off-duty!", TicketPluginBungee.ERROR_COLOR));
        }
    }

    @Cmd(value="Sets a staff-member on-duty.")
    public void onDuty(CommandSender sender, @Default("") String persistent)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Staff staff = database.getStaff(((ProxiedPlayer) sender).getUniqueId().toString());

            switch(persistent)
            {
                case "true":
                    if(staff == null)
                    {
                        database.staffOnDuty(player.getUniqueId().toString(), true);
                    }
                    else
                    {
                        database.updateStaffOnDuty(player.getUniqueId().toString(), true);
                    }
                    {
                        TextComponent message = ChatBuilder.appendTextComponent(null, "You are now marked as ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(message, "on-duty", ChatColor.GREEN);
                        ChatBuilder.appendTextComponent(message, ".", TicketPluginBungee.PREFIX);
                        sender.sendMessage(message);
                    }
                    break;

                case "false":
                    if(staff == null)
                    {
                        database.staffOnDuty(player.getUniqueId().toString(), false);
                    }
                    else
                    {
                        database.updateStaffOnDuty(player.getUniqueId().toString(), false);
                    }
                    {
                        TextComponent message = ChatBuilder.appendTextComponent(null, "You are now marked as ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(message, "on-duty", ChatColor.GREEN);
                        ChatBuilder.appendTextComponent(message, ".", TicketPluginBungee.PREFIX);
                        sender.sendMessage(message);
                    }
                    break;

                default:
                    TextComponent question;
                    if(staff == null)
                    {
                        question = ChatBuilder.appendTextComponent(null, "You are currently ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(question, "off-duty", ChatColor.RED);
                        ChatBuilder.appendTextComponent(question, ". Would you like to stay ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(question, "on-duty ", ChatColor.GREEN);
                        ChatBuilder.appendTextComponent(question, "between logins?", TicketPluginBungee.PREFIX);
                    }
                    else if(staff.isPersistent())
                    {
                        question = ChatBuilder.appendTextComponent(null, "You are currently ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(question, "on-duty", ChatColor.GREEN);
                        ChatBuilder.appendTextComponent(question, ". Would you like to stay ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(question, "on-duty ", ChatColor.GREEN);
                        ChatBuilder.appendTextComponent(question, "between logins?", TicketPluginBungee.PREFIX);
                    }
                    else
                    {
                        question = ChatBuilder.appendTextComponent(null, "You are currently ", TicketPluginBungee.PREFIX);
                        ChatBuilder.appendTextComponent(question, "on-duty", ChatColor.GREEN);
                        ChatBuilder.appendTextComponent(question, ", and will be taken off-duty upon logging off. Would you like to stay on-duty while logged off?", TicketPluginBungee.PREFIX);
                    }

                    BaseComponent yesButton = MessageUtil.CommandButton("YES", "/" + TicketPluginBungee.COMMAND_START +" staff onDuty " + "true", ChatColor.GREEN, ChatColor.GREEN);
                    TextComponent slashMark = new TextComponent("/");
                    slashMark.setColor(ChatColor.DARK_GRAY);
                    BaseComponent noButton = MessageUtil.CommandButton("NO", "/" + TicketPluginBungee.COMMAND_START + " staff onDuty " + "false", ChatColor.RED, ChatColor.RED);

                    ComponentBuilder componentBuilder = new ComponentBuilder("").append(question)
                            .append(yesButton)
                            .append(slashMark)
                            .append(noButton);

                    sender.sendMessage(componentBuilder.create());
                    break;
            }


        }
        else
        {
            sender.sendMessage(ChatBuilder.appendTextComponent(null, "Only players can go on-duty!", TicketPluginBungee.ERROR_COLOR));
        }
    }

    @Cmd(value="Allows a staff member to teleport to a location given by a ticket.")
    public void ticketTP(CommandSender sender, String worldName, int x, int y, int z, String serverName)
    {
        if(sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            ServerInfo server = plugin.getProxy().getServerInfo(serverName);
            if (server != null) {
                if (!player.getServer().getInfo().equals(server)) {
                    player.connect(server);
                }

                Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
                // perform a check to see if there are globally no players
                if ( networkPlayers == null || networkPlayers.isEmpty() )
                {
                    return;
                }
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(TicketPluginBungee.TP_SUB_CHANNEL);
                out.writeUTF(worldName + "," + x + "," + y + "," + z);
                out.writeUTF(player.getUniqueId().toString());

                server.sendData(TicketPluginBungee.CHANNEL, out.toByteArray());
            } else {
                sender.sendMessage(ChatBuilder.appendTextComponent(null, "Unable to find the server this ticket was made on. You can TP manually using the coordinates " + x + ", " + y + ", " + z, TicketPluginBungee.ERROR_COLOR));
            }
        }
    }

}
