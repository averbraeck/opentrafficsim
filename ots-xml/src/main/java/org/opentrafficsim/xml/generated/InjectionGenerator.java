
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AccelerationAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.PositiveDurationAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.AccelerationType;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Position" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Link" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Position" use="required" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="GtuCharacteristics" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <choice>
 *                     <element name="GtuTemplate" type="{http://www.opentrafficsim.org/ots}string"/>
 *                     <element name="GtuTemplateMix" type="{http://www.opentrafficsim.org/ots}string"/>
 *                   </choice>
 *                   <choice>
 *                     <element name="Route" type="{http://www.opentrafficsim.org/ots}string"/>
 *                     <element name="RouteMix" type="{http://www.opentrafficsim.org/ots}string"/>
 *                     <element name="ShortestRoute" type="{http://www.opentrafficsim.org/ots}string"/>
 *                     <element name="ShortestRouteMix" type="{http://www.opentrafficsim.org/ots}string"/>
 *                   </choice>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <choice>
 *           <element name="RoomChecker" type="{http://www.opentrafficsim.org/ots}RoomCheckerType"/>
 *           <element name="TimeToCollision" type="{http://www.opentrafficsim.org/ots}PositiveDurationType"/>
 *         </choice>
 *         <element name="RandomStream" type="{http://www.opentrafficsim.org/ots}RandomStreamSource" minOccurs="0"/>
 *         <element name="Arrivals">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="Arrival" maxOccurs="unbounded" minOccurs="0">
 *                     <complexType>
 *                       <simpleContent>
 *                         <extension base="<http://www.opentrafficsim.org/ots>PositiveDurationType">
 *                           <attribute name="Id" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="GtuType" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}PositiveSpeedType" />
 *                           <attribute name="Link" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Lane" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Position" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                           <attribute name="Length" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                           <attribute name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                           <attribute name="Front" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *                           <attribute name="MaxSpeed" type="{http://www.opentrafficsim.org/ots}PositiveSpeedType" />
 *                           <attribute name="MaxAcceleration" type="{http://www.opentrafficsim.org/ots}PositiveAccelerationType" />
 *                           <attribute name="MaxDeceleration" type="{http://www.opentrafficsim.org/ots}PositiveAccelerationType" />
 *                           <attribute name="Route" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Origin" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Destination" type="{http://www.opentrafficsim.org/ots}string" />
 *                         </extension>
 *                       </simpleContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *                 <attribute name="Uri" type="{http://www.opentrafficsim.org/ots}anyURI" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
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
    "position",
    "gtuCharacteristics",
    "roomChecker",
    "timeToCollision",
    "randomStream",
    "arrivals"
})
@XmlRootElement(name = "InjectionGenerator")
@SuppressWarnings("all") public class InjectionGenerator
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Provide a Position if there is no Link, Lane or Position (on lane)
     *               data.
     * 
     */
    @XmlElement(name = "Position")
    protected InjectionGenerator.Position position;
    /**
     * Provide Gtu Characteristics if there is no data on GTU size, speed,
     *               acceleration and route.
     * 
     */
    @XmlElement(name = "GtuCharacteristics")
    protected InjectionGenerator.GtuCharacteristics gtuCharacteristics;
    /**
     * Provide a Room Checker if there is no speed in the data.
     * 
     */
    @XmlElement(name = "RoomChecker")
    protected RoomCheckerType roomChecker;
    /**
     * Provide a Time To Collision if there is speed in the data.
     * 
     */
    @XmlElement(name = "TimeToCollision", type = String.class)
    @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
    protected DurationType timeToCollision;
    @XmlElement(name = "RandomStream")
    protected RandomStreamSource randomStream;
    /**
     * Provide link to arrivals file (csv with csv.header) or specify Arrivals.
     * 
     */
    @XmlElement(name = "Arrivals", required = true)
    protected InjectionGenerator.Arrivals arrivals;

    /**
     * Provide a Position if there is no Link, Lane or Position (on lane)
     *               data.
     * 
     * @return
     *     possible object is
     *     {@link InjectionGenerator.Position }
     *     
     */
    public InjectionGenerator.Position getPosition() {
        return position;
    }

    /**
     * Legt den Wert der position-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link InjectionGenerator.Position }
     *     
     * @see #getPosition()
     */
    public void setPosition(InjectionGenerator.Position value) {
        this.position = value;
    }

    /**
     * Provide Gtu Characteristics if there is no data on GTU size, speed,
     *               acceleration and route.
     * 
     * @return
     *     possible object is
     *     {@link InjectionGenerator.GtuCharacteristics }
     *     
     */
    public InjectionGenerator.GtuCharacteristics getGtuCharacteristics() {
        return gtuCharacteristics;
    }

    /**
     * Legt den Wert der gtuCharacteristics-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link InjectionGenerator.GtuCharacteristics }
     *     
     * @see #getGtuCharacteristics()
     */
    public void setGtuCharacteristics(InjectionGenerator.GtuCharacteristics value) {
        this.gtuCharacteristics = value;
    }

    /**
     * Provide a Room Checker if there is no speed in the data.
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
     * Legt den Wert der roomChecker-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RoomCheckerType }
     *     
     * @see #getRoomChecker()
     */
    public void setRoomChecker(RoomCheckerType value) {
        this.roomChecker = value;
    }

    /**
     * Provide a Time To Collision if there is speed in the data.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationType getTimeToCollision() {
        return timeToCollision;
    }

    /**
     * Legt den Wert der timeToCollision-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getTimeToCollision()
     */
    public void setTimeToCollision(DurationType value) {
        this.timeToCollision = value;
    }

    /**
     * Ruft den Wert der randomStream-Eigenschaft ab.
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
     * Legt den Wert der randomStream-Eigenschaft fest.
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
     * Provide link to arrivals file (csv with csv.header) or specify Arrivals.
     * 
     * @return
     *     possible object is
     *     {@link InjectionGenerator.Arrivals }
     *     
     */
    public InjectionGenerator.Arrivals getArrivals() {
        return arrivals;
    }

    /**
     * Legt den Wert der arrivals-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link InjectionGenerator.Arrivals }
     *     
     * @see #getArrivals()
     */
    public void setArrivals(InjectionGenerator.Arrivals value) {
        this.arrivals = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element name="Arrival" maxOccurs="unbounded" minOccurs="0">
     *           <complexType>
     *             <simpleContent>
     *               <extension base="<http://www.opentrafficsim.org/ots>PositiveDurationType">
     *                 <attribute name="Id" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="GtuType" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}PositiveSpeedType" />
     *                 <attribute name="Link" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Lane" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Position" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *                 <attribute name="Length" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *                 <attribute name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *                 <attribute name="Front" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *                 <attribute name="MaxSpeed" type="{http://www.opentrafficsim.org/ots}PositiveSpeedType" />
     *                 <attribute name="MaxAcceleration" type="{http://www.opentrafficsim.org/ots}PositiveAccelerationType" />
     *                 <attribute name="MaxDeceleration" type="{http://www.opentrafficsim.org/ots}PositiveAccelerationType" />
     *                 <attribute name="Route" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Origin" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Destination" type="{http://www.opentrafficsim.org/ots}string" />
     *               </extension>
     *             </simpleContent>
     *           </complexType>
     *         </element>
     *       </sequence>
     *       <attribute name="Uri" type="{http://www.opentrafficsim.org/ots}anyURI" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "arrival"
    })
    public static class Arrivals
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Arrival")
        protected List<InjectionGenerator.Arrivals.Arrival> arrival;
        @XmlAttribute(name = "Uri")
        protected String uri;

        /**
         * Gets the value of the arrival property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the arrival property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getArrival().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link InjectionGenerator.Arrivals.Arrival }
         * </p>
         * 
         * 
         * @return
         *     The value of the arrival property.
         */
        public List<InjectionGenerator.Arrivals.Arrival> getArrival() {
            if (arrival == null) {
                arrival = new ArrayList<>();
            }
            return this.arrival;
        }

        /**
         * Ruft den Wert der uri-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUri() {
            return uri;
        }

        /**
         * Legt den Wert der uri-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUri(String value) {
            this.uri = value;
        }


        /**
         * The value column is the arrival time.
         * 
         * <p>Java-Klasse für anonymous complex type.</p>
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
         * 
         * <pre>{@code
         * <complexType>
         *   <simpleContent>
         *     <extension base="<http://www.opentrafficsim.org/ots>PositiveDurationType">
         *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="GtuType" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="Speed" type="{http://www.opentrafficsim.org/ots}PositiveSpeedType" />
         *       <attribute name="Link" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="Lane" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="Position" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
         *       <attribute name="Length" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
         *       <attribute name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
         *       <attribute name="Front" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
         *       <attribute name="MaxSpeed" type="{http://www.opentrafficsim.org/ots}PositiveSpeedType" />
         *       <attribute name="MaxAcceleration" type="{http://www.opentrafficsim.org/ots}PositiveAccelerationType" />
         *       <attribute name="MaxDeceleration" type="{http://www.opentrafficsim.org/ots}PositiveAccelerationType" />
         *       <attribute name="Route" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="Origin" type="{http://www.opentrafficsim.org/ots}string" />
         *       <attribute name="Destination" type="{http://www.opentrafficsim.org/ots}string" />
         *     </extension>
         *   </simpleContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class Arrival
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
            protected DurationType value;
            @XmlAttribute(name = "Id")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType id;
            @XmlAttribute(name = "GtuType")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType gtuType;
            @XmlAttribute(name = "Speed")
            @XmlJavaTypeAdapter(SpeedAdapter.class)
            protected SpeedType speed;
            @XmlAttribute(name = "Link")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType link;
            @XmlAttribute(name = "Lane")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType lane;
            @XmlAttribute(name = "Position")
            @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
            protected LengthType position;
            @XmlAttribute(name = "Length")
            @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
            protected LengthType length;
            @XmlAttribute(name = "Width")
            @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
            protected LengthType width;
            @XmlAttribute(name = "Front")
            @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
            protected LengthType front;
            @XmlAttribute(name = "MaxSpeed")
            @XmlJavaTypeAdapter(SpeedAdapter.class)
            protected SpeedType maxSpeed;
            @XmlAttribute(name = "MaxAcceleration")
            @XmlJavaTypeAdapter(AccelerationAdapter.class)
            protected AccelerationType maxAcceleration;
            @XmlAttribute(name = "MaxDeceleration")
            @XmlJavaTypeAdapter(AccelerationAdapter.class)
            protected AccelerationType maxDeceleration;
            @XmlAttribute(name = "Route")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType route;
            @XmlAttribute(name = "Origin")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType origin;
            @XmlAttribute(name = "Destination")
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType destination;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public DurationType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(DurationType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setId(StringType value) {
                this.id = value;
            }

            /**
             * Ruft den Wert der gtuType-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getGtuType() {
                return gtuType;
            }

            /**
             * Legt den Wert der gtuType-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setGtuType(StringType value) {
                this.gtuType = value;
            }

            /**
             * Ruft den Wert der speed-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public SpeedType getSpeed() {
                return speed;
            }

            /**
             * Legt den Wert der speed-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSpeed(SpeedType value) {
                this.speed = value;
            }

            /**
             * Ruft den Wert der link-Eigenschaft ab.
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
             * Legt den Wert der link-Eigenschaft fest.
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
             * Ruft den Wert der lane-Eigenschaft ab.
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
             * Legt den Wert der lane-Eigenschaft fest.
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
             * Ruft den Wert der position-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthType getPosition() {
                return position;
            }

            /**
             * Legt den Wert der position-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPosition(LengthType value) {
                this.position = value;
            }

            /**
             * Ruft den Wert der length-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthType getLength() {
                return length;
            }

            /**
             * Legt den Wert der length-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLength(LengthType value) {
                this.length = value;
            }

            /**
             * Ruft den Wert der width-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthType getWidth() {
                return width;
            }

            /**
             * Legt den Wert der width-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setWidth(LengthType value) {
                this.width = value;
            }

            /**
             * Ruft den Wert der front-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LengthType getFront() {
                return front;
            }

            /**
             * Legt den Wert der front-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFront(LengthType value) {
                this.front = value;
            }

            /**
             * Ruft den Wert der maxSpeed-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public SpeedType getMaxSpeed() {
                return maxSpeed;
            }

            /**
             * Legt den Wert der maxSpeed-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMaxSpeed(SpeedType value) {
                this.maxSpeed = value;
            }

            /**
             * Ruft den Wert der maxAcceleration-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public AccelerationType getMaxAcceleration() {
                return maxAcceleration;
            }

            /**
             * Legt den Wert der maxAcceleration-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMaxAcceleration(AccelerationType value) {
                this.maxAcceleration = value;
            }

            /**
             * Ruft den Wert der maxDeceleration-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public AccelerationType getMaxDeceleration() {
                return maxDeceleration;
            }

            /**
             * Legt den Wert der maxDeceleration-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMaxDeceleration(AccelerationType value) {
                this.maxDeceleration = value;
            }

            /**
             * Ruft den Wert der route-Eigenschaft ab.
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
             * Legt den Wert der route-Eigenschaft fest.
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
             * Ruft den Wert der origin-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getOrigin() {
                return origin;
            }

            /**
             * Legt den Wert der origin-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOrigin(StringType value) {
                this.origin = value;
            }

            /**
             * Ruft den Wert der destination-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getDestination() {
                return destination;
            }

            /**
             * Legt den Wert der destination-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDestination(StringType value) {
                this.destination = value;
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
        "gtuTemplate",
        "gtuTemplateMix",
        "route",
        "routeMix",
        "shortestRoute",
        "shortestRouteMix"
    })
    public static class GtuCharacteristics
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

        /**
         * Ruft den Wert der gtuTemplate-Eigenschaft ab.
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
         * Legt den Wert der gtuTemplate-Eigenschaft fest.
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
         * Ruft den Wert der gtuTemplateMix-Eigenschaft ab.
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
         * Legt den Wert der gtuTemplateMix-Eigenschaft fest.
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
         * Ruft den Wert der route-Eigenschaft ab.
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
         * Legt den Wert der route-Eigenschaft fest.
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
         * Ruft den Wert der routeMix-Eigenschaft ab.
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
         * Legt den Wert der routeMix-Eigenschaft fest.
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
         * Ruft den Wert der shortestRoute-Eigenschaft ab.
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
         * Legt den Wert der shortestRoute-Eigenschaft fest.
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
         * Ruft den Wert der shortestRouteMix-Eigenschaft ab.
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
         * Legt den Wert der shortestRouteMix-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setShortestRouteMix(StringType value) {
            this.shortestRouteMix = value;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Link" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Position" use="required" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Position
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
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
         * Ruft den Wert der link-Eigenschaft ab.
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
         * Legt den Wert der link-Eigenschaft fest.
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
         * Ruft den Wert der lane-Eigenschaft ab.
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
         * Legt den Wert der lane-Eigenschaft fest.
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
         * Ruft den Wert der position-Eigenschaft ab.
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
         * Legt den Wert der position-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPosition(LengthBeginEndType value) {
            this.position = value;
        }

    }

}
