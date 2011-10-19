package com.xu_robert.BUpdate.Async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.xu_robert.BUpdate.BUpdateYAML;
import com.xu_robert.BUpdate.ThreadHelper;

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

public class Downloader extends Thread{
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private Plugin plugin;
	
	public Downloader(Player player, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
	}
	
	public void run() {
		try {
			if (update(plugin)) {
				th.sendTo(player, "GREEN", "The plugin "+plugin.getDescription().getName()+" was successfully updated :)");
			} else {
				th.sendTo(player, "RED", "The plugin "+plugin.getDescription().getName()+" update failed!");
			}
		} catch (IllegalStateException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		} catch (MalformedURLException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		} catch (ProtocolException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		} catch (IOException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		}
	}
	
	public boolean update(Plugin aPlugin) throws IllegalStateException, MalformedURLException, ProtocolException, IOException {
		OutputStream os;
		BUpdateYAML pluginProps = new BUpdateYAML(aPlugin.getDescription().getMain());
		String dtFile = th.cwd+"/plugins/"+aPlugin.getDescription().getName()+".jar";
		//String url = th.sendData(pluginName+"::url");
		String url = pluginProps.readString("download");
		
		// this used to be for checking for latest updates
		// it would return false if no update was available or no download link was there
		/*
		if (url.equalsIgnoreCase("false")) {
			return false;
		}
		*/
		
		backup(dtFile, th.cwd+"/plugins/BUpdate/backup/"+aPlugin.getDescription().getName()+".jar");
		os = new FileOutputStream(dtFile);
		downloadFile(url, os);
		return true;
	}
		
	public static void downloadFile(String url_str, OutputStream os)
	throws IllegalStateException, MalformedURLException,
	ProtocolException, IOException {
		URL url = new URL(url_str.replace(" ", "%20"));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
        	byte tmp_buffer[] = new byte[4096];
            InputStream is = conn.getInputStream();
            int n;
            while ((n = is.read(tmp_buffer)) > 0) {
            	os.write(tmp_buffer, 0, n);
            	os.flush();
            }
        } else {
        	throw new IllegalStateException("HTTP response: " + responseCode);
        }
	}
	
	public static void backup(String srFile, String dtFile) throws IOException{
		InputStream in;
		in = new FileInputStream(new File(srFile));
		OutputStream out = new FileOutputStream(new File(dtFile));
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}
