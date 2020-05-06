package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.sqlite.Database;
import io.github.plizga.ticketplugin.sqlite.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;


public class TicketCommand extends BaseCommand
{

    private Database database;


    public TicketCommand()
    {
        this.database = plugin.getDatabase();
    }
    @Cmd(value="Create a new ticket.", permission=TicketPlugin.PERMISSION_START + ".create")
    public void create(CommandSender sender, @Arg(value="info", description="the description of the ticket you are submitting.") String[] info)
    {

        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            String infoMessage = String.join(" ", info);
            database.createNewTicket(player, Status.OPEN, infoMessage, false);
            sender.sendMessage(plugin.PREFIX + "Your ticket, with the description: " +  plugin.ALT_COLOR +
                    infoMessage + plugin.PREFIX + " has been created!");

        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may create a ticket.");
        }
    }

   /* @Cmd(value="view open")
    public void view(CommandSender sender)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;

        }
    }*/

    //private Icon getTicketIcon()



    @Cmd(value="look at the list of tickets", permission=TicketPlugin.PERMISSION_START + ".viewOpen")
    public void viewOpen(CommandSender sender)
    {
        //Inventory inventory = Bukkit.createInventory(null, 20, "Tickets");

        List openTickets = database.getOpenTickets();

        if(openTickets.size() == 0)
        {
            sender.sendMessage(plugin.PREFIX + "No open tickets to view!");
        }
        int index = 1; //omg an index!!!!
        for(Object o : openTickets)
        {
            sender.sendMessage("Ticket " + index + ":");
            Ticket ticket = (Ticket) o;
            sender.sendMessage(plugin.PREFIX + ticket.toString());
            sender.sendMessage("\n\n");
            index++;

        }

    }



    /*@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            String ticketMessage = String.join(" ", args);
            database.createNewTicket(player, Status.OPEN , ticketMessage, false );
            sender.sendMessage(ChatColor.BLUE + "Your ticket has been created.");
        }
        else
        {
            sender.sendMessage(ChatColor.DARK_RED + "Only players may create a ticket.");
        }

        return true;
    }*/
}
