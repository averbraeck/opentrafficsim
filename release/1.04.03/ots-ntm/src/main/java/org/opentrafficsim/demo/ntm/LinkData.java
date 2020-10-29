package org.opentrafficsim.demo.ntm;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class LinkData
{

    private String name;

    private String linkTag;

    private String wegtype;

    private String typeWegVak;

    private String typeWeg;

    /**
     * @param name String;
     * @param linkTag String;
     * @param wegtype String;
     * @param typeWegVak String;
     * @param typeWeg String;
     */
    public LinkData(String name, String linkTag, String wegtype, String typeWegVak, String typeWeg)
    {
        this.name = name;
        this.linkTag = linkTag;
        this.wegtype = wegtype;
        this.typeWegVak = typeWegVak;
        this.typeWeg = typeWeg;
    }

    /**
     * @return name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name String; set name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return linkTag.
     */
    public String getLinkTag()
    {
        return this.linkTag;
    }

    /**
     * @param linkTag String; set linkTag.
     */
    public void setLinkTag(String linkTag)
    {
        this.linkTag = linkTag;
    }

    /**
     * @return wegtype.
     */
    public String getWegtype()
    {
        return this.wegtype;
    }

    /**
     * @param wegtype String; set wegtype.
     */
    public void setWegtype(String wegtype)
    {
        this.wegtype = wegtype;
    }

    /**
     * @return typeWegVak.
     */
    public String getTypeWegVak()
    {
        return this.typeWegVak;
    }

    /**
     * @param typeWegVak String; set typeWegVak.
     */
    public void setTypeWegVak(String typeWegVak)
    {
        this.typeWegVak = typeWegVak;
    }

    /**
     * @return typeWeg.
     */
    public String getTypeWeg()
    {
        return this.typeWeg;
    }

    /**
     * @param typeWeg String; set typeWeg.
     */
    public void setTypeWeg(String typeWeg)
    {
        this.typeWeg = typeWeg;
    }

}
