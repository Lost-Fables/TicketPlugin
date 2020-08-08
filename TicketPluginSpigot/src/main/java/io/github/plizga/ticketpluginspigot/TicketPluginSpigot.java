package io.github.plizga.ticketpluginspigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public final class TicketPluginSpigot extends JavaPlugin implements PluginMessageListener {

	/** The Bungee-Bukkit channels we use to communicate. */
	public final String CHANNEL = "lf:tickets";
	public final String TP_SUB_CHANNEL = "TicketsTeleport";

	@Override
	public void onEnable() {
		checkIfBungee();
		if (!getServer().getPluginManager().isPluginEnabled(this)) {
			return;
		}

		// Register our Bungee in/out channels.
		getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
		if (!channel.equalsIgnoreCase(CHANNEL)) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase(TP_SUB_CHANNEL)) {
			Location location = parseLocation(in.readUTF());
			UUID uuid = UUID.fromString(in.readUTF());

			if (location != null) {
				new BukkitRunnable() {
					@Override
					public void run() {
						Player player = Bukkit.getPlayer(uuid);
						if (player != null) {
							player.teleport(location);
							this.cancel();
						}
					}
				}.runTaskTimer(this, 1, 1);
			} else {
				throw new NullPointerException("Passed null location from teleport request.");
			}
		}
	}

	private Location parseLocation (String data) {
		int i = 0;
		String[] locationArray = data.split(",");

		World world = null;
		if (locationArray.length > i) {
			world = Bukkit.getWorld(locationArray[i++]);
		}

		int x = 0;
		if (locationArray.length > i) {
			x = (int) Double.parseDouble(locationArray[i++]);
		}

		int y = 0;
		if (locationArray.length > i) {
			y = (int) Double.parseDouble(locationArray[i++]);
		}

		int z = 0;
		if (locationArray.length > i) {
			z = (int) Double.parseDouble(locationArray[i]);
		}

		if (world != null) {
			return new Location(world, x, y, z);
		} else {
			return null;
		}
	}

	// we check like that if the specified server is BungeeCord.
	private void checkIfBungee() {
		// we check if the server is Spigot/Paper (because of the spigot.yml file)
		if (!getServer().getVersion().contains("Spigot") && !getServer().getVersion().contains("Paper")) {
			getLogger().severe("You probably run CraftBukkit... Please update atleast to spigot for this to work...");
			getLogger().severe("Plugin disabled!");
			getServer().getPluginManager().disablePlugin( this );
			return;
		}
		if (getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord")) {
			getLogger().severe("This server is not BungeeCord.");
			getLogger().severe("If the server is already hooked to BungeeCord, please enable it into your spigot.yml aswell.");
			getLogger().severe("Plugin disabled!");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
}
