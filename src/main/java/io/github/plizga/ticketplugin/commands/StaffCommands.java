package io.github.plizga.ticketplugin.commands;

import co.lotc.core.bukkit.book.BookStream;
import co.lotc.core.bukkit.util.BookUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.util.MessageUtil;
import com.comphenix.protocol.PacketType;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Comment;
import io.github.plizga.ticketplugin.database.Database;
import io.github.plizga.ticketplugin.helpers.Staff;
import io.github.plizga.ticketplugin.helpers.Ticket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffCommands extends BaseCommand
{
    private Database database;
    private ReassignCommands reassignCommands;
    public StaffCommands()
    {
        this.database = plugin.getDatabase();
        this.reassignCommands = new ReassignCommands();

    }

    @Cmd(value ="Internally used to reassign tickets to various teams.")
    public BaseCommand reassign()
    {
        return this.reassignCommands;
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
                List openTickets = database.getOpenTicketsByTeam(team.name());

                if(openTickets.size() == 0)
                {
                    sender.sendMessage(plugin.PREFIX + "There are no open tickets for the " + Team.getColor(team) + team.name() +
                            plugin.PREFIX + " team.");
                    return;
                }
                sender.sendMessage(plugin.PREFIX + "Viewing tickets for the " + Team.getColor(team) + team.name() +
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
            List openTickets = database.getOpenTicketsByTeam(team.name());

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
       List openTickets = new ArrayList<Ticket>();

        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Admin)))
            {

                openTickets.addAll(database.getAllOpenTickets());
            }
            else
            {
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Tech)))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Tech.name()));

                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Moderator)))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Moderator.name()));

                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Event)))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Event.name()));

                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Lore)))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Lore.name()));

                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Build)))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Build.name()));

                }
                if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Design)))
                {
                    openTickets.addAll(database.getOpenTicketsByTeam(Team.Design.name()));

                }


                openTickets.addAll(database.getOpenTicketsByTeam(Team.Global.name()));


            }


            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "There are no open tickets for any teams you are a part of. Pretty cool!");
                return;
            }
            sender.sendMessage(plugin.PREFIX + "Viewing tickets for your teams:");

            readTicketsBasic(sender, openTickets);

        }

    }

    @Cmd(value="allows a staff member to claim or unclaim a ticket, based upon whether they have currently claimed it or not.")
    public void claim(CommandSender sender, String uuid)
    {
        //todo: allow managers to claim tickets even if they are already claimed.
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            try
            {
                Database database = plugin.getDatabase();
                Ticket ticket = database.getTicketByUUID(uuid);

                String assignedModerator = ticket.getAssignedModerator();

                if(ticket.getStatus() == Status.OPEN)
                {
                    database.claimTicket(ticket.getId(), player.getName());
                    sender.sendMessage(plugin.PREFIX + "Ticket has been " + plugin.ALT_COLOR + "claimed.\n");

                }
                else if(assignedModerator.equalsIgnoreCase(player.getName()))
                {
                    database.unClaimTicket(uuid);
                    sender.sendMessage(plugin.PREFIX + "Ticket has been " + plugin.ALT_COLOR + "unclaimed.\n");

                }
                else if(player.hasPermission(plugin.PERMISSION_START + "." + ticket.getTeam().name() + ".manager"))
                {
                    database.unClaimTicket(uuid);
                    database.claimTicket(uuid, player.getName());
                    msg(plugin.PREFIX + "You have used your manager permissions to claim this ticket.");
                }
                else
                {
                    sender.sendMessage(plugin.PREFIX + "This ticket has been claimed by " + plugin.ALT_COLOR +
                            ticket.getAssignedModerator() + plugin.PREFIX + ".");
                }
            }
            catch(NullPointerException e)
            {
                sender.sendMessage(plugin.ERROR_COLOR + "Ticket not found! Contact a developer if this continues to occur." +
                        plugin.ALT_COLOR + " Method: claim");
            }
        }
        else
        {
            sender.sendMessage("Only players may claim tickets dude. " +
                    "If a console claimed a ticket, what do you think would happen? Seriously, what would happen? " +
                    "Tell me. I'd like to know.");
        }

    }

    @Cmd(value="Allows staff to view currently claimed tickets for all of their teams.")
    public void viewClaimed(CommandSender sender)
    {
        List openTickets = new ArrayList<Ticket>();

        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Admin)))
            {

                openTickets.addAll(database.getAllClaimedTickets());
            }
            else
            {
                if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Tech)))
                {
                    openTickets.addAll(database.getTeamClaimedTickets(Team.Tech.name()));

                }
                if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Moderator)))
                {
                    openTickets.addAll(database.getTeamClaimedTickets(Team.Moderator.name()));

                }
                if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Event)))
                {
                    openTickets.addAll(database.getTeamClaimedTickets(Team.Event.name()));

                }
                if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Lore)))
                {
                    openTickets.addAll(database.getTeamClaimedTickets(Team.Lore.name()));

                }
                if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Build)))
                {
                    openTickets.addAll(database.getTeamClaimedTickets(Team.Build.name()));

                }
                if (player.hasPermission(TicketPlugin.PERMISSION_START + Team.getPermission(Team.Design)))
                {
                    openTickets.addAll(database.getTeamClaimedTickets(Team.Design.name()));

                }


                openTickets.addAll(database.getTeamClaimedTickets(Team.Global.name()));


            }

            Collections.sort(openTickets);

            if (openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "There are no claimed tickets for any teams you are a part of.");
                return;
            }
            sender.sendMessage(plugin.PREFIX + "Viewing claimed tickets for your teams:");

            readTicketsBasic(sender, openTickets);
        }
    }

    @Cmd(value="Allows staff members to view claimed tickets for a specified team.")
    public void viewClaimed(CommandSender sender, Team team)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            if(player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(team)) ||
                    player.hasPermission(TicketPlugin.PERMISSION_START +  Team.getPermission(Team.Admin)))
            {
                List openTickets = database.getTeamClaimedTickets(team.name());

                if(openTickets.size() == 0)
                {
                    sender.sendMessage(plugin.PREFIX + "There are no claimed tickets for the " + plugin.ALT_COLOR + team.name() +
                            plugin.PREFIX + " team.");
                    return;
                }
                sender.sendMessage(plugin.PREFIX + "Viewing claimed tickets for the " + plugin.ALT_COLOR + team.name() +
                        plugin.PREFIX + " team:");
                readTicketsBasic(sender, openTickets);
            }
            else
            {
                sender.sendMessage(plugin.ERROR_COLOR + "You do not have permission to view that team's claimed tickets!");
            }
        }
        else
        {
            List openTickets = database.getTeamClaimedTickets(team.name());

            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "There are no claimed tickets for the " + plugin.ALT_COLOR + team.name() +
                        plugin.PREFIX + " team.");
                return;
            }
            sender.sendMessage(plugin.PREFIX + "Viewing claimed tickets for the " + plugin.ALT_COLOR + team.name() +
                    plugin.PREFIX + " team:");
            readTicketsBasic(sender, openTickets);
        }
    }

    @Cmd(value="Allows staff members to view all of their currently claimed tickets.")
    public void viewMyClaimed(CommandSender sender)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            try
            {
                Database database = plugin.getDatabase();
                List<Ticket> claimedTickets = database.getClaimedTickets(player.getName());

                Collections.sort(claimedTickets);

                if(claimedTickets.size() == 0)
                {

                    msg(plugin.PREFIX + "You have no currently claimed tickets!");
                    return;
                }

                msg( plugin.PREFIX +"Viewing your claimed tickets:");
                readTicketsBasic(sender, claimedTickets);



            }
            catch(NullPointerException e)
            {
                msg(plugin.ERROR_COLOR + "Ticket not found! Contact a developer if this continues to occur." +
                        plugin.ALT_COLOR + " Method: viewMyClaimed");
            }
        }
        else
        {
            msg("Only players may view their claimed tickets because you can't claim tickets dude you're a console.");
        }
    }

    @Cmd(value="Allows for tickets to be reassigned to other teams.")
    public void reassignTicket(CommandSender sender, String uuid)
    {
        msg("\n" + plugin.PREFIX + "Choose a team to reassign to:");
        for(Team t: Team.values())
        {
            BaseComponent cmdButton = MessageUtil.CommandButton("Reassign to " + t.name(),
                    "/" + plugin.COMMAND_START +" staff reassign " + t.name().toLowerCase() + " " + uuid);
            msg(cmdButton);
        }
    }

    @Cmd(value="Allows for the expansion of tickets. Called from an 'expand ticket' button.")
    public void expandTicket(CommandSender sender, String uuid)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            try
            {
                Database database = plugin.getDatabase();
                Ticket ticket =  database.getTicketByUUID(uuid);
                msg(plugin.PREFIX + "Ticket Information: ");
                sender.spigot().sendMessage(ticket.toExpandedInfo().create());
                BaseComponent addCommentsButton = MessageUtil.CommandButton("Add Staff Comment", "/" + plugin.COMMAND_START +" staff addComment " + ticket.getId() + " true");
                BaseComponent addCommentsButton2 = MessageUtil.CommandButton("Add Player and Staff Comment", "/" + plugin.COMMAND_START +" staff addComment " + ticket.getId() + " false");
                BaseComponent viewCommentsButton = MessageUtil.CommandButton("View Comments", "/" + plugin.COMMAND_START +" staff comment " + ticket.getId());

                msg(addCommentsButton);
                msg(addCommentsButton2);
                msg(viewCommentsButton);

                if(ticket.getAssignedModerator().equals(player.getName()))
                {
                    BaseComponent closeButton = MessageUtil.CommandButton("Close this Ticket", "/" + plugin.COMMAND_START + " staff closeTicket " + ticket.getId());
                    msg(closeButton);
                }

            }
            catch(NullPointerException e)
            {
                sender.sendMessage(plugin.ERROR_COLOR + "Ticket not found! Contact a developer if this continues to occur." +
                        plugin.ALT_COLOR + "Method: claim");
            }
        }
        else
        {
            sender.sendMessage("Only players may expand tickets dude.");
        }


    }

    @Cmd(value="access the comments section of a ticket.")
    public void comment(CommandSender sender, String uuid)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            bookMeta.setAuthor(TicketPlugin.PERMISSION_START);
            bookMeta.setTitle(plugin.PREFIX + "Comments for ticket " + uuid);

            ArrayList<String> pages = new ArrayList<String>();

            List<Comment> comments = database.getAllComments(uuid);

            if(!comments.isEmpty())
            {
                for(Comment c : comments)
                {
                    pages.add(c.toString());
                }
            }
            else
            {
                pages.add("There are no comments for this ticket!");
            }
            bookMeta.setPages(pages);
            book.setItemMeta(bookMeta);


            player.getInventory().addItem(book);


        }
        else
        {
            msg("Only players may access and modify comments.");
        }
    }

    @Cmd(value="add a comment to a ticket.")
    public void addComment(CommandSender sender, String uuid, boolean isStaffComment)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            ItemStack book = new ItemStack(Material.WRITABLE_BOOK);

            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            bookMeta.setAuthor(TicketPlugin.PERMISSION_START);
            bookMeta.setTitle(plugin.PREFIX + "Comments for ticket:  " + uuid);



            BookStream stream = new BookStream(player, book, plugin.PREFIX + "Add your comment!")
            {
                @Override
                public void onBookClose()
                {

                    BookMeta meta = getMeta();
                    String info = BookUtil.getPagesAsString(meta);
                    if(info.length() <= 205)
                    {
                        database.createNewComment(player.getName(), info, uuid, isStaffComment);
                    }
                    else
                    {
                        msg(plugin.ERROR_COLOR + "Please limit your comment to under 205 characters.");
                    }

                }
            };

            stream.open(player);

            //player.getInventory().addItem(book);


        }
        else
        {
            msg("Only players may access and modify comments.");
        }
    }

    @Cmd(value="Allows a staff member to close a given ticket.")
    public void closeTicket(CommandSender sender, String ticketUUID)
    {
        if(sender instanceof Player)
        {
            try
            {
                database.closeTicket(ticketUUID);

                sendCompletedMessage(database.getTicketByUUID(ticketUUID));

                msg(plugin.PREFIX + "Ticket has been closed.");
            }
            catch(NullPointerException e)
            {
                msg("Ticket does not exist. Contact a developer if this continues to occur.");
            }

        }
        else
        {
            msg("Only players may close tickets.");
        }
    }

    @Cmd(value="Sets a staff-member off-duty.")
    public void offDuty(CommandSender sender)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();
            if(plugin.getStaffUUIDsOnDuty().contains(playerUUID))
            {
                plugin.getDatabase().removeStaffFromOnDuty(playerUUID);
                msg(plugin.PREFIX + "You are now " + ChatColor.RED + "off-duty " +
                        plugin.PREFIX +"and will no longer receive notifications regarding new tickets.");
            }
            else
            {
                msg(plugin.ERROR_COLOR + "You are already off-duty!");
            }


        }
        else
        {
            msg("Only players can go off-duty!");
        }
    }

    @Cmd(value="Sets a staff-member on-duty.")
    public void onDuty(CommandSender sender, @Default("") String persistent)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            Staff staff = database.getStaff(((Player) sender).getUniqueId().toString());

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
                    msg(plugin.PREFIX + "You are now marked as " + ChatColor.GREEN + "on-duty.");
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
                    msg(plugin.PREFIX + "You are now marked as " + ChatColor.GREEN + "on-duty.");
                    break;

                default:
                    TextComponent question;
                    if(staff == null)
                    {
                        question = new TextComponent(plugin.PREFIX + "You are currently " +
                                ChatColor.DARK_RED + "off-duty" + plugin.PREFIX + ". " +
                                "Would you like to stay on-duty " + plugin.PREFIX + "while logged off? ");
                    }
                    else if(staff.isPersistent())
                    {
                        question = new TextComponent(plugin.PREFIX + "You are currently " +
                                ChatColor.GREEN + "on-duty" + plugin.PREFIX + ", and will stay on-duty after " + plugin.PREFIX + "logging off. Would you like to stay on-duty while logged off? ");
                    }
                    else
                    {
                        question = new TextComponent(plugin.PREFIX + "You are currently " +
                                ChatColor.GREEN + "on-duty" + plugin.PREFIX + ", and will be taken off-duty upon " + plugin.PREFIX + "logging off. Would you like to stay on-duty while logged off? ");
                    }

                    TextComponent yesButton = new TextComponent(ChatColor.GREEN + "[YES]");
                    yesButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/" + plugin.COMMAND_START +" staff onDuty " + "true"));
                    TextComponent slashMark = new TextComponent(ChatColor.DARK_GRAY+ "/");
                    TextComponent noButton = new TextComponent(ChatColor.RED + "[NO]");
                    noButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/" + plugin.COMMAND_START + " staff onDuty " + "false"));

                    ComponentBuilder componentBuilder = new ComponentBuilder("").append(question)
                            .append(yesButton)
                            .append(slashMark)
                            .append(noButton);

                    sender.spigot().sendMessage(componentBuilder.create());
                    break;
            }


        }
        else
        {
            msg("Only players can go on-duty!");
        }
    }

    @Cmd(value="Allows a staff member to teleport to a location given by a ticket.")
    public void ticketTP(CommandSender sender, String worldName, int x, int y, int z)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            World world = plugin.getServer().getWorld(worldName);

            Location location = new Location(world, x, y, z);

            player.teleport(location);
        }
    }

}
