package virtualPool.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;


public class Configuration
{
	private String filename;
	private List<Property> properties;
	private Properties manager;
	private boolean needsRewrite;
	
	private abstract class Property implements Comparable<Property>
	{
		protected String key;
		protected String stringValue;
		protected String description;

		protected abstract void parse();
		
		public Property(String key, String description)
		{
			this.key = key;
			this.description = description;
			this.stringValue = null;
			properties.add(this);
		}

		public String getKey()
		{
			return key;
		}

		public String getDescription()
		{
			return description;
		}

		public String getStringValue()
		{
			return stringValue;
 		}
		
		public int compareTo(Property other)
		{
			return key.compareTo(other.key);
		}
	}
	
	public class IntegerProperty extends Property
	{
		private int defaultValue;
		private int minValue;
		private int maxValue;
		private int finalValue;
		
		public IntegerProperty(String key, int defaultValue, int minValue, int maxValue, String description)
		{
			super(key, description);
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.defaultValue = defaultValue;
			this.finalValue = defaultValue;
			parse();
		}
		
		public int getValue()
		{
			return finalValue;
		}
				
		public IntegerProperty(String key, int defaultValue, String description)
		{
			this(key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, description);
		}
		
		public IntegerProperty(String key, String description)
		{
			this(key, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, description);
		}
		
		@Override
		protected void parse()
		{
			this.stringValue = manager.getProperty(key);
			
			if (stringValue == null)
			{
				Logs.log.warning("Could not find config key: " + key + ". Replacing with default value: " + defaultValue + ".");
				this.finalValue = this.defaultValue;
				this.stringValue = String.valueOf(this.finalValue);
			}
			else
			{
				try
				{
					this.finalValue = Integer.parseInt(this.stringValue);
				}
				catch (Exception ex)
				{
					Logs.log.warning("Integer value expected for config key: " + key + ". Replacing with default value: " + defaultValue + ".");
					this.finalValue = this.defaultValue;
					this.stringValue = String.valueOf(this.finalValue);
					return;
				}
				
				if (this.finalValue > this.maxValue || this.finalValue < this.minValue)
				{
					Logs.log.warning("Value expected for config key: " + key + " in interval [" + this.minValue +
							" .. " + this.maxValue + "]. Replacing with default value: " + defaultValue + ".");
					this.finalValue = this.defaultValue;
					this.stringValue = String.valueOf(this.finalValue);
				}
			}
		}
	}
	
	public class DoubleProperty extends Property
	{
		private double defaultValue;
		private double minValue;
		private double maxValue;
		private double finalValue;
		
		public DoubleProperty(String key, double defaultValue, double minValue, double maxValue, String description)
		{
			super(key, description);
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.defaultValue = defaultValue;
			this.finalValue = defaultValue;
			parse();
		}
		
		public double getValue()
		{
			return finalValue;
		}
				
		public DoubleProperty(String key, double defaultValue, String description)
		{
			this(key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, description);
		}
		
		public DoubleProperty(String key, String description)
		{
			this(key, 0, Double.MIN_VALUE, Double.MAX_VALUE, description);
		}
		
		@Override
		protected void parse()
		{
			this.stringValue = manager.getProperty(key);
			
			if (stringValue == null)
			{
				Logs.log.warning("Could not find config key: " + key + ". Replacing with default value: " + defaultValue + ".");
				this.finalValue = this.defaultValue;
				this.stringValue = String.valueOf(this.finalValue);
			}
			else
			{
				try
				{
					this.finalValue = Double.parseDouble(this.stringValue);
				}
				catch (Exception ex)
				{
					Logs.log.warning("Floating value expected for config key: " + key + ". Replacing with default value: " + defaultValue + ".");
					this.finalValue = this.defaultValue;
					this.stringValue = String.valueOf(this.finalValue);
					return;
				}
				
				if (this.finalValue > this.maxValue || this.finalValue < this.minValue)
				{
					Logs.log.warning("Value expected for config key: " + key + " in interval [" + this.minValue +
							" .. " + this.maxValue + "]. Replacing with default value: " + defaultValue + ".");
					this.finalValue = this.defaultValue;
					this.stringValue = String.valueOf(this.finalValue);
				}
			}
		}
	}
	
