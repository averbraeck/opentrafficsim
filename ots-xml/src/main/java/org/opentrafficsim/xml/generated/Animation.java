
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuColorers" minOccurs="0"/>
 *         <element name="Defaults" type="{http://www.opentrafficsim.org/ots}DefaultAnimationType" minOccurs="0"/>
 *         <element name="LinkType" type="{http://www.opentrafficsim.org/ots}LinkTypeAnimationType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="LaneType" type="{http://www.opentrafficsim.org/ots}LaneTypeAnimationType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="RoadLayout" type="{http://www.opentrafficsim.org/ots}RoadLayoutAnimationType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="Connector" type="{http://www.opentrafficsim.org/ots}ConnectorAnimationType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="Link" type="{http://www.opentrafficsim.org/ots}LinkAnimationType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="Layer" type="{http://www.opentrafficsim.org/ots}LayerToggleType" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
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
@SuppressWarnings("all") public class Animation
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "GtuColorers")
    protected GtuColorers gtuColorers;
    @XmlElement(name = "Defaults")
    protected DefaultAnimationType defaults;
    @XmlElement(name = "LinkType")
    protected List<LinkTypeAnimationType> linkType;
    @XmlElement(name = "LaneType")
    protected List<LaneTypeAnimationType> laneType;
    @XmlElement(name = "RoadLayout")
    protected List<RoadLayoutAnimationType> roadLayout;
    @XmlElement(name = "Connector")
    protected List<ConnectorAnimationType> connector;
    @XmlElement(name = "Link")
    protected List<LinkAnimationType> link;
    @XmlElement(name = "Layer")
    protected List<LayerToggleType> layer;

    /**
     * Gets the value of the gtuColorers property.
     * 
     * @return
     *     possible object is
     *     {@link GtuColorers }
     *     
     */
    public GtuColorers getGtuColorers() {
        return gtuColorers;
    }

    /**
     * Sets the value of the gtuColorers property.
     * 
     * @param value
     *     allowed object is
     *     {@link GtuColorers }
     *     
     */
    public void setGtuColorers(GtuColorers value) {
        this.gtuColorers = value;
    }

    /**
     * Gets the value of the defaults property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultAnimationType }
     *     
     */
    public DefaultAnimationType getDefaults() {
        return defaults;
    }

    /**
     * Sets the value of the defaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultAnimationType }
     *     
     */
    public void setDefaults(DefaultAnimationType value) {
        this.defaults = value;
    }

    /**
     * Gets the value of the linkType property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkType property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLinkType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkTypeAnimationType }
     * </p>
     * 
     * 
     * @return
     *     The value of the linkType property.
     */
    public List<LinkTypeAnimationType> getLinkType() {
        if (linkType == null) {
            linkType = new ArrayList<>();
        }
        return this.linkType;
    }

    /**
     * Gets the value of the laneType property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneType property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLaneType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LaneTypeAnimationType }
     * </p>
     * 
     * 
     * @return
     *     The value of the laneType property.
     */
    public List<LaneTypeAnimationType> getLaneType() {
        if (laneType == null) {
            laneType = new ArrayList<>();
        }
        return this.laneType;
    }

    /**
     * Gets the value of the roadLayout property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roadLayout property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRoadLayout().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RoadLayoutAnimationType }
     * </p>
     * 
     * 
     * @return
     *     The value of the roadLayout property.
     */
    public List<RoadLayoutAnimationType> getRoadLayout() {
        if (roadLayout == null) {
            roadLayout = new ArrayList<>();
        }
        return this.roadLayout;
    }

    /**
     * Gets the value of the connector property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConnectorAnimationType }
     * </p>
     * 
     * 
     * @return
     *     The value of the connector property.
     */
    public List<ConnectorAnimationType> getConnector() {
        if (connector == null) {
            connector = new ArrayList<>();
        }
        return this.connector;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkAnimationType }
     * </p>
     * 
     * 
     * @return
     *     The value of the link property.
     */
    public List<LinkAnimationType> getLink() {
        if (link == null) {
            link = new ArrayList<>();
        }
        return this.link;
    }

    /**
     * Gets the value of the layer property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the layer property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLayer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LayerToggleType }
     * </p>
     * 
     * 
     * @return
     *     The value of the layer property.
     */
    public List<LayerToggleType> getLayer() {
        if (layer == null) {
            layer = new ArrayList<>();
        }
        return this.layer;
    }

}
