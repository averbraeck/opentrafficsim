
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Task demand from tasks is simply added.
 * 
 * <p>Java class for FullerSummative complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FullerSummative">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}Fuller">
 *       <sequence>
 *         <element name="Tasks" type="{http://www.opentrafficsim.org/ots}FullerTasksSummativeAndAr"/>
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
@XmlType(name = "FullerSummative", propOrder = {
    "tasks",
    "behavioralAdaptations"
})
@SuppressWarnings("all") public class FullerSummative
    extends Fuller
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Tasks", required = true)
    protected FullerTasksSummativeAndAr tasks;
    @XmlElement(name = "BehavioralAdaptations", required = true)
    protected FullerBehavioralAdaptations behavioralAdaptations;

    /**
     * Gets the value of the tasks property.
     * 
     * @return
     *     possible object is
     *     {@link FullerTasksSummativeAndAr }
     *     
     */
    public FullerTasksSummativeAndAr getTasks() {
        return tasks;
    }

    /**
     * Sets the value of the tasks property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullerTasksSummativeAndAr }
     *     
     */
    public void setTasks(FullerTasksSummativeAndAr value) {
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

}
