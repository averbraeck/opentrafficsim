
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
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
 *         <element ref="{http://www.opentrafficsim.org/ots}Definitions"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Network"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Demand" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Control" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Models" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Scenarios" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Run"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Animation" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "definitions",
    "network",
    "demand",
    "control",
    "models",
    "scenarios",
    "run",
    "animation"
})
@XmlRootElement(name = "Ots")
@SuppressWarnings("all") public class Ots
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Definitions", required = true)
    protected Definitions definitions;
    @XmlElement(name = "Network", required = true)
    protected Network network;
    @XmlElement(name = "Demand")
    protected Demand demand;
    @XmlElement(name = "Control")
    protected Control control;
    @XmlElement(name = "Models")
    protected Models models;
    @XmlElement(name = "Scenarios")
    protected Scenarios scenarios;
    @XmlElement(name = "Run", required = true)
    protected Run run;
    @XmlElement(name = "Animation")
    protected Animation animation;
    @XmlAttribute(name = "Space")
    @XmlJavaTypeAdapter(SpaceAdapter.class)
    protected StringType space;

    /**
     * Gets the value of the definitions property.
     * 
     * @return
     *     possible object is
     *     {@link Definitions }
     *     
     */
    public Definitions getDefinitions() {
        return definitions;
    }

    /**
     * Sets the value of the definitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Definitions }
     *     
     */
    public void setDefinitions(Definitions value) {
        this.definitions = value;
    }

    /**
     * Gets the value of the network property.
     * 
     * @return
     *     possible object is
     *     {@link Network }
     *     
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Sets the value of the network property.
     * 
     * @param value
     *     allowed object is
     *     {@link Network }
     *     
     */
    public void setNetwork(Network value) {
        this.network = value;
    }

    /**
     * Gets the value of the demand property.
     * 
     * @return
     *     possible object is
     *     {@link Demand }
     *     
     */
    public Demand getDemand() {
        return demand;
    }

    /**
     * Sets the value of the demand property.
     * 
     * @param value
     *     allowed object is
     *     {@link Demand }
     *     
     */
    public void setDemand(Demand value) {
        this.demand = value;
    }

    /**
     * Gets the value of the control property.
     * 
     * @return
     *     possible object is
     *     {@link Control }
     *     
     */
    public Control getControl() {
        return control;
    }

    /**
     * Sets the value of the control property.
     * 
     * @param value
     *     allowed object is
     *     {@link Control }
     *     
     */
    public void setControl(Control value) {
        this.control = value;
    }

    /**
     * Gets the value of the models property.
     * 
     * @return
     *     possible object is
     *     {@link Models }
     *     
     */
    public Models getModels() {
        return models;
    }

    /**
     * Sets the value of the models property.
     * 
     * @param value
     *     allowed object is
     *     {@link Models }
     *     
     */
    public void setModels(Models value) {
        this.models = value;
    }

    /**
     * Gets the value of the scenarios property.
     * 
     * @return
     *     possible object is
     *     {@link Scenarios }
     *     
     */
    public Scenarios getScenarios() {
        return scenarios;
    }

    /**
     * Sets the value of the scenarios property.
     * 
     * @param value
     *     allowed object is
     *     {@link Scenarios }
     *     
     */
    public void setScenarios(Scenarios value) {
        this.scenarios = value;
    }

    /**
     * Gets the value of the run property.
     * 
     * @return
     *     possible object is
     *     {@link Run }
     *     
     */
    public Run getRun() {
        return run;
    }

    /**
     * Sets the value of the run property.
     * 
     * @param value
     *     allowed object is
     *     {@link Run }
     *     
     */
    public void setRun(Run value) {
        this.run = value;
    }

    /**
     * Gets the value of the animation property.
     * 
     * @return
     *     possible object is
     *     {@link Animation }
     *     
     */
    public Animation getAnimation() {
        return animation;
    }

    /**
     * Sets the value of the animation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Animation }
     *     
     */
    public void setAnimation(Animation value) {
        this.animation = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getSpace() {
        if (space == null) {
            return new SpaceAdapter().unmarshal("preserve");
        } else {
            return space;
        }
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(StringType value) {
        this.space = value;
    }

}
