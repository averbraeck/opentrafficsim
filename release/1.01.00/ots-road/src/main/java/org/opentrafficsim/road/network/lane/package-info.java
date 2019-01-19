/**
 * Classes that provide detailed cross-sections of a link using lanes, markers and sensors.
 * <p>
 * The network classes are independent of the type of vehicle (GTU) that is used. Specific subclasses for roads, rail, shipping
 * lanes, or planes should be added to sub-packages with names like road, rail, ship, pedestrians, and air. <br>
 * Geometry classes can be added for animation.
 * <p>
 * A couple of assumptions underly the lane model.
 * <ul>
 * <li>Lanes can overlap. A bike path on a road where a car can also use the bike path is an example of overlapping lanes. Tram
 * tracks in a road is another one;</li>
 * <li>GTUs can be in multiple lanes at the same time. They register with their front and de-register with their back. In the d2
 * model this can be extended to lateral registration and de-registration as well;</li>
 * <li>GTUs know in which lane(s) they are and lanes know which vehicles occupy space on them;</li>
 * <li>A lane knows its neighboring lane(s), and easy methods are available to identify whether a GTU is allowed to change to
 * another lane.</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 */
package org.opentrafficsim.road.network.lane;
