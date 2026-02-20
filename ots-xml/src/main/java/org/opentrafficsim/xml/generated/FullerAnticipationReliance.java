
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * Task demand from tasks is added and counteracted with an overall
 *         anticipation reliance.
 * 
 * <p>Java class for FullerAnticipationReliance complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FullerAnticipationReliance">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}Fuller">
 *       <sequence>
 *         <element name="Tasks">
 *           <complexType>
 *             <complexContent>
 *               <extension base="{http://www.opentrafficsim.org/ots}FullerTasksSummativeAndAr">
 *                 <attribute name="PrimaryTask">
 *                   <simpleType>
 *                     <restriction base="{http://www.opentrafficsim.org/ots}string">
 *                       <enumeration value="car-following"/>
 *                       <enumeration value="lane-changing"/>
 *                     </restriction>
 *                   </simpleType>
 *                 </attribute>
 *               </extension>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="BehavioralAdaptations" type="{http://www.opentrafficsim.org/ots}FullerBehavioralAdaptations"/>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FullerAnticipationReliance", propOrder = {
    "tasks",
    "behavioralAdaptations"
})
@SuppressWarnings("all") public class FullerAnticipationReliance
    extends Fuller
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Tasks", required = true)
    protected FullerAnticipationReliance.Tasks tasks;
    @XmlElement(name = "BehavioralAdaptations", required = true)
    protected FullerBehavioralAdaptations behavioralAdaptations;

    /**
     * Gets the value of the tasks property.
     * 
     * @return
     *     possible object is
     *     {@link FullerAnticipationReliance.Tasks }
     *     
     */
    public FullerAnticipationReliance.Tasks getTasks() {
        return tasks;
    }

    /**
     * Sets the value of the tasks property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullerAnticipationReliance.Tasks }
     *     
     */
    public void setTasks(FullerAnticipationReliance.Tasks value) {
        this.tasks = value;
    }

    /**
     * Gets the value of the behavioralAdaptations property.
     * 
     * @return
     *     possible object is
     *     {@link FullerBehavioralAdaptations }
     *     
     */
    public FullerBehavioralAdaptations getBehavioralAdaptations() {
        return behavioralAdaptations;
    }

    /**
     * Sets the value of the behavioralAdaptations property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullerBehavioralAdaptations }
     *     
     */
    public void setBehavioralAdaptations(FullerBehavioralAdaptations value) {
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
     *     <extension base="{http://www.opentrafficsim.org/ots}FullerTasksSummativeAndAr">
     *       <attribute name="PrimaryTask">
     *         <simpleType>
     *           <restriction base="{http://www.opentrafficsim.org/ots}string">
     *             <enumeration value="car-following"/>
     *             <enumeration value="lane-changing"/>
     *           </restriction>
     *         </simpleType>
     *       </attribute>
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
        extends FullerTasksSummativeAndAr
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        /**
         * Task that will receive the least anticipation reliance.
         * 
         */
        @XmlAttribute(name = "PrimaryTask")
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType primaryTask;

        /**
         * Task that will receive the least anticipation reliance.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getPrimaryTask() {
            return primaryTask;
        }

        /**
         * Sets the value of the primaryTask property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         * @see #getPrimaryTask()
         */
        public void setPrimaryTask(StringType value) {
            this.primaryTask = value;
        }

    }

}
