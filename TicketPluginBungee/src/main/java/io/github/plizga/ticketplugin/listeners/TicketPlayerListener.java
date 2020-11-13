package io.github.plizga.ticketplugin.listeners;

import co.lotc.core.bungee.util.ChatBuilder;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketPlayerListener implements Listener
{

    private TicketPluginBungee plugin;
    public static ArrayList<UUID> waiting = new ArrayList<>();

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
            UUID ticketUUID = UUID.fromString(in.readUTF());
            if (waiting.contains(ticketUUID)) {
                try {
                    waiting.remove(ticketUUID);
                } catch (Exception ignore) {
                    // If this throws, the ticket was already removed and ArrayList tried to use fastRemove();
                }

                UUID playerUUID = UUID.fromString(in.readUTF());
                Team team = Team.getByName(in.readUTF());
                String message = in.readUTF();
                String location = in.readUTF();
                ProxiedPlayer player = plugin.getProxy().getPlayer(playerUUID);


                plugin.getDatabase().createNewTicket(ticketUUID, player, location, Status.OPEN, team, message);
                TextComponent componentMessage = ChatBuilder.appendTextComponent(null, "Your ticket, with the description: ", plugin.PREFIX);
                ChatBuilder.appendTextComponent(componentMessage, message, plugin.ALT_COLOR);
                ChatBuilder.appendTextComponent(componentMessage, " has been created!", plugin.PREFIX);
                player.sendMessage(componentMessage);
                plugin.notifyOnDutyStaff(team);
            }
        }
    }
}
