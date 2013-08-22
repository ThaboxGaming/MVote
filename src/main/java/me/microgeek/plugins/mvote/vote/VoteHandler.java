package me.microgeek.plugins.mvote.vote;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.List;
import me.microgeek.plugins.mvote.Wrapper;
import me.microgeek.plugins.mvote.util.Util;
import me.microgeek.plugins.mvote.util.config.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteHandler implements Listener{

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVote(VotifierEvent event) {
		final Vote vote = event.getVote();

		if(vote == null) {
			return;
		}

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

		final Player player = Bukkit.getPlayer(vote.getUsername());

		//Handle mis-types		
		if(player == null || !player.getName().equalsIgnoreCase(vote.getUsername().replace(" ", "_"))) {
			if(onVoteError != "") {
				Bukkit.broadcastMessage(onVoteError);
			}
			return;
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
							player.sendMessage(onExpireMessage);
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
	}


}
