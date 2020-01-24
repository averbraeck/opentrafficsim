//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.01.23 at 04:07:30 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
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
 *         &lt;element name="LENGTHDIST" type="{http://www.opentrafficsim.org/ots}LENGTHDISTTYPE"/&gt;
 *         &lt;element name="WIDTHDIST" type="{http://www.opentrafficsim.org/ots}LENGTHDISTTYPE"/&gt;
 *         &lt;element name="MAXSPEEDDIST" type="{http://www.opentrafficsim.org/ots}SPEEDDISTTYPE"/&gt;
 *         &lt;element name="MAXACCELERATIONDIST" type="{http://www.opentrafficsim.org/ots}ACCELERATIONDISTTYPE" minOccurs="0"/&gt;
 *         &lt;element name="MAXDECELERATIONDIST" type="{http://www.opentrafficsim.org/ots}ACCELERATIONDISTTYPE" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="GTUTYPE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DEFAULT" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
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
    "lengthdist",
    "widthdist",
    "maxspeeddist",
    "maxaccelerationdist",
    "maxdecelerationdist"
})
@XmlRootElement(name = "GTUTEMPLATE")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
public class GTUTEMPLATE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "LENGTHDIST", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected LENGTHDISTTYPE lengthdist;
    @XmlElement(name = "WIDTHDIST", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected LENGTHDISTTYPE widthdist;
    @XmlElement(name = "MAXSPEEDDIST", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected SPEEDDISTTYPE maxspeeddist;
    @XmlElement(name = "MAXACCELERATIONDIST")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected ACCELERATIONDISTTYPE maxaccelerationdist;
    @XmlElement(name = "MAXDECELERATIONDIST")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected ACCELERATIONDISTTYPE maxdecelerationdist;
    @XmlAttribute(name = "ID", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected String id;
    @XmlAttribute(name = "GTUTYPE", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected String gtutype;
    @XmlAttribute(name = "DEFAULT")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected Boolean _default;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the lengthdist property.
     * 
     * @return
     *     possible object is
     *     {@link LENGTHDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public LENGTHDISTTYPE getLENGTHDIST() {
        return lengthdist;
    }

    /**
     * Sets the value of the lengthdist property.
     * 
     * @param value
     *     allowed object is
     *     {@link LENGTHDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setLENGTHDIST(LENGTHDISTTYPE value) {
        this.lengthdist = value;
    }

    /**
     * Gets the value of the widthdist property.
     * 
     * @return
     *     possible object is
     *     {@link LENGTHDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public LENGTHDISTTYPE getWIDTHDIST() {
        return widthdist;
    }

    /**
     * Sets the value of the widthdist property.
     * 
     * @param value
     *     allowed object is
     *     {@link LENGTHDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setWIDTHDIST(LENGTHDISTTYPE value) {
        this.widthdist = value;
    }

    /**
     * Gets the value of the maxspeeddist property.
     * 
     * @return
     *     possible object is
     *     {@link SPEEDDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public SPEEDDISTTYPE getMAXSPEEDDIST() {
        return maxspeeddist;
    }

    /**
     * Sets the value of the maxspeeddist property.
     * 
     * @param value
     *     allowed object is
     *     {@link SPEEDDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setMAXSPEEDDIST(SPEEDDISTTYPE value) {
        this.maxspeeddist = value;
    }

    /**
     * Gets the value of the maxaccelerationdist property.
     * 
     * @return
     *     possible object is
     *     {@link ACCELERATIONDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public ACCELERATIONDISTTYPE getMAXACCELERATIONDIST() {
        return maxaccelerationdist;
    }

    /**
     * Sets the value of the maxaccelerationdist property.
     * 
     * @param value
     *     allowed object is
     *     {@link ACCELERATIONDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setMAXACCELERATIONDIST(ACCELERATIONDISTTYPE value) {
        this.maxaccelerationdist = value;
    }

    /**
     * Gets the value of the maxdecelerationdist property.
     * 
     * @return
     *     possible object is
     *     {@link ACCELERATIONDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public ACCELERATIONDISTTYPE getMAXDECELERATIONDIST() {
        return maxdecelerationdist;
    }

    /**
     * Sets the value of the maxdecelerationdist property.
     * 
     * @param value
     *     allowed object is
     *     {@link ACCELERATIONDISTTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setMAXDECELERATIONDIST(ACCELERATIONDISTTYPE value) {
        this.maxdecelerationdist = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setGTUTYPE(String value) {
        this.gtutype = value;
    }

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public boolean isDEFAULT() {
        if (_default == null) {
            return false;
        } else {
            return _default;
        }
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setDEFAULT(Boolean value) {
        this._default = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T04:07:30+01:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}
