//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.01 at 04:16:12 PM CET 
//


package org.opentrafficsim.xml.generated;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="NAME" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="GTUTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ROUTE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="LANE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="FACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "CATEGORY")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
public class CATEGORY {

    @XmlAttribute(name = "NAME", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String name;
    @XmlAttribute(name = "GTUTYPE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String gtutype;
    @XmlAttribute(name = "ROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String route;
    @XmlAttribute(name = "LANE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String lane;
    @XmlAttribute(name = "FACTOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String factor;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getNAME() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setNAME(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the gtutype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setROUTE(String value) {
        this.route = value;
    }

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getLANE() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setLANE(String value) {
        this.lane = value;
    }

    /**
     * Gets the value of the factor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getFACTOR() {
        return factor;
    }

    /**
     * Sets the value of the factor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}
