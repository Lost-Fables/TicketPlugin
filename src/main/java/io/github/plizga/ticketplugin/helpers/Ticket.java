package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Represents a ticket as a Java object.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public class Ticket implements Comparable
{
    private String id;
    private String playerName;
    private Status status;
    private Team team;
    private String assignedModerator;
    private String dateCreated;
    private String dateCleared;
    private String location;
    private String info;

    private TicketPlugin plugin;

    public Ticket(JavaPlugin plugin, String id, String playerName, Status status, Team team, String assignedModerator,
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

    public String toBasicInfo()
    {
        String str = plugin.PREFIX + "User: " + plugin.ALT_COLOR + playerName +
                plugin.PREFIX + ", Info: " + plugin.ALT_COLOR + info +
                plugin.PREFIX + ", Team: " + plugin.ALT_COLOR + team +
                plugin.PREFIX + ", Date: " + plugin.ALT_COLOR + dateCreated;
        return str;
    }

    public String toPlayerInfo()
    {
        String str = plugin.PREFIX + "Info: " + plugin.ALT_COLOR + info +
                plugin.PREFIX + ", Team: " + plugin.ALT_COLOR + team +
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
