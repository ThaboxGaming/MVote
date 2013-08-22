package me.microgeek.plugins.mvote;

import java.util.List;
import java.util.Map.Entry;

import me.microgeek.plugins.mvote.util.Util;
import me.microgeek.plugins.mvote.util.config.ConfigWrapper;
import me.microgeek.plugins.mvote.vote.VoteHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.vexsoftware.votifier.model.Vote;

public class MVote extends JavaPlugin{

	@Override
	public void onEnable() {
		Wrapper.instance = this;
		Bukkit.getPluginManager().registerEvents(new VoteHandler(), this);
		ConfigWrapper.VOTE_SCRIPT.setSave(false);
		ConfigWrapper.PLAYER_DATA.setSave(false);
		Wrapper.pc.startLoop();
	}

	@Override
	public void onDisable() {
		for(Entry<Integer, List<String>> e : Wrapper.EXPIRY_QUEUE.entrySet()) {
			for(String s : e.getValue()) {
				Bukkit.dispatchCommand(Wrapper.instance.getServer().getConsoleSender(), s);
			}
		}
	}

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		final Vote vote = new Vote();

		if (cmd.getName().equalsIgnoreCase("mvote")) {
			if (args.length == 0) {
				if (player != null) {
					List<String> lines = ConfigWrapper.VOTE_SCRIPT.getConfig().getStringList("votecmd.message");
					for (String line : lines) {
						player.sendMessage(Util.replaceString(line, vote));
					}
				} else {
					List<String> lines = ConfigWrapper.VOTE_SCRIPT.getConfig().getStringList("votecmd.message");
					for (String line : lines) {
						Bukkit.getConsoleSender().sendMessage(Util.replaceString(line, vote));
					}
				}
			} else if (args.length <= 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					ConfigWrapper.VOTE_SCRIPT.reloadConfig();
					ConfigWrapper.PLAYER_DATA.reloadConfig();
					if (player == null) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Reloaded config of MVote.");
					} else {
						if (player.hasPermission("mvote.reload")) {
							player.sendMessage(ChatColor.GREEN + "Reloaded config of MVote.");
						} else {
							player.sendMessage(ChatColor.RED + "You don't have the permissions to use this command.");
						}
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("fakevote")) {
			if (player != null) {
				if (player.hasPermission("mvote.fakevote")) {
					vote.setServiceName("TestVote (FakeVote)");
					vote.setUsername(player.getName());
					vote.setTimeStamp(String.valueOf(System.currentTimeMillis()));

					final String onVoteMessage = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onvote.message.player"), vote);
					final String onVoteBroadcast = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onvote.message.server"), vote);
					final String onVoteError = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onvote.message.error"), vote);
					final String onExpireMessage = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onexpire.message.player"), vote);
					final String onExpireBroadcast = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onexpire.message.server"), vote);

					final boolean handleVote = ConfigWrapper.VOTE_SCRIPT.getConfig().getBoolean("onvote.enabled");
					final boolean handleExpire = ConfigWrapper.VOTE_SCRIPT.getConfig().getBoolean("onexpire.enabled");
					final boolean handleCountup = ConfigWrapper.VOTE_SCRIPT.getConfig().getBoolean("onexpire.countup");

					final List<String> onVoteCommands = ConfigWrapper.VOTE_SCRIPT.getConfig().getStringList("onvote.commands");
					final List<String> onExpireCommands = ConfigWrapper.VOTE_SCRIPT.getConfig().getStringList("onexpire.commands");

					final int expireDelay = ConfigWrapper.VOTE_SCRIPT.getConfig().getInt("onexpire.cooldown");

					final Player player2 = Bukkit.getPlayerExact(vote.getUsername());

					if(player == null || !player.getName().equalsIgnoreCase(vote.getUsername().replace(" ", "_"))) {
						if(onVoteError != "") {
							Bukkit.broadcastMessage(onVoteError);
						}
						return true;
					}

					if(handleVote) {
						if(onVoteBroadcast != "") {
							Bukkit.broadcastMessage(onVoteBroadcast);
						}
						if(onVoteMessage != "") {
							player.sendMessage(onVoteMessage);
						}
						for(String s : onVoteCommands) {
							s = Util.replaceString(s, vote);
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
						}
					}
					if(handleExpire) {
						final int id = (int) System.currentTimeMillis();
						if (!handleCountup) {
							Wrapper.EXPIRY_QUEUE.put(id, onExpireCommands);
							new BukkitRunnable() {
								@Override
								public void run() {
									if(onExpireBroadcast != "") {
										Bukkit.broadcastMessage(onExpireBroadcast);
									}
									if(onExpireMessage != "") {
										player2.sendMessage(onExpireMessage);
									}
									for(String s : onExpireCommands) {
										s = Util.replaceString(s, vote);
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
									}
									Wrapper.EXPIRY_QUEUE.remove(id);
								}
							}.runTaskLater(Wrapper.instance, ((expireDelay * 60) * 20));
						} else {
							long time = System.currentTimeMillis();
							long nexttime = time;
							if (ConfigWrapper.PLAYER_DATA.getConfig().getLong(player.getName()) == 0) {
								nexttime = nexttime + ((expireDelay * 60) * 1000);
								ConfigWrapper.PLAYER_DATA.getConfig().set(player.getName(), nexttime);
							} else {
								nexttime = ConfigWrapper.PLAYER_DATA.getConfig().getLong(player.getName());
								nexttime = nexttime + ((expireDelay * 60) * 1000);
								ConfigWrapper.PLAYER_DATA.getConfig().set(player.getName(), nexttime);
							}
							ConfigWrapper.PLAYER_DATA.saveConfig();
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have the permissions to use this command.");
				}
			} else {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command is an only-ingame command.");
			}
		}
		return true;
	}

}
