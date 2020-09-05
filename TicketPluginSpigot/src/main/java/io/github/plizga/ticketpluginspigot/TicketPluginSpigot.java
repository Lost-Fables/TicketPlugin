package io.github.plizga.ticketpluginspigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TicketPluginSpigot extends JavaPlugin implements PluginMessageListener {

	/** Defines the start of permissions used in this plugin. */
	public static final String PERMISSION_START = "ticketplugin";
	/** Defines the first common color used in the plugin. */
	public final String PREFIX = ChatColor.GRAY + "";
	/** Defines the second common color used in the plugin. */
	public final String ALT_COLOR = ChatColor.BLUE + "";
	/** Defines the common error color used in the plugin. */
	public final String ERROR_COLOR = ChatColor.DARK_RED + "";
	/** The Bungee-Bukkit channels we use to communicate. */
	public final String CHANNEL = "lf:tickets";
	public final String TP_SUB_CHANNEL = "TicketsTeleport";
	public final String CREATE_SUB_CHANNEL = "TicketsCreate";
	public final String COMMENT_SUB_CHANNEL = "TicketsComment";

	private List<UUID> processing = new ArrayList<>();

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
			pluginTPReceived(location, uuid);
		} else if (subChannel.equalsIgnoreCase(CREATE_SUB_CHANNEL)) {
			UUID ticketUUID = UUID.fromString(in.readUTF());
			if (!processing.contains(ticketUUID)) {
				processing.add(ticketUUID);
				UUID playerUUID = UUID.fromString(in.readUTF());
				String team = in.readUTF();
				String message = in.readUTF();
				returnWithLocation(ticketUUID, playerUUID, team, message);
			}
		} else if (subChannel.equalsIgnoreCase(COMMENT_SUB_CHANNEL)) {
			Player providedPlayer = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
			String commentUUID = in.readUTF();
			if (providedPlayer != null) {
				List<String> pages = new ArrayList<>();
				boolean endOfStream = false;
				while (!endOfStream) {
					try {
						pages.add(in.readUTF());
					} catch (IllegalStateException ise) {
						endOfStream = true;
					}
				}
				provideCommentBook(providedPlayer, commentUUID, pages);
			}
		}
	}

	private void provideCommentBook(Player player, String uuid, List<String> pages) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		if (bookMeta != null) {
			bookMeta.setPages(pages);
			bookMeta.setAuthor("Server");
			bookMeta.setTitle("Blank");

			book.setItemMeta(bookMeta);
			player.openBook(book);
		}
	}

	private void returnWithLocation(UUID ticketUUID, UUID playerUUID, String team, String message) {
		Player player = Bukkit.getPlayer(playerUUID);
		if (player != null && player.isOnline()) {
			Location loc = player.getLocation();
			if (loc.getWorld() != null) {
				String locationString = (loc.getWorld().getName() + "," +
										 loc.getBlockX() + "," +
										 loc.getBlockY() + "," +
										 loc.getBlockZ());

				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF(CREATE_SUB_CHANNEL);
				out.writeUTF(ticketUUID.toString());
				out.writeUTF(playerUUID.toString());
				out.writeUTF(team);
				out.writeUTF(message);
				out.writeUTF(locationString);
				this.getServer().sendPluginMessage(this, CHANNEL, out.toByteArray());
			}
		}
	}

	private void pluginTPReceived(Location location, UUID uuid) {
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
