package org.opentrafficsim.demo.web;

import java.util.Properties;

import org.djutils.logger.CategoryLogger;

/**
 * Args contains the command line arguments and a few methods to parse them. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class Args
{
    /** the properties for the web server. */
    private Properties properties = new Properties();

    /**
     * @param args String[]; arguments for demo server, e.g., port=8080
     */
    public Args(final String[] args)
    {
        for (String arg : args)
        {
            String[] p = arg.split("=");
            if (p.length == 2)
            {
                this.properties.put(p[1].toLowerCase(), p[2]);
            }
        }
    }

    /**
     * Parse an int from the args with a default value.
     * @param key The key to look up (e.g., when the argument port=8080 has been given, the key is "port")
     * @param defaultValue the default value if the key is missing or invalid
     * @return the int value or the default value.
     */
    public final int parseInt(final String key, final int defaultValue)
    {
        int ret = defaultValue;
        try
        {
            ret = Integer.parseInt(this.properties.getOrDefault(key, defaultValue).toString());
        }
        catch (Exception e)
        {
            CategoryLogger.always().warn(e,
                    "Could not parse command line key " + key + ", default value " + defaultValue + " has been used.");
        }
        return ret;
    }

    /**
     * Parse a String from the args with a default value.
     * @param key The key to look up (e.g., when the argument port=8080 has been given, the key is "port")
     * @param defaultValue the default value if the key is missing or invalid
     * @return the String value or the default value.
     */
    public final String parseString(final String key, final String defaultValue)
    {
        String ret = defaultValue;
        try
        {
            ret = this.properties.getOrDefault(key, defaultValue).toString();
        }
        catch (Exception e)
        {
            CategoryLogger.always().warn(e,
                    "Could not parse command line key " + key + ", default value " + defaultValue + " has been used.");
        }
        return ret;
    }
}
