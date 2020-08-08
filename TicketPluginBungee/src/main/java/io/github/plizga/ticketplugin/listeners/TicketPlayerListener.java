package io.github.plizga.ticketplugin.listeners;

import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.helpers.Staff;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class TicketPlayerListener implements Listener
{

    private TicketPlugin plugin;

    public TicketPlayerListener(Plugin plugin)
    {
        this.plugin = (TicketPlugin) plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        Staff staff = plugin.getStaffOnDuty(uuid);

        if(staff != null && !staff.isPersistent())
        {
            plugin.getDatabase().removeStaffFromOnDuty(uuid);
        }

    }
/*
    @EventHandler
    public void onDroppedItem(PlayerDropItemEvent event)
    {

        {
            ItemStack itemStack = event.getItemDrop().getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(itemMeta instanceof BookMeta)
            {
                if(((BookMeta) itemMeta).getAuthor() != null &&((BookMeta) itemMeta).getAuthor().equals(TicketPlugin.PERMISSION_START))
                {
                    event.setCancelled(true);
                    itemStack.setAmount(0);
                }
            }
        }

    }

    @EventHandler
    public void onInteractedItem(InventoryClickEvent event)
    {
        if(event.getCurrentItem() != null)
        {
            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(itemMeta instanceof BookMeta)
            {
                if(((BookMeta) itemMeta).getAuthor() != null &&((BookMeta) itemMeta).getAuthor().equals(TicketPlugin.PERMISSION_START))
                {
                    event.setCancelled(true);
                    itemStack.setAmount(0);
                }
            }
        }




    }*/
}
