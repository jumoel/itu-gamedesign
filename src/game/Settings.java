package game;

import java.io.InputStream;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.Yaml;

public class Settings
{
	private LinkedHashMap<String, Object> values;
	
	public Settings()
	{
		Yaml yaml = new Yaml();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("Settings.yaml");
		
		values = (LinkedHashMap<String, Object>) yaml.load(is);
	}
	
	public <T> T getValue(String name)
	{
		Object val;
		LinkedHashMap<String, Object> inner = values;
		
		String[] tokens = name.split("/");
		int index = 0;
		
		while (inner.containsKey(tokens[index]))
		{
			if (index == tokens.length - 1)
			{
				return (T) inner.get(tokens[index]);
			}
			else
			{
				inner = (LinkedHashMap<String, Object>) inner.get(tokens[index]);
				index++;
			}
		}
		
		System.err.println("Setting '" + name + "' not found.");
		return null;
	}
}
