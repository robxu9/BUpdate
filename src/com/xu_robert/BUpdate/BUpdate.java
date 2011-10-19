package com.xu_robert.BUpdate;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.xu_robert.BUpdate.Async.*;

/**
* BUpdate version 0.5
* Robert Xu <xu_robert@linux.com>
* 
* Based on BukkitUpdater by:
* Lukas 'zauberstuhl y33' Matt <lukas@zauberstuhl.de>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Permissions Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Permissions Public License for more details.
*
* You should have received a copy of the GNU Permissions Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/**
* This plugin was written for Craftbukkit
* @author zauberstuhl
*/

public class BUpdate extends JavaPlugin {
	private final ThreadHelper th = new ThreadHelper();
	private final BUpdatePlayerListener playerListener = new BUpdatePlayerListener(this);
	
	public static PermissionHandler permissionHandler = null;
	public static PermissionManager permissionExHandler = null;
	
	private String[] randomLoading = {"Loading package lists...","Grabbing from Github...","Refreshing...",
			"Watching Lucky Star to pass the time...","Being lazy...","Sweeping the floor...",
			"Thinking of something naughty...","Flirting with the CPU...","NYAN CATTTTing...",
			"Why hello there person! I'm working...","Software Updates are loading...",
			"That'ssss a niceeee houseeee you got thereeee... Let me calculate how to blow it uppp...",
			"Watching some questionable stuff...","Being an ero...","Annoying the server admin...",
			"Making up more random quotes for this space..."};
	Random randGen = new Random();
	
	@Override
	public void onDisable() {
		try {
		th.console.sendMessage(ChatColor.RED+"[BUpdate] version " + this.getDescription().getVersion() + " disabled.");
		} catch (Exception e){
			System.err.println("[BUpdate] Disabled, but couldn't show disabled message normally.");
		}
	}

