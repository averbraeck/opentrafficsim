
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * May replace the default validity with explicit validity information for an object. Multiple validity elements may be defined per object.
 * 
 * <p>Java-Klasse für t_road_objects_object_laneValidity complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_laneValidity">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="fromLane" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       <attribute name="toLane" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_laneValidity")
@SuppressWarnings("all") public class TRoadObjectsObjectLaneValidity
    extends OpenDriveElement
{

    /**
     * Minimum ID of the lanes for which the object is valid
     * 
     */
    @XmlAttribute(name = "fromLane", required = true)
    protected BigInteger fromLane;
    /**
     * Maximum ID of the lanes for which the object is valid
     * 
     */
    @XmlAttribute(name = "toLane", required = true)
    protected BigInteger toLane;

    /**
     * Minimum ID of the lanes for which the object is valid
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFromLane() {
        return fromLane;
    }

    /**
     * Legt den Wert der fromLane-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getFromLane()
     */
    public void setFromLane(BigInteger value) {
        this.fromLane = value;
    }

    /**
     * Maximum ID of the lanes for which the object is valid
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getToLane() {
        return toLane;
    }

    /**
     * Legt den Wert der toLane-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getToLane()
     */
    public void setToLane(BigInteger value) {
        this.toLane = value;
    }

}
