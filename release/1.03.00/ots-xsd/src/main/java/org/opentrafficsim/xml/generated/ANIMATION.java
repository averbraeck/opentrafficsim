//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.11.15 at 04:27:05 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GTUCOLORERS" minOccurs="0"/&gt;
 *         &lt;element name="DEFAULTS" type="{http://www.opentrafficsim.org/ots}DEFAULTANIMATIONTYPE" minOccurs="0"/&gt;
 *         &lt;element name="LINKTYPE" type="{http://www.opentrafficsim.org/ots}LINKTYPEANIMATIONTYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="LANETYPE" type="{http://www.opentrafficsim.org/ots}LANETYPEANIMATIONTYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ROADLAYOUT" type="{http://www.opentrafficsim.org/ots}ROADLAYOUTANIMATIONTYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="LINK" type="{http://www.opentrafficsim.org/ots}LINKANIMATIONTYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="LAYER" type="{http://www.opentrafficsim.org/ots}LAYERTOGGLETYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gtucolorers",
    "defaults",
    "linktype",
    "lanetype",
    "roadlayout",
    "link",
    "layer"
})
@XmlRootElement(name = "ANIMATION")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
public class ANIMATION
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "GTUCOLORERS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected GTUCOLORERS gtucolorers;
    @XmlElement(name = "DEFAULTS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected DEFAULTANIMATIONTYPE defaults;
    @XmlElement(name = "LINKTYPE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected List<LINKTYPEANIMATIONTYPE> linktype;
    @XmlElement(name = "LANETYPE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected List<LANETYPEANIMATIONTYPE> lanetype;
    @XmlElement(name = "ROADLAYOUT")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected List<ROADLAYOUTANIMATIONTYPE> roadlayout;
    @XmlElement(name = "LINK")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected List<LINKANIMATIONTYPE> link;
    @XmlElement(name = "LAYER")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    protected List<LAYERTOGGLETYPE> layer;

    /**
     * Gets the value of the gtucolorers property.
     * 
     * @return
     *     possible object is
     *     {@link GTUCOLORERS }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public GTUCOLORERS getGTUCOLORERS() {
        return gtucolorers;
    }

    /**
     * Sets the value of the gtucolorers property.
     * 
     * @param value
     *     allowed object is
     *     {@link GTUCOLORERS }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public void setGTUCOLORERS(GTUCOLORERS value) {
        this.gtucolorers = value;
    }

    /**
     * Gets the value of the defaults property.
     * 
     * @return
     *     possible object is
     *     {@link DEFAULTANIMATIONTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public DEFAULTANIMATIONTYPE getDEFAULTS() {
        return defaults;
    }

    /**
     * Sets the value of the defaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link DEFAULTANIMATIONTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public void setDEFAULTS(DEFAULTANIMATIONTYPE value) {
        this.defaults = value;
    }

    /**
     * Gets the value of the linktype property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linktype property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLINKTYPE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LINKTYPEANIMATIONTYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public List<LINKTYPEANIMATIONTYPE> getLINKTYPE() {
        if (linktype == null) {
            linktype = new ArrayList<LINKTYPEANIMATIONTYPE>();
        }
        return this.linktype;
    }

    /**
     * Gets the value of the lanetype property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lanetype property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLANETYPE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LANETYPEANIMATIONTYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public List<LANETYPEANIMATIONTYPE> getLANETYPE() {
        if (lanetype == null) {
            lanetype = new ArrayList<LANETYPEANIMATIONTYPE>();
        }
        return this.lanetype;
    }

    /**
     * Gets the value of the roadlayout property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roadlayout property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getROADLAYOUT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ROADLAYOUTANIMATIONTYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public List<ROADLAYOUTANIMATIONTYPE> getROADLAYOUT() {
        if (roadlayout == null) {
            roadlayout = new ArrayList<ROADLAYOUTANIMATIONTYPE>();
        }
        return this.roadlayout;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLINK().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LINKANIMATIONTYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public List<LINKANIMATIONTYPE> getLINK() {
        if (link == null) {
            link = new ArrayList<LINKANIMATIONTYPE>();
        }
        return this.link;
    }

    /**
     * Gets the value of the layer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the layer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLAYER().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LAYERTOGGLETYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-15T04:27:05+01:00", comments = "JAXB RI v2.3.0")
    public List<LAYERTOGGLETYPE> getLAYER() {
        if (layer == null) {
            layer = new ArrayList<LAYERTOGGLETYPE>();
        }
        return this.layer;
    }

}
