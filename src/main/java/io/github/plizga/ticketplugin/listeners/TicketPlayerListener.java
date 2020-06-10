package io.github.plizga.ticketplugin.listeners;

import io.github.plizga.ticketplugin.TicketPlugin;
import io.github.plizga.ticketplugin.helpers.Staff;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
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

        Staff staff = plugin.getStaffOnDuty(uuid);

        if(staff != null && !staff.isPersistent())
        {
            plugin.getDatabase().removeStaffFromOnDuty(uuid);
        }

    }

    @EventHandler
    public void onDroppedItem(PlayerDropItemEvent event)
    {

        {
            ItemStack itemStack = event.getItemDrop().getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(itemMeta instanceof BookMeta)
            {
                if(((BookMeta) itemMeta).getAuthor().equals(TicketPlugin.PERMISSION_START))
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
                if(((BookMeta) itemMeta).getAuthor().equals(TicketPlugin.PERMISSION_START))
                {
                    itemStack.setAmount(0);
                }
            }
        }




    }
}
