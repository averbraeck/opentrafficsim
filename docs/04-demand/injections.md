# Injection

Demand can be specified with a high level of control, variability and precision, by using injection. In this method the relevant initial information is defined for each GTU. This functionality is implemented in `Injections`, which has as primary input a `Table`. The table may be read from a .csv file.


## Injection table

The injection table may contain the following information:

_Table 4.2: Overview of information that can be specified for injections._
<table>
<tr><td><b>Column id</td><td><b>Value type</td><td><b>Remarks</b></td></tr>
<tr><td>time</td><td>Duration</td><td>required</td></tr>
<tr><td>id</td><td>String</td><td></td></tr>
<tr><td>gtuType</td><td>String</td><td>GTU type id</td></tr>
<tr><td>position<br>lane<br>link</td><td>Length<br>String<br>String</td><td>when defined, all three required</td></tr>
<tr><td>speed</td><td>Speed</td><td></td></tr>
<tr><td>origin</td><td>String</td><td>node id</td></tr>
<tr><td>destination</td><td>String</td><td>node id</td></tr>
<tr><td>route</td><td>String</td><td>route id</td></tr>
<tr><td>length</td><td>Length</td><td></td></tr>
<tr><td>width</td><td>Length</td><td></td></tr>
<tr><td>maxSpeed</td><td>Speed</td><td></td></tr>
<tr><td>maxAcceleration</td><td>Acceleration</td><td></td></tr>
<tr><td>maxDeceleration</td><td>Deceleration</td><td></td></tr>
<tr><td>front</td><td>Length</td><td></td></tr>
</table>

When the table is given to `Injections`, additional input may be required depending on the provided columns. For example, when a <i>speed</i> column is provided, a time-to-collision threshold needs to be provided such that the `Injections` can be used as a `RoomChecker` in `LaneBasedGtuGenerator`. GTU types, a strategical planner factory, the network and a random stream are required for all columns except time, id, position, lane, link and speed. In this case `Injections.asLaneBasedGtuCharacteristicsGenerator()` gives a characteristics generator for `LaneBasedGtuGenerator`. `Injections` can also be a `GeneratorPositions` if position columns are provided. Finally `Injections` is both a `Supplier<String>` for GTU id's and a `Generator<Duration>` for inter-arrival times, all to be used in `LaneBasedGtuGenerator`.

## Injection file

An injection file can be read in to an injection `Table` using various formats. Here the csv format is described. Translations to other formats are per `Table` functionality in DJUTILS. For csv, use `CsvData.readData(...)`. Two files are required for csv, one with the actual data, and a header file describing the columns. The data file may look as seen below. The first line gives column id's. On the second line we see a GTU injection a 2 {s}, with GTU id "car1", GTU type "CAR", an initial speed of 50 {km/h}, destination node "TRW", generation position 10 {m} on lane "LANE" on link "EE3", with a length of 1 {m}.

<pre>
time,id,gtuType,speed,destination,position,lane,link,length
2,car1,CAR,50,TRW,10,LANE,EE3,1
4,car2,CAR,50,BLE,10,LANE,SSC,2
6,car3,CAR,50,BLE,10,LANE,WWC,3
8,car4,CAR,50,BLS,10,LANE,EE3,4
13,car5,CAR,50,TRW,10,LANE,SSC,5
15,car6,CAR,50,BLS,10,LANE,WWC,6
17,car7,CAR,50,TRW,10,LANE,EE3,7
19,lorry1,LORRY,3,BLE,10,LANE,SSC,30
</pre>

Note that the units are not given in the file. Those are defined in the column descriptions in the header file. The header file will read the following. The first row indicates what columns can be read from the header file. The second line reflects the table itself, providing a table id, description, and the table type it should be read as. The unit is left empty here. From the third line the columns are described. On line three we see the time column described, with id "time", a description, the relevant value type (see Table 4.2), and a unit. The unit is optional and depends on the value type.

<pre>
id,description,className,unit
injections,test injections,org.djutils.data.Table,
time,injection time,org.djunits.value.vdouble.scalar.Duration,s
id,GTU id,java.lang.String,
gtuType,GTU type,java.lang.String,
speed,initial GTU speed,org.djunits.value.vdouble.scalar.Speed,km/h
destination,destination node,java.lang.String,
position,position on the lane,org.djunits.value.vdouble.scalar.Length,m
lane,lane id,java.lang.String,
link,link id,java.lang.String,
length,GTU length,org.djunits.value.vdouble.scalar.Length,m
</pre>