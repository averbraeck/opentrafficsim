package org.opentrafficsim.core.definitions;

import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;

/**
 * The Definitions interface contains access to the core definitions that can be used to interpret the Network and the
 * PerceivableContext. Example interfaces allow the retrieval of GtuTypes and LinkTypes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public interface Definitions
{
    /***************************************************************************************/
    /************************************** LinkTypes **************************************/
    /***************************************************************************************/

    /**
     * Add the default LinkTypes that have been defined in the enum LinkType.DEFAULTS to the network. It is not necessary to
     * call this method on every network; when the LinkTypes are for instance defined in an XML file, adding the default types
     * might not be needed.
     */
    void addDefaultLinkTypes();

    /**
     * Add a Link type to the map. This method is automatically called from the LinkType constructor.
     * @param linkType the LinkType to add
     */
    void addLinkType(LinkType linkType);

    /**
     * Retrieve a defined LinkType based on its id.
     * @param linkId the id to search for
     * @return the LinkType or null in case it could not be found
     */
    LinkType getLinkType(String linkId);

    /**
     * Retrieve a defined default LinkType based on its enum.
     * @param linkEnum the enum to search for
     * @return the LinkType or null in case it could not be found
     */
    LinkType getLinkType(LinkType.DEFAULTS linkEnum);

    /**
     * Retrieve a safe copy of the map of defined LinkTypes in this network.
     * @return the map of defined LinkTypes
     */
    ImmutableMap<String, LinkType> getLinkTypes();

    /***************************************************************************************/
    /************************************** GtuTypes ***************************************/
    /***************************************************************************************/

    /**
     * Add a GTU type to the map. This method is automatically called from the GtuType constructor.
     * @param gtuType the GtuType to add
     */
    void addGtuType(GtuType gtuType);

    /**
     * Retrieve a defined GtuType based on its id.
     * @param gtuId the id to search for
     * @return the GtuType or null in case it could not be found
     */
    GtuType getGtuType(String gtuId);

    /**
     * Retrieve a safe copy of the map of defined GtuTypes in this network.
     * @return the map of defined GtuTypes
     */
    ImmutableMap<String, GtuType> getGtuTypes();

}
