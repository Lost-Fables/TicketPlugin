package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a ticket as a Java object.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public class Ticket implements Comparable
{
    private static final int MINI_DESCRIPTION_LENGTH = 25;
    private static final String USERNAME_COLOR = ChatColor.DARK_AQUA + "";
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

    public ComponentBuilder toExpandedInfo()
    {
        ArrayList<TextComponent> textComponents = new ArrayList<>();

        //id
        TextComponent ticketID = new TextComponent(plugin.PREFIX + "Ticket ID: " + plugin.ALT_COLOR + id + "\n");
        textComponents.add(ticketID);

        //Player
        TextComponent username = new TextComponent(plugin.PREFIX + "Player: " + plugin.ALT_COLOR + playerName + "\n");
        username.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        username.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to message this player.").create()));

        textComponents.add(username);

        //Info
        TextComponent infoText = new TextComponent(plugin.PREFIX + "Info: " + plugin.ALT_COLOR + info + "\n");

        textComponents.add(infoText);

        //Status
        TextComponent statusText = new TextComponent(plugin.PREFIX + "Status: " + plugin.ALT_COLOR + status + "\n");
        if(status.equals(Status.CLAIMED) || status.equals(Status.OPEN))
        {
            statusText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/" + plugin.COMMAND_START +" staff claim " + this.id));
            statusText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to attempt to claim this ticket.").create()));
        }

        textComponents.add(statusText);

        //Team

        TextComponent teamText = new TextComponent(plugin.PREFIX + "Team: " + Team.getColor(team) + team.name() + "\n");
        teamText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START + " staff reassignTicket " + this.id));
        teamText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to reassign this ticket to another team.").create()));

        textComponents.add(teamText);


        //Assigned Staff Member
        TextComponent assignedStaffText = new TextComponent(plugin.PREFIX + "Assigned Staff: " + plugin.ALT_COLOR + assignedModerator + "\n");
        assignedStaffText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + assignedModerator + " "));
        assignedStaffText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to message this staff member.").create()));

        textComponents.add(assignedStaffText);

        //Date Created and Date Cleared
        TextComponent dateText = new TextComponent(plugin.PREFIX + "Date Created: " + plugin.ALT_COLOR + dateCreated + "\n" +
                plugin.PREFIX + "Date Cleared: " + plugin.ALT_COLOR + dateCleared + "\n");
        dateText.setHoverEvent(null);

        textComponents.add(dateText);


        //Location - There will be 4 items in the String array due to being hard-coded into the SQL database
        TextComponent locationText;
        if(location.contains(","))
        {
            String[] locationArray = this.location.split(",");

            String worldName = locationArray[0];

            Double x = Double.parseDouble(locationArray[1]);

            Double y = Double.parseDouble(locationArray[2]);

            Double z = Double.parseDouble(locationArray[3]);

            locationText = new TextComponent(plugin.PREFIX + "Location: " + plugin.ALT_COLOR + worldName +
                    ": X:" + x.shortValue() + " Y:" + y.shortValue() + " Z:" + z.shortValue());
            locationText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/" + plugin.COMMAND_START + " staff ticketTP "+ worldName + " " + x.intValue() + " " + y.intValue() + " " + z.intValue()));
            locationText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to teleport to this location.").create()));

        }
        else
        {
            locationText = new TextComponent(plugin.ERROR_COLOR +
                    "The following location information is from an erroneous or out-dated format. Please contact a developer if this persists. \n" +
                    plugin.PREFIX + "Location: " + plugin.ALT_COLOR + location);
        }

        textComponents.add(locationText);

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
        teamPrefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.info).create()));

        textComponents[0] = teamPrefix;

        //Set the player
        TextComponent username = new TextComponent(USERNAME_COLOR + "" + playerName + ChatColor.DARK_GRAY + ": ");
        username.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        username.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.info).create()));

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
        description.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.info).create()));


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

        TextComponent timeSinceCreated;
        if(differenceHours == 0)
        {
            if(differenceMinutes == 0)
            {
                timeSinceCreated = new TextComponent(TIME_COLOR + "Just Now ");
            }
            else
            {
                timeSinceCreated = new TextComponent(TIME_COLOR + differenceMinutes + "M ");
            }

        }
        else
        {
            timeSinceCreated = new TextComponent(TIME_COLOR + differenceHours +
                    "H, " + differenceMinutes + "M ");

        }
        timeSinceCreated.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START + " staff expandTicket " + this.id));
        timeSinceCreated.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.info).create()));



        textComponents[3] = timeSinceCreated;


        //get the claimer
        TextComponent claimer;

        if(this.assignedModerator.equals("None"))
        {
            claimer = new TextComponent(CLAIMED_COLOR +"UNCLAIMED");
        }
        else
        {
            claimer = new TextComponent(CLAIMED_COLOR +assignedModerator);
        }
        claimer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + plugin.COMMAND_START +" staff claim " + this.id));
        claimer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.info).create()));

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
            playerInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to message the assigned staff member.").create()));
        }
        return new ComponentBuilder(playerInfo);
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
