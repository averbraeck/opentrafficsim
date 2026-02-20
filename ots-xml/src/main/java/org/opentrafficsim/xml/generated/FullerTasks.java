
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.OnOffAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;


/**
 * Tasks are sources of mental task load that the mental model has to spent
 *         mental capacity on. If capacity is insufficient anticipation is applied.
 * 
 * <p>Java class for FullerTasks complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FullerTasks">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="CarFollowing" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *       <attribute name="LaneChanging" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *       <attribute name="RoadSideDistraction" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FullerTasks")
@XmlSeeAlso({
    FullerTasksSummativeAndAr.class,
    org.opentrafficsim.xml.generated.FullerAttentionMatrix.Tasks.class
})
@SuppressWarnings("all") public class FullerTasks
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "CarFollowing")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType carFollowing;
    @XmlAttribute(name = "LaneChanging")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType laneChanging;
    @XmlAttribute(name = "RoadSideDistraction")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType roadSideDistraction;

    /**
     * Gets the value of the carFollowing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getCarFollowing() {
        return carFollowing;
    }

    /**
     * Sets the value of the carFollowing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCarFollowing(BooleanType value) {
        this.carFollowing = value;
    }

    /**
     * Gets the value of the laneChanging property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getLaneChanging() {
        return laneChanging;
    }

    /**
     * Sets the value of the laneChanging property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaneChanging(BooleanType value) {
        this.laneChanging = value;
    }

    /**
     * Gets the value of the roadSideDistraction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getRoadSideDistraction() {
        return roadSideDistraction;
    }

    /**
     * Sets the value of the roadSideDistraction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoadSideDistraction(BooleanType value) {
        this.roadSideDistraction = value;
    }

}
