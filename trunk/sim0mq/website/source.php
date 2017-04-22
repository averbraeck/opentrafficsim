<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>SIM0MQ - Simulation Message Bus on 0MQ - Source Code</title>
<meta name="Author" content="Alexander Verbraeck, a.verbraeck@tudelft.nl" />
<meta name="Description" content="Simulation Message Bus on 0MQ" />
<meta name="Copyright" content="Copyright (c) 2015-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved." />
<meta name="Language" content="en" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-Language" content="en" />
<link rev="made" href="mailto:a.verbraeck@tudelft.nl" />
<link rel="StyleSheet" href="sinorcaish-screen.css" type="text/css" media="screen" />

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
  bodyStart ("source");
  ?>

    <h2>SVN Location</h2>

    <p>
      Source code can be checked out as an anonymous user from <a href="https://svn.tbm.tudelft.nl/OTS/trunk" target="_blank">https://svn.tbm.tudelft.nl/OTS/</a>.
      Releases can be found at <a href="https://svn.tbm.tudelft.nl/OTS/release" target="_blank">https://svn.tbm.tudelft.nl/OTS/release</a>.
    </p>

    <h2>Documentation</h2>

    <p>
      Documentation can be found at <a href="http://sim0mq.org/docs/current" target="_blank">http://sim0mq.org/docs/current</a>.
    </p>

    <h2>Package structure</h2>

    <p>SIM0MQ is divided into a number of packages:</p>

    <ul>
      <li><b>org.sim0mq</b> with generic classes such as the Sim0MQException.<br />&nbsp;</li>

      <li><b>org.sim0mq.util</b> with utility classes such as EndianUtil.<br />&nbsp;</li>

      <li><b>org.sim0mq.message</b> containing the basec message structures such as the abstract Sim0MQMessage and the abstract Sim0MQReply.<br />&nbsp;</li>

      <li><b>org.sim0mq.message.types</b> which defines all types, including DJUNITS types to be used in the messages<br />&nbsp;</li>

      <li><b>org.sim0mq.message.federatestarter</b> provides all messages sent by a Federate Starter.<br />&nbsp;</li>

      <li><b>org.sim0mq.message.federationmanager</b> provides all messages sent by a Federation Manager.<br />&nbsp;</li>

      <li><b>org.sim0mq.message.model</b> provides all messages sent by a Model.<br />&nbsp;</li>

      <li><b>org.sim0mq.federatestarter</b> provides a reference implementation of a federate starter. <br />&nbsp;</li>

      <li><b>org.sim0mq.federationmanager</b> provides an abstract implementation of a federation manager.<br />&nbsp;</li>

      <li><b>org.sim0mq.model</b> provides helper classes for a model implementation.<br />&nbsp;</li>

  </div>


  <!-- ======== Footer ======== -->

  <div id="footer">
    <hr />
    Copyright &copy; 2016-2017, Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br /> SIM0MQ uses a BSD-style
    license. See <a href="http://sim0mq.org/docs/license.html">SIM0MQ License</a>.
  </div>

</body>
</html>
