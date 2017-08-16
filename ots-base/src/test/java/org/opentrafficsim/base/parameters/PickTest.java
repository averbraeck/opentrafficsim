package org.opentrafficsim.base.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
        PickListItem<String> item1 = new PickListItem<String>("id1", "description1", "long description 1");
        assertEquals("can retrieve id", "id1", item1.getId());
        assertEquals("can retrieve display text", "description1", item1.getDisplayText());
        assertEquals("can retrieve description", "long description 1", item1.getDescription());
        assertTrue("toString returns something with PickListItem in it", item1.toString().indexOf("PickListItem") >= 0);
        PickList<String> pl = new PickList<String>("id", "description", item1);
        assertEquals("can retrieve id", "id", pl.getId());
        assertEquals("can retrieve description", "description", pl.getDescription());
        assertTrue("does not contain an item with junkId", pl.fails("junkId"));
        assertFalse("does contain an item with id1", pl.fails("id1"));
        assertTrue("does not contain an item with null id", pl.fails(null));
        assertTrue("toString returns something with PickList in it", pl.toString().indexOf("PickList") >= 0);
        PickListItem<String> item2 = new PickListItem<String>("id1", "description2"); // Same id!
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
        item2 = new PickListItem<String>("id2", "description2", "long description 2");
        assertTrue("does not (yet) contain an item with id2", pl.fails("id2"));
        pl.addItem(item2); // should not throw an exception
        assertFalse("does contain an item with id2", pl.fails("id2"));
        assertFalse("does contain an item with id1", pl.fails("id1"));
        String failMessage = pl.failMessage();
        assertNotNull("fail message may not be null", failMessage);
        assertTrue("fail message contains id1", failMessage.indexOf("id1") >= 0);
        assertTrue("fail message contains id2", failMessage.indexOf("id2") >= 0);
        pl = new PickList<String>("id", "description", item1, item2);
        assertFalse("does contain an item with id2", pl.fails("id2"));
        assertFalse("does contain an item with id1", pl.fails("id1"));
        List<PickListItem<String>> list = new ArrayList<>();
        try
        {
            new PickList<String>("idXX", "descriptionXX", list);
        }
        catch (ParameterException pe)
        {
            // Ignore expected exception
        }
        list.add(item1);
        pl = new PickList<String>("idXX", "descriptionXX", list);
        assertEquals("can retrieve id", "idXX", pl.getId());
        assertEquals("can retrieve description", "descriptionXX", pl.getDescription());
        assertTrue("does not contain an item with junkId", pl.fails("junkId"));
        assertFalse("does contain an item with id1", pl.fails("id1"));
        assertTrue("does not contain an item with null id", pl.fails(null));
        assertTrue("toString returns something with PickList in it", pl.toString().indexOf("PickList") >= 0);
    }
}
