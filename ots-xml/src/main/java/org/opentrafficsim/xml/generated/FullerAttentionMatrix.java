
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.OnOffAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;


/**
 * Task demand is assigned to channels. A Markov-chain attention matrix divides
 *         attention over the channels, resulting in different levels of anticipation and perception delay.
 * 
 * <p>Java class for FullerAttentionMatrix complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FullerAttentionMatrix">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}Fuller">
 *       <sequence>
 *         <element name="Tasks">
 *           <complexType>
 *             <complexContent>
 *               <extension base="{http://www.opentrafficsim.org/ots}FullerTasks">
 *                 <attribute name="FreeAcceleration" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="TrafficLights" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Signal" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Cooperation" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *                 <attribute name="Intersection" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *               </extension>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="BehavioralAdaptations">
 *           <complexType>
 *             <complexContent>
 *               <extension base="{http://www.opentrafficsim.org/ots}FullerBehavioralAdaptations">
 *                 <attribute name="UpdateTime" type="{http://www.opentrafficsim.org/ots}OnOff" />
 *               </extension>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FullerAttentionMatrix", propOrder = {
    "tasks",
    "behavioralAdaptations"
})
@SuppressWarnings("all") public class FullerAttentionMatrix
    extends Fuller
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Tasks", required = true)
    protected FullerAttentionMatrix.Tasks tasks;
    @XmlElement(name = "BehavioralAdaptations", required = true)
    protected FullerAttentionMatrix.BehavioralAdaptations behavioralAdaptations;

    /**
     * Gets the value of the tasks property.
     * 
     * @return
     *     possible object is
     *     {@link FullerAttentionMatrix.Tasks }
     *     
     */
    public FullerAttentionMatrix.Tasks getTasks() {
        return tasks;
    }

    /**
     * Sets the value of the tasks property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullerAttentionMatrix.Tasks }
     *     
     */
    public void setTasks(FullerAttentionMatrix.Tasks value) {
        this.tasks = value;
    }

    /**
     * Gets the value of the behavioralAdaptations property.
     * 
     * @return
     *     possible object is
     *     {@link FullerAttentionMatrix.BehavioralAdaptations }
     *     
     */
    public FullerAttentionMatrix.BehavioralAdaptations getBehavioralAdaptations() {
        return behavioralAdaptations;
    }

    /**
     * Sets the value of the behavioralAdaptations property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullerAttentionMatrix.BehavioralAdaptations }
     *     
     */
    public void setBehavioralAdaptations(FullerAttentionMatrix.BehavioralAdaptations value) {
        this.behavioralAdaptations = value;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <extension base="{http://www.opentrafficsim.org/ots}FullerBehavioralAdaptations">
     *       <attribute name="UpdateTime" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class BehavioralAdaptations
        extends FullerBehavioralAdaptations
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Dynamically adjusts the model update time depending on
         *                         maximum channel attention.
         * 
         */
        @XmlAttribute(name = "UpdateTime")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType updateTime;

        /**
         * Dynamically adjusts the model update time depending on
         *                         maximum channel attention.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getUpdateTime() {
            return updateTime;
        }

        /**
         * Sets the value of the updateTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getUpdateTime()
         */
        public void setUpdateTime(BooleanType value) {
            this.updateTime = value;
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
     *     <extension base="{http://www.opentrafficsim.org/ots}FullerTasks">
     *       <attribute name="FreeAcceleration" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="TrafficLights" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Signal" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Cooperation" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *       <attribute name="Intersection" type="{http://www.opentrafficsim.org/ots}OnOff" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Tasks
        extends FullerTasks
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Boosts attention to accelerate from a queue. This is
         *                         relevant if behavioral adaptation on the update time is applied. Without this, long update times may
         *                         result.
         * 
         */
        @XmlAttribute(name = "FreeAcceleration")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType freeAcceleration;
        @XmlAttribute(name = "TrafficLights")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType trafficLights;
        /**
         * Involves braking lights of the leader in the same lane and
         *                         indicators towards the current lane of leaders in adjacent lanes.
         * 
         */
        @XmlAttribute(name = "Signal")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType signal;
        /**
         * Minding adjacent leaders' lane change desire.
         * 
         */
        @XmlAttribute(name = "Cooperation")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType cooperation;
        /**
         * Navigating conflicts at intersections.
         * 
         */
        @XmlAttribute(name = "Intersection")
        @XmlJavaTypeAdapter(OnOffAdapter.class)
        protected BooleanType intersection;

        /**
         * Boosts attention to accelerate from a queue. This is
         *                         relevant if behavioral adaptation on the update time is applied. Without this, long update times may
         *                         result.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getFreeAcceleration() {
            return freeAcceleration;
        }

        /**
         * Sets the value of the freeAcceleration property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getFreeAcceleration()
         */
        public void setFreeAcceleration(BooleanType value) {
            this.freeAcceleration = value;
        }

        /**
         * Gets the value of the trafficLights property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getTrafficLights() {
            return trafficLights;
        }

        /**
         * Sets the value of the trafficLights property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTrafficLights(BooleanType value) {
            this.trafficLights = value;
        }

        /**
         * Involves braking lights of the leader in the same lane and
         *                         indicators towards the current lane of leaders in adjacent lanes.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getSignal() {
            return signal;
        }

        /**
         * Sets the value of the signal property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getSignal()
         */
        public void setSignal(BooleanType value) {
            this.signal = value;
        }

        /**
         * Minding adjacent leaders' lane change desire.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getCooperation() {
            return cooperation;
        }

        /**
         * Sets the value of the cooperation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getCooperation()
         */
        public void setCooperation(BooleanType value) {
            this.cooperation = value;
        }

        /**
         * Navigating conflicts at intersections.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public BooleanType getIntersection() {
            return intersection;
        }

        /**
         * Sets the value of the intersection property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getIntersection()
         */
        public void setIntersection(BooleanType value) {
            this.intersection = value;
        }

    }

}
