package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.annotate.Cmd;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.sqlite.Database;
import org.bukkit.command.CommandSender;

import java.util.List;

public class StaffCommands extends BaseCommand
{
    private Database database;
    public StaffCommands()
    {
        this.database = plugin.getDatabase();
    }

    @Cmd(value="look at the list of tickets", permission= TicketPlugin.PERMISSION_START + ".view")
    public void view(CommandSender sender)
    {


        List openTickets = database.getOpenTickets();

        if(openTickets.size() == 0)
        {
            sender.sendMessage(plugin.PREFIX + "No open tickets to view!");
            return;
        }
        readTicketsBasic(sender, openTickets);
    }
}
