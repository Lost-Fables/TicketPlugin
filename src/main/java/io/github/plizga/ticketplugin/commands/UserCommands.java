package io.github.plizga.ticketplugin.commands;


import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;

import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.sqlite.Database;
import io.github.plizga.ticketplugin.sqlite.Ticket;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class UserCommands extends BaseCommand
{

    private Database database;
    private StaffCommands staffCommands;


    public UserCommands()
    {
        this.database = plugin.getDatabase();
        this.staffCommands = new StaffCommands();
    }

    @Cmd(value="Moderator access to tickets.", permission=TicketPlugin.PERMISSION_START + ".staff")
    public BaseCommand staff()
    {
        return staffCommands;
    }


    @Cmd(value="Create a new ticket.", permission=TicketPlugin.PERMISSION_START + ".create")
    public void create(CommandSender sender, Team team,
                       @Arg(value="info", description="The description for the ticket being created.") String[] info)
    {

        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            String infoMessage = String.join(" ", info);


            database.createNewTicket(player, Status.OPEN, team, infoMessage, false);

            sender.sendMessage(plugin.PREFIX + "Your ticket, with the description: " +  plugin.ALT_COLOR +
                    infoMessage + plugin.PREFIX + " has been created!");

        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may create a ticket.");
        }
    }


    @Cmd(value="look at the open tickets you have created", permission=TicketPlugin.PERMISSION_START + ".view")
    public void view(CommandSender sender)
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
                    " /request cancel <number> " + plugin.PREFIX + ", where <number> is the ticket number as it appears" +
                    " on this list. \n Alternatively, use " + plugin.ALT_COLOR + "/request cancel all" +
                    plugin.PREFIX + " to cancel all of your current tickets.");
        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may get a list of their own tickets.");
        }

    }

    @Cmd(value="allows a player to cancel their own tickets", permission=TicketPlugin.PERMISSION_START + ".cancel")
    public void cancel(CommandSender sender, @Arg(value = "number (or 'all')", description = "number of ticket to delete") String num)
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
                if(num.equals("all"))
                {
                    database.removeTicketByPlayer(player.getName().toLowerCase());
                    sender.sendMessage(plugin.PREFIX +  "All of your open tickets have been cancelled.");
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
                        sender.sendMessage(plugin.ERROR_COLOR + "Failed to execute command. Use 'all' or a number" +
                                "instead of " + plugin.ALT_COLOR + num + plugin.ERROR_COLOR + ".");
                        return;
                    }
                    Ticket ticket = (Ticket) openTickets.get(numAsInt);
                    database.removeTicketByUUID(ticket.getId());
                    sender.sendMessage(plugin.PREFIX + "Ticket " + plugin.ALT_COLOR + num + plugin.PREFIX + " has been deleted.");
                }

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





}