	@Override
	public void onEnable() {		
		PluginManager pm = this.getServer().getPluginManager();	
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		setupBUpdate();
		th.console.sendMessage(ChatColor.GREEN+"[BUpdate] version " + this.getDescription().getVersion() + " enabled.");
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		Player player = null;
		String commandName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		if (commandName.equalsIgnoreCase("u2d")) {			
			if (args.length == 0) {
				if (!perm(player, "usage", true))
					return false;
				th.sendTo(player, "WHITE", "");
				th.sendTo(player, "WHITE", "BUpdate version "+this.getDescription().getVersion());
				th.sendTo(player, "WHITE", "by xu_robert. Original by zauberstuhl.");
				th.sendTo(player, "RED", randomLoading[randGen.nextInt(randomLoading.length)]);
				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
						new Overview(player,
								this.getServer().getPluginManager().getPlugins(),
								"u2d"));
				return true;
			} else {
				if ((args[0].equalsIgnoreCase("update")) && args.length > 1) {
					if (!perm(player, "update", true))
						return false;
					// if there are quotes, take them out.
					// needed especially if there is a space in the name.
					// knowing bukkit, spaces = another argument
					if (args[1].charAt(0)=='\"'){
						args[1]=args[1].substring(1);
						for (int i=2; i<args.length; i++){
							if (args[i].charAt(args.length-1)=='"'){
								args[1]+=" "+args[i].substring(0,args[i].length()-1);
								i=args.length;
							}
							else
								args[1]+=" "+args[i];
						}
					}
					// find a plugin with this name...
					Plugin[] plugins = this.getServer().getPluginManager().getPlugins();
					for (int i=0; i<plugins.length; i++){
						if (args[1].equals(plugins[i].getDescription().getName())){
							th.sendTo(player, "RED", "Updating plugin...");
							this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
									new Downloader(player, plugins[i]));
							return true;
						}
					}
					th.sendTo(player, "RED", "Could not find plugin by that name!");
					return false;
				}
				if (args[0].equalsIgnoreCase("update") && args.length == 1){
					if (!perm(player,"update",true))
						return false;
					// update all supported plugins
					th.sendTo(player, "GREEN", "No plugin specified, assuming all available upgrades:");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Overview(player,
									this.getServer().getPluginManager().getPlugins(),
									"updateall"));
					return true;
				}
				if (args[0].equalsIgnoreCase("unsupported")) {
					if (!perm(player, "usage", true))
						return false;
					th.sendTo(player, "RED", "Searching for unsupported plugins...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Overview(player,
									this.getServer().getPluginManager().getPlugins(),
									"unsupported"));
					return true;
				}
				if (args[0].equalsIgnoreCase("reload") && args.length > 1) {
					if (!perm(player, "reload", true))
						return false;
					PluginManager pm = this.getServer().getPluginManager();
					Plugin reloadPlugin = pm.getPlugin(args[1]);
					
					if (reloadPlugin != null) {
						this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
								new Reloader(player, pm, reloadPlugin));
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("ignore")) {
					if (!perm(player, "ignore", true)
							|| args[1].equalsIgnoreCase(""))
						return false;
					// if there are quotes, take them out.
					// needed especially if there is a space in the name.
					// knowing bukkit, spaces = another argument
					if (args[1].charAt(0)=='\"'){
						args[1]=args[1].substring(1);
						for (int i=2; i<args.length; i++){
							if (args[i].charAt(args.length-1)=='"'){
								args[1]+=" "+args[i].substring(0,args[i].length()-1);
								i=args.length;
							}
							else
								args[1]+=" "+args[i];
						}
					}
					th.sendTo(player, "RED", "Searching ignored plugins...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Blacklist(player, args[1]));
					return true;
				}
				if (args[0].equalsIgnoreCase("help")) {
					th.helper(player);
					return true;
				}
			}
		}			
		return false;
	}
	
	public void setupBUpdate(){
		String uuid = UUID.randomUUID().toString();
		
		if (!th.folder.exists())
			if (!th.folder.mkdir()) {
				th.console.sendMessage("[BUpdate][WARN] Creating main directory failed!");
				onDisable();
			}
		if (!th.backupFolder.exists())
			if (!th.backupFolder.mkdir()) {
				th.console.sendMessage("[BUpdate][WARN] Creating backup directory failed!");
				onDisable();
			}
		if (!th.yamlFolder.exists())
			if (!th.yamlFolder.mkdir()) {
				th.console.sendMessage("[BUpdate][WARN] Creating YAML files directory failed!");
				onDisable();
			}
		
		/*
		if (!th.token.exists()) {
			try {
				th.writeToFile(th.token, uuid);
				th.console.sendMessage("[BUpdate] Created token:");
				th.console.sendMessage("[BUpdate] "+uuid);
				String buffer = th.sendData(uuid);
				if (buffer.equals("success")) {
					th.console.sendMessage("[BUpdate] Send token success.");
				} else
					th.console.sendMessage("[BUpdate] Failed to send token.");
			} catch (IOException e) {
				th.console.sendMessage("[BUpdate][WARN] Was not able to create a new token");
			}
		}
		*/
		
		if (!th.blacklist.exists()) {
			String comment = "# Here you can write plugin names\n" +
					"# seperated by ',' (without the quotes)\n" +
					"# if they are not to be tested for their topicality.\n\n" +
					"# e.g.:\n" +
					"TestPluginName1,\n" +
					"TestPluginName2,\n";
			try {
				th.writeToFile(th.blacklist, comment);
				th.console.sendMessage("[BUpdate] Created new blacklist");
			} catch (IOException e) {
				th.console.sendMessage("[BUpdate][WARN] Was not able to create a new blacklist");
			} catch (NullPointerException e) {
				th.console.sendMessage("[BUpdate][WARN] Blacklist creation went slightly off track...");
			}
		}
		
		//setting up permissions
		Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
		Plugin permissionsEx = this.getServer().getPluginManager().getPlugin("PermissionsEx");
		Plugin permissionsBukkit = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
		
		// PermissionsBukkit
		if (permissionsBukkit != null) {
			th.console.sendMessage("[BUpdate] Found and will use plugin "+permissionsBukkit.getDescription().getFullName());
			return;
		}
		// Permissions (TheYeti)
		if (permissions != null) {
			String permissionsVersion = permissions.getDescription().getVersion().replaceAll("\\.","");
			// version > 2.5
			if (Integer.parseInt(permissionsVersion) >= 250) {
				permissionHandler = ((Permissions) permissions).getHandler();
				th.console.sendMessage("[BUpdate] Found and will use plugin "+((Permissions)permissions).getDescription().getFullName());
				return;
			}
		}
		// PermissionEx
		if (permissionsEx != null) {
			permissionExHandler = PermissionsEx.getPermissionManager();
			th.console.sendMessage("[BUpdate] Found and will use plugin "+permissionsEx.getDescription().getFullName());
			return;
		}

		th.console.sendMessage("[BUpdate][WARN] Permission system not detected, defaulting to Op");	    
	}
	
	public boolean perm(Player player, String perm, Boolean notify){
		if (player == null)
			return true;
								
	    // TheYeti permission
	    if (permissionHandler != null) {
	    	if (BUpdate.permissionHandler.has(player, "BUpdate."+perm))
	    		return true;
	    	else {
	    		if (notify) th.sendTo(player, "GRAY", "(You don't seem to have permissions...)");
	    		return false;
	    	}
	    }
	    // PermissionEx
	    if (permissionExHandler != null) {
	    	if (BUpdate.permissionExHandler.has(player, "BUpdate."+perm))
		   		return true;
	    	else {
	    		if (notify) th.sendTo(player, "GRAY", "(You don't seem to have permissions...)");
	    		return false;
	    	}
	    }
	    // SuperPermissions
	    return player.hasPermission("BUpdate."+perm);
	}
}
