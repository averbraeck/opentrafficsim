//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.11.03 at 01:02:34 PM CET
//

package org.opentrafficsim.road.network.factory.vissim.xsd;

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
 *         &lt;element name="SPEEDGTUCOLORER" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="MAXSPEED" use="required" type="{http://www.opentrafficsim.org/ots}SPEEDTYPE" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ACCELERATIONGTUCOLORER" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="MAXDECELERATION" use="required" type="{http://www.opentrafficsim.org/ots}ACCELERATIONTYPE" />
 *                 &lt;attribute name="MAXACCELERATION" use="required" type="{http://www.opentrafficsim.org/ots}ACCELERATIONTYPE" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="LANECHANGEURGEGTUCOLORER" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="MINLANECHANGEDISTANCE" use="required" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE" />
 *                 &lt;attribute name="HORIZON" use="required" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"speedgtucolorer", "accelerationgtucolorer", "lanechangeurgegtucolorer"})
@XmlRootElement(name = "GLOBAL")
public class GLOBAL
{

    @XmlElement(name = "SPEEDGTUCOLORER")
    protected GLOBAL.SPEEDGTUCOLORER speedgtucolorer;

    @XmlElement(name = "ACCELERATIONGTUCOLORER")
    protected GLOBAL.ACCELERATIONGTUCOLORER accelerationgtucolorer;

    @XmlElement(name = "LANECHANGEURGEGTUCOLORER")
    protected GLOBAL.LANECHANGEURGEGTUCOLORER lanechangeurgegtucolorer;

    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    protected String base;

    /**
     * Gets the value of the speedgtucolorer property.
     * @return possible object is {@link GLOBAL.SPEEDGTUCOLORER }
     */
    public GLOBAL.SPEEDGTUCOLORER getSPEEDGTUCOLORER()
    {
        return speedgtucolorer;
    }

    /**
     * Sets the value of the speedgtucolorer property.
     * @param value GLOBAL.SPEEDGTUCOLORER; allowed object is {@link GLOBAL.SPEEDGTUCOLORER }
     */
    public void setSPEEDGTUCOLORER(GLOBAL.SPEEDGTUCOLORER value)
    {
        this.speedgtucolorer = value;
    }

    /**
     * Gets the value of the accelerationgtucolorer property.
     * @return possible object is {@link GLOBAL.ACCELERATIONGTUCOLORER }
     */
    public GLOBAL.ACCELERATIONGTUCOLORER getACCELERATIONGTUCOLORER()
    {
        return accelerationgtucolorer;
    }

    /**
     * Sets the value of the accelerationgtucolorer property.
     * @param value GLOBAL.ACCELERATIONGTUCOLORER; allowed object is {@link GLOBAL.ACCELERATIONGTUCOLORER }
     */
    public void setACCELERATIONGTUCOLORER(GLOBAL.ACCELERATIONGTUCOLORER value)
    {
        this.accelerationgtucolorer = value;
    }

    /**
     * Gets the value of the lanechangeurgegtucolorer property.
     * @return possible object is {@link GLOBAL.LANECHANGEURGEGTUCOLORER }
     */
    public GLOBAL.LANECHANGEURGEGTUCOLORER getLANECHANGEURGEGTUCOLORER()
    {
        return lanechangeurgegtucolorer;
    }

    /**
     * Sets the value of the lanechangeurgegtucolorer property.
     * @param value GLOBAL.LANECHANGEURGEGTUCOLORER; allowed object is {@link GLOBAL.LANECHANGEURGEGTUCOLORER }
     */
    public void setLANECHANGEURGEGTUCOLORER(GLOBAL.LANECHANGEURGEGTUCOLORER value)
    {
        this.lanechangeurgegtucolorer = value;
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
     * @param value String; allowed object is {@link String }
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
     *       &lt;attribute name="MAXDECELERATION" use="required" type="{http://www.opentrafficsim.org/ots}ACCELERATIONTYPE" />
     *       &lt;attribute name="MAXACCELERATION" use="required" type="{http://www.opentrafficsim.org/ots}ACCELERATIONTYPE" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ACCELERATIONGTUCOLORER
    {

        @XmlAttribute(name = "MAXDECELERATION", required = true)
        protected String maxdeceleration;

        @XmlAttribute(name = "MAXACCELERATION", required = true)
        protected String maxacceleration;

        /**
         * Gets the value of the maxdeceleration property.
         * @return possible object is {@link String }
         */
        public String getMAXDECELERATION()
        {
            return maxdeceleration;
        }

        /**
         * Sets the value of the maxdeceleration property.
         * @param value String; allowed object is {@link String }
         */
        public void setMAXDECELERATION(String value)
        {
            this.maxdeceleration = value;
        }

        /**
         * Gets the value of the maxacceleration property.
         * @return possible object is {@link String }
         */
        public String getMAXACCELERATION()
        {
            return maxacceleration;
        }

        /**
         * Sets the value of the maxacceleration property.
         * @param value String; allowed object is {@link String }
         */
        public void setMAXACCELERATION(String value)
        {
            this.maxacceleration = value;
        }

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
     *       &lt;attribute name="MINLANECHANGEDISTANCE" use="required" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE" />
     *       &lt;attribute name="HORIZON" use="required" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LANECHANGEURGEGTUCOLORER
    {

        @XmlAttribute(name = "MINLANECHANGEDISTANCE", required = true)
        protected String minlanechangedistance;

        @XmlAttribute(name = "HORIZON", required = true)
        protected String horizon;

        /**
         * Gets the value of the minlanechangedistance property.
         * @return possible object is {@link String }
         */
        public String getMINLANECHANGEDISTANCE()
        {
            return minlanechangedistance;
        }

        /**
         * Sets the value of the minlanechangedistance property.
         * @param value String; allowed object is {@link String }
         */
        public void setMINLANECHANGEDISTANCE(String value)
        {
            this.minlanechangedistance = value;
        }

        /**
         * Gets the value of the horizon property.
         * @return possible object is {@link String }
         */
        public String getHORIZON()
        {
            return horizon;
        }

        /**
         * Sets the value of the horizon property.
         * @param value String; allowed object is {@link String }
         */
        public void setHORIZON(String value)
        {
            this.horizon = value;
        }

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
     *       &lt;attribute name="MAXSPEED" use="required" type="{http://www.opentrafficsim.org/ots}SPEEDTYPE" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SPEEDGTUCOLORER
    {

        @XmlAttribute(name = "MAXSPEED", required = true)
        protected String maxspeed;

        /**
         * Gets the value of the maxspeed property.
         * @return possible object is {@link String }
         */
        public String getMAXSPEED()
        {
            return maxspeed;
        }

        /**
         * Sets the value of the maxspeed property.
         * @param value String; allowed object is {@link String }
         */
        public void setMAXSPEED(String value)
        {
            this.maxspeed = value;
        }

    }

}
