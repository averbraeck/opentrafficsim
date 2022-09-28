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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 */
package org.opentrafficsim.road.network.lane;
