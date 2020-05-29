package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class OfflineStorage
{
    private TicketPlugin plugin;
    public OfflineStorage(JavaPlugin plugin)
    {
        this.plugin = (TicketPlugin) plugin;
    }
}
