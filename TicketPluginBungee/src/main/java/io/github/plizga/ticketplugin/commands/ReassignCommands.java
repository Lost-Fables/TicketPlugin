package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.annotate.Cmd;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Ticket;
import io.github.plizga.ticketplugin.database.Database;
import net.md_5.bungee.api.CommandSender;

public class ReassignCommands extends BaseCommand
{
    private Database database;


    public ReassignCommands()
    {
        this.database = plugin.getDatabase();
    }


    @Cmd(value="Reassigns tickets to the admin team.")
    public void admin(CommandSender sender, String uuid)
    {
        reassign(sender,uuid,Team.Admin);
    }


    @Cmd(value="Reassigns tickets to the mod team.")
    public void moderator(CommandSender sender, String uuid)
    {
        reassign(sender,uuid,Team.Moderator);
    }


    @Cmd(value="Reassigns tickets to the event team.")
    public void event(CommandSender sender, String uuid)
    {
        reassign(sender,uuid,Team.Event);
    }


    @Cmd(value="Reassigns tickets to the tech team.")
    public void tech(CommandSender sender, String uuid)
    {
        reassign(sender,uuid,Team.Tech);
    }


    @Cmd(value="Reassigns tickets to the lore team.")
    public void lore(CommandSender sender, String uuid)
    {
       reassign(sender,uuid,Team.Lore);
    }


    @Cmd(value="Reassigns tickets to the build team.")
    public void build(CommandSender sender, String uuid)
    {
        reassign(sender,uuid,Team.Build);
    }


    /*@Cmd(value="Reassigns tickets to the build team.")
    public void design(CommandSender sender, String uuid)
    {
        reassign(sender, uuid, Team.Design);
    }*/


    @Cmd(value="Reassigns tickets to global.")
    public void global(CommandSender sender, String uuid)
    {
        reassign(sender, uuid, Team.Global);
    }

    private void reassign(CommandSender sender, String uuid, Team team)
    {
        database.setTeam(uuid, team.name());

        if(!team.equals(Team.Global))
        {
            msg(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + team.name() +
                    plugin.PREFIX + " team.");
        }
        else
        {msg(plugin.PREFIX + "Ticket has been reassigned to " + plugin.ALT_COLOR + team.name() +
                plugin.PREFIX + ".");

        }

        Ticket ticket = database.getTicketByUUID(uuid);
        sendReassignMessage(ticket, team);
        plugin.notifyOnDutyStaff(team);
    }
}
