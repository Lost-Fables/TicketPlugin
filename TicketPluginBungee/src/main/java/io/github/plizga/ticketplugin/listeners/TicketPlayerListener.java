package io.github.plizga.ticketplugin.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.github.plizga.ticketplugin.TicketPluginBungee;
import io.github.plizga.ticketplugin.enums.Status;
import io.github.plizga.ticketplugin.enums.Team;
import io.github.plizga.ticketplugin.helpers.Staff;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class TicketPlayerListener implements Listener
{

    private TicketPluginBungee plugin;

    public TicketPlayerListener(Plugin plugin)
    {
        this.plugin = (TicketPluginBungee) plugin;
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

    @EventHandler
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase(plugin.CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();

        if (subChannel.equalsIgnoreCase(plugin.CREATE_SUB_CHANNEL)) {
            UUID uuid = UUID.fromString(in.readUTF());
            Team team = Team.getByName(in.readUTF());
            String message = in.readUTF();
            String location = in.readUTF();

            ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
            plugin.getDatabase().createNewTicket(player, location, Status.OPEN, team, message);

            player.sendMessage(new TextComponent(plugin.PREFIX + "Your ticket, with the description: " + plugin.ALT_COLOR +
                                                 message + plugin.PREFIX + " has been created!"));
            plugin.notifyOnDutyStaff(team);
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
