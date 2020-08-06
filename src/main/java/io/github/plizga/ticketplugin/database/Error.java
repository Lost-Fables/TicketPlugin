package io.github.plizga.ticketplugin.database;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Level;

/**
 * This class provides errors which can be used when MySQL information is unable to execute or a database connection does
 * not close.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public class Error {
    public static void execute(Plugin plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(Plugin plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}