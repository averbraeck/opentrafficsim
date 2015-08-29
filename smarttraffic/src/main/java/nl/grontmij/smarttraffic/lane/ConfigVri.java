package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigVri
{

    private String name;

    private Map<Integer, String> detectors = new HashMap<Integer, String>();

    private Map<Integer, String> signalGroups = new HashMap<Integer, String>();

    public ConfigVri(String name, Map<Integer, String> detectors, Map<Integer, String> signalGroups,
        BufferedWriter outputFileLogConfigVRI)
    {
        super();
        this.name = name;
        this.detectors = detectors;
        this.signalGroups = signalGroups;
        // System.out.println("cfg read for vri: " + name + " - signalgroups= "
        // + signalGroups);
        // System.out.println("cfg read for vri: " + name + " - detectors   = "
        // + detectors);

        try
        {
            outputFileLogConfigVRI.write("cfg read for vri: " + name + " - signalgroups= " + signalGroups + "\n");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try
        {
            outputFileLogConfigVRI.write("cfg read for vri: " + name + " - detectors   = " + detectors + "\n");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try
        {
            outputFileLogConfigVRI.flush();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getName()
    {
        return name;
    }

    public Map<Integer, String> getDetectors()
    {
        return detectors;
    }

    public Map<Integer, String> getSignalGroups()
    {
        return signalGroups;
    }

}
