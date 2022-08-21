# Inter-arrival time generator

Functionally this is a simple component, that implements`Generator<Duration>`, which only defines a single `draw()` method to return headways. Internally, this has to implement a demand pattern. A simple instance would for example always return a specific value, meaning constant demand and a uniform arrival pattern. 

```java
    Generator<Duration> const = new ConstantGenerator<>(Duration.createSI(2.0));
```

But generally, demand is dynamic, and arrivals may be assumed not to be uniform. Arrival times may be defined in an external file where each line is the arrival time of a vehicle in seconds. This is implemented in `ListHeadways`, which generates the inter-arrival times from the file.

A general purpose headway generator based on a demand pattern is implemented in ArrivalsHeadwayGenerator. The demand pattern is defined in arrivals as a piecewise linear pattern. One implementation of arrivals is DemandPattern, while another is internally used when an OD matrix is used to create GTU generators (see section 3.9). The headway distribution (uniform, exponential, etc.) is also provided.

<pre>
Lane-based GTU generator
&lfloor; Inter-arrival time generator
  &lfloor; <b><i>Arrivals headway generator</i></b>
    &lfloor; Arrivals
    &lfloor; Headway distribution
</pre>

The algorithm of the arrivals headway generator works by integrating over the demand pattern, and a GTU is generated at the time where sufficient additional area under the demand curve is found. On average, the additional area should be 1, such that the demand pattern is obeyed. However, for individual GTUs the required additional area may be randomly drawn from the headway distribution. This may follow any distribution so long as all values are positive and the mean value is 1. In this way i) the demand pattern is obeyed (by randomized approximation), ii) headways are distributed according to a (random) distribution which is not parameterized to any particular demand level, and iii) infinite headways or GTUs arriving in 0-demand periods are automatically prevented. Figure 2 shows how this works out for some scenarios.

![](../images/OTS_Figure_2.png)
Figure 2: Demand interpolation and headway distribution. Each white dot represents a vehicle arrival.
