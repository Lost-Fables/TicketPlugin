package io.github.plizga.ticketplugin.sqlite;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database
{
    /** The name of the database as provided by the config of the plugin. */
    private String databaseName;
    /** This is the String that represents the creation of this sql table. */
    private String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS " + TICKET_TABLE_NAME + " (" +
            "`ID` int(11) NOT NULL," + //unique id of ticket
            "`Player` varchar(32) NOT NULL," + //player who gen'd the ticket
            "`PlayerUUID` int(11) NOT NULL" + //uuid of player who gen'd the ticket
            "`Status` varchar(32) NOT NULL" + //status of the ticket (OPEN, CLAIMED, CLOSED)
            "`Assigned_Team` varchar(32)" + //team assigned to the ticket, may be nobody
            "`Assigned_Moderator` varchar(32)" + //moderator working on the ticket
            "`Date_Created` varchar(32) NOT NULL" + //date the ticket was created
            "`Time_Created' varchar(32) NOT NULL" + //time the ticket was created
            "`Date_Cleared` varchar(32)" + //date the ticket was completed
            "`Time_Cleared' varchar(32)" + //time the ticket was completed
            "`Location` varchar(32) NOT NULL" + //location where the ticket was originally generated.
            "`Initial_Request` varchar(100) NOT NULL" + //request string associated with the ticket. Basically wtf is going on in the ticket.
            "`Admin_Flag` bool NOT NULL" + //admin flag, applied by a mod to a ticket that needs an admin to look at it.
            "PRIMARY KEY (`ID`)" + //The primary key of our table is going to be the ID of ticket because that's the id of the ticket and that's the ID of the ticket.
            ");"; //this is a closing parenthesis.


    /**
     * Constructor for an instance of SQLite. Attaches to the plugin passed upon thee, and establishes the name of the
     * database.
     * @param plugin    the JavaPlugin being attached to this SQLite database.
     */
    public SQLite(JavaPlugin plugin)
    {
        super(plugin);
        databaseName = plugin.getConfig().getString("SQLite.Filename", TICKET_TABLE_NAME);
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
            plugin.getLogger().log(Level.SEVERE, "SQLite JDBC library is required for TicketPlugin.");
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
}
