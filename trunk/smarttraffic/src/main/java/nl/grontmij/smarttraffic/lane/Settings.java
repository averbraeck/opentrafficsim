package nl.grontmij.smarttraffic.lane;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Settings
{
    /** the map from simulator to properties. */
    private static Map<Integer, Properties> PROPS = new HashMap<>();

    /** instantiate settings per simulator. */
    public Settings(final OTSDEVSSimulatorInterface simulator, final String propertiesFileName)
    {
        Properties props = new Properties();
        try
        {
            InputStream fis = URLResource.getResourceAsStream(propertiesFileName);
            props.load(new BufferedInputStream(fis));
            PROPS.put(simulator.hashCode(), props);
            fis.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void test(final OTSDEVSSimulatorInterface simulator, final String name)
    {
        if (!PROPS.containsKey(simulator.hashCode()))
        {
            System.err.println("Cannot find properties for simulator " + simulator.toString());
            System.exit(-1);
        }

        if (!PROPS.get(simulator.hashCode()).containsKey(name))
        {
            System.err.println("Cannot find property " + name);
            System.exit(-1);
        }
    }

    public static boolean getBoolean(final OTSDEVSSimulatorInterface simulator, final String name)
    {
        test(simulator, name);
        String s = PROPS.get(simulator.hashCode()).getProperty(name);
        return s.toUpperCase().startsWith("T");
    }

    public static double getDouble(final OTSDEVSSimulatorInterface simulator, final String name)
    {
        test(simulator, name);
        String s = PROPS.get(simulator.hashCode()).getProperty(name);
        return Double.parseDouble(s);
    }

    public static double getInt(final OTSDEVSSimulatorInterface simulator, final String name)
    {
        test(simulator, name);
        String s = PROPS.get(simulator.hashCode()).getProperty(name);
        return Integer.parseInt(s);
    }

    public static String getString(final OTSDEVSSimulatorInterface simulator, final String name)
    {
        test(simulator, name);
        return PROPS.get(simulator.hashCode()).getProperty(name);
    }

}
