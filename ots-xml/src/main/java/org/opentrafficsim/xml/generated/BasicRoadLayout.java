
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LaneKeepingPolicyAdapter;
import org.opentrafficsim.xml.bindings.types.LaneKeepingPolicyType;


/**
 * <p>Java class for BasicRoadLayout complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="BasicRoadLayout">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice maxOccurs="unbounded">
 *           <element name="Stripe" type="{http://www.opentrafficsim.org/ots}CseStripe" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Lane" type="{http://www.opentrafficsim.org/ots}CseLane" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Shoulder" type="{http://www.opentrafficsim.org/ots}CseShoulder" maxOccurs="unbounded" minOccurs="0"/>
 *         </choice>
 *         <element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="LaneKeeping" type="{http://www.opentrafficsim.org/ots}LaneKeepingType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BasicRoadLayout", propOrder = {
    "stripeOrLaneOrShoulder",
    "speedLimit"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.Link.RoadLayout.class,
    org.opentrafficsim.xml.generated.RoadLayout.class
})
@SuppressWarnings("all") public class BasicRoadLayout
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Stripe", type = CseStripe.class),
        @XmlElement(name = "Lane", type = CseLane.class),
        @XmlElement(name = "Shoulder", type = CseShoulder.class)
    })
    protected List<Serializable> stripeOrLaneOrShoulder;
    @XmlElement(name = "SpeedLimit")
    protected List<SpeedLimit> speedLimit;
    @XmlAttribute(name = "LaneKeeping")
    @XmlJavaTypeAdapter(LaneKeepingPolicyAdapter.class)
    protected LaneKeepingPolicyType laneKeeping;

    /**
     * Gets the value of the stripeOrLaneOrShoulder property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stripeOrLaneOrShoulder property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getStripeOrLaneOrShoulder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CseLane }
     * {@link CseShoulder }
     * {@link CseStripe }
     * </p>
     * 
     * 
     * @return
     *     The value of the stripeOrLaneOrShoulder property.
     */
    public List<Serializable> getStripeOrLaneOrShoulder() {
        if (stripeOrLaneOrShoulder == null) {
            stripeOrLaneOrShoulder = new ArrayList<>();
        }
        return this.stripeOrLaneOrShoulder;
    }

    /**
     * Gets the value of the speedLimit property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speedLimit property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSpeedLimit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpeedLimit }
     * </p>
     * 
     * 
     * @return
     *     The value of the speedLimit property.
     */
    public List<SpeedLimit> getSpeedLimit() {
        if (speedLimit == null) {
            speedLimit = new ArrayList<>();
        }
        return this.speedLimit;
    }

    /**
     * Gets the value of the laneKeeping property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LaneKeepingPolicyType getLaneKeeping() {
        return laneKeeping;
    }

    /**
     * Sets the value of the laneKeeping property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaneKeeping(LaneKeepingPolicyType value) {
        this.laneKeeping = value;
    }

}
