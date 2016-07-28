package dk.kb.webdanica.utils;

import java.io.File;
import java.util.logging.Logger;

/**
 * Utility class for settingsfile utilities.
 */
public class SettingsUtilities {
	
	/** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(SettingsUtilities.class.getName());

    /**
     * Tests if the given settingsFile is a valid SimpleXMl settingsfile.
     * @param settingsFile a given SimpleXml based settings-file.
     * @param verbose if true, write the stack-trace in the logfile.
     * @return true, if the settingsFile is valid, else false;
     */
    public static boolean isValidSimpleXmlSettingsFile(File settingsFile, boolean verbose) {
    	try {
    		new SimpleXml(settingsFile);
    	} catch(Throwable e) {
    		if (verbose) {
    			logger.warning(e.toString());
    		}
    		return false;
    	}
    	return true;
    }
    
    /**
     * Tests if the given settingsFile is a valid SimpleXMl settingsfile.
     * @param settingsFile a given SimpleXml based settings-file.
     * @return true, if the settingsFile is valid, else false;
     */
	public static boolean isValidSimpleXmlSettingsFile(File settingsFile) {
		return isValidSimpleXmlSettingsFile(settingsFile, false);
	}
	
	public static String getStringSetting(String settingsName, String default_string_value) {
    	String returnValue = default_string_value;
	    if (Settings.hasKey(settingsName)) {
	    	String settingsValue = Settings.get(settingsName);  
	    	if (settingsValue == null || settingsValue.isEmpty()) {
	    		logger.warning("Using default value '" + default_string_value + "' for setting '" + settingsName + "', as the value in the settings is null or empty");
	    	} else {
	    		returnValue = settingsValue;
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: " + default_string_value);
	    }
	    return returnValue;
    }
    

	public static int getIntegerSetting(String settingsName, int default_int_value) {
	 	int returnValue = default_int_value;
	    if (Settings.hasKey(settingsName)) {
	    	String settingsValueAsString = Settings.get(settingsName);  
	    	if (settingsValueAsString == null || settingsValueAsString.isEmpty()) {
	    		logger.warning("Using default value '" + default_int_value + "' for setting '" + settingsName + "', as the value in the settings is null or empty");
	    		int intValue;
	    		try {
	            	intValue = Integer.parseInt(settingsValueAsString);
	            	returnValue = intValue;
	            } catch (NumberFormatException e) {
	            	logger.warning("Using default value '" + default_int_value + "' for setting '" + settingsName + "', as the value '" + settingsValueAsString 
	            			+ "'  in the settings is not a valid integer");
	            }
	    	} else {
	    		returnValue = default_int_value;
	    	}
	    } else {
	    	logger.warning("The setting '" + settingsName + "' is not defined in the settingsfile. Using the default value: " + default_int_value);
	    }
	    return returnValue;
    }

	public static void testPropertyFile(String propertyKey) throws Throwable {
		String setting = System.getProperty(propertyKey);
		if (setting == null) {
			throw new Throwable("Required java property '" + propertyKey + "' is undefined");
		}
		File settingsFile = new File(setting);
	
		if (!settingsFile.exists()) {
			throw new Throwable("The settings file defined by property '" + propertyKey + "' does not exist: " 
					+ settingsFile.getAbsolutePath() + "' does not exist");
		}
	}
}

