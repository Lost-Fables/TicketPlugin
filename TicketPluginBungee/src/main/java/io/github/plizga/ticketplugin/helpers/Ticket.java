package io.github.plizga.ticketplugin.helpers;

import co.lotc.core.bungee.util.ChatBuilder;
import io.github.plizga.ticketplugin.TicketPluginBungee;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Represents a ticket as a Java object.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public class Ticket implements Comparable<Ticket>
{
    private static final int MINI_DESCRIPTION_LENGTH = 25;
    private static final ChatColor USERNAME_COLOR_ONLINE = ChatColor.GREEN;
    private static final ChatColor USERNAME_COLOR_OFFLINE = ChatColor.RED;
    private static final ChatColor TEXT_COLOR = ChatColor.GRAY;
    private static final ChatColor TIME_COLOR = ChatColor.YELLOW;
    private static final ChatColor CLAIMED_COLOR = ChatColor.AQUA;
    private String id;
    private String playerName;
    private Status status;
    private Team team;
    private String assignedStaff;
    private String dateCreated;
    private String dateCleared;
    private String location;
    private String info;
    private String playerID;


    private TicketPluginBungee plugin;

    public Ticket(Plugin plugin, String id, String playerName, String playerID, Status status, Team team, String assignedStaff,
                  String dateCreated, String dateCleared, String location, String info)
    {
        this.plugin = (TicketPluginBungee) plugin;
        this.id = id;
        this.playerName = playerName;
        this.status = status;
        this.team = team;
        this.assignedStaff = assignedStaff;
        this.dateCreated = dateCreated;
        this.dateCleared = dateCleared;
        this.location = location;
        this.info = info;
        this.playerID = playerID;
    }

    @Override
    public String toString()
    {

        return plugin.PREFIX + "Ticket ID: " + plugin.ALT_COLOR + id +
               plugin.PREFIX + "\nPlayer: " + plugin.ALT_COLOR + playerName +
               plugin.PREFIX + "\nInfo: " + plugin.ALT_COLOR + info +
               plugin.PREFIX + "\nStatus: " + plugin.ALT_COLOR + status.toString() +
               plugin.PREFIX + "\nTeam: " + plugin.ALT_COLOR + team.toString() +
               plugin.PREFIX + "\nAssigned Staff Member: " + plugin.ALT_COLOR + assignedStaff +
               plugin.PREFIX + "\nDate Created: " + plugin.ALT_COLOR + dateCreated +
               plugin.PREFIX + "\nDate Cleared: " + plugin.ALT_COLOR + dateCleared +
               plugin.PREFIX + "\nLocation: " + plugin.ALT_COLOR + location;
    }

    public List<BaseComponent> toExpandedInfo()
    {
        List<BaseComponent> output = new ArrayList<>();
        //id
        {
            TextComponent message = ChatBuilder.appendTextComponent(null, "Ticket ID: ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, id, plugin.ALT_COLOR);
            output.add(message);
        }

        //Player
        {
            ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
            TextComponent message = ChatBuilder.appendTextComponent(null, "Player: ", plugin.PREFIX);

            if (player == null)
            {
                ChatBuilder.appendTextComponent(message, playerName, USERNAME_COLOR_OFFLINE);
            } else {
                ChatBuilder.appendTextComponent(message, playerName, USERNAME_COLOR_ONLINE);
            }

            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to message this player.")));

            output.add(message);
        }

        //Info
        {
            TextComponent message = ChatBuilder.appendTextComponent(null, "Info: ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, info, plugin.ALT_COLOR);
            output.add(message);
        }

        //Status, Team, and Assigned Staff
        {
            TextComponent message = new TextComponent();
            {
                TextComponent statusMessage = ChatBuilder.appendTextComponent(null, "Status: ", plugin.PREFIX);
                ChatBuilder.appendTextComponent(statusMessage, status.name(), plugin.ALT_COLOR);
                if (status.equals(Status.CLAIMED) || status.equals(Status.OPEN)) {
                    statusMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " staff claim " + this.id));
                    statusMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to attempt to claim this ticket.")));
                }
                message.addExtra(statusMessage);
            }

            {
                TextComponent teamMessage = ChatBuilder.appendTextComponent(null, " Team: ", plugin.PREFIX);
                ChatBuilder.appendTextComponent(teamMessage, team.name(), team.color);
                teamMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " staff reassignTicket " + this.id));
                teamMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to reassign this ticket to another team.")));
                message.addExtra(teamMessage);
            }

            {
                TextComponent assignedMessage = ChatBuilder.appendTextComponent(null, " Assigned Staff: ", plugin.PREFIX);
                ChatBuilder.appendTextComponent(assignedMessage, assignedStaff, plugin.ALT_COLOR);
                assignedMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + assignedStaff + " "));
                assignedMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to message this staff member.")));
                message.addExtra(assignedMessage);
            }

            output.add(message);
        }

        //Date Created and Date Cleared
        {
            TextComponent message = ChatBuilder.appendTextComponent(null, "Date Created: ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, dateCreated, plugin.ALT_COLOR);
            output.add(message);
        }
        if (dateCleared != null)
        {
            TextComponent message = ChatBuilder.appendTextComponent(null, "Date Cleared: ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, dateCleared, plugin.ALT_COLOR);
            output.add(message);
        }

        //Location - There will be 5 items in the String array due to being hard-coded into the SQL database
        {
            TextComponent locationMessage = null;
            if (location.contains(",")) {
                String[] locationArray = this.location.split(",");

                String text = null;
                String click = null;
                String hover = null;

                int i = 0;

                String worldName = "null";
                if (locationArray.length > i) worldName = locationArray[i++];

                int x = 0;
                if (locationArray.length > i) {
                    try {
                        x = Integer.parseInt(locationArray[i]);
                    } catch (NumberFormatException nfe) {
                        x = (int) Double.parseDouble(locationArray[i]);
                    }
                    i++;
                }

                int y = 0;
                if (locationArray.length > i) {
                    try {
                        y = Integer.parseInt(locationArray[i]);
                    } catch (NumberFormatException nfe) {
                        y = (int) Double.parseDouble(locationArray[i]);
                    }
                    i++;
                }

                int z = 0;
                if (locationArray.length > i) {
                    try {
                        z = Integer.parseInt(locationArray[i]);
                    } catch (NumberFormatException nfe) {
                        z = (int) Double.parseDouble(locationArray[i]);
                    }
                    i++;
                }

                String serverName = "main";
                if (locationArray.length > i) serverName = locationArray[i];

                locationMessage = ChatBuilder.appendTextComponent(null, "Location: ", plugin.PREFIX);
                ChatBuilder.appendTextComponent(locationMessage, serverName + " | " + worldName + ": X:" + (short) x + " Y:" + (short) y + " Z:" + (short) z, plugin.ALT_COLOR);
                locationMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " staff ticketTP " + worldName + " " + x + " " + y + " " + z + " " + serverName));
                locationMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to teleport to this location.")));
            } else {
                locationMessage = ChatBuilder.appendTextComponent(null, "The following location information is from an erroneous or out-dated format. Please contact a developer if this persists.\n", plugin.ERROR_COLOR);
                ChatBuilder.appendTextComponent(locationMessage, "Location: ", plugin.PREFIX);
                ChatBuilder.appendTextComponent(locationMessage, location, plugin.ALT_COLOR);
            }
            output.add(locationMessage);
        }

        //Comments
        {
            int commentAmount = getCommentAmount();
            TextComponent message = ChatBuilder.appendTextComponent(null, "Comments: ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, commentAmount + "", plugin.ALT_COLOR);
            output.add(message);
        }

        return output;
    }


    public TextComponent toBasicInfo()
    {

        //Set the team
        TextComponent message = ChatBuilder.appendTextComponent(null, "[" + team.name().charAt(0) + "] ", team.color,
                                                                false, false, false, false, false, null,
                                                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)),
                                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " staff reassignTicket " + this.id));

        //Set the player
        ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
        if (player == null)
        {
            ChatBuilder.appendTextComponent(message, playerName, USERNAME_COLOR_OFFLINE,
                                            false, false, false, false, false, null,
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)),
                                            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        }
        else
        {
            ChatBuilder.appendTextComponent(message, playerName, USERNAME_COLOR_ONLINE,
                                            false, false, false, false, false, null,
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)),
                                            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        }
        ChatBuilder.appendTextComponent(message, ": ", ChatColor.DARK_GRAY);


        //Set the description
        String shortDescription;
        if(info.length() > MINI_DESCRIPTION_LENGTH)
        {
            shortDescription = info.substring(0, MINI_DESCRIPTION_LENGTH -1);
            shortDescription = shortDescription.concat("...");
        }
        else
        {
            shortDescription = info;
        }

        ChatBuilder.appendTextComponent(message, shortDescription + " ", TEXT_COLOR,
                                        false, false, false, false, false, null,
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)),
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " staff expandTicket " + this.getId()));

        //Get the time between the ticket's creation and now.
        Date currentDate = new Date();
        Date ticketDate = null;

        try
        {
            ticketDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dateCreated);
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
        if (ticketDate != null) {
            long difference = currentDate.getTime() - ticketDate.getTime();
            long differenceDays = difference / (60 * 60 * 24 * 1000);
            long differenceHours = difference / (60 * 60 * 1000) % 24;
            long differenceMinutes = difference / (60 * 1000) % 60;
            boolean days = false, hours = false, minutes = false;
            String timeSinceCreated;

            if (differenceDays == 0) {
                if (differenceHours == 0) {
                    if (differenceMinutes == 0) {
                        timeSinceCreated = "Just Now ";
                    } else {
                        timeSinceCreated = differenceMinutes + "M ";
                    }
                } else {
                    timeSinceCreated = differenceHours + "H, " + differenceMinutes + "M ";
                }
            } else {
                timeSinceCreated = differenceDays + "D, " + differenceHours + "H, " + differenceMinutes + "M ";
            }

            ChatBuilder.appendTextComponent(message, timeSinceCreated, TIME_COLOR,
                                            false, false, false, false, false, null,
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)),
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START + " staff expandTicket " + this.id));
        } else {
            ChatBuilder.appendTextComponent(message, "Error getting Ticket Date.", plugin.ERROR_COLOR);
        }


        //Get the Claimer
        String staff;
        if (this.assignedStaff.equalsIgnoreCase("None"))
        {
            staff = "UNCLAIMED";
        }
        else
        {
            staff = assignedStaff;
        }

        ChatBuilder.appendTextComponent(message, staff, CLAIMED_COLOR,
                                        false, false, false, false, false, null,
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)),
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.COMMAND_START +" staff claim " + this.id));

        return message;
    }


    public TextComponent toPlayerInfo()
    {
        TextComponent message = ChatBuilder.appendTextComponent(null, "Info: ", plugin.PREFIX);
        ChatBuilder.appendTextComponent(message, info, plugin.ALT_COLOR);

        ChatBuilder.appendTextComponent(message, ", Team: ", plugin.PREFIX);
        ChatBuilder.appendTextComponent(message, team.name(), team.color);

        ChatBuilder.appendTextComponent(message, ", Staff Member: ", plugin.PREFIX);
        ChatBuilder.appendTextComponent(message, assignedStaff, plugin.ALT_COLOR);

        ChatBuilder.appendTextComponent(message, " Created: ", plugin.PREFIX);
        ChatBuilder.appendTextComponent(message, dateCreated, plugin.ALT_COLOR);

        if(dateCleared != null)
        {
            ChatBuilder.appendTextComponent(message, " Cleared: ", plugin.PREFIX);
            ChatBuilder.appendTextComponent(message, dateCleared, plugin.ALT_COLOR);
        }

        if(!this.assignedStaff.equals("None"))
        {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + assignedStaff + " "));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here to message the assigned staff member.")));
        }

        TextComponent comments = ChatBuilder.appendTextComponent(null, " Comments: ", plugin.PREFIX);
        ChatBuilder.appendTextComponent(comments, getCommentAmountPlayer() + "", plugin.ALT_COLOR);
        comments.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,  "/" + plugin.COMMAND_START + " comment " + this.id));
        comments.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to open the comments for this ticket.")));

        message.addExtra(comments);

        return message;
    }


    //a fuckin heap of mutators and accessors below

    public String getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id.toString();
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public Team getTeam()
    {
        return team;
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public String getAssignedStaff()
    {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff)
    {
        this.assignedStaff = assignedStaff;
    }

    public String getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public String getDateCleared()
    {
        return dateCleared;
    }

    public void setDateCleared(String dateCleared)
    {
        this.dateCleared = dateCleared;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getInfo()
    {
        return info;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }



    @Override
    public int compareTo(Ticket ticket)
    {
        String str = ((Ticket) ticket).getDateCreated();
        return this.getDateCreated().compareTo(str);
    }

    private int getCommentAmount()
    {
        List<Comment> commentList = plugin.getDatabase().getAllComments(this.id);
        int commentAmount;

        if(commentList ==null)
        {
            commentAmount = 0;
        }
        else
        {
            commentAmount = commentList.size();
        }

        return commentAmount;
    }

    private int getCommentAmountPlayer()
    {
        List<Comment> commentList = plugin.getDatabase().getCommentsForPlayer(this.id);
        int commentAmount;

        if(commentList == null)
        {
            commentAmount = 0;
        }
        else
        {
            commentAmount = commentList.size();
        }

        return commentAmount;
    }
}
