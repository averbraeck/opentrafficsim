//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.02.21 at 07:52:17 PM CET 
//


package org.opentrafficsim.xml.generated;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="NAME" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="COORDINATE" type="{http://www.opentrafficsim.org/ots}COORDINATETYPE" /&gt;
 *       &lt;attribute name="ANGLE" type="{http://www.opentrafficsim.org/ots}ANGLETYPE" /&gt;
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
@XmlRootElement(name = "NODE")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
public class NODE {

    @XmlAttribute(name = "NAME", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    protected String name;
    @XmlAttribute(name = "COORDINATE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    protected String coordinate;
    @XmlAttribute(name = "ANGLE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    protected String angle;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    public void setNAME(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the coordinate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    public String getCOORDINATE() {
        return coordinate;
    }

    /**
     * Sets the value of the coordinate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    public void setCOORDINATE(String value) {
        this.coordinate = value;
    }

    /**
     * Gets the value of the angle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    public String getANGLE() {
        return angle;
    }

    /**
     * Sets the value of the angle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    public void setANGLE(String value) {
        this.angle = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-02-21T07:52:17+01:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}