	public class BooleanProperty extends Property
	{
		private boolean defaultValue;
		private boolean finalValue;
		
		public BooleanProperty(String key, boolean defaultValue, String description)
		{
			super(key, description);
			this.defaultValue = defaultValue;
			this.finalValue = defaultValue;
			parse();
		}
		
		public boolean getValue()
		{
			return finalValue;
		}
				
		public BooleanProperty(String key, String description)
		{
			this(key, true, description);
		}
		
		@Override
		protected void parse()
		{
			this.stringValue = manager.getProperty(key);
			
			if (stringValue == null)
			{
				Logs.log.warning("Could not find config key: " + key + ". Replacing with default value: " + defaultValue + ".");
				this.finalValue = this.defaultValue;
				this.stringValue = String.valueOf(this.finalValue);
			}
			else
			{
				try
				{
					this.finalValue = Boolean.parseBoolean(this.stringValue);
				}
				catch (Exception ex)
				{
					Logs.log.warning("Boolean value expected for config key: " + key + ". Replacing with default value: " + defaultValue + ".");
					this.finalValue = this.defaultValue;
					this.stringValue = String.valueOf(this.finalValue);
				}
			}
		}
	}
	
	public class StringProperty extends Property
	{
		private String defaultValue;
		private int minLength;
		private int maxLength;
		private String finalValue;
		
		public StringProperty(String key, String defaultValue, int minLength, int maxLength, String description)
		{
			super(key, description);
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.defaultValue = defaultValue;
			this.finalValue = defaultValue;
			parse();
		}
		
		public String getValue()
		{
			return finalValue;
		}
				
		public StringProperty(String key, String defaultValue, String description)
		{
			this(key, defaultValue, 0, 100, description);
		}
		
		public StringProperty(String key, String description)
		{
			this(key, "default", 0, 100, description);
		}
		
		@Override
		protected void parse()
		{
			this.stringValue = manager.getProperty(key);
			
			if (stringValue == null)
			{
				Logs.log.warning("Could not find config key: " + key + ". Replacing with default value: " + defaultValue + ".");
				this.finalValue = this.defaultValue;
				this.stringValue = String.valueOf(this.finalValue);
			}
			else
			{
				this.finalValue = this.stringValue;
				
				if (this.finalValue.length() > this.maxLength || this.finalValue.length() < this.minLength)
				{
					Logs.log.warning("Length of value for config key: " + key + " in interval [" + this.minLength +
							" .. " + this.maxLength + "]. Replacing with default value: " + defaultValue + ".");
					this.finalValue = this.defaultValue;
					this.stringValue = String.valueOf(this.finalValue);
				}
			}
		}
	}
	
	
	public Configuration(String filename)
	{
		this.filename = filename;
		
		properties = new ArrayList<Property>();
		manager = new Properties();
		needsRewrite = false;
						
		try
		{
			manager.load(new FileReader(filename));
		}
		catch (Exception ex)
		{
			Logs.log.log(Level.WARNING, "Error reading configuration file '" + filename + "'. Replacing with default.", ex);
			needsRewrite = true;
		}
	}
	
	public Property getProperty(String key)
	{
		for (Property p : properties)
			if (p.getKey().equals(key))
				return p;
		return null;
	}
	
	public void DoRewrite()
	{
		if (!needsRewrite) return;

		Collections.sort(properties);
		
		manager.clear();
		
		for (Property p : properties)
			manager.setProperty(p.getKey(), p.getStringValue());
		
		try
		{
			//manager.store(new FileWriter(filename), null);
			
			PrintWriter pw = new PrintWriter(new FileWriter(filename));
			pw.println("# " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(new Date()));
			pw.println();
			for (Property p : properties)
			{
				if (p.getDescription() != null) pw.println("# " + p.getDescription());
				pw.println(p.key + " = " + p.stringValue);
			}
			pw.close();
			
			needsRewrite = false;
			Logs.log.log(Level.INFO, "Configuration file written successfully '" + filename + "'.");
		}
		catch (Exception ex)
		{
			Logs.log.log(Level.WARNING, "Error writing configuration file '" + filename + "'.", ex);
			needsRewrite = true;
		}
	}
	
	public String getFilename()
	{
		return filename;
	}
}
