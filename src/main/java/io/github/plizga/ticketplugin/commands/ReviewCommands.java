package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.annotate.Cmd;
import io.github.plizga.ticketplugin.database.Database;
import org.bukkit.command.CommandSender;

public class ReviewCommands extends BaseCommand
{
    private Database database;

    public ReviewCommands()
    {
        this.database = plugin.getDatabase();
    }

    @Cmd(value="Leaves a one-star review.")
    public void one(CommandSender sender, String ticketUUID)
    {
        database.createNewReview(ticketUUID, 1);
        msg(plugin.PREFIX + "Thank you for submitting a review!");
    }

    @Cmd(value="Leaves a two-star review.")
    public void two(CommandSender sender, String ticketUUID)
    {
        database.createNewReview(ticketUUID, 2);
        msg(plugin.PREFIX + "Thank you for submitting a review!");
    }

    @Cmd(value="Leaves a three-star review.")
    public void three(CommandSender sender, String ticketUUID)
    {
        database.createNewReview(ticketUUID, 3);
        msg(plugin.PREFIX + "Thank you for submitting a review!");
    }

    @Cmd(value="Leaves a four-star review.")
    public void four(CommandSender sender, String ticketUUID)
    {
        database.createNewReview(ticketUUID, 4);
        msg(plugin.PREFIX + "Thank you for submitting a review!");
    }

    @Cmd(value="Leaves a five-star review.")
    public void five(CommandSender sender, String ticketUUID)
    {
        database.createNewReview(ticketUUID, 5);
        msg(plugin.PREFIX + "Thank you for submitting a review!");
    }

}
