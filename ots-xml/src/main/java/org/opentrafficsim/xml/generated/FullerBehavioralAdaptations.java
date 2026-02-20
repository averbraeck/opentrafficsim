
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
 * Behavioral adaptations respond to a high mental task load by changing the
 *         situation to achieve lower task demand.
 * 
 * <p>Java class for FullerBehavioralAdaptations complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FullerBehavioralAdaptations">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *       <attribute name="Headway" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *       <attribute name="LaneChange" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FullerBehavioralAdaptations")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.FullerAttentionMatrix.BehavioralAdaptations.class
})
@SuppressWarnings("all") public class FullerBehavioralAdaptations
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Lower desired speed to reduce task demand.
     * 
     */
    @XmlAttribute(name = "Speed")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType speed;
    /**
     * Increase desired headway to reduce task demand.
     * 
     */
    @XmlAttribute(name = "Headway")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType headway;
    /**
     * Lower voluntary lane change desire to reduce task demand.
     * 
     */
    @XmlAttribute(name = "LaneChange")
    @XmlJavaTypeAdapter(OnOffAdapter.class)
    protected BooleanType laneChange;

    /**
     * Lower desired speed to reduce task demand.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getSpeed() {
        return speed;
    }

    /**
     * Sets the value of the speed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSpeed()
     */
    public void setSpeed(BooleanType value) {
        this.speed = value;
    }

    /**
     * Increase desired headway to reduce task demand.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getHeadway() {
        return headway;
    }

    /**
     * Sets the value of the headway property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getHeadway()
     */
    public void setHeadway(BooleanType value) {
        this.headway = value;
    }

    /**
     * Lower voluntary lane change desire to reduce task demand.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getLaneChange() {
        return laneChange;
    }

    /**
     * Sets the value of the laneChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLaneChange()
     */
    public void setLaneChange(BooleanType value) {
        this.laneChange = value;
    }

}
