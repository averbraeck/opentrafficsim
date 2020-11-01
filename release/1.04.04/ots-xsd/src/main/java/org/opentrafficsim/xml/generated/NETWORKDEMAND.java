//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.10.31 at 11:54:56 AM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}ROUTE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}ROUTEMIX" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SHORTESTROUTE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SHORTESTROUTEMIX" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GTUTEMPLATEMIX" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}ODOPTIONS" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}OD" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="MODELIDREFERRAL" type="{http://www.opentrafficsim.org/ots}MODELIDREFERRALTYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}GENERATOR" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}LISTGENERATOR" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SINK" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "route",
    "routemix",
    "shortestroute",
    "shortestroutemix",
    "gtutemplatemix",
    "odoptions",
    "od",
    "modelidreferral",
    "generator",
    "listgenerator",
    "sink"
})
@XmlRootElement(name = "NETWORKDEMAND")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
public class NETWORKDEMAND
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "ROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<ROUTE> route;
    @XmlElement(name = "ROUTEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<ROUTEMIX> routemix;
    @XmlElement(name = "SHORTESTROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<SHORTESTROUTE> shortestroute;
    @XmlElement(name = "SHORTESTROUTEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<SHORTESTROUTEMIX> shortestroutemix;
    @XmlElement(name = "GTUTEMPLATEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<GTUTEMPLATEMIX> gtutemplatemix;
    @XmlElement(name = "ODOPTIONS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<ODOPTIONS> odoptions;
    @XmlElement(name = "OD")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<OD> od;
    @XmlElement(name = "MODELIDREFERRAL")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<MODELIDREFERRALTYPE> modelidreferral;
    @XmlElement(name = "GENERATOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<GENERATOR> generator;
    @XmlElement(name = "LISTGENERATOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<LISTGENERATOR> listgenerator;
    @XmlElement(name = "SINK")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    protected List<SINK> sink;

    /**
     * Gets the value of the route property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the route property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getROUTE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ROUTE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<ROUTE> getROUTE() {
        if (route == null) {
            route = new ArrayList<ROUTE>();
        }
        return this.route;
    }

    /**
     * Gets the value of the routemix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routemix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getROUTEMIX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ROUTEMIX }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<ROUTEMIX> getROUTEMIX() {
        if (routemix == null) {
            routemix = new ArrayList<ROUTEMIX>();
        }
        return this.routemix;
    }

    /**
     * Gets the value of the shortestroute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestroute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSHORTESTROUTE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SHORTESTROUTE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<SHORTESTROUTE> getSHORTESTROUTE() {
        if (shortestroute == null) {
            shortestroute = new ArrayList<SHORTESTROUTE>();
        }
        return this.shortestroute;
    }

    /**
     * Gets the value of the shortestroutemix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestroutemix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSHORTESTROUTEMIX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SHORTESTROUTEMIX }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<SHORTESTROUTEMIX> getSHORTESTROUTEMIX() {
        if (shortestroutemix == null) {
            shortestroutemix = new ArrayList<SHORTESTROUTEMIX>();
        }
        return this.shortestroutemix;
    }

    /**
     * Gets the value of the gtutemplatemix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gtutemplatemix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGTUTEMPLATEMIX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GTUTEMPLATEMIX }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<GTUTEMPLATEMIX> getGTUTEMPLATEMIX() {
        if (gtutemplatemix == null) {
            gtutemplatemix = new ArrayList<GTUTEMPLATEMIX>();
        }
        return this.gtutemplatemix;
    }

    /**
     * Gets the value of the odoptions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the odoptions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getODOPTIONS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ODOPTIONS }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<ODOPTIONS> getODOPTIONS() {
        if (odoptions == null) {
            odoptions = new ArrayList<ODOPTIONS>();
        }
        return this.odoptions;
    }

    /**
     * Gets the value of the od property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the od property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OD }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<OD> getOD() {
        if (od == null) {
            od = new ArrayList<OD>();
        }
        return this.od;
    }

    /**
     * Gets the value of the modelidreferral property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelidreferral property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMODELIDREFERRAL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MODELIDREFERRALTYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<MODELIDREFERRALTYPE> getMODELIDREFERRAL() {
        if (modelidreferral == null) {
            modelidreferral = new ArrayList<MODELIDREFERRALTYPE>();
        }
        return this.modelidreferral;
    }

    /**
     * Gets the value of the generator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGENERATOR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GENERATOR }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<GENERATOR> getGENERATOR() {
        if (generator == null) {
            generator = new ArrayList<GENERATOR>();
        }
        return this.generator;
    }

    /**
     * Gets the value of the listgenerator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listgenerator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLISTGENERATOR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LISTGENERATOR }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<LISTGENERATOR> getLISTGENERATOR() {
        if (listgenerator == null) {
            listgenerator = new ArrayList<LISTGENERATOR>();
        }
        return this.listgenerator;
    }

    /**
     * Gets the value of the sink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSINK().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SINK }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-10-31T11:54:56+01:00", comments = "JAXB RI v2.3.0")
    public List<SINK> getSINK() {
        if (sink == null) {
            sink = new ArrayList<SINK>();
        }
        return this.sink;
    }

}
