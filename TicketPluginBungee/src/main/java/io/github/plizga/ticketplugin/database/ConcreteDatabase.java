package io.github.plizga.ticketplugin.database;

import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Comment;
import io.github.plizga.ticketplugin.helpers.Review;
import io.github.plizga.ticketplugin.helpers.Staff;
import io.github.plizga.ticketplugin.helpers.Ticket;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;

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
    private final String DATABASE_NAME;
    private final String USERNAME;
    private final String PASSWORD;
    private final String HOST;
    private final int PORT;



    private final String CONNECTION_STRING;


    /** This is the String that represents the creation of this sql table. IMPORTANT!!! If changes are made here,
     * please ensure you make the necessary changes in the CreateNewTicket function as well. */
    private String mySQLTicketTable = "CREATE TABLE IF NOT EXISTS " + TICKET_TABLE_NAME + " (" +
            "`id` varchar(36) NOT NULL," + //unique id of ticket
            "`Player` varchar(32) NOT NULL," +
            "`player_id` varchar(36) NOT NULL," +//player who gen'd the ticket
            "`Status` varchar(32) NOT NULL," + //status of the ticket (OPEN, CLAIMED, CLOSED)
            "`Assigned_Team` varchar(32) NOT NULL," + //team assigned to the ticket
            "`Assigned_Moderator` varchar(32)," + //moderator working on the ticket
            "`Date_Created` varchar(32) NOT NULL," + //date the ticket was created
            "`Date_Cleared` varchar(32)," + //date the ticket was completed
            "`Location` varchar(255) NOT NULL," + //location where the ticket was originally generated.
            "`Initial_Request` varchar(255) NOT NULL," + //request string associated with the ticket. Basically wtf is going on in the ticket.
            "PRIMARY KEY (`id`)" + //The primary key of our table is going to be the ID of ticket because that's the id of the ticket and that's the ID of the ticket.
            ");"; //this is a closing parenthesis.

    private String mySQLCommentsTable = "CREATE TABLE IF NOT EXISTS " + COMMENT_TABLE_NAME + " (" +
            "`id` varchar(36) NOT NULL," + //unique id of comment
            "`ticket_id` varchar(36) NOT NULL," +
            "`author` varchar(32) NOT NULL," +
            "`comment` varchar(100) NOT NULL," +
            "`date_created` varchar(32) NOT NULL," +
            "`staff_only` varchar(8) NOT NULL," +
            "PRIMARY KEY (`id`)" +
            ");";

    private String mySQLReviewTable = "CREATE TABLE IF NOT EXISTS " + REVIEW_TABLE_NAME + " (" +
            "`id` varchar(36) NOT NULL," +
            "`rating` tinyint NOT NULL," +
            "PRIMARY KEY (`id`)" +
            ");";

    private String mySqlOnDutyTable = "CREATE TABLE IF NOT EXISTS " + DUTY_TABLE_NAME + " (" +
            "`id` varchar(36) NOT NULL," +
            "`duty_status` tinyint NOT NULL," +
            "PRIMARY KEY (`id`)" +
            ");";



    /**
     * Constructor for an instance of ConcreteDatabase. Attaches to the plugin passed upon thee, and establishes the name of the
     * database.
     * @param plugin    the JavaPlugin being attached to this ConcreteDatabase database.
     */
    public ConcreteDatabase(Plugin plugin, String host, String username, String password, String database, int port)
    {
        super(plugin);
        this.HOST = host;
        this.USERNAME = username;
        this.PASSWORD = password;
        this.PORT = port;
        this.DATABASE_NAME = database;



        this.CONNECTION_STRING = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME + "?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=utf8&jdbcCompliantTruncation=false";

    }


    /**
     * The getSqlConnection method performs the necessary actions for establishing a db file/accessing a pre-existing
     * file, as well as connection to the jdbc socket in order to not get its shit wrecked.
     * @return  Connection if successful, null if otherwise. Upon being returned, the connection should be established.
     */
    @Override
    protected Connection getSqlConnection()
    {
        try
        {
            if (connection != null && !connection.isClosed())
            {
                return connection;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(CONNECTION_STRING, USERNAME, PASSWORD);

            return connection;
        }
        catch (SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error initializing mySQL database in method getSQLConnection.");
        }
        catch (ClassNotFoundException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Unable to find jdbc library for mySQL connection in method getSqlConnection.");
        }
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
            s.executeUpdate(mySQLTicketTable);
            s.executeUpdate(mySQLCommentsTable);
            s.executeUpdate(mySQLReviewTable);
            s.executeUpdate(mySqlOnDutyTable);
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
     * Creates a new ticket, given a player, a status, and the String sent with the ticket.
     * @param player    the player filing the ticket
     * @param status    the status of the ticket upon creation
     * @param ticketData    the string representation of the ticket.
     */
    @Override
    public void createNewTicket(ProxiedPlayer player, String location, Status status, Team team, String ticketData)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            //BELOW - The question marks need to match the amount of columns. Don't ask me why
            preparedStatement = connection.prepareStatement("REPLACE INTO " + TICKET_TABLE_NAME +
                    " (id,Player,player_id,Status,Assigned_Team,Assigned_Moderator,Date_Created," +
                    "Date_Cleared,Location,Initial_Request) VALUES(?,?,?,?,?,?,?,?,?,?)");
            //SetString requires an index associated with the column being changed. For this default dance ticket, we will modify some core details that cannot be null.
            //NOTE!! Index is 1-based here, not 0. don't fucking zero index it. that's so silly dude imagine using the number 0 in literally anything.
            preparedStatement.setString(1, UUID.randomUUID().toString());

            preparedStatement.setString(2, player.getName());

            preparedStatement.setString(3, player.getUniqueId().toString());

            preparedStatement.setString(4, status.name());

            preparedStatement.setString(5, team.name());

            preparedStatement.setString(6, "None");

            //create a date for the next one.
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            preparedStatement.setString(7, dateFormat.format(date));

            preparedStatement.setString(8, null);

            String locationString = location + "," + player.getServer().getInfo().getName();

            preparedStatement.setString(9, locationString);


            if(ticketData.length() >= 255)
            {
                ticketData = ticketData.substring(0,253);
            }

            preparedStatement.setString(10, ticketData);


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
    public void createNewComment(String player, String text, String ticketUUID, boolean isStaffOnly)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("REPLACE INTO " + COMMENT_TABLE_NAME +
                    " (id,ticket_id,author,comment,date_created,staff_only) VALUES(?,?,?,?,?,?)");

            preparedStatement.setString(1, UUID.randomUUID().toString());

            preparedStatement.setString(2, ticketUUID);

            preparedStatement.setString(3, player);

            preparedStatement.setString(4, text);

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            preparedStatement.setString(5,dateFormat.format(date)) ;

            if(isStaffOnly)
            {
                preparedStatement.setString(6, "true");
            }
            else
            {
                preparedStatement.setString(6, "false");
            }

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
    public void createNewReview(String ticketUUID, int rating)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("REPLACE INTO " + REVIEW_TABLE_NAME +
                    " (id,rating) VALUES(?,?)");

            preparedStatement.setString(1, ticketUUID);


            preparedStatement.setInt(2, rating);


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
    public Review getReview(String ticketUUID)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + REVIEW_TABLE_NAME + " WHERE id = '" +
                    ticketUUID +
                    "';");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
               Review review = makeReview(resultSet);

                return review;
            }

                return null;
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
                    " WHERE Player = '" + playerName + "' AND (Status = '" + Status.OPEN.name() +
                    "' OR Status = '" + Status.CLAIMED.name() +
                    "') ORDER BY `Date_Created` ASC;");

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
    public List getOpenTicketsByTeam(String team)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Ticket> ticketList = new ArrayList<>();

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME + " WHERE Assigned_Team = '" +
                    team + "' AND (Status = '" + Status.OPEN.name() + "' OR Status = '" + Status.CLAIMED.name() +
                    "') ORDER BY `Date_Created` ASC;");

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
                    " WHERE Status = '" + Status.OPEN.name() + "' OR Status = '" + Status.CLAIMED.name() +
                    "' ORDER BY `Date_Created` ASC;");

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
    public List<Ticket> getCompletedPlayerTickets(String playerUUID)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Ticket> ticketList = new ArrayList<Ticket>();
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + TICKET_TABLE_NAME +
                    " WHERE Status = '" + Status.CLOSED.name() +
                    "' AND player_id = '" + playerUUID +
                    "' ORDER BY `Date_Created` ASC;");

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
            "' AND Assigned_Moderator = '" + player +
                    "' ORDER BY `Date_Created` ASC;");

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
    public List<Ticket> getTeamClaimedTickets(String team)
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
                    "' AND Assigned_Team = '" + team +
                    "' ORDER BY `Date_Created` ASC;");

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
    public List<Ticket> getAllClaimedTickets()
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
                    "' ORDER BY `Date_Created` ASC;");

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
                    "', Assigned_Moderator = '" + "None" +
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
    public List<Comment> getAllComments(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Comment> commentList = new ArrayList<Comment>();
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + COMMENT_TABLE_NAME +
                    " WHERE ticket_id = '" + uuid +
                    "' ORDER BY `date_created` ASC;");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Comment comment = makeComment(resultSet);

                commentList.add(comment);

            }

            return commentList;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return commentList;
    }

    @Override
    public void closeTicket(String ticketUUID)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateFormatted = dateFormat.format(date);
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + TICKET_TABLE_NAME +
                    " Set Status = '" + Status.CLOSED.name() +
                    "', Date_Cleared = '" + dateFormatted +
                    "' WHERE id = '" + ticketUUID +
                    "';");

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
    public void staffOnDuty(String playerUUID, boolean onDuty)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("REPLACE INTO " + DUTY_TABLE_NAME +
                    " (id,duty_status) VALUES(?,?)");

            preparedStatement.setString(1, playerUUID);

            if(onDuty)
            {
                preparedStatement.setInt(2, 1);
            }
            else
            {
                preparedStatement.setInt(2, 0);
            }


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
    public List<Staff> getStaff()
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Staff> staffList = new ArrayList<>();
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + DUTY_TABLE_NAME +
                    ";");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Staff staff = makeStaff(resultSet);

                staffList.add(staff);

            }

            return staffList;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return staffList;
    }

    @Override
    public Staff getStaff(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + DUTY_TABLE_NAME +
                    " WHERE id ='" + uuid +
                    "';");

            resultSet = preparedStatement.executeQuery();

            Staff staff;

            while(resultSet.next())
            {
                staff = makeStaff(resultSet);
                return staff;
            }

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
    public void updateStaffOnDuty(String playerUUID, boolean onDuty)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int onDutyAsInt = 0;

        if(onDuty)
        {
            onDutyAsInt++;
        }

        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("UPDATE " + DUTY_TABLE_NAME +
                    " Set duty_status = '" + onDutyAsInt +
                    "' WHERE id = '" + playerUUID + "';");

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

    public void removeStaffFromOnDuty(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM " + DUTY_TABLE_NAME +
                    " WHERE id = '" + uuid + "';");

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
    public List<Comment> getCommentsForPlayer(String uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Comment> commentList = new ArrayList<Comment>();
        try
        {
            connection = getSqlConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + COMMENT_TABLE_NAME +
                    " WHERE ticket_id = '" + uuid +
                    "' AND staff_only = 'false'" +
                    " ORDER BY `date_created` ASC;");

            resultSet = preparedStatement.executeQuery();

            while(resultSet.next())
            {
                Comment comment = makeComment(resultSet);

                commentList.add(comment);

            }

            return commentList;
        }
        catch(SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        finally
        {
            close(preparedStatement, resultSet, connection);
        }
        return commentList;
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
        String playerID = resultSet.getString("player_id");

        return new Ticket(this.plugin, id, playerName, playerID, status, team, assignedModerator, dateCreated, dateCleared,
                location, info);
    }

    private Comment makeComment(ResultSet resultSet) throws SQLException
    {
        String id = resultSet.getString("id");
        String ticketId = resultSet.getString("ticket_id");
        String author = resultSet.getString("author");
        String comment = resultSet.getString("comment");
        String dateCreated = resultSet.getString("date_created");
        String staff_only = resultSet.getString("staff_only");
        Boolean isStaffOnly;
        if(staff_only.equalsIgnoreCase("true"))
        {
            isStaffOnly = true;
        }
        else
        {
            isStaffOnly = false;
        }

        return new Comment(this.plugin, id, ticketId, author, comment, dateCreated, isStaffOnly);
    }


    private Review makeReview(ResultSet resultSet) throws SQLException
    {
        String id = resultSet.getString("id");
        int rating = resultSet.getInt("rating");

        return new Review(this.plugin, id, rating);
    }

    private Staff makeStaff(ResultSet resultSet) throws SQLException
    {
        String id = resultSet.getString("id");
        boolean onDuty;
        if(resultSet.getInt("duty_status") == 0)
        {
            onDuty = false;
        }
        else
        {
            onDuty = true;
        }

        return new Staff(id, onDuty);
    }

}