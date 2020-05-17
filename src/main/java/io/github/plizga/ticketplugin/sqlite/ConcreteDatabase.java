package io.github.plizga.ticketplugin.sqlite;

import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ConcreteDatabase extends Database
{
    /** The name of the database as provided by the config of the plugin. */
    private String databaseName;
    /** This is the String that represents the creation of this sql table. IMPORTANT!!! If changes are made here,
     * please ensure you make the necessary changes in the CreateNewTicket function as well. */
    private String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS " + TICKET_TABLE_NAME + " (" +
            "`id` varchar(36) NOT NULL," + //unique id of ticket
            "`Player` varchar(32) NOT NULL," + //player who gen'd the ticket
            "`Status` varchar" +
            "(32) NOT NULL," + //status of the ticket (OPEN, CLAIMED, CLOSED)
            "`Assigned_Team` varchar(32) NOT NULL," + //team assigned to the ticket
            "`Assigned_Moderator` varchar(32)," + //moderator working on the ticket
            "`Date_Created` varchar(32) NOT NULL," + //date the ticket was created
            "`Date_Cleared` varchar(32)," + //date the ticket was completed
            "`Location` varchar(32) NOT NULL," + //location where the ticket was originally generated.
            "`Initial_Request` varchar(100) NOT NULL," + //request string associated with the ticket. Basically wtf is going on in the ticket.
            "PRIMARY KEY (`id`)" + //The primary key of our table is going to be the ID of ticket because that's the id of the ticket and that's the ID of the ticket.
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
     */
    @Override
    public void createNewTicket(Player player, Status status, Team team, String ticketData)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            //BELOW - The question marks need to match the amount of columns. Don't ask me why
            preparedStatement = connection.prepareStatement("REPLACE INTO " + TICKET_TABLE_NAME +
                    " (id,Player,Status,Assigned_Team,Assigned_Moderator,Date_Created," +
                    "Date_Cleared,Location,Initial_Request) VALUES(?,?,?,?,?,?,?,?,?)");
            //SetString requires an index associated with the column being changed. For this default dance ticket, we will modify some core details that cannot be null.
            //NOTE!! Index is 1-based here, not 0. don't fucking zero index it. that's so silly dude imagine using the number 0 in literally anything.
            preparedStatement.setString(1, UUID.randomUUID().toString());

            preparedStatement.setString(2, player.getName().toLowerCase());

            preparedStatement.setString(3, status.name());

            preparedStatement.setString(4, team.name());

            preparedStatement.setString(5, "None");

            //create a date for the next one.
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            preparedStatement.setString(6, dateFormat.format(date));

            preparedStatement.setString(7, null);

            preparedStatement.setString(8, player.getLocation().toString());

            preparedStatement.setString(9, ticketData);


            preparedStatement.executeUpdate();

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
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME + " WHERE id = '" +
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
            close(preparedStatement, resultSet, connection);
        }

        return null;
    }

    public List getPlayerOpenTickets(String playerName)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Ticket> ticketList = new ArrayList<>();

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME +
                    " WHERE Player = '" + playerName + "' AND Status = '" + Status.OPEN.name() +
                    "' ORDER BY 'Date_Created';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Ticket ticket = makeTicket(resultSet);

                ticketList.add(ticket);
            }
            return ticketList;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return null;
    }

    @Override
    public List getTicketsByTeam(String team)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Ticket> ticketList = new ArrayList<>();

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME + " WHERE Assigned_Team = '" +
                    team + "' AND Status = '" + Status.OPEN.name() +
                    "' ORDER BY 'Date_Created';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Ticket ticket = makeTicket(resultSet);

                ticketList.add(ticket);
            }
            return ticketList;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return null;

    }

    public List getAllOpenTickets()
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Ticket> ticketList = new ArrayList<Ticket>();
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME +
                    " WHERE Status = '" + Status.OPEN.name() + "';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Ticket ticket = makeTicket(resultSet);

                ticketList.add(ticket);

            }

            return ticketList;

        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }

        return null;
    }

    @Override
    public Ticket getTicketByUUID(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Ticket ticket = null;


        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME +
                    " WHERE id = '" + uuid + "';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                ticket = makeTicket(resultSet);
            }

            return ticket;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return null;
    }

    @Override
    public void cancelTicketByUUID(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;


        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + TICKET_TABLE_NAME +
                    " SET Status = '" + Status.CANCELLED.name() +
                    "' WHERE id = '" + uuid + "';");

            preparedStatement.executeUpdate();
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

    @Override
    public void cancelTicketByPlayer(String player)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + TICKET_TABLE_NAME +
                    " Set Status ='" + Status.CANCELLED.name() +
                    "' WHERE Player = '" + player + "';");
            preparedStatement.executeUpdate();
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
     * Claims a ticket.
     * PRECONDITION: The ticket's claimability should be checked using the method {isClaimable} prior to this method
     * being called. This method will override the ticket's current claimer otherwise. This could be applicable for admins,
     * however it should not be used otherwise.
     * @param uuid  uuid of the ticket
     * @param player    name of the player attempting to claim
     */
    @Override
    public void claimTicket(String uuid, String player)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + TICKET_TABLE_NAME +
                    " Set Assigned_Moderator = '" + player +
                    "', Status = '" + Status.CLAIMED +
                    "' WHERE id = '" + uuid + "';");

            preparedStatement.executeUpdate();
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

    @Override
    public void unClaimTicket(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + TICKET_TABLE_NAME +
                    " Set Assigned_Moderator = '" + "None" +
                    "', Status = '" + Status.OPEN.name() +
                    "' WHERE id = '" + uuid + "';");

            preparedStatement.executeUpdate();
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

    @Override
    public List<Ticket> getClaimedTickets(String player)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Ticket> ticketList = new ArrayList<Ticket>();
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME +
                    " WHERE Status = '" + Status.CLAIMED.name() +
            "' AND Assigned_Moderator = '" + player + "';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Ticket ticket = makeTicket(resultSet);

                ticketList.add(ticket);

            }

            return ticketList;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return ticketList;
    }

    @Override
    public void setTeam(String uuid, String team)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + TICKET_TABLE_NAME +
                    " Set Assigned_Team = '" + team +
                    "', Status = '" + Status.OPEN.name() +
                    "', Assigned_Moderator = '" + "NONE" +
                    "' WHERE id = '" + uuid + "';");

            preparedStatement.executeUpdate();
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

    private Ticket makeTicket(ResultSet resultSet) throws SQLException
    {
        String id = resultSet.getString("id");
        String playerName = resultSet.getString("Player");
        Status status = Status.valueOf(resultSet.getString("Status"));
        Team team = Team.valueOf(resultSet.getString("Assigned_Team"));
        String assignedModerator = resultSet.getString("Assigned_Moderator");
        String dateCreated = resultSet.getString("Date_Created");
        String dateCleared = resultSet.getString("Date_Cleared");
        String location = resultSet.getString("Location");
        String info = resultSet.getString("Initial_Request");

        return new Ticket(this.plugin, id, playerName, status, team, assignedModerator, dateCreated, dateCleared,
                location, info);
    }

}