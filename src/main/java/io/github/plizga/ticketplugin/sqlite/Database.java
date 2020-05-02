package io.github.plizga.ticketplugin.sqlite;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Database
{
    private JavaPlugin plugin;

    private Connection connection;

    protected final String TABLE_NAME = "ticket_table";
    private int tokens = 0;

    public Database(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    protected abstract Connection getSqlConnection();

    public abstract void load();

    public void initialize()
    {
        connection = getSqlConnection();

        try
        {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME +
                    " WHERE ticket = ?");
            ResultSet resultSet = preparedStatement.executeQuery();

            close(preparedStatement, resultSet);
        }
        catch(SQLException e)
        {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "TicketPlugin unable to establish connection to the database... that fuckin sucks.");
        }
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
            //todo change later
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Unable to close preparedstatement and resultset!");
        }
    }

    //Getters coming up!
}
