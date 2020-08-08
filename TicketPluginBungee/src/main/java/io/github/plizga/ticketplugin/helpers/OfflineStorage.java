package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPlugin;
import net.md_5.bungee.api.plugin.Plugin;

public class OfflineStorage
{
    private TicketPlugin plugin;
    public OfflineStorage(Plugin plugin)
    {
        this.plugin = (TicketPlugin) plugin;
    }
}
