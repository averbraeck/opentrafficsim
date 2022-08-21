# Detector data

An alternative to trajectory data is detector data. At the cost of being spatially (and usually temporally) less precise, detector data requires much less memory and processing time. In OTS one can use `Detector`, which is a type of sensor. What the detector measures can be defined by providing `DetectorMeasurement`s to the detector. Every time a GTU triggers the detector, each measurement is notified to accumulate the result with the GTU. This holds for both the front and the rear of GTUs. Due to lane changes it may be that only one of these two is triggered by a GTU.

<pre>
<b>Detector</b>
&lfloor; Position
&lfloor; Aggregation time
&lfloor; {Measurement}
</pre>

There are two types of measurements: periodic and mesoscopic. Periodic measurements aggregate a result every aggregation time, while mesoscopic measurements maintain a growing list of individual measurements. Detectors always keep track of vehicle counts (flow measurements) as part of the information that is made available to all other measurements. Other available periodic measurements are: mean speed, harmonic mean speed and occupancy. Available mesoscopic measurements are: vehicle passage times and platoon sizes. Most are available as static fields in the `Detector` class. For platoon sizes, a sub class `PlatoonSizes`, an instance should be created as the threshold gap should be specified. Other measurements can be defined in sub classes of `DetectorMeasurement`.

Data that detectors generate can be saved to a file using the method `writeToFile(…)`. This method writes either all periodic or all mesoscopic data to a file. This is separated as the structure of the data is different. Below is an example of periodic data. Each line has the detector id, start time of the aggregation period, flow, and in this case mean speed. Besides mean speed, other specified detector measurements will form additional columns.

<pre>
    id,t[s],q[veh/h],v[km/h]
    …
    A03,3720,360,72.345
    A03,3780,180,120.646
    A03,3840,0,NaN
    A04,0,540,123.586
    A04,60,1200,123.533
    A04,120,1080,125.316
    …
</pre>

An example mesoscopic data file is shown below. Each line provides detector id, the label of the mesoscopic data, and finally a possibly long list of mesoscopic measurements, e.g. for each GTU or platoon.

<pre>
    id,measurement,data
    …    
    A01,passage times,[14.152, 18.859, …, 3745.168, 3833.675]
    A01,vGain,[6.496, 8.322, …, 13.134, 9.948]
    A01,sigma,[0.292, 0.303, …, 0.071, 0.753]
    A01,vDes,[41.11, 35.886, …, 25.772, 30.547]
    A01,vDes0,[41.11, 35.886, …, 25.772, 30.547]
    …
</pre>

Mesoscopic measurements are likely to use a `List<Double>` internally. As a file is written to disk, each measurement is asked to provide a `String` representation of the final result with a number format specified. In case of `List<Double>` the static method `printListDouble(…)` of class `Detector` is then useful.
