package io.github.plizga.ticketplugin.database;

import io.github.plizga.ticketplugin.commands.BaseCommand;
import io.github.plizga.ticketplugin.enums.Status;

import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Comment;
import io.github.plizga.ticketplugin.helpers.Review;
import io.github.plizga.ticketplugin.helpers.Staff;
import io.github.plizga.ticketplugin.helpers.Ticket;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Abstract class for the database. Defines the table name to be used in the ConcreteDatabase, and handles some basic
 * information about the database that can be abstracted here.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public abstract class Database extends BaseCommand
{
    /** The Java Plugin which will be associated with this database. */
    protected Plugin plugin;
    /** The SQL Connection reference.*/
    Connection connection;
    /** The name of the table to be established in the database. */
     final String TICKET_TABLE_NAME = "ticket_table";
    /** The name of the table to be used for comments. */
    final String COMMENT_TABLE_NAME = "comment_table";
    /** The name of the table to be used for reviews. */
    final String REVIEW_TABLE_NAME = "review_table";
    /** The name of the table to be used for tracking duty. */
    final String DUTY_TABLE_NAME = "duty_table";
    /**
     * The Constructor for the abstract class Database. Simply takes in the plugin.
     * @param plugin    the JavaPlugin associated with this database.
     */
    Database(Plugin plugin)
    {
        this.plugin = plugin;
    }



    /**
     * Handles initialization of the database by establishing the connection.
     */
    public void initialize()
    {
        connection = getSqlConnection();
    }


    /**
     * First of the overloaded close methods. Closes a {PreparedStatement} and a {ResultSet}
     * @param preparedStatement The PreparedStatement being closed.
     * @param resultSet The ResultSet being closed.
     */
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

    /**
     * Second of the overloaded close methods. Closes a {PreparedStatement} and a {Connection}
     * @param preparedStatement     The PreparedStatement being closed.
     * @param connection    The Connection being closed.
     */
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

    /**
     * Third of the overloaded close methods. Closes a {PreparedStatement} and a {ResultSet}
     * @param preparedStatement     The PreparedStatement being closed.
     * @param resultSet     The ResultSet being closed.
     * @param connection    The Connection being closed.
     */
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

    /**
     * Creates a new ticket while generating a random UUID for it.
     * @param player     the player filing the ticket
     * @param status     the status of the ticket upon creation
     * @param ticketData the string representation of the ticket.
     */
    public void createNewTicket(ProxiedPlayer player, String location, Status status, Team team, String ticketData) {
        createNewTicket(UUID.randomUUID(), player, location, status, team, ticketData);
    }


    protected abstract Connection getSqlConnection();

    public abstract void load();

    public abstract void createNewTicket(UUID ticketUUID, ProxiedPlayer player, String location, Status status, Team team, String ticketData);

    public abstract void createNewComment(String player, String text, String ticketUUID, boolean isStaffOnly);

    public abstract List<Ticket> getAllOpenTickets();

    public abstract List<Ticket> getAllClaimedTickets();

    public abstract List<Ticket> getPlayerOpenTickets(String playerName);

    public abstract void cancelTicketByUUID(String uuid);

    public abstract void cancelTicketByPlayer(String player);

    public abstract List<Ticket> getOpenTicketsByTeam(String team);

    public abstract Ticket getTicketByUUID(String uuid);

    public abstract void claimTicket(String uuid, String player);

    public abstract void unClaimTicket(String uuid);

    public abstract List<Ticket> getClaimedTickets(String player);

    public abstract void setTeam(String uuid, String team);

    public abstract List<Comment> getAllComments(String uuid);

    public abstract List<Comment> getCommentsForPlayer(String uuid);

    public abstract List<Ticket> getTeamClaimedTickets(String team);

    public abstract List<Ticket> getCompletedPlayerTickets(String player);

    public abstract void createNewReview(String ticketUUID, int rating);

    public abstract Review getReview(String ticketUUID);

    public abstract void closeTicket(String ticketUUID);

    public abstract void staffOnDuty(String playerUUID, boolean onDuty);

    public abstract List<Staff> getStaff();

    public abstract Staff getStaff(String staff);

    public abstract void removeStaffFromOnDuty(String uuid);

    public abstract void updateStaffOnDuty(String playerUUID, boolean onDuty);






}
