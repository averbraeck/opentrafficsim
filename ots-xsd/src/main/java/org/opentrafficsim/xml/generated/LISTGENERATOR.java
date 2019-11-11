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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;


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
 *         &lt;choice&gt;
 *           &lt;element name="GTUTEMPLATE" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="GTUTEMPPLATEMIX" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice&gt;
 *           &lt;element name="ROUTE" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="ROUTEMIX" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="SHORTESTROUTE" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="SHORTESTROUTEMIX" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="ROOMCHECKER" type="{http://www.opentrafficsim.org/ots}ROOMCHECKERTYPE" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="LINK" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="LANE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DIRECTION" use="required" type="{http://www.opentrafficsim.org/ots}GTUDIRECTIONTYPE" /&gt;
 *       &lt;attribute name="POSITION" type="{http://www.opentrafficsim.org/ots}LENGTHBEGINENDTYPE" /&gt;
 *       &lt;attribute name="URI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gtutemplate",
    "gtutempplatemix",
    "route",
    "routemix",
    "shortestroute",
    "shortestroutemix",
    "roomchecker"
})
@XmlRootElement(name = "LISTGENERATOR")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
public class LISTGENERATOR
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "GTUTEMPLATE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String gtutemplate;
    @XmlElement(name = "GTUTEMPPLATEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String gtutempplatemix;
    @XmlElement(name = "ROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String route;
    @XmlElement(name = "ROUTEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String routemix;
    @XmlElement(name = "SHORTESTROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String shortestroute;
    @XmlElement(name = "SHORTESTROUTEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String shortestroutemix;
    @XmlElement(name = "ROOMCHECKER", defaultValue = "CF")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String roomchecker;
    @XmlAttribute(name = "LINK", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String link;
    @XmlAttribute(name = "LANE", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String lane;
    @XmlAttribute(name = "DIRECTION", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String direction;
    @XmlAttribute(name = "POSITION")
    @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected LengthBeginEnd position;
    @XmlAttribute(name = "URI", required = true)
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    protected String uri;

    /**
     * Gets the value of the gtutemplate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getGTUTEMPLATE() {
        return gtutemplate;
    }

    /**
     * Sets the value of the gtutemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setGTUTEMPLATE(String value) {
        this.gtutemplate = value;
    }

    /**
     * Gets the value of the gtutempplatemix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getGTUTEMPPLATEMIX() {
        return gtutempplatemix;
    }

    /**
     * Sets the value of the gtutempplatemix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setGTUTEMPPLATEMIX(String value) {
        this.gtutempplatemix = value;
    }

    /**
     * Gets the value of the route property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setROUTE(String value) {
        this.route = value;
    }

    /**
     * Gets the value of the routemix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getROUTEMIX() {
        return routemix;
    }

    /**
     * Sets the value of the routemix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setROUTEMIX(String value) {
        this.routemix = value;
    }

    /**
     * Gets the value of the shortestroute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getSHORTESTROUTE() {
        return shortestroute;
    }

    /**
     * Sets the value of the shortestroute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setSHORTESTROUTE(String value) {
        this.shortestroute = value;
    }

    /**
     * Gets the value of the shortestroutemix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getSHORTESTROUTEMIX() {
        return shortestroutemix;
    }

    /**
     * Sets the value of the shortestroutemix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setSHORTESTROUTEMIX(String value) {
        this.shortestroutemix = value;
    }

    /**
     * Gets the value of the roomchecker property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getROOMCHECKER() {
        return roomchecker;
    }

    /**
     * Sets the value of the roomchecker property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setROOMCHECKER(String value) {
        this.roomchecker = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getLINK() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setLINK(String value) {
        this.link = value;
    }

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setLANE(String value) {
        this.lane = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getDIRECTION() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setDIRECTION(String value) {
        this.direction = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setPOSITION(LengthBeginEnd value) {
        this.position = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public String getURI() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-11-11T03:39:39+01:00", comments = "JAXB RI v2.3.0")
    public void setURI(String value) {
        this.uri = value;
    }

}
