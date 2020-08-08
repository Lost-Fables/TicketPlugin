package io.github.plizga.ticketplugin.helpers;

import io.github.plizga.ticketplugin.TicketPluginBungee;
import net.md_5.bungee.api.plugin.Plugin;

public class OfflineStorage
{
    private TicketPluginBungee plugin;
    public OfflineStorage(Plugin plugin)
    {
        this.plugin = (TicketPluginBungee) plugin;
    }
}
