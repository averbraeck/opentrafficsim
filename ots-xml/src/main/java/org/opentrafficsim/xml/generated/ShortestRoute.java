
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.FrequencyAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.FrequencyType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         <choice minOccurs="0">
 *           <element name="Distance" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *           <element name="FreeFlowTime" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *           <element name="DistanceAndFreeFlowTime">
 *             <complexType>
 *               <complexContent>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   <attribute name="DistanceCost" type="{http://www.opentrafficsim.org/ots}LinearDensityType" />
 *                   <attribute name="TimeCost" type="{http://www.opentrafficsim.org/ots}FrequencyType" />
 *                 </restriction>
 *               </complexContent>
 *             </complexType>
 *           </element>
 *         </choice>
 *         <element name="From" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Via" type="{http://www.opentrafficsim.org/ots}string" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="To" type="{http://www.opentrafficsim.org/ots}string"/>
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
    "distance",
    "freeFlowTime",
    "distanceAndFreeFlowTime",
    "from",
    "via",
    "to"
})
@XmlRootElement(name = "ShortestRoute")
@SuppressWarnings("all") public class ShortestRoute
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Distance")
    protected EmptyType distance;
    @XmlElement(name = "FreeFlowTime")
    protected EmptyType freeFlowTime;
    @XmlElement(name = "DistanceAndFreeFlowTime")
    protected ShortestRoute.DistanceAndFreeFlowTime distanceAndFreeFlowTime;
    @XmlElement(name = "From", required = true, type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType from;
    @XmlElement(name = "Via", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected List<StringType> via;
    @XmlElement(name = "To", required = true, type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType to;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;

    /**
     * Gets the value of the distance property.
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
     * Sets the value of the distance property.
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
     * Gets the value of the freeFlowTime property.
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
     * Sets the value of the freeFlowTime property.
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
     * Gets the value of the distanceAndFreeFlowTime property.
     * 
     * @return
     *     possible object is
     *     {@link ShortestRoute.DistanceAndFreeFlowTime }
     *     
     */
    public ShortestRoute.DistanceAndFreeFlowTime getDistanceAndFreeFlowTime() {
        return distanceAndFreeFlowTime;
    }

    /**
     * Sets the value of the distanceAndFreeFlowTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link ShortestRoute.DistanceAndFreeFlowTime }
     *     
     */
    public void setDistanceAndFreeFlowTime(ShortestRoute.DistanceAndFreeFlowTime value) {
        this.distanceAndFreeFlowTime = value;
    }

    /**
     * Gets the value of the from property.
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
     * Sets the value of the from property.
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
     * Gets the value of the to property.
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
     * Sets the value of the to property.
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
     * Gets the value of the id property.
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
     * Sets the value of the id property.
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
     * Gets the value of the gtuType property.
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
     * Sets the value of the gtuType property.
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
         * Gets the value of the distanceCost property.
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
         * Sets the value of the distanceCost property.
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
         * Gets the value of the timeCost property.
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
         * Sets the value of the timeCost property.
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
