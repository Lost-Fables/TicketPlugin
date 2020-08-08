package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPluginBungee;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * semi-stub class for Reviews. Reviews aren't currently accessed, so this is kinda just here.
 */
public class Review
{
    private String id;
    private int rating;

    private TicketPluginBungee plugin;

    public Review(Plugin plugin, String id, int rating)
    {
        this.plugin = (TicketPluginBungee) plugin;
        this.id = id;
        this.rating = rating;
    }

    public String getId()
    {
        return id;
    }



    public int getRating()
    {
        return rating;
    }
}
