package virtualPool.util;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logs
{
	public static final String LOGS_FOLDER = "logs";
	public static Logger log;
	
	static
	{
		File file = new File(LOGS_FOLDER);
		if (!file.exists()) file.mkdir();
		
		log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		log.setLevel(Level.ALL);
		
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new SimpleFormatter());
		//log.addHandler(ch);
		
		try
		{ 
			FileHandler fh = new FileHandler(LOGS_FOLDER + File.separator + "stardate-" + System.currentTimeMillis() + ".log");
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}
}
