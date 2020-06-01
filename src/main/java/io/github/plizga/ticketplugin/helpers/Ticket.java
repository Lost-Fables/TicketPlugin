package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import net.md_5.bungee.api.chat.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a ticket as a Java object.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public class Ticket implements Comparable
{
    private static final int MINI_DESCRIPTION_LENGTH = 25;
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


    private TicketPlugin plugin;

    public Ticket(JavaPlugin plugin, String id, String playerName, String playerID, Status status, Team team, String assignedModerator,
                  String dateCreated, String dateCleared, String location, String info)
    {
        this.plugin = (TicketPlugin) plugin;
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
        String str = plugin.PREFIX + "Ticket ID: " + plugin.ALT_COLOR + id +
                plugin.PREFIX + "\nPlayer: " + plugin.ALT_COLOR + playerName +
                plugin.PREFIX + "\nInfo: " + plugin.ALT_COLOR+ info +
                plugin.PREFIX + "\nStatus: " + plugin.ALT_COLOR+ status.toString() +
                plugin.PREFIX + "\nTeam: " + plugin.ALT_COLOR+ team.toString() +
                plugin.PREFIX +  "\nAssigned Staff Member: " + plugin.ALT_COLOR+ assignedModerator +
                plugin.PREFIX + "\nDate Created: " + plugin.ALT_COLOR+ dateCreated +
                plugin.PREFIX + "\nDate Cleared: " + plugin.ALT_COLOR+ dateCleared +
                plugin.PREFIX +  "\nLocation: " + plugin.ALT_COLOR+ location
                ;
        return str;
    }

    public ComponentBuilder toBasicInfo()
    {
        /*String str = plugin.PREFIX + "User: " + plugin.ALT_COLOR + playerName +
                plugin.PREFIX + ", Info: " + plugin.ALT_COLOR + info +
                plugin.PREFIX + ", Team: " + plugin.ALT_COLOR + team +
                plugin.PREFIX + ", Date: " + plugin.ALT_COLOR + dateCreated;
        return str;*/

        TextComponent[] textComponents = new TextComponent[5];
        //Set the team
        TextComponent teamPrefix = new TextComponent(plugin.PREFIX + "[" + plugin.ALT_COLOR + team.toString().charAt(0) +
                plugin.PREFIX + "] ");
        teamPrefix.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                "/" + plugin.COMMAND_START + " staff reassignTicket " + this.id));

        textComponents[0] = teamPrefix;

        //Set the player
        TextComponent username = new TextComponent(plugin.ALT_COLOR + "" + playerName + plugin.PREFIX + ": ");
        username.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName));

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

        TextComponent description = new TextComponent(plugin.ALT_COLOR + shortDescription + " ");
        description.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START + " staff expandTicket " + this.getId()));

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

        long difference = currentDate.getTime() - ticketDate.getTime();
        long differenceHours = difference / (60 * 60 * 1000) % 24;
        long differenceMinutes = difference / (60 * 1000) % 60;

        TextComponent timeSinceCreated = new TextComponent(plugin.PREFIX + " | " + plugin.ALT_COLOR + differenceHours +
                plugin.PREFIX + "H, " +plugin.ALT_COLOR+ differenceMinutes +plugin.PREFIX + "M | ");
        timeSinceCreated.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START + " staff expandTicket " + this.id));

        textComponents[3] = timeSinceCreated;


        //get the claimer
        TextComponent claimer;

        if(this.assignedModerator.equals("None"))
        {
            claimer = new TextComponent(plugin.ALT_COLOR +"UNCLAIMED");
        }
        else
        {
            claimer = new TextComponent(plugin.ALT_COLOR +assignedModerator);
        }
        claimer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START +" staff claim " + this.id));

        textComponents[4] = claimer;

        ComponentBuilder componentBuilder = new ComponentBuilder("");
        componentBuilder.append(textComponents);

        //todo fix event
        componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.info).create()));

        return componentBuilder;

    }

    public String toBasicInfoClaimed()
    {
        String str = plugin.PREFIX + "User: " + plugin.ALT_COLOR + playerName +
                plugin.PREFIX + ", Info: " + plugin.ALT_COLOR + info +
                plugin.PREFIX + ", Team: " + plugin.ALT_COLOR + team +
                plugin.PREFIX + ", Claimed by: " + plugin.ALT_COLOR + assignedModerator +
                plugin.PREFIX + ", Date: " + plugin.ALT_COLOR + dateCreated;
        return str;
    }

    public String toPlayerInfo()
    {
        String str = plugin.PREFIX + "Info: " + plugin.ALT_COLOR + info +
                plugin.PREFIX + ", Team: " + plugin.ALT_COLOR + team +
                plugin.PREFIX + ", Staff Member: " + plugin.ALT_COLOR + assignedModerator +
                plugin.PREFIX + " Created: " + plugin.ALT_COLOR + dateCreated;
        return str;
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
    public int compareTo(Object o)
    {
        if(o instanceof Ticket)
        {
            String str = ((Ticket) o).getDateCreated();
            return this.getDateCreated().compareTo(str);
        }
        return -1;
    }
}
