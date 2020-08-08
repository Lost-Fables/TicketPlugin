package io.github.plizga.ticketplugin.database;

/**
 * Class containing different types of errors to return. Not all of these will necessarily be used.
 * @author <a href="brad.plizga@mail.rit.edu">Plizga</a>
 */
public class Errors {
    public static String sqlConnectionExecute(){
        return "Couldn't execute MySQL statement: ";
    }
    public static String sqlConnectionClose(){
        return "Failed to close MySQL connection: ";
    }
    public static String noSQLConnection(){
        return "Unable to retreive MYSQL connection: ";
    }
    public static String noTableFound(){
        return "Database Error: No Table Found";
    }
}