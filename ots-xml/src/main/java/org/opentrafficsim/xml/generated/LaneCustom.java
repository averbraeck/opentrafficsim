
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LaneCustom complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LaneCustom">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}Compatibility" maxOccurs="unbounded" minOccurs="0"/>
 *         <choice minOccurs="0">
 *           <element name="SpeedLimit" type="{http://www.opentrafficsim.org/ots}SpeedLimit"/>
 *           <element name="TemporalSpeedLimit" type="{http://www.opentrafficsim.org/ots}TemporalSpeedLimit"/>
 *         </choice>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuTypeSpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LaneCustom", propOrder = {
    "compatibility",
    "speedLimit",
    "temporalSpeedLimit",
    "gtuTypeSpeedLimit"
})
@SuppressWarnings("all") public class LaneCustom
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Compatibility")
    protected List<Compatibility> compatibility;
    @XmlElement(name = "SpeedLimit")
    protected SpeedLimit speedLimit;
    @XmlElement(name = "TemporalSpeedLimit")
    protected TemporalSpeedLimit temporalSpeedLimit;
    @XmlElement(name = "GtuTypeSpeedLimit")
    protected List<GtuTypeSpeedLimit> gtuTypeSpeedLimit;

    /**
     * Gets the value of the compatibility property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compatibility property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCompatibility().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Compatibility }
     * </p>
     * 
     * 
     * @return
     *     The value of the compatibility property.
     */
    public List<Compatibility> getCompatibility() {
        if (compatibility == null) {
            compatibility = new ArrayList<>();
        }
        return this.compatibility;
    }

    /**
     * Gets the value of the speedLimit property.
     * 
     * @return
     *     possible object is
     *     {@link SpeedLimit }
     *     
     */
    public SpeedLimit getSpeedLimit() {
        return speedLimit;
    }

    /**
     * Sets the value of the speedLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpeedLimit }
     *     
     */
    public void setSpeedLimit(SpeedLimit value) {
        this.speedLimit = value;
    }

    /**
     * Gets the value of the temporalSpeedLimit property.
     * 
     * @return
     *     possible object is
     *     {@link TemporalSpeedLimit }
     *     
     */
    public TemporalSpeedLimit getTemporalSpeedLimit() {
        return temporalSpeedLimit;
    }

    /**
     * Sets the value of the temporalSpeedLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemporalSpeedLimit }
     *     
     */
    public void setTemporalSpeedLimit(TemporalSpeedLimit value) {
        this.temporalSpeedLimit = value;
    }

    /**
     * Gets the value of the gtuTypeSpeedLimit property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gtuTypeSpeedLimit property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGtuTypeSpeedLimit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GtuTypeSpeedLimit }
     * </p>
     * 
     * 
     * @return
     *     The value of the gtuTypeSpeedLimit property.
     */
    public List<GtuTypeSpeedLimit> getGtuTypeSpeedLimit() {
        if (gtuTypeSpeedLimit == null) {
            gtuTypeSpeedLimit = new ArrayList<>();
        }
        return this.gtuTypeSpeedLimit;
    }

}
