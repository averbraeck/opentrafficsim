
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SpeedLimitInfraType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="SpeedLimitInfraType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}GtuCompatibleInfraType">
 *       <sequence>
 *         <choice minOccurs="0">
 *           <element name="SpeedLimit" type="{http://www.opentrafficsim.org/ots}SpeedLimit"/>
 *           <element name="TemporalSpeedLimit" type="{http://www.opentrafficsim.org/ots}TemporalSpeedLimit"/>
 *         </choice>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuTypeSpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpeedLimitInfraType", propOrder = {
    "speedLimit",
    "temporalSpeedLimit",
    "gtuTypeSpeedLimit"
})
@SuppressWarnings("all") public class SpeedLimitInfraType
    extends GtuCompatibleInfraType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "SpeedLimit")
    protected SpeedLimit speedLimit;
    @XmlElement(name = "TemporalSpeedLimit")
    protected TemporalSpeedLimit temporalSpeedLimit;
    @XmlElement(name = "GtuTypeSpeedLimit")
    protected List<GtuTypeSpeedLimit> gtuTypeSpeedLimit;

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
