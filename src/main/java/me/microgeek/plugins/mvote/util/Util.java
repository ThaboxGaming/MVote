package me.microgeek.plugins.mvote.util;

import org.bukkit.ChatColor;

import com.vexsoftware.votifier.model.Vote;

public class Util {

	public static String replaceString(String string, Vote vote) {
		String tmp = string;
		if(vote.getUsername() != null) {
			tmp = tmp.replace("%player%", vote.getUsername());
		}
		if(vote.getServiceName() != null) {
			tmp = tmp.replace("%service%", vote.getServiceName());
		}
		tmp = ChatColor.translateAlternateColorCodes('&', tmp);
		return tmp;
	}

}
