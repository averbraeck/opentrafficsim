package org.sim0mq.message.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.AngleSolidUnit;
import org.djunits.unit.AngleUnit;
import org.djunits.unit.AreaUnit;
import org.djunits.unit.DensityUnit;
import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.ElectricalChargeUnit;
import org.djunits.unit.ElectricalCurrentUnit;
import org.djunits.unit.ElectricalPotentialUnit;
import org.djunits.unit.ElectricalResistanceUnit;
import org.djunits.unit.EnergyUnit;
import org.djunits.unit.FlowMassUnit;
import org.djunits.unit.FlowVolumeUnit;
import org.djunits.unit.ForceUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.MassUnit;
import org.djunits.unit.MoneyPerAreaUnit;
import org.djunits.unit.MoneyPerEnergyUnit;
import org.djunits.unit.MoneyPerLengthUnit;
import org.djunits.unit.MoneyPerMassUnit;
import org.djunits.unit.MoneyPerTimeUnit;
import org.djunits.unit.MoneyPerVolumeUnit;
import org.djunits.unit.MoneyUnit;
import org.djunits.unit.PowerUnit;
import org.djunits.unit.PressureUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TemperatureUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.TorqueUnit;
import org.djunits.unit.Unit;
import org.djunits.unit.VolumeUnit;

