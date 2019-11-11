//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.11.11 at 03:39:40 AM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;


/**
 * <p>Java class for CROSSSECTIONELEMENT complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CROSSSECTIONELEMENT"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="CENTEROFFSET" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *           &lt;element name="LEFTOFFSET" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *           &lt;element name="RIGHTOFFSET" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *           &lt;sequence&gt;
 *             &lt;choice&gt;
 *               &lt;element name="CENTEROFFSETSTART" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *               &lt;element name="LEFTOFFSETSTART" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *               &lt;element name="RIGHTOFFSETSTART" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *             &lt;/choice&gt;
 *             &lt;choice&gt;
 *               &lt;element name="CENTEROFFSETEND" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *               &lt;element name="LEFTOFFSETEND" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *               &lt;element name="RIGHTOFFSETEND" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *             &lt;/choice&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;choice&gt;
 *           &lt;element name="WIDTH" type="{http://www.opentrafficsim.org/ots}POSITIVELENGTHTYPE"/&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="WIDTHSTART" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *             &lt;element name="WIDTHEND" type="{http://www.opentrafficsim.org/ots}LENGTHTYPE"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CROSSSECTIONELEMENT", propOrder = {
    "centeroffset",
    "leftoffset",
    "rightoffset",
    "centeroffsetstart",
    "leftoffsetstart",
    "rightoffsetstart",
    "centeroffsetend",
    "leftoffsetend",
    "rightoffsetend",
    "width",
    "widthstart",
    "widthend"
})
@XmlSeeAlso({
    CSELANE.class,
    CSENOTRAFFICLANE.class,
    CSESHOULDER.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
public class CROSSSECTIONELEMENT implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "CENTEROFFSET", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length centeroffset;
    @XmlElement(name = "LEFTOFFSET", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length leftoffset;
    @XmlElement(name = "RIGHTOFFSET", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length rightoffset;
    @XmlElement(name = "CENTEROFFSETSTART", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length centeroffsetstart;
    @XmlElement(name = "LEFTOFFSETSTART", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length leftoffsetstart;
    @XmlElement(name = "RIGHTOFFSETSTART", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length rightoffsetstart;
    @XmlElement(name = "CENTEROFFSETEND", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length centeroffsetend;
    @XmlElement(name = "LEFTOFFSETEND", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length leftoffsetend;
    @XmlElement(name = "RIGHTOFFSETEND", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length rightoffsetend;
    @XmlElement(name = "WIDTH", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length width;
    @XmlElement(name = "WIDTHSTART", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length widthstart;
    @XmlElement(name = "WIDTHEND", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected org.djunits.value.vdouble.scalar.Length widthend;

    /**
     * Gets the value of the centeroffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getCENTEROFFSET() {
        return centeroffset;
    }

    /**
     * Sets the value of the centeroffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setCENTEROFFSET(org.djunits.value.vdouble.scalar.Length value) {
        this.centeroffset = value;
    }

    /**
     * Gets the value of the leftoffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getLEFTOFFSET() {
        return leftoffset;
    }

    /**
     * Sets the value of the leftoffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setLEFTOFFSET(org.djunits.value.vdouble.scalar.Length value) {
        this.leftoffset = value;
    }

    /**
     * Gets the value of the rightoffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getRIGHTOFFSET() {
        return rightoffset;
    }

    /**
     * Sets the value of the rightoffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setRIGHTOFFSET(org.djunits.value.vdouble.scalar.Length value) {
        this.rightoffset = value;
    }

    /**
     * Gets the value of the centeroffsetstart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getCENTEROFFSETSTART() {
        return centeroffsetstart;
    }

    /**
     * Sets the value of the centeroffsetstart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setCENTEROFFSETSTART(org.djunits.value.vdouble.scalar.Length value) {
        this.centeroffsetstart = value;
    }

    /**
     * Gets the value of the leftoffsetstart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getLEFTOFFSETSTART() {
        return leftoffsetstart;
    }

    /**
     * Sets the value of the leftoffsetstart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setLEFTOFFSETSTART(org.djunits.value.vdouble.scalar.Length value) {
        this.leftoffsetstart = value;
    }

    /**
     * Gets the value of the rightoffsetstart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getRIGHTOFFSETSTART() {
        return rightoffsetstart;
    }

    /**
     * Sets the value of the rightoffsetstart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setRIGHTOFFSETSTART(org.djunits.value.vdouble.scalar.Length value) {
        this.rightoffsetstart = value;
    }

    /**
     * Gets the value of the centeroffsetend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getCENTEROFFSETEND() {
        return centeroffsetend;
    }

    /**
     * Sets the value of the centeroffsetend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setCENTEROFFSETEND(org.djunits.value.vdouble.scalar.Length value) {
        this.centeroffsetend = value;
    }

    /**
     * Gets the value of the leftoffsetend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getLEFTOFFSETEND() {
        return leftoffsetend;
    }

    /**
     * Sets the value of the leftoffsetend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setLEFTOFFSETEND(org.djunits.value.vdouble.scalar.Length value) {
        this.leftoffsetend = value;
    }

    /**
     * Gets the value of the rightoffsetend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getRIGHTOFFSETEND() {
        return rightoffsetend;
    }

    /**
     * Sets the value of the rightoffsetend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setRIGHTOFFSETEND(org.djunits.value.vdouble.scalar.Length value) {
        this.rightoffsetend = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getWIDTH() {
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setWIDTH(org.djunits.value.vdouble.scalar.Length value) {
        this.width = value;
    }

    /**
     * Gets the value of the widthstart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getWIDTHSTART() {
        return widthstart;
    }

    /**
     * Sets the value of the widthstart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setWIDTHSTART(org.djunits.value.vdouble.scalar.Length value) {
        this.widthstart = value;
    }

    /**
     * Gets the value of the widthend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public org.djunits.value.vdouble.scalar.Length getWIDTHEND() {
        return widthend;
    }

    /**
     * Sets the value of the widthend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setWIDTHEND(org.djunits.value.vdouble.scalar.Length value) {
        this.widthend = value;
    }

}
