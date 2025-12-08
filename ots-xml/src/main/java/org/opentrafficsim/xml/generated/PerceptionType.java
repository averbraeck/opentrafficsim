
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.AnticipationAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.EstimationAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.AnticipationType;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.EstimationType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for PerceptionType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="PerceptionType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Categories" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <all minOccurs="0">
 *                   <element name="Ego" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                   <element name="Infrastructure" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                   <element name="Neighbors" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                   <element name="Intersection" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                   <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                   <element name="Traffic" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                 </all>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="HeadwayGtuType" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Wrap" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                   <element name="Perceived">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="Estimation">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="NONE"/>
 *                                       <enumeration value="UNDERESTIMATION"/>
 *                                       <enumeration value="OVERESTIMATION"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                             <element name="Anticipation">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="NONE"/>
 *                                       <enumeration value="CONSTANT_SPEED"/>
 *                                       <enumeration value="CONSTANT_ACCELERATION"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Mental" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Fuller">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="Task" type="{http://www.opentrafficsim.org/ots}ClassNameType" maxOccurs="unbounded" minOccurs="0"/>
 *                             <element name="BehavioralAdaptations" minOccurs="0">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <all minOccurs="0">
 *                                       <element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                       <element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
 *                                     </all>
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                             <element name="TaskManager" minOccurs="0">
 *                               <simpleType>
 *                                 <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *                                   <simpleType>
 *                                     <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                       <enumeration value="SUMMATIVE"/>
 *                                       <enumeration value="ANTICIPATION_RELIANCE"/>
 *                                     </restriction>
 *                                   </simpleType>
 *                                 </union>
 *                               </simpleType>
 *                             </element>
 *                           </sequence>
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
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerceptionType", propOrder = {
    "categories",
    "headwayGtuType",
    "mental"
})
@SuppressWarnings("all") public class PerceptionType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Class: category by class name, should have LanePerception as constructor
     *             parameter, and optionally a second HeadwayGtuType parameter. Ego: perception of own vehicle. BusStop: perceive bus
     *             stops for scheduled busses. Infrastructure: infrastructure. Intersection: conflicts and traffic lights. Neighbors:
     *             surrounding GTUs. Traffic: speed and density on lanes.
     * 
     */
    @XmlElement(name = "Categories")
    protected PerceptionType.Categories categories;
    /**
     * Wrap: perfect perception. Perceived: imperfect (delayed) perception with
     *             estimation and anticipation.
     * 
     */
    @XmlElement(name = "HeadwayGtuType")
    protected PerceptionType.HeadwayGtuType headwayGtuType;
    @XmlElement(name = "Mental")
    protected PerceptionType.Mental mental;

    /**
     * Class: category by class name, should have LanePerception as constructor
     *             parameter, and optionally a second HeadwayGtuType parameter. Ego: perception of own vehicle. BusStop: perceive bus
     *             stops for scheduled busses. Infrastructure: infrastructure. Intersection: conflicts and traffic lights. Neighbors:
     *             surrounding GTUs. Traffic: speed and density on lanes.
     * 
     * @return
     *     possible object is
     *     {@link PerceptionType.Categories }
     *     
     */
    public PerceptionType.Categories getCategories() {
        return categories;
    }

    /**
     * Sets the value of the categories property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerceptionType.Categories }
     *     
     * @see #getCategories()
     */
    public void setCategories(PerceptionType.Categories value) {
        this.categories = value;
    }

    /**
     * Wrap: perfect perception. Perceived: imperfect (delayed) perception with
     *             estimation and anticipation.
     * 
     * @return
     *     possible object is
     *     {@link PerceptionType.HeadwayGtuType }
     *     
     */
    public PerceptionType.HeadwayGtuType getHeadwayGtuType() {
        return headwayGtuType;
    }

    /**
     * Sets the value of the headwayGtuType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerceptionType.HeadwayGtuType }
     *     
     * @see #getHeadwayGtuType()
     */
    public void setHeadwayGtuType(PerceptionType.HeadwayGtuType value) {
        this.headwayGtuType = value;
    }

    /**
     * Gets the value of the mental property.
     * 
     * @return
     *     possible object is
     *     {@link PerceptionType.Mental }
     *     
     */
    public PerceptionType.Mental getMental() {
        return mental;
    }

    /**
     * Sets the value of the mental property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerceptionType.Mental }
     *     
     */
    public void setMental(PerceptionType.Mental value) {
        this.mental = value;
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
     *       <all minOccurs="0">
     *         <element name="Ego" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *         <element name="Infrastructure" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *         <element name="Neighbors" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *         <element name="Intersection" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *         <element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *         <element name="Traffic" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *       </all>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    public static class Categories
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Ego")
        protected EmptyType ego;
        @XmlElement(name = "Infrastructure")
        protected EmptyType infrastructure;
        @XmlElement(name = "Neighbors")
        protected EmptyType neighbors;
        @XmlElement(name = "Intersection")
        protected EmptyType intersection;
        @XmlElement(name = "BusStop")
        protected EmptyType busStop;
        @XmlElement(name = "Traffic")
        protected EmptyType traffic;

        /**
         * Gets the value of the ego property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getEgo() {
            return ego;
        }

        /**
         * Sets the value of the ego property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setEgo(EmptyType value) {
            this.ego = value;
        }

        /**
         * Gets the value of the infrastructure property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getInfrastructure() {
            return infrastructure;
        }

        /**
         * Sets the value of the infrastructure property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setInfrastructure(EmptyType value) {
            this.infrastructure = value;
        }

        /**
         * Gets the value of the neighbors property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getNeighbors() {
            return neighbors;
        }

        /**
         * Sets the value of the neighbors property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setNeighbors(EmptyType value) {
            this.neighbors = value;
        }

        /**
         * Gets the value of the intersection property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getIntersection() {
            return intersection;
        }

        /**
         * Sets the value of the intersection property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setIntersection(EmptyType value) {
            this.intersection = value;
        }

        /**
         * Gets the value of the busStop property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getBusStop() {
            return busStop;
        }

        /**
         * Sets the value of the busStop property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setBusStop(EmptyType value) {
            this.busStop = value;
        }

        /**
         * Gets the value of the traffic property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getTraffic() {
            return traffic;
        }

        /**
         * Sets the value of the traffic property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setTraffic(EmptyType value) {
            this.traffic = value;
        }

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
     *       <choice>
     *         <element name="Wrap" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *         <element name="Perceived">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="Estimation">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="NONE"/>
     *                             <enumeration value="UNDERESTIMATION"/>
     *                             <enumeration value="OVERESTIMATION"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                   <element name="Anticipation">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="NONE"/>
     *                             <enumeration value="CONSTANT_SPEED"/>
     *                             <enumeration value="CONSTANT_ACCELERATION"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                 </sequence>
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
        "wrap",
        "perceived"
    })
    public static class HeadwayGtuType
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Wrap")
        protected EmptyType wrap;
        @XmlElement(name = "Perceived")
        protected PerceptionType.HeadwayGtuType.Perceived perceived;

        /**
         * Gets the value of the wrap property.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        public EmptyType getWrap() {
            return wrap;
        }

        /**
         * Sets the value of the wrap property.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        public void setWrap(EmptyType value) {
            this.wrap = value;
        }

        /**
         * Gets the value of the perceived property.
         * 
         * @return
         *     possible object is
         *     {@link PerceptionType.HeadwayGtuType.Perceived }
         *     
         */
        public PerceptionType.HeadwayGtuType.Perceived getPerceived() {
            return perceived;
        }

        /**
         * Sets the value of the perceived property.
         * 
         * @param value
         *     allowed object is
         *     {@link PerceptionType.HeadwayGtuType.Perceived }
         *     
         */
        public void setPerceived(PerceptionType.HeadwayGtuType.Perceived value) {
            this.perceived = value;
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
         *       <sequence>
         *         <element name="Estimation">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="NONE"/>
         *                   <enumeration value="UNDERESTIMATION"/>
         *                   <enumeration value="OVERESTIMATION"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
         *         </element>
         *         <element name="Anticipation">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="NONE"/>
         *                   <enumeration value="CONSTANT_SPEED"/>
         *                   <enumeration value="CONSTANT_ACCELERATION"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
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
            "estimation",
            "anticipation"
        })
        public static class Perceived
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            /**
             * Estimation entails perfect (NONE) or under- or overestimation
             *                         (UNDERESTIMATION, OVERESTIMATION) of the delayed speed and headway.
             * 
             */
            @XmlElement(name = "Estimation", required = true, type = String.class)
            @XmlJavaTypeAdapter(EstimationAdapter.class)
            protected EstimationType estimation;
            /**
             * Anticipation entails NONE, CONSTANTSPEED or
             *                         CONSTANT_ACCELERATION, where the latter two anticipate to compensate the delay.
             * 
             */
            @XmlElement(name = "Anticipation", required = true, type = String.class)
            @XmlJavaTypeAdapter(AnticipationAdapter.class)
            protected AnticipationType anticipation;

            /**
             * Estimation entails perfect (NONE) or under- or overestimation
             *                         (UNDERESTIMATION, OVERESTIMATION) of the delayed speed and headway.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public EstimationType getEstimation() {
                return estimation;
            }

            /**
             * Sets the value of the estimation property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             * @see #getEstimation()
             */
            public void setEstimation(EstimationType value) {
                this.estimation = value;
            }

            /**
             * Anticipation entails NONE, CONSTANTSPEED or
             *                         CONSTANT_ACCELERATION, where the latter two anticipate to compensate the delay.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public AnticipationType getAnticipation() {
                return anticipation;
            }

            /**
             * Sets the value of the anticipation property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             * @see #getAnticipation()
             */
            public void setAnticipation(AnticipationType value) {
                this.anticipation = value;
            }

        }

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
     *       <choice>
     *         <element name="Fuller">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="Task" type="{http://www.opentrafficsim.org/ots}ClassNameType" maxOccurs="unbounded" minOccurs="0"/>
     *                   <element name="BehavioralAdaptations" minOccurs="0">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <all minOccurs="0">
     *                             <element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                             <element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
     *                           </all>
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                   <element name="TaskManager" minOccurs="0">
     *                     <simpleType>
     *                       <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
     *                         <simpleType>
     *                           <restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                             <enumeration value="SUMMATIVE"/>
     *                             <enumeration value="ANTICIPATION_RELIANCE"/>
     *                           </restriction>
     *                         </simpleType>
     *                       </union>
     *                     </simpleType>
     *                   </element>
     *                 </sequence>
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
        "fuller"
    })
    public static class Mental
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Fuller")
        protected PerceptionType.Mental.Fuller fuller;

        /**
         * Gets the value of the fuller property.
         * 
         * @return
         *     possible object is
         *     {@link PerceptionType.Mental.Fuller }
         *     
         */
        public PerceptionType.Mental.Fuller getFuller() {
            return fuller;
        }

        /**
         * Sets the value of the fuller property.
         * 
         * @param value
         *     allowed object is
         *     {@link PerceptionType.Mental.Fuller }
         *     
         */
        public void setFuller(PerceptionType.Mental.Fuller value) {
            this.fuller = value;
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
         *       <sequence>
         *         <element name="Task" type="{http://www.opentrafficsim.org/ots}ClassNameType" maxOccurs="unbounded" minOccurs="0"/>
         *         <element name="BehavioralAdaptations" minOccurs="0">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <all minOccurs="0">
         *                   <element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                   <element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
         *                 </all>
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
         *         <element name="TaskManager" minOccurs="0">
         *           <simpleType>
         *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
         *               <simpleType>
         *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *                   <enumeration value="SUMMATIVE"/>
         *                   <enumeration value="ANTICIPATION_RELIANCE"/>
         *                 </restriction>
         *               </simpleType>
         *             </union>
         *           </simpleType>
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
            "task",
            "behavioralAdaptations",
            "taskManager"
        })
        public static class Fuller
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            /**
             * Implementations of Task with constructor without input.
             * 
             */
            @XmlElement(name = "Task", type = String.class)
            @XmlJavaTypeAdapter(ClassAdapter.class)
            protected List<ClassType> task;
            /**
             * Implementations of BehavioralAdaptation with constructor
             *                         without input (value CLASS, with CLASS attribute), or a default. SITUATIONALAWARENESS: sets parameters
             *                         for situational awareness and reaction time. HEADWAY: increases the headway with high task demand.
             *                         SPEED: decreases speed with high task demand.
             * 
             */
            @XmlElement(name = "BehavioralAdaptations")
            protected PerceptionType.Mental.Fuller.BehavioralAdaptations behavioralAdaptations;
            /**
             * SUMMATIVE: add task demand of tasks. ANTICIPATION_RELIANCE:
             *                         rely on anticipation to reduce task demand of secondary task(s).
             * 
             */
            @XmlElement(name = "TaskManager", type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            protected StringType taskManager;

            /**
             * Implementations of Task with constructor without input.
             * 
             * Gets the value of the task property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the task property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getTask().add(newItem);
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
             *     The value of the task property.
             */
            public List<ClassType> getTask() {
                if (task == null) {
                    task = new ArrayList<>();
                }
                return this.task;
            }

            /**
             * Implementations of BehavioralAdaptation with constructor
             *                         without input (value CLASS, with CLASS attribute), or a default. SITUATIONALAWARENESS: sets parameters
             *                         for situational awareness and reaction time. HEADWAY: increases the headway with high task demand.
             *                         SPEED: decreases speed with high task demand.
             * 
             * @return
             *     possible object is
             *     {@link PerceptionType.Mental.Fuller.BehavioralAdaptations }
             *     
             */
            public PerceptionType.Mental.Fuller.BehavioralAdaptations getBehavioralAdaptations() {
                return behavioralAdaptations;
            }

            /**
             * Sets the value of the behavioralAdaptations property.
             * 
             * @param value
             *     allowed object is
             *     {@link PerceptionType.Mental.Fuller.BehavioralAdaptations }
             *     
             * @see #getBehavioralAdaptations()
             */
            public void setBehavioralAdaptations(PerceptionType.Mental.Fuller.BehavioralAdaptations value) {
                this.behavioralAdaptations = value;
            }

            /**
             * SUMMATIVE: add task demand of tasks. ANTICIPATION_RELIANCE:
             *                         rely on anticipation to reduce task demand of secondary task(s).
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public StringType getTaskManager() {
                return taskManager;
            }

            /**
             * Sets the value of the taskManager property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             * @see #getTaskManager()
             */
            public void setTaskManager(StringType value) {
                this.taskManager = value;
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
             *       <all minOccurs="0">
             *         <element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *         <element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/>
             *       </all>
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * }</pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            public static class BehavioralAdaptations
                implements Serializable
            {

                private static final long serialVersionUID = 10102L;
                @XmlElement(name = "SituationalAwareness")
                protected EmptyType situationalAwareness;
                @XmlElement(name = "Headway")
                protected EmptyType headway;
                @XmlElement(name = "Speed")
                protected EmptyType speed;

                /**
                 * Gets the value of the situationalAwareness property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getSituationalAwareness() {
                    return situationalAwareness;
                }

                /**
                 * Sets the value of the situationalAwareness property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setSituationalAwareness(EmptyType value) {
                    this.situationalAwareness = value;
                }

                /**
                 * Gets the value of the headway property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getHeadway() {
                    return headway;
                }

                /**
                 * Sets the value of the headway property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setHeadway(EmptyType value) {
                    this.headway = value;
                }

                /**
                 * Gets the value of the speed property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                public EmptyType getSpeed() {
                    return speed;
                }

                /**
                 * Sets the value of the speed property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                public void setSpeed(EmptyType value) {
                    this.speed = value;
                }

            }

        }

    }

}
