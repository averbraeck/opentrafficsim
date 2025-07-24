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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LaneKeepingPolicyAdapter;
import org.opentrafficsim.xml.bindings.types.LaneKeepingPolicyType;


/**
 * <p>Java-Klasse für BasicRoadLayout complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BasicRoadLayout"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="Stripe" type="{http://www.opentrafficsim.org/ots}CseStripe" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Lane" type="{http://www.opentrafficsim.org/ots}CseLane" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Shoulder" type="{http://www.opentrafficsim.org/ots}CseShoulder" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="LaneKeeping" type="{http://www.opentrafficsim.org/ots}LaneKeepingType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class BasicRoadLayout
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Stripe", type = CseStripe.class),
        @XmlElement(name = "Lane", type = CseLane.class),
        @XmlElement(name = "Shoulder", type = CseShoulder.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Serializable> stripeOrLaneOrShoulder;
    @XmlElement(name = "SpeedLimit")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<SpeedLimit> speedLimit;
    @XmlAttribute(name = "LaneKeeping")
    @XmlJavaTypeAdapter(LaneKeepingPolicyAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected LaneKeepingPolicyType laneKeeping;

    /**
     * Gets the value of the stripeOrLaneOrShoulder property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stripeOrLaneOrShoulder property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStripeOrLaneOrShoulder().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CseLane }
     * {@link CseShoulder }
     * {@link CseStripe }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Serializable> getStripeOrLaneOrShoulder() {
        if (stripeOrLaneOrShoulder == null) {
            stripeOrLaneOrShoulder = new ArrayList<Serializable>();
        }
        return this.stripeOrLaneOrShoulder;
    }

    /**
     * Gets the value of the speedLimit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speedLimit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpeedLimit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpeedLimit }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<SpeedLimit> getSpeedLimit() {
        if (speedLimit == null) {
            speedLimit = new ArrayList<SpeedLimit>();
        }
        return this.speedLimit;
    }

    /**
     * Ruft den Wert der laneKeeping-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public LaneKeepingPolicyType getLaneKeeping() {
        return laneKeeping;
    }

    /**
     * Legt den Wert der laneKeeping-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLaneKeeping(LaneKeepingPolicyType value) {
        this.laneKeeping = value;
    }

}
