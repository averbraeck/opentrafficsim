//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.11.03 at 01:02:34 PM CET
//

package org.opentrafficsim.road.network.factory.vissim.xsd;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SPEEDLIMIT" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="GTUTYPE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="LEGALSPEEDLIMIT" use="required" type="{http://www.opentrafficsim.org/ots}SPEEDTYPE" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="NAME" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DEFAULTLANEWIDTH" use="required" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE" />
 *       &lt;attribute name="DEFAULTLANEKEEPING" use="required" type="{http://www.opentrafficsim.org/ots}LANEKEEPINGTYPE" />
 *       &lt;attribute name="DEFAULTOVERTAKING" use="required" type="{http://www.opentrafficsim.org/ots}OVERTAKINGTYPE" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "speedlimit" })
@XmlRootElement(name = "ROADTYPE")
public class ROADTYPE
{

    @XmlElement(name = "SPEEDLIMIT", required = true)
    protected List<ROADTYPE.SPEEDLIMIT> speedlimit;

    @XmlAttribute(name = "NAME", required = true)
    protected String name;

    @XmlAttribute(name = "DEFAULTLANEWIDTH", required = true)
    protected String defaultlanewidth;

    @XmlAttribute(name = "DEFAULTLANEKEEPING", required = true)
    protected String defaultlanekeeping;

    @XmlAttribute(name = "DEFAULTOVERTAKING", required = true)
    protected String defaultovertaking;

    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    protected String base;

    /**
     * Gets the value of the speedlimit property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
     * speedlimit property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getSPEEDLIMIT().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ROADTYPE.SPEEDLIMIT }
     */
    public List<ROADTYPE.SPEEDLIMIT> getSPEEDLIMIT()
    {
        if (speedlimit == null)
        {
            speedlimit = new ArrayList<ROADTYPE.SPEEDLIMIT>();
        }
        return this.speedlimit;
    }

    /**
     * Gets the value of the name property.
     * @return possible object is {@link String }
     */
    public String getNAME()
    {
        return name;
    }

    /**
     * Sets the value of the name property.
     * @param value allowed object is {@link String }
     */
    public void setNAME(String value)
    {
        this.name = value;
    }

    /**
     * Gets the value of the defaultlanewidth property.
     * @return possible object is {@link String }
     */
    public String getDEFAULTLANEWIDTH()
    {
        return defaultlanewidth;
    }

    /**
     * Sets the value of the defaultlanewidth property.
     * @param value allowed object is {@link String }
     */
    public void setDEFAULTLANEWIDTH(String value)
    {
        this.defaultlanewidth = value;
    }

    /**
     * Gets the value of the defaultlanekeeping property.
     * @return possible object is {@link String }
     */
    public String getDEFAULTLANEKEEPING()
    {
        return defaultlanekeeping;
    }

    /**
     * Sets the value of the defaultlanekeeping property.
     * @param value allowed object is {@link String }
     */
    public void setDEFAULTLANEKEEPING(String value)
    {
        this.defaultlanekeeping = value;
    }

    /**
     * Gets the value of the defaultovertaking property.
     * @return possible object is {@link String }
     */
    public String getDEFAULTOVERTAKING()
    {
        return defaultovertaking;
    }

    /**
     * Sets the value of the defaultovertaking property.
     * @param value allowed object is {@link String }
     */
    public void setDEFAULTOVERTAKING(String value)
    {
        this.defaultovertaking = value;
    }

    /**
     * Gets the value of the base property.
     * @return possible object is {@link String }
     */
    public String getBase()
    {
        return base;
    }

    /**
     * Sets the value of the base property.
     * @param value allowed object is {@link String }
     */
    public void setBase(String value)
    {
        this.base = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="GTUTYPE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="LEGALSPEEDLIMIT" use="required" type="{http://www.opentrafficsim.org/ots}SPEEDTYPE" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SPEEDLIMIT
    {

        @XmlAttribute(name = "GTUTYPE", required = true)
        protected String gtutype;

        @XmlAttribute(name = "LEGALSPEEDLIMIT", required = true)
        protected String legalspeedlimit;

        /**
         * Gets the value of the gtutype property.
         * @return possible object is {@link String }
         */
        public String getGTUTYPE()
        {
            return gtutype;
        }

        /**
         * Sets the value of the gtutype property.
         * @param value allowed object is {@link String }
         */
        public void setGTUTYPE(String value)
        {
            this.gtutype = value;
        }

        /**
         * Gets the value of the legalspeedlimit property.
         * @return possible object is {@link String }
         */
        public String getLEGALSPEEDLIMIT()
        {
            return legalspeedlimit;
        }

        /**
         * Sets the value of the legalspeedlimit property.
         * @param value allowed object is {@link String }
         */
        public void setLEGALSPEEDLIMIT(String value)
        {
            this.legalspeedlimit = value;
        }

    }

}
