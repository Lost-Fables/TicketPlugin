package io.github.plizga.ticketplugin;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class TicketPlugin extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Ayyy lmao, TicketPlugin V1 Enabled!");

    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
