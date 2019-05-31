//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 09:43:59 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.xml.bindings.ClassNameAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;


/**
 * <p>Java class for TRAFFICLIGHTSENSORTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TRAFFICLIGHTSENSORTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="LANE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="POSITION" use="required" type="{http://www.opentrafficsim.org/ots}LENGTHBEGINENDTYPE" /&gt;
 *       &lt;attribute name="LENGTH" use="required" type="{http://www.opentrafficsim.org/ots}POSITIVELENGTHTYPE" /&gt;
 *       &lt;attribute name="CLASS" use="required" type="{http://www.opentrafficsim.org/ots}CLASSNAMETYPE" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TRAFFICLIGHTSENSORTYPE")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
public class TRAFFICLIGHTSENSORTYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlAttribute(name = "ID", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected String id;
    @XmlAttribute(name = "LANE", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected String lane;
    @XmlAttribute(name = "POSITION", required = true)
    @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected LengthBeginEnd position;
    @XmlAttribute(name = "LENGTH", required = true)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected Length length;
    @XmlAttribute(name = "CLASS", required = true)
    @XmlJavaTypeAdapter(ClassNameAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected Class _class;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setLANE(String value) {
        this.lane = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public LengthBeginEnd getPOSITION() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setPOSITION(LengthBeginEnd value) {
        this.position = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public Length getLENGTH() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setLENGTH(Length value) {
        this.length = value;
    }

    /**
     * Gets the value of the class property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public Class getCLASS() {
        return _class;
    }

    /**
     * Sets the value of the class property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public void setCLASS(Class value) {
        this._class = value;
    }

}