package io.github.plizga.ticketplugin.commands;


import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;

import co.lotc.core.util.MessageUtil;
import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Comment;
import io.github.plizga.ticketplugin.database.Database;
import io.github.plizga.ticketplugin.helpers.Ticket;

import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UserCommands extends BaseCommand
{

    private Database database;
    private StaffCommands staffCommands;
    private ReviewCommands reviewCommands;


    public UserCommands()
    {
        this.database = plugin.getDatabase();
        this.staffCommands = new StaffCommands();
        this.reviewCommands = new ReviewCommands();
    }

    @Cmd(value="Moderator access to tickets.", permission=TicketPlugin.PERMISSION_START + ".staff")
    public BaseCommand staff()
    {
        return staffCommands;
    }

    @Cmd(value="Allows access to reviews.")
    public BaseCommand review() {return reviewCommands;}


    @Cmd(value="Create a new ticket.", permission=TicketPlugin.PERMISSION_START + ".create")
    public void create(CommandSender sender, Team team,
                       @Arg(value="info", description="The description for the ticket being created.") String[] info)
    {

        if(sender instanceof Player)
        {
            Player player = (Player) sender;

            String infoMessage = String.join(" ", info);


            database.createNewTicket(player, Status.OPEN, team, infoMessage);

            sender.sendMessage(plugin.PREFIX + "Your ticket, with the description: " +  plugin.ALT_COLOR +
                    infoMessage + plugin.PREFIX + " has been created!");

            plugin.notifyOnDutyStaff(team);

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
            List openTickets = database.getPlayerOpenTickets(player.getName());

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
            List openTickets = database.getPlayerOpenTickets(player.getName());
            if(openTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "No open tickets to cancel!");
                return;
            }

            try
            {
                if(num.equals("all"))
                {
                    database.cancelTicketByPlayer(player.getName());
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
                    database.cancelTicketByUUID(ticket.getId());
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

    @Cmd(value="access the comments section of a ticket.")
    public void comment(CommandSender sender, String uuid)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

            BookMeta bookMeta = (BookMeta) book.getItemMeta();

            bookMeta.setAuthor(TicketPlugin.PERMISSION_START);
            bookMeta.setTitle(plugin.PREFIX + "Comments for ticket " + uuid);


            ArrayList<String> pages = new ArrayList<String>();

            List<Comment> comments = database.getCommentsForPlayer(uuid);

            if (!comments.isEmpty())
            {
                for (Comment c : comments)
                {
                    pages.add(c.toString());
                }
            } else
            {
                pages.add("There are no comments for this ticket!");
            }
            bookMeta.setPages(pages);
            book.setItemMeta(bookMeta);


            HashMap<Integer, ItemStack> itemStackHashMap= player.getInventory().addItem(book);

            if(!itemStackHashMap.isEmpty())
            {
                msg(plugin.ERROR_COLOR + "Please ensure you have an empty item slot in your inventory.");
            }

        }
        else
        {
            msg("Only players may access and modify comments.");
        }
    }

    @Cmd(value="Allows a plyer to view their completed tickets.")
    public void viewCompleted(CommandSender sender)
    {
        if(sender instanceof  Player)
        {
            Player player = (Player) sender;
            List completedTickets = database.getCompletedPlayerTickets(player.getUniqueId().toString());

            if(completedTickets.size() == 0)
            {
                sender.sendMessage(plugin.PREFIX + "No completed tickets to view!");
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
        if(sender instanceof  Player)
        {
            Player player = (Player) sender;

            msg(plugin.PREFIX + "Please leave a review below, where 1 is the worst, and 5 is the best.\n");
            TextComponent oneStarButton = new TextComponent("[ * ");
            oneStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review one " + ticketUUID));
            oneStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("1").create()));

            TextComponent twoStarButton = new TextComponent("* ");
            twoStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review two " + ticketUUID));
            twoStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("2").create()));

            TextComponent threeStarButton = new TextComponent("* ");
            threeStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review three " + ticketUUID));
            threeStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("3").create()));

            TextComponent fourStarButton = new TextComponent("* ");
            fourStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review four " + ticketUUID));
            fourStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("4").create()));

            TextComponent fiveStarButton = new TextComponent("* ]");
            fiveStarButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " review five " + ticketUUID));
            fiveStarButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("5").create()));
            msg(plugin.PREFIX + "Review Rating: ");
            ComponentBuilder componentBuilder = new ComponentBuilder("").append(oneStarButton).append(twoStarButton).append(threeStarButton).append(fourStarButton).append(fiveStarButton);
            sender.spigot().sendMessage(componentBuilder.create());
        }
        else
        {
            msg(plugin.ERROR_COLOR + "Only players may leave reviews on their tickets.");
        }
    }
}
