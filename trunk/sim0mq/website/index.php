<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>SIM0MQ - Simulation Message Bus on 0MQ, version 0.01</title>
<meta name="Author" content="Alexander Verbraeck, a.verbraeck@tudelft.nl" />
<meta name="Description" content="Simulation Message Bus on 0MQ" />
<meta name="Copyright" content="Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved." />
<meta name="Language" content="en" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-Language" content="en" />
<link rev="made" href="mailto:a.verbraeck@tudelft.nl" />
<link rel="StyleSheet" href="sinorcaish-screen.css" type="text/css" media="screen" />
<link href="/favicon.ico" type="image/x-icon" rel="icon" />

<script type="text/javascript">
	/* Prevent another site from "framing" this web page */
	if (top != self) {
		top.location.href = location.href;
	}
</script>
</head>

<body>

  <?php
  include ("common.php");
  bodyStart ("index");
  ?>

    <h2>Introduction</h2>
    <p>
      Sim0MQ makes use of the ØMQ (or 0MQ or ZMQ) message bus, and contains a layer of simulation-specific components and messages to aid in 
      creating distributed simulation execution. The Sim0MQ message bus is a fast messaging protocol to create loosely coupled simulations.
    </p>
    <ul>
	  <li>Sim0MQ uses the 0MQ protocol as its basis, enabling up to 2 million messages per second.</li>
	  <li>Sim0MQ provides low-level binary messages for fast message exchange.</li>
	  <li>Sim0MQ provides structured binary messages for more controlled message exchange.</li>
	  <li>Sim0MQ will be enabled with an IDL-type feature to generate message stubs automatically.</li>
	  <li>Sim0MQ can be used with any language that 0MQ supports: Java, C, C++, C#, Python, and a few dozen more.</li> 
	  <li>Sim0MQ works in a brokerless fashion due to the nature of the underlying 0MQ protocol.</li>
	  <li>Sim0MQ uses DJUNITS for a strongly typed unit system for values. A length scalar cannot be added to a time scalar; if 
	    length is divided by time, a speed variable results. This is all checked at compile time rather than at run time.</li>
	  <li>Sim0MQ can be linked to DSOL as the underlying, powerful simulation platform. DSOL takes care of time advance mechanisms, 
	    discrete-event and continuous simulation, random streams, probability distribution functions, experiment management, etc.</li>
    </ul>
    
    <h2>Origin</h2>
    <p>
      SIM0MQ was developed at the <a href="http://www.tudelft.nl">Delft University of Technology</a> as part of the 
      <a href="http://www.opentrafficsim.org/">Open Traffic Simulator</a> project (started in 2014).
    </p>
    <p>In November 2016 it became obvious that the simulation message bus developed for the Open Traffic Simulator were sufficiently mature to be used in
      other projects.</p>
    <p>
      The main authors/contributors of the SIM0MQ project are <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>, <a
        href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>, Wouter Schakel, and Sibel Eker.
    </p>

    <h2>Manual</h2>
    <p>
      The latest version of the manual can be found here: <a href="sim0mq-manual.pdf">Sim0MQ Manual</a> (PDF document).
    </p>

    <h2>Mode information about 0MQ / ZeroMQ</h2>
    <p>
      More information about ZeroMQ can be found on the <a href="http://zeromq.org">0MQ Website</a>.
    </p>


  </div>


  <!-- ======== Footer ======== -->

  <div id="footer">
    <hr />
    Copyright &copy; 2016-2017, Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br /> SIM0MQ uses a BSD-style
    license. See <a href="http://sim0mq.org/docs/license.html">SIM0MQ License</a>.
  </div>

</body>
</html>
