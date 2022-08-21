# DJUNITS - Delft Java Units

[DJUNITS](https://djunits.org)  is a java project which reduces the programming workload concerning physical units of numbers, and it makes software considerably less error-prone concerning unit errors. For example, when using a speed value of type double, one usually has to be very careful and consider whether it’s speed in m/s, km/h, or even some other unit. DJUNITS solves this by wrapping a speed value in a `Speed` object. That is to say that DJUNITS makes the values _strongly typed_. It wraps the speed value in SI unit (m/s for speed), and provides various mathematical operation, some of which may result in another quantity such as: `Length`, `Acceleration`, `Duration`, `Frequency`. DJUNITS has many such quantities, and also supports vectors and matrices.

For each quantity, DJUNITS has a number of _units_. Values can be obtained or created with a unit. For example:

```java
    Speed speedKm = new Speed(90.0, SpeedUnit.KM_PER_HOUR);
    double speedKmps = speedKm.getInUnit(SpeedUnit.KM_PER_SECOND);
```

A slightly more convenient way to create values is by directly creating it in SI units:

```java
    Speed speedSi = Speed.instantiateSI(25.0);
```

Note that `speedKm` and `speedSi` are essentially equal as 25m/s = 90km/h. The creation unit is stored with the value, which is mostly for display purposes.

```java
    System.out.println(speedSi.equals(speedKm)); // true
    System.out.println(speedSi.si + ", " + speedSi); // 25.0, 25.0000000m/s
    System.out.println(speedKm.si + ", " + speedKm); // 25.0, 90.0000000km/h
```

There is a downside to using DJUNITS, which is that code can run a bit slower than working with doubles, and code can become more verbose and difficult to read. For example, take the following calculation:

```java
    public Length move(final Speed v, final Duration t, final Acceleration a)
    {
        return v.multiplyBy(t).plus(a.multiplyBy(.5).multiplyBy(t).multiplyBy(t));
    }
```

It is difficult to see what is exactly added to, or multiplied by, what. There is however an alternative which is ‘the best of both worlds’. We can simply perform calculations directly on the SI values, without having to care about the units:

```java
    public Length move(final Speed v, final Duration t, final Acceleration a)
    {
        return Length.instantiateSI(v.si * t.si + .5 * a.si * t.si * t.si);
    } 
```

There is one risk to this, which is that the values we calculate may not at all be consistent with the quantity we create. We know that ‘speed &times; duration’ + ‘0.5 &times; acceleration &times; duration<sup>2</sup>’ is length, but this is not checked. For example, no error is given on the following line where we forget to square the duration.

```java
    Length.instantiateSI(v.si * t.si + .5 * a.si * t.si);
```

Therefore usage of DJUNITS is advised as follows:

* Method input and output is strongly typed as DJUNITS quantities.
* Short calculations within a method remain strongly typed.
* Longer calculations, or calculations that are performance critical (i.e. performed very often), are done using `.si`, and `.instantiateSI()` on the result for output.

As calculations are strongly typed or using SI units, unit errors are unlikely to arise. Using strongly typed method input and output prevents that methods are invoked with values in units the method does not expect.
