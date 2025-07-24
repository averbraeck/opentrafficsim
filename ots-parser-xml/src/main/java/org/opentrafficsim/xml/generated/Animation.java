//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
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
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GtuColorers" minOccurs="0"/&gt;
 *         &lt;element name="Defaults" type="{http://www.opentrafficsim.org/ots}DefaultAnimationType" minOccurs="0"/&gt;
 *         &lt;element name="LinkType" type="{http://www.opentrafficsim.org/ots}LinkTypeAnimationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="LaneType" type="{http://www.opentrafficsim.org/ots}LaneTypeAnimationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="RoadLayout" type="{http://www.opentrafficsim.org/ots}RoadLayoutAnimationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Connector" type="{http://www.opentrafficsim.org/ots}ConnectorAnimationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Link" type="{http://www.opentrafficsim.org/ots}LinkAnimationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Layer" type="{http://www.opentrafficsim.org/ots}LayerToggleType" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "gtuColorers",
    "defaults",
    "linkType",
    "laneType",
    "roadLayout",
    "connector",
    "link",
    "layer"
})
@XmlRootElement(name = "Animation")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Animation
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "GtuColorers")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected GtuColorers gtuColorers;
    @XmlElement(name = "Defaults")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DefaultAnimationType defaults;
    @XmlElement(name = "LinkType")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<LinkTypeAnimationType> linkType;
    @XmlElement(name = "LaneType")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<LaneTypeAnimationType> laneType;
    @XmlElement(name = "RoadLayout")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<RoadLayoutAnimationType> roadLayout;
    @XmlElement(name = "Connector")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<ConnectorAnimationType> connector;
    @XmlElement(name = "Link")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<LinkAnimationType> link;
    @XmlElement(name = "Layer")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<LayerToggleType> layer;

    /**
     * Ruft den Wert der gtuColorers-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GtuColorers }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public GtuColorers getGtuColorers() {
        return gtuColorers;
    }

    /**
     * Legt den Wert der gtuColorers-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GtuColorers }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setGtuColorers(GtuColorers value) {
        this.gtuColorers = value;
    }

    /**
     * Ruft den Wert der defaults-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DefaultAnimationType getDefaults() {
        return defaults;
    }

    /**
     * Legt den Wert der defaults-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setDefaults(DefaultAnimationType value) {
        this.defaults = value;
    }

    /**
     * Gets the value of the linkType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkTypeAnimationType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<LinkTypeAnimationType> getLinkType() {
        if (linkType == null) {
            linkType = new ArrayList<LinkTypeAnimationType>();
        }
        return this.linkType;
    }

    /**
     * Gets the value of the laneType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLaneType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LaneTypeAnimationType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<LaneTypeAnimationType> getLaneType() {
        if (laneType == null) {
            laneType = new ArrayList<LaneTypeAnimationType>();
        }
        return this.laneType;
    }

    /**
     * Gets the value of the roadLayout property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roadLayout property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoadLayout().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RoadLayoutAnimationType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<RoadLayoutAnimationType> getRoadLayout() {
        if (roadLayout == null) {
            roadLayout = new ArrayList<RoadLayoutAnimationType>();
        }
        return this.roadLayout;
    }

    /**
     * Gets the value of the connector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConnectorAnimationType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<ConnectorAnimationType> getConnector() {
        if (connector == null) {
            connector = new ArrayList<ConnectorAnimationType>();
        }
        return this.connector;
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
     *    getLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkAnimationType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<LinkAnimationType> getLink() {
        if (link == null) {
            link = new ArrayList<LinkAnimationType>();
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
     *    getLayer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LayerToggleType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<LayerToggleType> getLayer() {
        if (layer == null) {
            layer = new ArrayList<LayerToggleType>();
        }
        return this.layer;
    }

}
