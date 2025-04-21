
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


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
 *         <element name="DefaultInputParameters" type="{http://www.opentrafficsim.org/ots}InputParameters" minOccurs="0"/>
 *         <element name="Scenario" type="{http://www.opentrafficsim.org/ots}ScenarioType" maxOccurs="unbounded"/>
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
    "defaultInputParameters",
    "scenario"
})
@XmlRootElement(name = "Scenarios")
@SuppressWarnings("all") public class Scenarios
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "DefaultInputParameters")
    protected InputParameters defaultInputParameters;
    @XmlElement(name = "Scenario", required = true)
    protected List<ScenarioType> scenario;

    /**
     * Gets the value of the defaultInputParameters property.
     * 
     * @return
     *     possible object is
     *     {@link InputParameters }
     *     
     */
    public InputParameters getDefaultInputParameters() {
        return defaultInputParameters;
    }

    /**
     * Sets the value of the defaultInputParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link InputParameters }
     *     
     */
    public void setDefaultInputParameters(InputParameters value) {
        this.defaultInputParameters = value;
    }

    /**
     * Gets the value of the scenario property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scenario property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getScenario().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ScenarioType }
     * </p>
     * 
     * 
     * @return
     *     The value of the scenario property.
     */
    public List<ScenarioType> getScenario() {
        if (scenario == null) {
            scenario = new ArrayList<>();
        }
        return this.scenario;
    }

}
