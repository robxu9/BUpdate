package com.xu_robert.BUpdate;

import java.io.File;
import java.util.List;

import org.bukkit.util.config.Configuration;
import com.xu_robert.BUpdate.ThreadHelper;

@SuppressWarnings("deprecation")
public class BUpdateYAML {
	private final ThreadHelper th = new ThreadHelper();
	private String name;
	public String directory = th.yamlFolder.toString();
	private File file;
	public BUpdateYAML(String aName) {
		name = aName;
		file = new File(directory + File.separator + name + ".yml");
	}


	public void write(String root, Object x){
		Configuration config = load();
		config.setProperty(root, x);
		config.save();
	}
	public Boolean readBoolean(String root){
		Configuration config = load();
		return config.getBoolean(root, true);
	}

	public Double readDouble(String root){
		Configuration config = load();
		return config.getDouble(root, 0);
	}
	public List<String> readStringList(String root){
		Configuration config = load();
		return config.getKeys(root);
	}
	public String readString(String root){
		Configuration config = load();
		return config.getString(root);
	}
	private Configuration load(){

		try {
			Configuration config = new Configuration(file);
			config.load();
			return config;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}