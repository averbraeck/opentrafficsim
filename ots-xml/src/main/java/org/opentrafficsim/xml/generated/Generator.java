
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.FrequencyAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.FrequencyType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.StringType;


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
 *         <choice>
 *           <element name="GtuTemplate" type="{http://www.opentrafficsim.org/ots}string"/>
 *           <element name="GtuTemplateMix" type="{http://www.opentrafficsim.org/ots}string"/>
 *         </choice>
 *         <choice>
 *           <element name="Route" type="{http://www.opentrafficsim.org/ots}string"/>
 *           <element name="RouteMix" type="{http://www.opentrafficsim.org/ots}string"/>
 *           <element name="ShortestRoute" type="{http://www.opentrafficsim.org/ots}string"/>
 *           <element name="ShortestRouteMix" type="{http://www.opentrafficsim.org/ots}string"/>
 *         </choice>
 *         <element name="Frequency" type="{http://www.opentrafficsim.org/ots}FrequencyType"/>
 *         <element name="RoomChecker" type="{http://www.opentrafficsim.org/ots}RoomCheckerType" minOccurs="0"/>
 *         <element name="RandomStream" type="{http://www.opentrafficsim.org/ots}RandomStreamSource" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Link" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Position" use="required" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType" />
 *       <attribute name="NoLaneChangeDistance" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gtuTemplate",
    "gtuTemplateMix",
    "route",
    "routeMix",
    "shortestRoute",
    "shortestRouteMix",
    "frequency",
    "roomChecker",
    "randomStream"
})
@XmlRootElement(name = "Generator")
@SuppressWarnings("all") public class Generator
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "GtuTemplate", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuTemplate;
    @XmlElement(name = "GtuTemplateMix", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuTemplateMix;
    @XmlElement(name = "Route", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType route;
    @XmlElement(name = "RouteMix", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType routeMix;
    @XmlElement(name = "ShortestRoute", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType shortestRoute;
    @XmlElement(name = "ShortestRouteMix", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType shortestRouteMix;
    @XmlElement(name = "Frequency", required = true, type = String.class)
    @XmlJavaTypeAdapter(FrequencyAdapter.class)
    protected FrequencyType frequency;
    @XmlElement(name = "RoomChecker")
    protected RoomCheckerType roomChecker;
    @XmlElement(name = "RandomStream")
    protected RandomStreamSource randomStream;
    @XmlAttribute(name = "Link", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType link;
    @XmlAttribute(name = "Lane", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType lane;
    @XmlAttribute(name = "Position", required = true)
    @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
    protected LengthBeginEndType position;
    /**
     * Length over which GTUs are not allowed to change lane after being
     *             generated, to avoid interference with generation on adjacent lanes. If no value is specified and there are adjacent
     *             lanes, 50m will be used.
     * 
     */
    @XmlAttribute(name = "NoLaneChangeDistance")
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    protected LengthType noLaneChangeDistance;

    /**
     * Gets the value of the gtuTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getGtuTemplate() {
        return gtuTemplate;
    }

    /**
     * Sets the value of the gtuTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGtuTemplate(StringType value) {
        this.gtuTemplate = value;
    }

    /**
     * Gets the value of the gtuTemplateMix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getGtuTemplateMix() {
        return gtuTemplateMix;
    }

    /**
     * Sets the value of the gtuTemplateMix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGtuTemplateMix(StringType value) {
        this.gtuTemplateMix = value;
    }

    /**
     * Gets the value of the route property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getRoute() {
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
    public void setRoute(StringType value) {
        this.route = value;
    }

    /**
     * Gets the value of the routeMix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getRouteMix() {
        return routeMix;
    }

    /**
     * Sets the value of the routeMix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRouteMix(StringType value) {
        this.routeMix = value;
    }

    /**
     * Gets the value of the shortestRoute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getShortestRoute() {
        return shortestRoute;
    }

    /**
     * Sets the value of the shortestRoute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortestRoute(StringType value) {
        this.shortestRoute = value;
    }

    /**
     * Gets the value of the shortestRouteMix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getShortestRouteMix() {
        return shortestRouteMix;
    }

    /**
     * Sets the value of the shortestRouteMix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortestRouteMix(StringType value) {
        this.shortestRouteMix = value;
    }

    /**
     * Gets the value of the frequency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public FrequencyType getFrequency() {
        return frequency;
    }

    /**
     * Sets the value of the frequency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrequency(FrequencyType value) {
        this.frequency = value;
    }

    /**
     * Gets the value of the roomChecker property.
     * 
     * @return
     *     possible object is
     *     {@link RoomCheckerType }
     *     
     */
    public RoomCheckerType getRoomChecker() {
        return roomChecker;
    }

    /**
     * Sets the value of the roomChecker property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoomCheckerType }
     *     
     */
    public void setRoomChecker(RoomCheckerType value) {
        this.roomChecker = value;
    }

    /**
     * Gets the value of the randomStream property.
     * 
     * @return
     *     possible object is
     *     {@link RandomStreamSource }
     *     
     */
    public RandomStreamSource getRandomStream() {
        return randomStream;
    }

    /**
     * Sets the value of the randomStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomStreamSource }
     *     
     */
    public void setRandomStream(RandomStreamSource value) {
        this.randomStream = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLink() {
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
    public void setLink(StringType value) {
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
    public StringType getLane() {
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
    public void setLane(StringType value) {
        this.lane = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthBeginEndType getPosition() {
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
    public void setPosition(LengthBeginEndType value) {
        this.position = value;
    }

    /**
     * Length over which GTUs are not allowed to change lane after being
     *             generated, to avoid interference with generation on adjacent lanes. If no value is specified and there are adjacent
     *             lanes, 50m will be used.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthType getNoLaneChangeDistance() {
        return noLaneChangeDistance;
    }

    /**
     * Sets the value of the noLaneChangeDistance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getNoLaneChangeDistance()
     */
    public void setNoLaneChangeDistance(LengthType value) {
        this.noLaneChangeDistance = value;
    }

}
