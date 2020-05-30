package io.github.plizga.ticketplugin.listeners;

import io.github.plizga.ticketplugin.TicketPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TicketPlayerListener implements Listener
{

    private TicketPlugin plugin;

    public TicketPlayerListener(JavaPlugin plugin)
    {
        this.plugin = (TicketPlugin) plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if(plugin.getStaffOnDuty().contains(uuid))
            plugin.getStaffOnDuty().remove(uuid);

    }
}
