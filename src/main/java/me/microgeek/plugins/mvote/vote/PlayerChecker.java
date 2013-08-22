package me.microgeek.plugins.mvote.vote;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.vexsoftware.votifier.model.Vote;

import me.microgeek.plugins.mvote.Wrapper;
import me.microgeek.plugins.mvote.util.Util;
import me.microgeek.plugins.mvote.util.config.ConfigWrapper;

public class PlayerChecker {

	public void startLoop() {
		final boolean handleCountup = ConfigWrapper.VOTE_SCRIPT.getConfig().getBoolean("onexpire.countup");
		final List<String> onExpireCommands = ConfigWrapper.VOTE_SCRIPT.getConfig().getStringList("onexpire.commands");
		
		if (handleCountup) {
			Bukkit.getScheduler().runTaskTimerAsynchronously(Wrapper.instance, new Runnable() {
				@Override
				public void run() {
					for (String playername : ConfigWrapper.PLAYER_DATA.getConfig().getKeys(false)) {
						if (Bukkit.getPlayerExact(playername) != null
								&& ConfigWrapper.PLAYER_DATA.getConfig().getLong(playername) != 0) {
							long until = ConfigWrapper.PLAYER_DATA.getConfig().getLong(playername);
							long time = System.currentTimeMillis();
							Player player = Bukkit.getPlayerExact(playername);
							Vote vote  = new Vote();
							vote.setUsername(playername);
							final String onExpireMessage = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onexpire.message.player"), vote);
							final String onExpireBroadcast = Util.replaceString(ConfigWrapper.VOTE_SCRIPT.getConfig().getString("onexpire.message.server"), vote);

							if (time >= until) {
								if(onExpireBroadcast != "") {
									Bukkit.broadcastMessage(onExpireBroadcast);
								}
								if(onExpireMessage != "") {
									player.sendMessage(onExpireMessage);
								}
								for(String s : onExpireCommands) {
									s = Util.replaceString(s, vote);
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
								}

								ConfigWrapper.PLAYER_DATA.getConfig().set(playername, null);
								ConfigWrapper.PLAYER_DATA.saveConfig();
							}
						}
					}
				}
			}, 40, 20);
		}
	}
}
