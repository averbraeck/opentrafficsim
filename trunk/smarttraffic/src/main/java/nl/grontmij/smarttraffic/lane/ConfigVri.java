package nl.grontmij.smarttraffic.lane;

import java.util.HashMap;
import java.util.Map;

public class ConfigVri
{

    private String name;

    private Map<Integer, String> detectors = new HashMap<Integer, String>();

    private Map<Integer, String> signalGroups = new HashMap<Integer, String>();

    public ConfigVri(String name, Map<Integer, String> detectors, Map<Integer, String> signalGroups)
    {
        super();
        this.name = name;
        this.detectors = detectors;
        this.signalGroups = signalGroups;
        System.out.println("cfg read for vri: " + name + " - signalgroups= " + signalGroups);
        System.out.println("cfg read for vri: " + name + " - detectors   = " + detectors);
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
