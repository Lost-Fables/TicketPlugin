package io.github.plizga.ticketplugin.sqlite;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

public class SQLite extends Database
{
    String databaseName;

    public SQLite(JavaPlugin plugin)
    {
        super(plugin);
        databaseName = plugin.getConfig().getString("SQLite.Filename", TABLE_NAME);
    }
    @Override
    protected Connection getSqlConnection()
    {
        return null;
    }

    @Override
    public void load()
    {

    }
}
