# Detector data

An alternative to trajectory data is detector data. At the cost of being spatially (and usually temporally) less precise, detector data requires much less memory and processing time. In OTS one can use `LoopDetector`, which is a type of `Detector`. What the detector measures can be defined by providing `LoopDetectorMeasurement`s to the detector. Every time a GTU triggers the detector, each measurement is notified to accumulate the result with the GTU. This holds for both the front and the rear of GTUs. Due to lane changes it may be that only one of these two is triggered by a GTU.

<pre>
<b>Detector</b>
&lfloor; Position
&lfloor; Aggregation time
&lfloor; {Measurement}
</pre>

There are two types of measurements: periodic and non-periodic. Periodic measurements aggregate a result every aggregation time, while non-periodic measurements maintain a growing list of individual measurements. Detectors always keep track of vehicle counts (flow measurements) as part of the information that is made available to all other measurements. Other available periodic measurements are: mean speed, harmonic mean speed and occupancy. Available non-periodic measurements are: vehicle passage times and platoon sizes. Most are available as static fields in the `LoopDetector` class. For platoon sizes, a sub class `PlatoonSizes`, an instance should be created as the threshold gap should be specified. Other measurements can be defined in sub classes of `LoopDetectorMeasurement`.

Data that detectors generate can be obtain in `Table` form, and thus saved in various file types using DJUTILS. Method `asTablePeriodicData(...)` provides a table with all the periodic data. Below is an example of periodic data. Each line has the detector id, start time of the aggregation period, flow, and in this case mean speed. Besides mean speed, other specified detector measurements will form additional columns. Saved files will also come with descriptions of the columns, including their units.

<pre>
    id,t,q,v
    …
    A03,3720,360,72.345
    A03,3780,180,120.646
    A03,3840,0,NaN
    A04,0,540,123.586
    A04,60,1200,123.533
    A04,120,1080,125.316
    …
</pre>

Non-periodic data is obtain in `Table` form using `asTableNonPeriodicData(...)`. An example non-periodic data file is shown below. Each line provides detector id, the label of the non-periodic data, and finally a possibly long list of non-periodic measurements, e.g. for each GTU or platoon. Five different non-periodic measurements are shown for detector A01.

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

Information on the detector positions can be obtained in `Table` form using `asTablePositions(...)`. For each detector it stores in one row the id, lane id, link id, and position on the lane.
