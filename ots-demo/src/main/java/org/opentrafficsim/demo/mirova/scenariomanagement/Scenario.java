package org.opentrafficsim.demo.mirova.scenariomanagement;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.unit.SpeedUnit;

public class Scenario {

    /** Name of the scenario (used for folder names etc.). */
    private final String name;

    /** Demand, distributions, network parameters… */
    public Frequency demandCar = new Frequency(1800, FrequencyUnit.PER_HOUR);
    public Frequency demandTruck = new Frequency(200, FrequencyUnit.PER_HOUR);

    public Speed maxSpeedCar = new Speed(180, SpeedUnit.KM_PER_HOUR);
    public Speed maxSpeedTruck = new Speed(90, SpeedUnit.KM_PER_HOUR);

    public long seed = 1;

    public Scenario(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /** Deep copy constructor. */
    public Scenario copy(final String newName) {
        Scenario s = new Scenario(newName);
        s.demandCar = this.demandCar;
        s.demandTruck = this.demandTruck;
        s.maxSpeedCar = this.maxSpeedCar;
        s.maxSpeedTruck = this.maxSpeedTruck;
        s.seed = this.seed;
        return s;
    }
}
