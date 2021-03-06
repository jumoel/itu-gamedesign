package game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class Settings
{
	private LinkedHashMap<String, Object> values;
	
	public Settings()
	{
		Yaml yaml = new Yaml();
		InputStream is = null;
		try
		{
			is = new FileInputStream("Settings.yaml");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (is != null) {
			values = (LinkedHashMap<String, Object>) yaml.load(is);
		}
	}
	
	public <T> T getValue(String name)
	{
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
