package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.annotate.Cmd;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.sqlite.Database;
import org.bukkit.command.CommandSender;

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
        database.setTeam(uuid, Team.Admin.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Admin.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to the mod team.")
    public void moderator(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Moderator.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Moderator.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to the event team.")
    public void event(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Event.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Event.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to the tech team.")
    public void tech(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Tech.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Tech.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to the lore team.")
    public void lore(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Lore.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Lore.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to the build team.")
    public void build(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Build.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Build.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to the build team.")
    public void design(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Design.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to the " + plugin.ALT_COLOR + Team.Design.name()
                + plugin.PREFIX + " team.");
    }


    @Cmd(value="Reassigns tickets to global.")
    public void global(CommandSender sender, String uuid)
    {
        database.setTeam(uuid, Team.Global.name());
        sender.sendMessage(plugin.PREFIX + "Ticket has been reassigned to " + plugin.ALT_COLOR + Team.Global.name()
                + plugin.PREFIX + " .");
    }
}
