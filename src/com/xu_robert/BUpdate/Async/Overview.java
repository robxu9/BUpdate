package com.xu_robert.BUpdate.Async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.xu_robert.BUpdate.ThreadHelper;
import com.xu_robert.BUpdate.Async.Downloader;
import com.xu_robert.BUpdate.BUpdateYAML;;

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

public class Overview extends Thread{
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private Plugin[] plugins;
	private String action;

	// supported = new supported updates
	public String supportedPlugins = "";
	public String unsupportedPlugins = "";
	public String mainsupportedPlugins = "";

	public Overview(Player player, Plugin[] plugins, String action) {
		this.player = player;
		this.plugins = plugins;
		this.action = action;
	}

	public void run() {
		String[] supported;
		String[] unsupported;
		String[] mainsupported;

		try {
			// start a lookup
			boolean u2d = u2d(player);

			if (action.equalsIgnoreCase("info")) {
				if (u2d) th.sendTo(player, "RED", "There are new updates available for your server. (/u2d)");
				else th.sendTo(player, "GREEN", "No new updates available for your plugins.");
			} else if (action.equalsIgnoreCase("unsupported")) {
				if (unsupportedPlugins.matches(".*;.*")) {
					unsupported = unsupportedPlugins.split(";");
					th.sendTo(player, "WHITE", "These plugins are not supported by BUpdate:");
					for(int i = 0; unsupported.length > i; i++)
						th.sendTo(player, "RED", unsupported[i]);
				} else
					th.sendTo(player, "GREEN", "You do not have any unsupported plugins.");
			} else if (action.equalsIgnoreCase("updateall")) {
				if (u2d){
					th.sendTo(player, "GREEN", "The following plugins will be upgraded:");
					supported = supportedPlugins.split(";");
					for(int i = 0; supported.length > i; i++)
						th.sendTo(player, "WHITE", supported[i]);
					mainsupported = mainsupportedPlugins.split(";");
					for(int i = 0; mainsupported.length > i; i++){
						for (int j=0; j<plugins.length; j++){
							if (mainsupported[i].equals(plugins[j].getDescription().getMain())){
								th.sendTo(player, "YELLOW", "Updating "+plugins[j].getDescription().getName());
								new Downloader(player, plugins[j]).run();
								j=plugins.length;
							}
						}
					}
					th.sendTo(player, "GREEN", "Completed.");
				}
				else th.sendTo(player, "GREEN", "No upgrades available. Nothing to do.");
			} else {
				if (u2d) {
					supported = supportedPlugins.split(";");
					th.sendTo(player, "GOLD", "New Updates are available for:");
					for(int i = 0; supported.length > i; i++)
						th.sendTo(player, "GREEN", supported[i]);
				} else
					th.sendTo(player, "GREEN", "Currently there are no new updates available.");
				if (unsupportedPlugins.matches(".*;.*")) {
					unsupported = unsupportedPlugins.split(";");
					if (unsupported.length>1)
						th.sendTo(player, "RED", "There are "+unsupported.length+" unsupported plugins. For more info: /u2d unsupported");
					else
						th.sendTo(player, "RED", "There is "+unsupported.length+" unsupported plugin. For more info: /u2d unsupported");
				}			
			}
		} catch (IOException e) {
			if (e.toString().matches(".*Connection\\stimed\\sout.*"))
				th.sendTo(player, "RED", "(The database is currently not available...)");
			else
				th.sendTo(player, "GRAY", "(Something went wrong. See the console for more output.)");
		}
	}

	public boolean u2d(Player player) throws IOException{
		String allVersions = "";
		String supported = "";
		String unsupported = "";
		String mainsupported = "";
		String buffer;
		OutputStream os;
		String url = "https://github.com/robxu9/BUpdate-Updates/raw/master/";

		/* used in the old system
		for(int i = 0; i < plugins.length; i++){
			String version = plugins[i].getDescription().getVersion();
			String name = plugins[i].getDescription().getName();
			buffer = name+"::"+version;
			allVersions += name+"::"+version+",";
			buffer = th.sendData(buffer);			

			if(!buffer.equals("false") &&
					!buffer.equals("unsupported") &&
					blacklist(name)) {

				supported += buffer+";";
			} else if (buffer.equals("unsupported"))
				unsupported += plugins[i]+";";
		}
		supportedPlugins = supported;
		unsupportedPlugins = unsupported;

		buffer = th.sendData(allVersions);
		if(!buffer.equals("false"))
			return true;
		return false;
		 */
		// one important difference is that doing an install will just do a force installation...

		for(int i = 0; i < plugins.length; i++){
			String version = plugins[i].getDescription().getVersion();
			String name = plugins[i].getDescription().getName();
			String main = plugins[i].getDescription().getMain();
			boolean keepGoing=true;
			//allVersions += name+"::"+version+",";
			//buffer = th.sendData(buffer);
			// download repository file to yamlfiles to check
			try {
				os = new FileOutputStream(th.yamlFolder + File.separator + main + ".yml");
				Downloader.downloadFile(url+main+".yml", os);
			}catch (IllegalStateException e){
				// didn't find it. that os is now useless. bye bye.
				File todelete = new File(th.yamlFolder+File.separator+main+".yml");
				todelete.delete();
				unsupported+=plugins[i]+";";
				keepGoing=false;
			}
			if (keepGoing){
				if (blacklist(name)){
					BUpdateYAML check = new BUpdateYAML(main);
					try {
						if (!check.readString("version").equals(version))
						{
							if (!check.readBoolean("supported") && check.readBoolean("maintained")){
								supported+=plugins[i].getDescription().getFullName()+" to version "+check.readString("version")+";";
								mainsupported+=plugins[i].getDescription().getMain()+";";
								unsupported+=plugins[i]+";";
							}
							else if (!check.readBoolean("maintained")){
								//automatically not supported
								unsupported+=plugins[i]+";";
							}
							else {
								supported+=plugins[i].getDescription().getFullName()+" to version "+check.readString("version")+";";
								mainsupported+=plugins[i].getDescription().getMain()+";";
							}
						}
					} catch (NullPointerException e){
						th.sendTo(player, "RED", "Seems that a plugin file is corrupt. You should notify the BUpdate admin.");
						e.printStackTrace();
						th.sendTo(player, "RED", "Marking "+name+" as unsupported for now.");
						unsupported+=plugins[i]+";";
					}
				}
			}
		}
		// make sure to pass it back up
		supportedPlugins = supported;
		unsupportedPlugins = unsupported;
		mainsupportedPlugins = mainsupported;
		// I don't know what allVersions does but I won't change this to a void until I know what it does.
		// for now, return true if there are updates for supported plugins, false if none
		if (supportedPlugins.length()!=0)
			return true;
		return false;
	}

	/**
	 * checks plugin if on blacklist
	 * @param plugin
	 * @return true if IT'S *NOT* on the blacklist o_o"
	 * @throws IOException
	 */
	public boolean blacklist(String plugin) throws IOException {
		// read the blacklist and separate
		String blacklist = th.readFile(th.blacklist);
		if (blacklist.indexOf(plugin) > 0)
			return false;
		return true;
	}
}
