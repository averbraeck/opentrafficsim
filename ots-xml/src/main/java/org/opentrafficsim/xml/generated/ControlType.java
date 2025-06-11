
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für ControlType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="ControlType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="SignalGroup" maxOccurs="unbounded">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="TrafficLight" maxOccurs="unbounded">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <attribute name="Link" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                           <attribute name="TrafficLightId" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="StartTime" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="EndTime" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ControlType", propOrder = {
    "signalGroup"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.Control.FixedTime.class,
    ResponsiveControlType.class
})
@SuppressWarnings("all") public class ControlType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "SignalGroup", required = true)
    protected List<ControlType.SignalGroup> signalGroup;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "StartTime")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType startTime;
    @XmlAttribute(name = "EndTime")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType endTime;

    /**
     * Gets the value of the signalGroup property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signalGroup property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSignalGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ControlType.SignalGroup }
     * </p>
     * 
     * 
     * @return
     *     The value of the signalGroup property.
     */
    public List<ControlType.SignalGroup> getSignalGroup() {
        if (signalGroup == null) {
            signalGroup = new ArrayList<>();
        }
        return this.signalGroup;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
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
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der startTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getStartTime() {
        return startTime;
    }

    /**
     * Legt den Wert der startTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(StringType value) {
        this.startTime = value;
    }

    /**
     * Ruft den Wert der endTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getEndTime() {
        return endTime;
    }

    /**
     * Legt den Wert der endTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndTime(StringType value) {
        this.endTime = value;
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
     *         <element name="TrafficLight" maxOccurs="unbounded">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <attribute name="Link" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *                 <attribute name="TrafficLightId" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *       </sequence>
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "trafficLight"
    })
    public static class SignalGroup
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "TrafficLight", required = true)
        protected List<ControlType.SignalGroup.TrafficLight> trafficLight;
        @XmlAttribute(name = "Id", required = true)
        protected String id;

        /**
         * Gets the value of the trafficLight property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the trafficLight property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getTrafficLight().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ControlType.SignalGroup.TrafficLight }
         * </p>
         * 
         * 
         * @return
         *     The value of the trafficLight property.
         */
        public List<ControlType.SignalGroup.TrafficLight> getTrafficLight() {
            if (trafficLight == null) {
                trafficLight = new ArrayList<>();
            }
            return this.trafficLight;
        }

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
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
        public void setId(String value) {
            this.id = value;
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
         *       <attribute name="TrafficLightId" use="required" type="{http://www.opentrafficsim.org/ots}string" />
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class TrafficLight
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "Link", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType link;
            @XmlAttribute(name = "Lane", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType lane;
            @XmlAttribute(name = "TrafficLightId", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType trafficLightId;

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
             * Ruft den Wert der trafficLightId-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getTrafficLightId() {
                return trafficLightId;
            }

            /**
             * Legt den Wert der trafficLightId-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTrafficLightId(StringType value) {
                this.trafficLightId = value;
            }

        }

    }

}
