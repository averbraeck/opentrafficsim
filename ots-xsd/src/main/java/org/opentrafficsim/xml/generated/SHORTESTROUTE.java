//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.04 at 05:47:23 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FROM" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="TO" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="VIA" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="2"/&gt;
 *         &lt;element name="DISTANCECOST" type="{http://www.opentrafficsim.org/ots}DISTANCECOSTTYPE"/&gt;
 *         &lt;element name="TIMECOST" type="{http://www.opentrafficsim.org/ots}TIMECOSTTYPE"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "from",
    "to",
    "via",
    "distancecost",
    "timecost"
})
@XmlRootElement(name = "SHORTESTROUTE")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
public class SHORTESTROUTE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "FROM", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String from;
    @XmlElement(name = "TO", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String to;
    @XmlElement(name = "VIA", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected List<Object> via;
    @XmlElement(name = "DISTANCECOST", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String distancecost;
    @XmlElement(name = "TIMECOST", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String timecost;
    @XmlAttribute(name = "ID", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String id;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public String getFROM() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setFROM(String value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public String getTO() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setTO(String value) {
        this.to = value;
    }

    /**
     * Gets the value of the via property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the via property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVIA().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public List<Object> getVIA() {
        if (via == null) {
            via = new ArrayList<Object>();
        }
        return this.via;
    }

    /**
     * Gets the value of the distancecost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public String getDISTANCECOST() {
        return distancecost;
    }

    /**
     * Sets the value of the distancecost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setDISTANCECOST(String value) {
        this.distancecost = value;
    }

    /**
     * Gets the value of the timecost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public String getTIMECOST() {
        return timecost;
    }

    /**
     * Sets the value of the timecost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setTIMECOST(String value) {
        this.timecost = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}
