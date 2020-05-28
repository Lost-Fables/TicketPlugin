package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Comment implements Comparable
{
    private TicketPlugin plugin;

    private String id;
    private String ticketId;
    private String author;
    private String comment;
    private String dateCreated;
    private boolean isStaffOnly;

   public Comment(JavaPlugin plugin, String id, String ticketId, String author, String comment, String dateCreated, boolean isStaffOnly)
   {
       this.plugin = (TicketPlugin) plugin;
       this.id = id;
       this.ticketId = ticketId;
       this.author = author;
       this.comment = comment;
       this.dateCreated = dateCreated;
       this.isStaffOnly = isStaffOnly;
   }

   @Override
   public String toString()
   {
       return plugin.PREFIX + "Comment Author: " + plugin.ALT_COLOR + author
               + plugin.PREFIX +"\nComment: " + plugin.ALT_COLOR + comment
               + plugin.PREFIX + "\nDate Entered: " + plugin.ALT_COLOR + dateCreated
               + "\n";
   }

    //mutators and accessors

    public String getId()
    {
        return id;
    }

    public String getTicketId()
    {
        return ticketId;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getComment()
    {
        return comment;
    }

    public String getDateCreated()
    {
        return dateCreated;
    }

    public boolean isStaffOnly()
    {
        return isStaffOnly;
    }

    @Override
    public int compareTo(Object o)
    {
        if(o instanceof Comment)
        {
            String str = ((Comment) o).getDateCreated();
            return this.getDateCreated().compareTo(str);
        }
        return -1;
    }
}
