package org.opentrafficsim.core.definitions;

import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;

/**
 * The Definitions interface contains access to the core definitions that can be used to interpret the Network and the
 * PerceivableContext. Example interfaces allow the retrieval of GTUTypes and LinkTypes.<br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
    /************************************** GTUTypes ***************************************/
    /***************************************************************************************/

    /**
     * Add the default GTU Types that have been defined in the enum GTUType.DEFAULTS to the network. It is not necessary to call
     * this method on every network; when the GTUTypes are for instance defined in an XML file, adding the default types might
     * not be needed.
     */
    void addDefaultGtuTypes();

    /**
     * Add a GTU type to the map. This method is automatically called from the GTUType constructor.
     * @param gtuType the GTUType to add
     */
    void addGtuType(GTUType gtuType);

    /**
     * Retrieve a defined GTUType based on its id.
     * @param gtuId the id to search for
     * @return the GTUType or null in case it could not be found
     */
    GTUType getGtuType(String gtuId);

    /**
     * Retrieve a defined default GTUType based on its enum.
     * @param gtuEnum the enum to search for
     * @return the GTUType or null in case it could not be found
     */
    GTUType getGtuType(GTUType.DEFAULTS gtuEnum);

    /**
     * Retrieve a safe copy of the map of defined GTUTypes in this network.
     * @return the map of defined GTUTypes
     */
    ImmutableMap<String, GTUType> getGtuTypes();

}
