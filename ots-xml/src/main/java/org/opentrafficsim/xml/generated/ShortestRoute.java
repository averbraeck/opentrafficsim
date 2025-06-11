
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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.FrequencyAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.FrequencyType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
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
 *         <element name="From" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Via" type="{http://www.opentrafficsim.org/ots}string" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="To" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Cost" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Distance" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                   <element name="FreeFlowTime" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                   <element name="DistanceAndFreeFlowTime">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <attribute name="DistanceCost" type="{http://www.opentrafficsim.org/ots}LinearDensityType" />
 *                           <attribute name="TimeCost" type="{http://www.opentrafficsim.org/ots}FrequencyType" />
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "from",
    "via",
    "to",
    "cost"
})
@XmlRootElement(name = "ShortestRoute")
@SuppressWarnings("all") public class ShortestRoute
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "From", required = true, type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType from;
    @XmlElement(name = "Via", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected List<StringType> via;
    @XmlElement(name = "To", required = true, type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType to;
    @XmlElement(name = "Cost")
    protected ShortestRoute.Cost cost;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;

    /**
     * Ruft den Wert der from-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getFrom() {
        return from;
    }

    /**
     * Legt den Wert der from-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(StringType value) {
        this.from = value;
    }

    /**
     * Gets the value of the via property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the via property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getVia().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * </p>
     * 
     * 
     * @return
     *     The value of the via property.
     */
    public List<StringType> getVia() {
        if (via == null) {
            via = new ArrayList<>();
        }
        return this.via;
    }

    /**
     * Ruft den Wert der to-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getTo() {
        return to;
    }

    /**
     * Legt den Wert der to-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(StringType value) {
        this.to = value;
    }

    /**
     * Ruft den Wert der cost-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ShortestRoute.Cost }
     *     
     */
    public ShortestRoute.Cost getCost() {
        return cost;
    }

    /**
     * Legt den Wert der cost-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ShortestRoute.Cost }
     *     
     */
    public void setCost(ShortestRoute.Cost value) {
        this.cost = value;
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
     * <p>Java-Klasse für anonymous complex type.</p>
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <choice>
     *         <element name="Distance" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *         <element name="FreeFlowTime" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *         <element name="DistanceAndFreeFlowTime">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <attribute name="DistanceCost" type="{http://www.opentrafficsim.org/ots}LinearDensityType" />
     *                 <attribute name="TimeCost" type="{http://www.opentrafficsim.org/ots}FrequencyType" />
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *       </choice>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "distance",
        "freeFlowTime",
        "distanceAndFreeFlowTime"
    })
    public static class Cost
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Distance")
        protected EmptyType distance;
        @XmlElement(name = "FreeFlowTime")
        protected EmptyType freeFlowTime;
        @XmlElement(name = "DistanceAndFreeFlowTime")
        protected ShortestRoute.Cost.DistanceAndFreeFlowTime distanceAndFreeFlowTime;

        /**
         * Ruft den Wert der distance-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getDistance() {
            return distance;
        }

        /**
         * Legt den Wert der distance-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setDistance(EmptyType value) {
            this.distance = value;
        }

        /**
         * Ruft den Wert der freeFlowTime-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getFreeFlowTime() {
            return freeFlowTime;
        }

        /**
         * Legt den Wert der freeFlowTime-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setFreeFlowTime(EmptyType value) {
            this.freeFlowTime = value;
        }

        /**
         * Ruft den Wert der distanceAndFreeFlowTime-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ShortestRoute.Cost.DistanceAndFreeFlowTime }
         *     
         */
        public ShortestRoute.Cost.DistanceAndFreeFlowTime getDistanceAndFreeFlowTime() {
            return distanceAndFreeFlowTime;
        }

        /**
         * Legt den Wert der distanceAndFreeFlowTime-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ShortestRoute.Cost.DistanceAndFreeFlowTime }
         *     
         */
        public void setDistanceAndFreeFlowTime(ShortestRoute.Cost.DistanceAndFreeFlowTime value) {
            this.distanceAndFreeFlowTime = value;
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
         *       <attribute name="DistanceCost" type="{http://www.opentrafficsim.org/ots}LinearDensityType" />
         *       <attribute name="TimeCost" type="{http://www.opentrafficsim.org/ots}FrequencyType" />
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class DistanceAndFreeFlowTime
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlAttribute(name = "DistanceCost")
            @XmlJavaTypeAdapter(LinearDensityAdapter.class)
            protected LinearDensityType distanceCost;
            @XmlAttribute(name = "TimeCost")
            @XmlJavaTypeAdapter(FrequencyAdapter.class)
            protected FrequencyType timeCost;

            /**
             * Ruft den Wert der distanceCost-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public LinearDensityType getDistanceCost() {
                return distanceCost;
            }

            /**
             * Legt den Wert der distanceCost-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDistanceCost(LinearDensityType value) {
                this.distanceCost = value;
            }

            /**
             * Ruft den Wert der timeCost-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public FrequencyType getTimeCost() {
                return timeCost;
            }

            /**
             * Legt den Wert der timeCost-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTimeCost(FrequencyType value) {
                this.timeCost = value;
            }

        }

    }

}
