package org.opentrafficsim.demo.ntm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 *  A Cell extends a Zone and is used for the NetworkTransmissionModel
 *  The Cells cover a preferably homogeneous area and have their specific characteristics
 *  such as their free speed, a capacity and an NFD diagram
 *  A trip matrix quantifies the amount of trips between Cells in a network.
 *  The connection of neighbouring Cells are expressed by Links (connectors) 
 *  The cost to go from one to another Cell is quantified through the weights on the Connectors   
 *
 * </pre>
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX
 * Delft, the Netherlands. All rights reserved.
 * 
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/">
 * www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * 
 * @version 4 Sep 2014 <br>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> 
 */

public class CsvFileReader {

	
	public static TripDemand ReadTrafficDemand(final String csvFileName) throws IOException {	    
        URL url;
        if (new File(csvFileName).canRead())
            url = new File(csvFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(csvFileName);

        Map<Integer, Map<Integer, TripInfo>> tripDemand = new HashMap<Integer, Map<Integer, TripInfo>>();
        ArrayList<Long> zoneNames;
        
        //read the demand file from Omnitrans line by line
        // separator is a semicolon 
        
        // the first line contains the destinations: put them in the array
        String name;
        Long nr = CheckName(name);
        zoneNames.add(nr);

        // then, read all other lines: first column contains the name of the origin
        // this can be either a link or a centroid (starts with "C")
        tripDemand.put(nr, link);
		return tripDemand;   
	}
	
	public static Long CheckName(String name)  {
        Long nr = null;
		if (name.substring(0, 1).equals("C")) {
	        nr = ShapeFileReader.InspectNodeCentroid(name);
	    }
	    else { 
	    	String[] names = name.split(":");
	    	if (names[0] != null)  {
	    		nr = Long.parseLong(names[0]);
	    	}
	    }
		return nr;
	}

}