/**
 * The Sim0MQ unit types with their code, including static methods to quickly find a unit type.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 4, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Sim0MQUnitType implements Serializable
{
    /** */
    private static final long serialVersionUID = 20170304L;

    /** the unit types from number to type. */
    private static Map<Byte, Sim0MQUnitType> byteTypeMap = new HashMap<>();

    /** the unit types from class to type. */
    private static Map<Class<? extends Unit<?>>, Sim0MQUnitType> unitTypeMap = new HashMap<>();

    /** Dimensionless unit type with code 0. */
    public static final Sim0MQUnitType DIMENSIONLESS =
            new Sim0MQUnitType(0, DimensionlessUnit.class, "Dimensionless", "Unit without a dimension", "[]");

    /** Acceleration unit type with code 1. */
    public static final Sim0MQUnitType ACCELERATION =
            new Sim0MQUnitType(1, AccelerationUnit.class, "Acceleration", "Acceleration", "[m/s^2]");

    /** AngleSolid unit type with code 2. */
    public static final Sim0MQUnitType ANGLESOLID =
            new Sim0MQUnitType(2, AngleSolidUnit.class, "AngleSolid", "Solid angle ", "[steradian]");

    /** Angle unit type with code 3. */
    public static final Sim0MQUnitType ANGLE = new Sim0MQUnitType(3, AngleUnit.class, "Angle", "Angle (relative)", "[rad]");

    /** Direction unit type with code 4. */
    public static final Sim0MQUnitType DIRECTION =
            new Sim0MQUnitType(4, AngleUnit.class, "Direction", "Angle  (absolute)", "[rad]");

    /** Area unit type with code 5. */
    public static final Sim0MQUnitType AREA = new Sim0MQUnitType(5, AreaUnit.class, "Area", "Area", "[m^2]");

    /** Density unit type with code 6. */
    public static final Sim0MQUnitType DENSITY =
            new Sim0MQUnitType(6, DensityUnit.class, "Density", "Density based on mass and length", "[kg/m^3]");

    /** ElectricalCharge unit type with code 7. */
    public static final Sim0MQUnitType ELECTRICALCHARGE =
            new Sim0MQUnitType(7, ElectricalChargeUnit.class, "ElectricalCharge", "Electrical charge (Coulomb)", "[sA]");

    /** ElectricalCurrent unit type with code 8. */
    public static final Sim0MQUnitType ELECTRICALCURRENT =
            new Sim0MQUnitType(8, ElectricalCurrentUnit.class, "ElectricalCurrent", "Electrical current (Ampere)", "[A]");

    /** ElectricalPotential unit type with code 9. */
    public static final Sim0MQUnitType ELECTRICALPOTENTIAL = new Sim0MQUnitType(9, ElectricalPotentialUnit.class,
            "ElectricalPotential", "Electrical potential (Volt)", "[kgm^2/s^3A]");

    /** ElectricalResistance unit type with code 10. */
    public static final Sim0MQUnitType ELECTRICALRESISTANCE = new Sim0MQUnitType(10, ElectricalResistanceUnit.class,
            "ElectricalResistance", "Electrical resistance (Ohm)", "[kgm^2/s^3A^2]");

    /** Energy unit type with code 11. */
    public static final Sim0MQUnitType ENERGY =
            new Sim0MQUnitType(11, EnergyUnit.class, "Energy", "Energy (Joule)", "[kgm^2/s^2]");

    /** FlowMass unit type with code 12. */
    public static final Sim0MQUnitType FLOWMASS =
            new Sim0MQUnitType(12, FlowMassUnit.class, "FlowMass", "Mass flow rate ", "[kg/s]");

    /** FlowVolume unit type with code 13. */
    public static final Sim0MQUnitType FLOWVOLUME =
            new Sim0MQUnitType(13, FlowVolumeUnit.class, "FlowVolume", "Volume flow rate", "[m^3/s]");

    /** Force unit type with code 14. */
    public static final Sim0MQUnitType FORCE = new Sim0MQUnitType(14, ForceUnit.class, "Force", "Force (Newton)", "[kgm/s^2]");

    /** Frequency unit type with code 15. */
    public static final Sim0MQUnitType FREQUENCY =
            new Sim0MQUnitType(15, FrequencyUnit.class, "Frequency", "Frequency (Hz)", "[1/s]");

    /** Length unit type with code 16. */
    public static final Sim0MQUnitType LENGTH = new Sim0MQUnitType(16, LengthUnit.class, "Length", "Length (relative)", "[m]");

    /** Position unit type with code 17. */
    public static final Sim0MQUnitType POSITION =
            new Sim0MQUnitType(17, LengthUnit.class, "Position", "Length (absolute)", "[m]");

    /** LinearDensity unit type with code 18. */
    public static final Sim0MQUnitType LINEARDENSITY =
            new Sim0MQUnitType(18, LinearDensityUnit.class, "LinearDensity", "Linear density ", "[1/m]");

    /** Mass unit type with code 19. */
    public static final Sim0MQUnitType MASS = new Sim0MQUnitType(19, MassUnit.class, "Mass", "Mass", "[kg]");

    /** Power unit type with code 20. */
    public static final Sim0MQUnitType POWER = new Sim0MQUnitType(20, PowerUnit.class, "Power", "Power (Watt)", "[kgm^2/s^3]");

    /** Pressure unit type with code 21. */
    public static final Sim0MQUnitType PRESSURE =
            new Sim0MQUnitType(21, PressureUnit.class, "Pressure", "Pressure (Pascal)", "[kg/ms^2]");

    /** Speed unit type with code 22. */
    public static final Sim0MQUnitType SPEED = new Sim0MQUnitType(22, SpeedUnit.class, "Speed", "Speed", "[m/s]");

    /** Temperature unit type with code 23. */
    public static final Sim0MQUnitType TEMPERATURE =
            new Sim0MQUnitType(23, TemperatureUnit.class, "Temperature", "Temperature (relative)", "[K]");

    /** AbsoluteTemperature unit type with code 24. */
    public static final Sim0MQUnitType ABSOLUTETEMPERATURE =
            new Sim0MQUnitType(24, TemperatureUnit.class, "AbsoluteTemperature", "Temperature (absolute)", "[K]");

    /** Duration unit type with code 25. */
    public static final Sim0MQUnitType DURATION = new Sim0MQUnitType(25, TimeUnit.class, "Duration", "Time (relative)", "[s]");

    /** Time unit type with code 26. */
    public static final Sim0MQUnitType TIME = new Sim0MQUnitType(26, TimeUnit.class, "Time", "Time (absolute)", "[s]");

    /** Torque unit type with code 27. */
    public static final Sim0MQUnitType TORQUE =
            new Sim0MQUnitType(27, TorqueUnit.class, "Torque", "Torque (Newton-meter)", "[kgm^2/s^2]");

    /** Volume unit type with code 28. */
    public static final Sim0MQUnitType VOLUME = new Sim0MQUnitType(28, VolumeUnit.class, "Volume", "Volume", "[m^3]");

    /** Money unit type with code 100. */
    public static final Sim0MQUnitType MONEY =
            new Sim0MQUnitType(100, MoneyUnit.class, "Money", "Money (cost in e.g., $, €, ...)", "[$]");

    /** MoneyPerArea unit type with code 101. */
    public static final Sim0MQUnitType MONEYPERAREA =
            new Sim0MQUnitType(101, MoneyPerAreaUnit.class, "MoneyPerArea", "Money/Area (cost/m^2)", "[$/m^2]");

    /** MoneyPerEnergy unit type with code 102. */
    public static final Sim0MQUnitType MONEYPERENERGY =
            new Sim0MQUnitType(102, MoneyPerEnergyUnit.class, "MoneyPerEnergy", "Money/Energy (cost/W)", "[$s^3/kgm^2]");

    /** MoneyPerLength unit type with code 103. */
    public static final Sim0MQUnitType MONEYPERLENGTH =
            new Sim0MQUnitType(103, MoneyPerLengthUnit.class, "MoneyPerLength", "Money/Length (cost/m)", "[$/m]");

    /** MoneyPerMass unit type with code 104. */
    public static final Sim0MQUnitType MONEYPERMASS =
            new Sim0MQUnitType(104, MoneyPerMassUnit.class, "MoneyPerMass", "Money/Mass (cost/kg)", "[$/kg]");

    /** MoneyPerTime unit type with code 105. */
    public static final Sim0MQUnitType MONEYPERTIME =
            new Sim0MQUnitType(105, MoneyPerTimeUnit.class, "MoneyPerTime", "Money/Duration (cost/s)", "[$/s]");

    /** MoneyPerVolume unit type with code 106. */
    public static final Sim0MQUnitType MONEYPERVOLUME =
            new Sim0MQUnitType(106, MoneyPerVolumeUnit.class, "MoneyPerVolume", "Money/Volume (cost/m^3)", "[$/m^3]");

    /** the code of the unit as a byte. */
    private final byte code;

    /** the djunits data type. */
    private final Class<? extends Unit<?>> djunitsType;

    /** the unit name. */
    private final String name;

    /** the unit description. */
    private final String description;

    /** the SI or default unit in SI-elements. */
    private final String siUnit;

    /**
     * @param code the byte code of the unit provided as an int
     * @param djunitsType the djunits data type
     * @param name the unit name
     * @param description the unit description
     * @param siUnit the SI or default unit in SI-elements
     */
    public <U extends Unit<U>> Sim0MQUnitType(final int code, final Class<U> djunitsType, final String name,
            final String description, final String siUnit)
    {
        super();
        this.code = (byte) code;
        this.djunitsType = djunitsType;
        this.name = name;
        this.description = description;
        this.siUnit = siUnit;

        byteTypeMap.put(this.code, this);
        unitTypeMap.put(this.djunitsType, this);
    }

    /**
     * Return the unit type belonging to the byte code.
     * @param code the code to search for.
     * @return the unit type, or null if not found.
     */
    public static Sim0MQUnitType getUnitType(final byte code)
    {
        return byteTypeMap.get(code);
    }

    /**
     * Return the unit class belonging to the byte code.
     * @param code the code to search for.
     * @return the unit class, or null if not found.
     */
    public static Class<? extends Unit<?>> getUnitClass(final byte code)
    {
        Sim0MQUnitType type = byteTypeMap.get(code);
        return type == null ? null : type.getDjunitsType();
    }

    /**
     * Return the unit type belonging to the unit class.
     * @param unit the unit to search for.
     * @return the unit type, or null if not found.
     */
    public static <U extends Unit<U>> Sim0MQUnitType getUnitType(final U unit)
    {
        return unitTypeMap.get(unit.getClass());
    }

    /**
     * Return the byte code belonging to the unit class.
     * @param unit the unit to search for.
     * @return the unit type, or null if not found.
     */
    public static <U extends Unit<U>> byte getUnitCode(final U unit)
    {
        Sim0MQUnitType type = unitTypeMap.get(unit.getClass());
        return type == null ? null : type.getCode();
    }

    /**
     * @return code
     */
    public final byte getCode()
    {
        return this.code;
    }

    /**
     * @return djunitsType
     */
    public final Class<? extends Unit<?>> getDjunitsType()
    {
        return this.djunitsType;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return description
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * @return siUnit
     */
    public final String getSiUnit()
    {
        return this.siUnit;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.code;
        result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + ((this.djunitsType == null) ? 0 : this.djunitsType.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.siUnit == null) ? 0 : this.siUnit.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "checkstyle:designforextension", "needbraces" })
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sim0MQUnitType other = (Sim0MQUnitType) obj;
        if (this.code != other.code)
            return false;
        if (this.description == null)
        {
            if (other.description != null)
                return false;
        }
        else if (!this.description.equals(other.description))
            return false;
        if (this.djunitsType == null)
        {
            if (other.djunitsType != null)
                return false;
        }
        else if (!this.djunitsType.equals(other.djunitsType))
            return false;
        if (this.name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.siUnit == null)
        {
            if (other.siUnit != null)
                return false;
        }
        else if (!this.siUnit.equals(other.siUnit))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Sim0MQUnitType [code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", siUnit="
                + this.siUnit + "]";
    }

}
