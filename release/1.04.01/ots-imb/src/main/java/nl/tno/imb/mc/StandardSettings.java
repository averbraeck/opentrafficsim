package nl.tno.imb.mc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 17, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class StandardSettings
{
    /** Map of collected switches. */
    Map<String, String> switches = new LinkedHashMap<>();

    /** Non-switch command line arguments. */
    List<String> arguments = new ArrayList<>();

    /** Index of next argument to return in <cite>nextArgument</cite>. */
    private int argumentIndex = 0;

    /**
     * Construct StandardSettings from the command line arguments.
     * @param commandLineArguments String[]; the command line arguments
     */
    public StandardSettings(final String[] commandLineArguments)
    {
        for (String arg : commandLineArguments)
        {
            if (arg.startsWith("/") || arg.startsWith("-"))
            {
                String[] fields = arg.substring(1).split("[=:]");
                String value = "";
                if (fields.length > 1)
                {
                    value = fields[1];
                    // Silently ignoring the case where fields.length > 2
                }
                this.switches.put(fields[0].toLowerCase(), value);
            }
            else
            {
                this.arguments.add(arg);
            }
        }
    }

    /**
     * Report if a switch is present.
     * @param switchName String; name of the switch to report presence of
     * @return boolean; true if the switch is present; false if the switch is not present
     */
    public boolean testSwitch(final String switchName)
    {
        return this.switches.containsKey(switchName.toLowerCase());
    }

    /**
     * Lookup and return a switch value.
     * @param switchName String; name of the switch to lookup
     * @param defaultValue String; default value to return if lookup fails
     * @return String; the value of the switch, or <cite>defaultValue</cite> if the switch is not defined
     */
    public String getSwitch(final String switchName, final String defaultValue)
    {
        String result = this.switches.get(switchName.toLowerCase());
        if (null != result)
        {
            return result;
        }
        return defaultValue;
    }

    /**
     * Reset the internal argument index and return the first non-switch command line argument.
     * @return String
     */
    public String firstArgument()
    {
        this.argumentIndex = 0;
        return nextArgument();
    }

    /**
     * Return the next non-switch command line argument and increment the internal argument index.
     * @return String; the next non-switch command line argument, or <cite>""</cite> if there are no more non-switch command
     *         line arguments
     */
    public String nextArgument()
    {
        if (this.argumentIndex >= this.arguments.size())
        {
            return "";
        }
        return this.arguments.get(this.argumentIndex++);
    }

    public String getSetting(final String settingName, final String defaultValue)
    {
        if (!testSwitch(settingName))
        {
            try
            {
                String result = ConfigurationManager.appSettings(settingName);
                if (null == result)
                {
                    return defaultValue;
                }
                return result;
            }
            catch (ConfigurationErrorsException cee)
            {
                return defaultValue;
            }
        }
        else
        {
            return getSwitch(settingName, defaultValue);
        }
    }

}
