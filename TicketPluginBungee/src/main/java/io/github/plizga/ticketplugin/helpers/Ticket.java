package io.github.plizga.ticketplugin.helpers;

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
    private static final String USERNAME_COLOR_ONLINE = ChatColor.GREEN + "";
    private static final String USERNAME_COLOR_OFFLINE = ChatColor.RED + "";
    private static final String TEXT_COLOR = ChatColor.GRAY + "";
    private static final String TIME_COLOR = ChatColor.YELLOW + "";
    private static final String CLAIMED_COLOR = ChatColor.AQUA + "";
    private String id;
    private String playerName;
    private Status status;
    private Team team;
    private String assignedModerator;
    private String dateCreated;
    private String dateCleared;
    private String location;
    private String info;
    private String playerID;


    private TicketPluginBungee plugin;

    public Ticket(Plugin plugin, String id, String playerName, String playerID, Status status, Team team, String assignedModerator,
                  String dateCreated, String dateCleared, String location, String info)
    {
        this.plugin = (TicketPluginBungee) plugin;
        this.id = id;
        this.playerName = playerName;
        this.status = status;
        this.team = team;
        this.assignedModerator = assignedModerator;
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
               plugin.PREFIX + "\nAssigned Staff Member: " + plugin.ALT_COLOR + assignedModerator +
               plugin.PREFIX + "\nDate Created: " + plugin.ALT_COLOR + dateCreated +
               plugin.PREFIX + "\nDate Cleared: " + plugin.ALT_COLOR + dateCleared +
               plugin.PREFIX + "\nLocation: " + plugin.ALT_COLOR + location;
    }

    public ComponentBuilder toExpandedInfo()
    {
        ArrayList<TextComponent> textComponents = new ArrayList<>();

        //id
        TextComponent ticketID = new TextComponent(plugin.PREFIX + "Ticket ID: " + plugin.ALT_COLOR + id + "\n");
        textComponents.add(ticketID);

        //Player
        TextComponent username;
        ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
        if(player == null)
        {
            username = new TextComponent(plugin.PREFIX + "Player: " + USERNAME_COLOR_OFFLINE + playerName + "\n");
        }
        else
        {
            username = new TextComponent(plugin.PREFIX + "Player: " + USERNAME_COLOR_ONLINE + playerName + "\n");
        }

        username.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        username.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to message this player.")));

        textComponents.add(username);

        //Info
        TextComponent infoText = new TextComponent(plugin.PREFIX + "Info: " + plugin.ALT_COLOR + info + "\n");

        textComponents.add(infoText);

        //Status
        TextComponent statusText = new TextComponent(plugin.PREFIX + "Status: " + plugin.ALT_COLOR + status + " ");
        if(status.equals(Status.CLAIMED) || status.equals(Status.OPEN))
        {
            statusText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/" + plugin.COMMAND_START +" staff claim " + this.id));
            statusText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("Click to attempt to claim this ticket.")));
        }

        textComponents.add(statusText);

        //Team
        TextComponent teamText = new TextComponent(plugin.PREFIX + "Team: " + Team.getColor(team) + team.name() + " ");
        teamText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START + " staff reassignTicket " + this.id));
        teamText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text("Click to reassign this ticket to another team.")));

        textComponents.add(teamText);


        //Assigned Staff Member
        TextComponent assignedStaffText = new TextComponent(plugin.PREFIX + "Assigned Staff: " + plugin.ALT_COLOR + assignedModerator + "\n");
        assignedStaffText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + assignedModerator + " "));
        assignedStaffText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to message this staff member.")));

        textComponents.add(assignedStaffText);

        //Date Created and Date Cleared
        TextComponent dateText = new TextComponent(plugin.PREFIX + "Date Created: " + plugin.ALT_COLOR + dateCreated + "\n" +
                plugin.PREFIX + "Date Cleared: " + plugin.ALT_COLOR + dateCleared + "\n");
        dateText.setHoverEvent(null);

        textComponents.add(dateText);


        //Location - There will be 4 items in the String array due to being hard-coded into the SQL database
        TextComponent locationText = null;
        if(location.contains(",")) {
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

            locationText = new TextComponent(plugin.PREFIX + "Location: " + plugin.ALT_COLOR + worldName +
                                             ": X:" + (short) x + " Y:" + (short) y + " Z:" + (short) z);
            locationText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                      "/" + plugin.COMMAND_START + " staff ticketTP " + worldName + " " + x + " " + y + " " + z + " " + serverName));
            locationText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to teleport to this location.")));
        }
        else
        {
            locationText = new TextComponent(plugin.ERROR_COLOR +
                    "The following location information is from an erroneous or out-dated format. Please contact a developer if this persists. \n" +
                    plugin.PREFIX + "Location: " + plugin.ALT_COLOR + location);
        }

        textComponents.add(locationText);

        //Comments
        int commentAmount = getCommentAmount();
        TextComponent commentText = new TextComponent(plugin.PREFIX + " Comments: " + plugin.ALT_COLOR + commentAmount + " ");
        textComponents.add(commentText);

        //build
        ComponentBuilder componentBuilder = new ComponentBuilder("");

        for(TextComponent textComponent : textComponents)
        {
            componentBuilder.append(textComponent);

        }

        return componentBuilder;

    }


    public ComponentBuilder toBasicInfo()
    {

        TextComponent[] textComponents = new TextComponent[5];
        //Set the team
        TextComponent teamPrefix = new TextComponent(Team.getColor(team) + "[" + team.toString().charAt(0) +
                "] ");
        teamPrefix.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/" + plugin.COMMAND_START + " staff reassignTicket " + this.id));
        teamPrefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)));

        textComponents[0] = teamPrefix;

        //Set the player

        ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
        TextComponent username;

        if(player == null)
        {
            username = new TextComponent(USERNAME_COLOR_OFFLINE + "" + playerName + ChatColor.DARK_GRAY + ": ");
        }
        else
        {
            username = new TextComponent(USERNAME_COLOR_ONLINE + "" + playerName + ChatColor.DARK_GRAY + ": ");
        }

        username.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        username.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)));

        textComponents[1] = username;


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

        TextComponent description = new TextComponent(TEXT_COLOR + shortDescription + " ");
        description.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START + " staff expandTicket " + this.getId()));
        description.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)));


        textComponents[2] = description;


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
            TextComponent timeSinceCreated;

            if (differenceDays == 0) {
                if (differenceHours == 0) {
                    if (differenceMinutes == 0) {
                        timeSinceCreated = new TextComponent(TIME_COLOR + "Just Now ");
                    } else {
                        timeSinceCreated = new TextComponent(TIME_COLOR + differenceMinutes + "M ");
                    }
                } else {
                    timeSinceCreated = new TextComponent(TIME_COLOR + differenceHours +
                                                         "H, " + differenceMinutes + "M ");
                }
            } else {
                timeSinceCreated = new TextComponent(TIME_COLOR + differenceDays +
                                                     "D, " + differenceHours +
                                                     "H, " + differenceMinutes + "M ");
            }

            timeSinceCreated.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                          "/" + plugin.COMMAND_START + " staff expandTicket " + this.id));
            timeSinceCreated.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)));


            textComponents[3] = timeSinceCreated;
        } else {
            textComponents[3] = new TextComponent("Error getting Ticket Date.");
        }


        //get the claimer
        TextComponent claimer;

        if(this.assignedModerator.equalsIgnoreCase("None"))
        {
            claimer = new TextComponent(CLAIMED_COLOR +"UNCLAIMED");
        }
        else
        {
            claimer = new TextComponent(CLAIMED_COLOR +assignedModerator);
        }
        claimer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START +" staff claim " + this.id));
        claimer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.info)));

        textComponents[4] = claimer;

        ComponentBuilder componentBuilder = new ComponentBuilder("");
        componentBuilder.append(textComponents);




        return componentBuilder;

    }


    public ComponentBuilder toPlayerInfo()
    {
        String str = plugin.PREFIX + "Info: " + plugin.ALT_COLOR + info +
                plugin.PREFIX + ", Team: " + Team.getColor(team) + team +
                plugin.PREFIX + ", Staff Member: " + plugin.ALT_COLOR + assignedModerator +
                plugin.PREFIX + " Created: " + plugin.ALT_COLOR + dateCreated;

        if(dateCleared != null)
        {
            str = str.concat(plugin.PREFIX + " Cleared: " + plugin.ALT_COLOR + dateCleared);
        }

        TextComponent playerInfo = new TextComponent(str);
        if(!this.assignedModerator.equals("None"))
        {
            playerInfo.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + assignedModerator + " "));
            playerInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here to message the assigned staff member.")));
        }

        int commentAmount = getCommentAmountPlayer();
        TextComponent viewComments = new TextComponent(plugin.PREFIX + " Comments: " + plugin.ALT_COLOR + commentAmount);
        viewComments.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,  "/" + plugin.COMMAND_START + " comment " + this.id));
        viewComments.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to open the comments for this ticket.")));
        return new ComponentBuilder(playerInfo).append(viewComments);
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

    public String getAssignedModerator()
    {
        return assignedModerator;
    }

    public void setAssignedModerator(String assignedModerator)
    {
        this.assignedModerator = assignedModerator;
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
