
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
 *         <element ref="{http://www.opentrafficsim.org/ots}Od" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}OdOptions" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Route" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}RouteMix" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}ShortestRoute" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}ShortestRouteMix" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}GtuTemplateMix" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}ModelIdReferralType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Generator" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}InjectionGenerator" maxOccurs="unbounded" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}Sink" maxOccurs="unbounded" minOccurs="0"/>
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
    "od",
    "odOptions",
    "route",
    "routeMix",
    "shortestRoute",
    "shortestRouteMix",
    "gtuTemplateMix",
    "modelIdReferral",
    "generator",
    "injectionGenerator",
    "sink"
})
@XmlRootElement(name = "Demand")
@SuppressWarnings("all") public class Demand
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Od")
    protected List<Od> od;
    @XmlElement(name = "OdOptions")
    protected List<OdOptions> odOptions;
    @XmlElement(name = "Route")
    protected List<Route> route;
    @XmlElement(name = "RouteMix")
    protected List<RouteMix> routeMix;
    @XmlElement(name = "ShortestRoute")
    protected List<ShortestRoute> shortestRoute;
    @XmlElement(name = "ShortestRouteMix")
    protected List<ShortestRouteMix> shortestRouteMix;
    @XmlElement(name = "GtuTemplateMix")
    protected List<GtuTemplateMix> gtuTemplateMix;
    @XmlElement(name = "ModelIdReferral")
    protected List<ModelIdReferralType> modelIdReferral;
    @XmlElement(name = "Generator")
    protected List<Generator> generator;
    @XmlElement(name = "InjectionGenerator")
    protected List<InjectionGenerator> injectionGenerator;
    @XmlElement(name = "Sink")
    protected List<Sink> sink;

    /**
     * Gets the value of the od property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the od property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getOd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Od }
     * </p>
     * 
     * 
     * @return
     *     The value of the od property.
     */
    public List<Od> getOd() {
        if (od == null) {
            od = new ArrayList<>();
        }
        return this.od;
    }

    /**
     * Gets the value of the odOptions property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the odOptions property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getOdOptions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OdOptions }
     * </p>
     * 
     * 
     * @return
     *     The value of the odOptions property.
     */
    public List<OdOptions> getOdOptions() {
        if (odOptions == null) {
            odOptions = new ArrayList<>();
        }
        return this.odOptions;
    }

    /**
     * Gets the value of the route property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the route property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRoute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Route }
     * </p>
     * 
     * 
     * @return
     *     The value of the route property.
     */
    public List<Route> getRoute() {
        if (route == null) {
            route = new ArrayList<>();
        }
        return this.route;
    }

    /**
     * Gets the value of the routeMix property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routeMix property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRouteMix().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RouteMix }
     * </p>
     * 
     * 
     * @return
     *     The value of the routeMix property.
     */
    public List<RouteMix> getRouteMix() {
        if (routeMix == null) {
            routeMix = new ArrayList<>();
        }
        return this.routeMix;
    }

    /**
     * Gets the value of the shortestRoute property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestRoute property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getShortestRoute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ShortestRoute }
     * </p>
     * 
     * 
     * @return
     *     The value of the shortestRoute property.
     */
    public List<ShortestRoute> getShortestRoute() {
        if (shortestRoute == null) {
            shortestRoute = new ArrayList<>();
        }
        return this.shortestRoute;
    }

    /**
     * Gets the value of the shortestRouteMix property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestRouteMix property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getShortestRouteMix().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ShortestRouteMix }
     * </p>
     * 
     * 
     * @return
     *     The value of the shortestRouteMix property.
     */
    public List<ShortestRouteMix> getShortestRouteMix() {
        if (shortestRouteMix == null) {
            shortestRouteMix = new ArrayList<>();
        }
        return this.shortestRouteMix;
    }

    /**
     * Gets the value of the gtuTemplateMix property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gtuTemplateMix property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGtuTemplateMix().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GtuTemplateMix }
     * </p>
     * 
     * 
     * @return
     *     The value of the gtuTemplateMix property.
     */
    public List<GtuTemplateMix> getGtuTemplateMix() {
        if (gtuTemplateMix == null) {
            gtuTemplateMix = new ArrayList<>();
        }
        return this.gtuTemplateMix;
    }

    /**
     * Gets the value of the modelIdReferral property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelIdReferral property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getModelIdReferral().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelIdReferralType }
     * </p>
     * 
     * 
     * @return
     *     The value of the modelIdReferral property.
     */
    public List<ModelIdReferralType> getModelIdReferral() {
        if (modelIdReferral == null) {
            modelIdReferral = new ArrayList<>();
        }
        return this.modelIdReferral;
    }

    /**
     * Gets the value of the generator property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generator property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGenerator().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Generator }
     * </p>
     * 
     * 
     * @return
     *     The value of the generator property.
     */
    public List<Generator> getGenerator() {
        if (generator == null) {
            generator = new ArrayList<>();
        }
        return this.generator;
    }

    /**
     * Gets the value of the injectionGenerator property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the injectionGenerator property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getInjectionGenerator().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InjectionGenerator }
     * </p>
     * 
     * 
     * @return
     *     The value of the injectionGenerator property.
     */
    public List<InjectionGenerator> getInjectionGenerator() {
        if (injectionGenerator == null) {
            injectionGenerator = new ArrayList<>();
        }
        return this.injectionGenerator;
    }

    /**
     * Gets the value of the sink property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sink property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sink }
     * </p>
     * 
     * 
     * @return
     *     The value of the sink property.
     */
    public List<Sink> getSink() {
        if (sink == null) {
            sink = new ArrayList<>();
        }
        return this.sink;
    }

}
