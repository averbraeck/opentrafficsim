//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 09:43:59 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.awt.Color;
import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.xml.bindings.ColorAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;


/**
 * <p>Java class for DEFAULTANIMATIONTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DEFAULTANIMATIONTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="LINK" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
 *                 &lt;attribute name="WIDTH" type="{http://www.opentrafficsim.org/ots}POSITIVELENGTHTYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="LANE" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="STRIPE" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="SHOULDER" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="NOTRAFFICLANE" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DEFAULTANIMATIONTYPE", propOrder = {
    "link",
    "lane",
    "stripe",
    "shoulder",
    "notrafficlane"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
public class DEFAULTANIMATIONTYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "LINK")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected DEFAULTANIMATIONTYPE.LINK link;
    @XmlElement(name = "LANE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected DEFAULTANIMATIONTYPE.LANE lane;
    @XmlElement(name = "STRIPE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected DEFAULTANIMATIONTYPE.STRIPE stripe;
    @XmlElement(name = "SHOULDER")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected DEFAULTANIMATIONTYPE.SHOULDER shoulder;
    @XmlElement(name = "NOTRAFFICLANE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected DEFAULTANIMATIONTYPE.NOTRAFFICLANE notrafficlane;

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link DEFAULTANIMATIONTYPE.LINK }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public DEFAULTANIMATIONTYPE.LINK getLINK() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link DEFAULTANIMATIONTYPE.LINK }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setLINK(DEFAULTANIMATIONTYPE.LINK value) {
        this.link = value;
    }

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link DEFAULTANIMATIONTYPE.LANE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public DEFAULTANIMATIONTYPE.LANE getLANE() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link DEFAULTANIMATIONTYPE.LANE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setLANE(DEFAULTANIMATIONTYPE.LANE value) {
        this.lane = value;
    }

    /**
     * Gets the value of the stripe property.
     * 
     * @return
     *     possible object is
     *     {@link DEFAULTANIMATIONTYPE.STRIPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public DEFAULTANIMATIONTYPE.STRIPE getSTRIPE() {
        return stripe;
    }

    /**
     * Sets the value of the stripe property.
     * 
     * @param value
     *     allowed object is
     *     {@link DEFAULTANIMATIONTYPE.STRIPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setSTRIPE(DEFAULTANIMATIONTYPE.STRIPE value) {
        this.stripe = value;
    }

    /**
     * Gets the value of the shoulder property.
     * 
     * @return
     *     possible object is
     *     {@link DEFAULTANIMATIONTYPE.SHOULDER }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public DEFAULTANIMATIONTYPE.SHOULDER getSHOULDER() {
        return shoulder;
    }

    /**
     * Sets the value of the shoulder property.
     * 
     * @param value
     *     allowed object is
     *     {@link DEFAULTANIMATIONTYPE.SHOULDER }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setSHOULDER(DEFAULTANIMATIONTYPE.SHOULDER value) {
        this.shoulder = value;
    }

    /**
     * Gets the value of the notrafficlane property.
     * 
     * @return
     *     possible object is
     *     {@link DEFAULTANIMATIONTYPE.NOTRAFFICLANE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public DEFAULTANIMATIONTYPE.NOTRAFFICLANE getNOTRAFFICLANE() {
        return notrafficlane;
    }

    /**
     * Sets the value of the notrafficlane property.
     * 
     * @param value
     *     allowed object is
     *     {@link DEFAULTANIMATIONTYPE.NOTRAFFICLANE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setNOTRAFFICLANE(DEFAULTANIMATIONTYPE.NOTRAFFICLANE value) {
        this.notrafficlane = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class LANE
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "COLOR", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Color color;

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Color getCOLOR() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setCOLOR(Color value) {
            this.color = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
     *       &lt;attribute name="WIDTH" type="{http://www.opentrafficsim.org/ots}POSITIVELENGTHTYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class LINK
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "COLOR", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Color color;
        @XmlAttribute(name = "WIDTH")
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Length width;

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Color getCOLOR() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setCOLOR(Color value) {
            this.color = value;
        }

        /**
         * Gets the value of the width property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Length getWIDTH() {
            return width;
        }

        /**
         * Sets the value of the width property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setWIDTH(Length value) {
            this.width = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class NOTRAFFICLANE
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "COLOR", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Color color;

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Color getCOLOR() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setCOLOR(Color value) {
            this.color = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class SHOULDER
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "COLOR", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Color color;

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Color getCOLOR() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setCOLOR(Color value) {
            this.color = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="COLOR" use="required" type="{http://www.opentrafficsim.org/ots}COLORTYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class STRIPE
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "COLOR", required = true)
        @XmlJavaTypeAdapter(ColorAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Color color;

        /**
         * Gets the value of the color property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Color getCOLOR() {
            return color;
        }

        /**
         * Sets the value of the color property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setCOLOR(Color value) {
            this.color = value;
        }

    }

}
