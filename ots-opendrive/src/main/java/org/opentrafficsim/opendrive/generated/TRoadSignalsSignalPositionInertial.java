
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Describes the reference point of the physical position in inertial coordinates in cases where it deviates from the logical position. Defines the inertial position.
 * 
 * <p>Java class for t_road_signals_signal_positionInertial complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_signals_signal_positionInertial">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="x" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="y" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="z" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="hdg" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
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
@XmlType(name = "t_road_signals_signal_positionInertial")
@SuppressWarnings("all") public class TRoadSignalsSignalPositionInertial
    extends OpenDriveElement
{

    /**
     * x-coordinate
     * 
     */
    @XmlAttribute(name = "x", required = true)
    protected double x;
    /**
     * y-coordinate
     * 
     */
    @XmlAttribute(name = "y", required = true)
    protected double y;
    /**
     * z-coordinate
     * 
     */
    @XmlAttribute(name = "z", required = true)
    protected double z;
    /**
     * Heading of the signal, relative to the inertial system
     * 
     */
    @XmlAttribute(name = "hdg", required = true)
    protected double hdg;
    /**
     * Pitch angle of the signal after applying heading, relative to the inertial system (x’y’-plane)
     * 
     */
    @XmlAttribute(name = "pitch")
    protected Double pitch;
    /**
     * Roll angle of the signal after applying heading and pitch, relative to the inertial system (x’’y’’-plane)
     * 
     */
    @XmlAttribute(name = "roll")
    protected Double roll;

    /**
     * x-coordinate
     * 
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     */
    public void setX(double value) {
        this.x = value;
    }

    /**
     * y-coordinate
     * 
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     */
    public void setY(double value) {
        this.y = value;
    }

    /**
     * z-coordinate
     * 
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     */
    public void setZ(double value) {
        this.z = value;
    }

    /**
     * Heading of the signal, relative to the inertial system
     * 
     */
    public double getHdg() {
        return hdg;
    }

    /**
     * Sets the value of the hdg property.
     * 
     */
    public void setHdg(double value) {
        this.hdg = value;
    }

    /**
     * Pitch angle of the signal after applying heading, relative to the inertial system (x’y’-plane)
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
     * Roll angle of the signal after applying heading and pitch, relative to the inertial system (x’’y’’-plane)
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
