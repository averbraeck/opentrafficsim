//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 08:30:33 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CATEGORYTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CATEGORYTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="LANE" type="{http://www.opentrafficsim.org/ots}LANELINKTYPE" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="GTUTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ROUTE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="FACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" default="1.0" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CATEGORYTYPE", propOrder = {
    "lane"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
public class CATEGORYTYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "LANE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected LANELINKTYPE lane;
    @XmlAttribute(name = "ID", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String id;
    @XmlAttribute(name = "GTUTYPE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String gtutype;
    @XmlAttribute(name = "ROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String route;
    @XmlAttribute(name = "FACTOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String factor;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link LANELINKTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public LANELINKTYPE getLANE() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link LANELINKTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setLANE(LANELINKTYPE value) {
        this.lane = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the gtutype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getGTUTYPE() {
        return gtutype;
    }

    /**
     * Sets the value of the gtutype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setGTUTYPE(String value) {
        this.gtutype = value;
    }

    /**
     * Gets the value of the route property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getROUTE() {
        return route;
    }

    /**
     * Sets the value of the route property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setROUTE(String value) {
        this.route = value;
    }

    /**
     * Gets the value of the factor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getFACTOR() {
        if (factor == null) {
            return "1.0";
        } else {
            return factor;
        }
    }

    /**
     * Sets the value of the factor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setFACTOR(String value) {
        this.factor = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}
