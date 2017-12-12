package org.opentrafficsim.base.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opentrafficsim.base.modelproperties.PickList;
import org.opentrafficsim.base.modelproperties.PickListItem;

/**
 * Test the PickList class and closely related classes.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 16, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class PickTest
{

    /**
     * Test the PickList class.
     * @throws ParameterException if this happens uncaught; this test has failed
     */
    @Test
    public final void testPickList() throws ParameterException
    {
        PickListItem<String> item1 = new PickListItem<>("id1", "description1", "long description 1");
        assertEquals("can retrieve id", "id1", item1.getId());
        assertEquals("can retrieve display text", "description1", item1.getDisplayText());
        assertEquals("can retrieve description", "long description 1", item1.getDescription());
        assertTrue("toString returns something with PickListItem in it", item1.toString().indexOf("PickListItem") >= 0);
        PickList<String> pl = new PickList<>("id", "description", item1);
        assertEquals("can retrieve id", "id", pl.getId());
        assertEquals("can retrieve description", "description", pl.getDescription());
        assertTrue("toString returns something with PickList in it", pl.toString().indexOf("PickList") >= 0);
        PickListItem<String> item2 = new PickListItem<>("id1", "description2"); // Same id!
        assertEquals("using 2 arg constructor sets description to display text", "description2", item2.getDisplayText());
        assertEquals("using 2 arg constructor sets description to display text", "description2", item2.getDescription());
        try
        {
            pl.addItem(item2);
        }
        catch (ParameterException pe)
        {
            // Ignore expected exception
        }
        item2 = new PickListItem<>("id2", "description2", "long description 2");
        pl.addItem(item2); // should not throw an exception
        String constraintDescription = pl.getConstraint().toString();
        assertNotNull("constraint toString may not be null", constraintDescription);
        assertTrue("constraint toString contains id1", constraintDescription.indexOf("id1") >= 0);
        assertTrue("constraint toString contains id2", constraintDescription.indexOf("id2") >= 0);
        
        pl = new PickList<>("id", "description", item1, item2);
        List<PickListItem<String>> list = new ArrayList<>();
        try
        {
            new PickList<>("idXX", "descriptionXX", list);
        }
        catch (ParameterException pe)
        {
            // Ignore expected exception
        }
        list.add(item1);
        pl = new PickList<>("idXX", "descriptionXX", list);
        assertEquals("can retrieve id", "idXX", pl.getId());
        assertEquals("can retrieve description", "descriptionXX", pl.getDescription());
        assertTrue("toString returns something with PickList in it", pl.toString().indexOf("PickList") >= 0);
    }
}
