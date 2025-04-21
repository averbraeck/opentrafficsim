
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Describes the reference point of the physical position road coordinates in cases where it deviates from the logical position. Defines the position on the road.
 * 
 * <p>Java class for t_road_signals_signal_positionRoad complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_signals_signal_positionRoad">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="roadId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="t" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="hOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="pitch" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="roll" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_signals_signal_positionRoad")
@SuppressWarnings("all") public class TRoadSignalsSignalPositionRoad
    extends OpenDriveElement
{

    /**
     * Unique ID of the referenced road
     * 
     */
    @XmlAttribute(name = "roadId", required = true)
    protected String roadId;
    /**
     * s-coordinate
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * t-coordinate
     * 
     */
    @XmlAttribute(name = "t", required = true)
    protected double t;
    /**
     * z offset from road level to bottom edge of the signal
     * 
     */
    @XmlAttribute(name = "zOffset", required = true)
    protected double zOffset;
    /**
     * Heading offset of the signal (relative to @orientation)
     * 
     */
    @XmlAttribute(name = "hOffset", required = true)
    protected double hOffset;
    /**
     * Pitch angle of the signal after applying hOffset, relative to the inertial system (x’y’-plane)
     * 
     */
    @XmlAttribute(name = "pitch")
    protected Double pitch;
    /**
     * Roll angle of the signal after applying hOffset and pitch, relative to the inertial system (x’’y’’-plane)
     * 
     */
    @XmlAttribute(name = "roll")
    protected Double roll;

    /**
     * Unique ID of the referenced road
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoadId() {
        return roadId;
    }

    /**
     * Sets the value of the roadId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getRoadId()
     */
    public void setRoadId(String value) {
        this.roadId = value;
    }

    /**
     * s-coordinate
     * 
     */
    public double getS() {
        return s;
    }

    /**
     * Sets the value of the s property.
     * 
     */
    public void setS(double value) {
        this.s = value;
    }

    /**
     * t-coordinate
     * 
     */
    public double getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     */
    public void setT(double value) {
        this.t = value;
    }

    /**
     * z offset from road level to bottom edge of the signal
     * 
     */
    public double getZOffset() {
        return zOffset;
    }

    /**
     * Sets the value of the zOffset property.
     * 
     */
    public void setZOffset(double value) {
        this.zOffset = value;
    }

    /**
     * Heading offset of the signal (relative to @orientation)
     * 
     */
    public double getHOffset() {
        return hOffset;
    }

    /**
     * Sets the value of the hOffset property.
     * 
     */
    public void setHOffset(double value) {
        this.hOffset = value;
    }

    /**
     * Pitch angle of the signal after applying hOffset, relative to the inertial system (x’y’-plane)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPitch() {
        return pitch;
    }

    /**
     * Sets the value of the pitch property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getPitch()
     */
    public void setPitch(Double value) {
        this.pitch = value;
    }

    /**
     * Roll angle of the signal after applying hOffset and pitch, relative to the inertial system (x’’y’’-plane)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRoll() {
        return roll;
    }

    /**
     * Sets the value of the roll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getRoll()
     */
    public void setRoll(Double value) {
        this.roll = value;
    }

}
