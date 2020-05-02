package io.github.plizga.ticketplugin.sqlite;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class ConcreteDatabase extends Database
{
    /** The name of the database as provided by the config of the plugin. */
    private String databaseName;
    /** This is the String that represents the creation of this sql table. IMPORTANT!!! If changes are made here,
     * please ensure you make the necessary changes in the CreateNewTicket function as well. */
    private String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS " + TICKET_TABLE_NAME + " (" +
            "`id` int(11) NOT NULL," + //unique id of ticket
            "`Player` varchar(32) NOT NULL," + //player who gen'd the ticket
            "`PlayerUUID` int(11) NOT NULL" + //uuid of player who gen'd the ticket
            "`Status` varchar(32) NOT NULL" + //status of the ticket (OPEN, CLAIMED, CLOSED)
            "`Assigned_Team` varchar(32)" + //team assigned to the ticket, may be nobody
            "`Assigned_Moderator` varchar(32)" + //moderator working on the ticket
            "`Date_Created` varchar(32) NOT NULL" + //date the ticket was created
            "`Date_Cleared` varchar(32)" + //date the ticket was completed
            "`Location` varchar(32) NOT NULL" + //location where the ticket was originally generated.
            "`Initial_Request` varchar(100) NOT NULL" + //request string associated with the ticket. Basically wtf is going on in the ticket.
            "`Admin_Flag` bool NOT NULL" + //admin flag, applied by a mod to a ticket that needs an admin to look at it.
            "PRIMARY KEY (`ID`)" + //The primary key of our table is going to be the ID of ticket because that's the id of the ticket and that's the ID of the ticket.
            ");"; //this is a closing parenthesis.


    /**
     * Constructor for an instance of ConcreteDatabase. Attaches to the plugin passed upon thee, and establishes the name of the
     * database.
     * @param plugin    the JavaPlugin being attached to this ConcreteDatabase database.
     */
    public ConcreteDatabase(JavaPlugin plugin)
    {
        super(plugin);
        databaseName = plugin.getConfig().getString("ConcreteDatabase.Filename", TICKET_TABLE_NAME);
    }


    /**
     * The getSqlConnection method performs the necessary actions for establishing a db file/accessing a pre-existing
     * file, as well as connection to the jdbc socket in order to not get its shit wrecked.
     * @return  Connection if successful, null if otherwise. Upon being returned, the connection should be established.
     */
    @Override
    protected Connection getSqlConnection()
    {
        File data = new File(plugin.getDataFolder(), databaseName+".db");
        if(!data.exists())
        {
            try
            {
                data.createNewFile();
            }
            catch(IOException e)
            {
                plugin.getLogger().log(Level.SEVERE, "Error writing file:" + databaseName + ".db");
            }
        }
        try
        {
            if(connection != null && !connection.isClosed())
            {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + data);
            return connection;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error initializing SQlite database in method getSQLConnection.");
        }
        catch(ClassNotFoundException e)
        {
            plugin.getLogger().log(Level.SEVERE, "ConcreteDatabase JDBC library is required for TicketPlugin.");
        }
        //really shouldn't reach this...
        return null;
    }

    /**
     * The load function handles the execution of updates of {Statements} inside the Connection.
     */
    @Override
    public void load()
    {
        this.connection = getSqlConnection();

        try
        {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.close();
        }
        catch(SQLException e )
        {
            e.printStackTrace();
        }
        //we can initialize now!
        initialize();
    }

    /**
     * Creates a new ticket, given a player, a status, and the String sent with the ticket. Easily the most clusterfucky
     * of all the things in this package (so far :3 ).
     * @param player    the player filing the ticket
     * @param status    the status of the ticket upon creation
     * @param ticketData    the string representation of the ticket.
     * @param adminFlag represents whether this is a request that needs admin attention or not. (0 false, 1 true)
     */
    @Override
    void createNewTicket(Player player, String status, String ticketData, boolean adminFlag)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            //BELOW - The question marks need to match the amount of columns. Don't ask me why
            preparedStatement = connection.prepareStatement("REPLACE INTO " + TICKET_TABLE_NAME +
                    " (id,Player,PlayerUUID,Status,Assigned_Team,Assigned_Moderator,Date_Created," +
                    "Date_Cleared,Location,Initial_Request,Admin_Flag) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
            //SetString requires an index associated with the column being changed. For this default dance ticket, we will modify some core details that cannot be null.
            //NOTE!! Index is 1-based here, not 0. don't fucking zero index it. that's so silly dude imagine using the number 0 in literally anything.
            preparedStatement.setString(1, UUID.randomUUID().toString());

            preparedStatement.setString(2, player.getName());

            preparedStatement.setString(3, player.getUniqueId().toString());

            preparedStatement.setString(4, status);

            //create a date for the next one.
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            preparedStatement.setString(7, dateFormat.format(date));

            preparedStatement.setString(9, player.getLocation().toString());

            preparedStatement.setString(10, ticketData);

            if(adminFlag) //set to 1 if true, 0 otherwise.
            {
                preparedStatement.setString(11, "1");
            }
            else
            {
                preparedStatement.setString(11, "0");
            }

        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, connection);
        }
    }

    /**
     * Returns the STRING of the player associated with a ticket.
     * @param id    the ticket id associated with the player being requested.
     * @return  the STRING representation of the player associated with the ticket.
     */
    public String getPlayer(int id)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME + "WHERE id = '" +
                    id + "';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                if(resultSet.getInt("id") == id)
                {
                    return resultSet.getString("Player");
                }
            }
        } catch (SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet);
        }

        return null;
    }
}