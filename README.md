# OpenTrafficSim

## What is OpenTrafficSim?

### Project overview

OpenTrafficSim aims to bring traffic simulation to a new level by
* Combining micro-simulation, macro-simulation and meta-simulation in a single environment
* Combining all traffic modes (private car, buses, bicycles, pedestrians, etc.) in a single simulator
* Providing options to link to external code, driving simulators, and data sources
We bring together state of the art simulation techniques, software development techniques and the best people in the traffic, simulation and software fields.


### Open source

Everything in OpenTrafficSim shall have a BSD, Apache, MIT, or similar open source license ensuring that
* OpenTrafficSim can be incorporated in part or in full in other products for any use (educational, commercial, whatever)
* OpenTrafficSim may be extended, evolved into anything else for any purpose


### Project information

The project information, compilation info, dependencies, unit test coverage, and API for all modules of OpenTrafficSim are stored in the [OTS project documentation](https://opentrafficsim.org/docs/latest/).<br>


### Who are we?

All current contributors work at the Delft University of Technology in the Netherlands. We are not picky; if you believe you can contribute to this ambitious project, please contact us. The current group of contributors is:
* Hans van Lint, TU Delft, faculty CEG
* Alexander Verbraeck, TU Delft, faculty TBM
* Peter Knoppers, TU Delft, faculty CEG
* Wouter Schakel, TU Delft, faculty CEG

Early contributors to the project were:
* Guus Tamminga, TU Delft, faculty CEG
* Yufei Yuan, TU Delft, faculty CEG

## Differences of this fork to OTS

### GtuCreator.java
* Custom class to spawn GTUs with fixed attributes at fixed locations
* Necessary for resimulation purposes

### WorldType.java, WorldLength.java, WorldWidth.java
* Additional attributes used in the roadSampler exports
* Necessary information for the conversion back to CR scenarios

### HeadwayGenerator.java
* Copied code from other private headway generator classes in demo files
* Necessary to use this class from within python

### Maven Shade plugin
* Additional plugin in the pom.xml
* Necessary to create fat-jars that are required by Jpype

## Where is the manual?

The **technical manual** for OTS can be found at [ReadTheDocs](https://opentrafficsim.readthedocs.io) or at [opentrafficsim.org](https://opentrafficsim.org/manual). There are also demo models. A more user-oriented manual are within our future plans.
