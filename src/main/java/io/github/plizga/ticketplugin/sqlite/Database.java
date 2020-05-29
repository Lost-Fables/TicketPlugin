package io.github.plizga.ticketplugin.sqlite;

import io.github.plizga.ticketplugin.commands.BaseCommand;
import io.github.plizga.ticketplugin.enums.Status;

import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Comment;
import io.github.plizga.ticketplugin.helpers.Ticket;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Abstract class for the database. Defines the table name to be used in the ConcreteDatabase, and handles some basic
 * information about the database that can be abstracted here. This is especially cool because I used SQLite like
 * a dingus so when all that has to be replaced, abstraction should make it a bit easier.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public abstract class Database extends BaseCommand
{
    /** The Java Plugin which will be associated with this database. */
    protected JavaPlugin plugin;

    protected Connection connection;

    /** The name of the table to be established in the database. */
    protected final String TICKET_TABLE_NAME = "ticket_table";

    /** The name of the table to be used for comments. */
    protected final String COMMENT_TABLE_NAME = "comment_table";

    /**
     * The Constructor for the abstract class Database. Simply takes in the plugin.
     * @param plugin    the JavaPlugin associated with this database.
     */
    public Database(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    protected abstract Connection getSqlConnection();

    public abstract void load();

    /**
     * Handles initialization of the database following other pieces of setup.
     */
    public void initialize()
    {
        connection = getSqlConnection();
    }

     void close(PreparedStatement preparedStatement, ResultSet resultSet)
    {
        try
        {
            if(preparedStatement != null)
            {
                preparedStatement.close();
            }
            if(resultSet != null)
            {
                resultSet.close();
            }
        }
        catch (SQLException e)
        {

            Error.close(plugin, e);
        }
    }
     void close(PreparedStatement preparedStatement, Connection connection)
    {
        try
        {
            if(preparedStatement != null)
            {
                preparedStatement.close();
            }
            if(connection != null)
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {

            Error.close(plugin, e);
        }
    }

    void close(PreparedStatement preparedStatement, ResultSet resultSet, Connection connection)
    {
        try
        {
            if(preparedStatement != null)
            {
                preparedStatement.close();
            }
            if(resultSet != null)
            {
                preparedStatement.close();
            }
            if(connection != null)
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {

            Error.close(plugin, e);
        }
    }


    //todo will need to be updated when enum status is a thing!!
    public abstract void createNewTicket(Player player, Status status, Team team, String ticketData);

    public abstract void createNewComment(String player, String text, String ticketUUID, boolean isStaffOnly);

    public abstract List getAllOpenTickets();

    public abstract List<Ticket> getAllClaimedTickets();

    public abstract List getPlayerOpenTickets(String playerName);

    public abstract void cancelTicketByUUID(String uuid);

    public abstract void cancelTicketByPlayer(String player);

    public abstract List getOpenTicketsByTeam(String team);

    public abstract Ticket getTicketByUUID(String uuid);

    public abstract void claimTicket(String uuid, String player);

    public abstract void unClaimTicket(String uuid);

    public abstract List<Ticket> getClaimedTickets(String player);

    public abstract void setTeam(String uuid, String team);

    public abstract List<Comment> getAllComments(String uuid);

    public abstract List<Comment> getCommentsForPlayer(String uuid);

    public abstract List<Ticket> getTeamClaimedTickets(String team);

    public List getAllClaimedTickets(Player player)
    {
        return null;
    }

    public List getAllAccessibleTickets(Player player)
    {
        return null;
    }





}
