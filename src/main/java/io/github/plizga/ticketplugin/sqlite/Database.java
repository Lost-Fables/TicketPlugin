package io.github.plizga.ticketplugin.sqlite;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstract class for the database. Defines the table name to be used in the ConcreteDatabase, and handles some basic
 * information about the database that can be abstracted here.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public abstract class Database
{
    /** The Java Plugin which will be associated with this database. */
    protected JavaPlugin plugin;

    protected Connection connection;

    /** The name of the table to be established in the database. */
    protected final String TICKET_TABLE_NAME = "ticket_table";

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

    public void close(PreparedStatement preparedStatement, ResultSet resultSet)
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
    public void close(PreparedStatement preparedStatement, Connection connection)
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

    //todo will need to be updated when enum status is a thing!!
    abstract void createNewTicket(Player player, String status, String ticketData, boolean adminFlag);


}
