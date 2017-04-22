<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>SIM0MQ - Simulation Message Bus on 0MQ - Maven use</title>
<meta name="Author" content="Alexander Verbraeck, a.verbraeck@tudelft.nl" />
<meta name="Description" content="Simulation Message Bus on 0MQ" />
<meta name="Copyright" content="Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved." />
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
  bodyStart ("maven");
  ?>

    <h2>Maven use</h2>
    <p>
      Maven is one of the easiest ways to include SIM0MQ in a Java project. The Maven files for SIM0MQ reside at 
      <a href="http://sim0mq.org/maven" target="_blank">http://sim0mq.org/maven</a>.
      When a POM-file is created for the project, the following snippet needs to be included to include SIM0MQ:
    </p>

    <pre class="highlight">
&lt;dependencies&gt;
  &lt;dependency&gt;
    &lt;groupId&gt;org.sim0mq&lt;/groupId&gt;
    &lt;artifactId&gt;sim0mq&lt;/artifactId&gt;
    &lt;version&gt;0.00.01&lt;/version&gt;
  &lt;/dependency&gt;
&lt;/dependencies&gt;
</pre>

    <p>Of course, the version number (0.00.01 in the above example) needs to be replaced with the version that one wants to include in the project.</p>
    <p>Right now, the SIM0MQ files are kept on a server at TU Delft, and are not yet made available on Maven Central. Therefore, the repository location
      has to be specified separately in the Maven POM-file:</p>

    <pre class="highlight">
&lt;repositories&gt;
  &lt;repository&gt;
    &lt;name&gt;sim0mq Public Repository&lt;/name&gt;
    &lt;id&gt;sim0mq&lt;/id&gt;
    &lt;url&gt;http://sim0mq.org/maven&lt;/url&gt;
  &lt;/repository&gt;
&lt;/repositories&gt;
</pre>

    <h2>Dependencies</h2>
    <p>SIM0MQ is directly dependent on two packages, which have no further dependencies:</p>
    <ul>
      <li><b>jeromq</b> for a native java implementation of ZeroMQ. jeromq has no further dependencies.</li>
      <li><b>dunits</b> for the use of units. djunits is dependent on joda-money and ojalgo.</li>
      <li><b>dsol-base</b> for the use of several helper classes. dsol-base is dependent on log4j, veecmath and j3d-core.</li>
    </ul>
    <p>If the SIM0MQ library is used as a part of a Maven project, all dependencies will be automatically resolved, and the programmer / user does not have
      to worry about finding the libraries.</p>

  </div>

  <!-- ======== Footer ======== -->

  <div id="footer">
    <hr />
    Copyright &copy; 2015-2017, Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br /> SIM0MQ uses a BSD-style
    license. See <a href="http://sim0mq.org/docs/license.html">SIM0MQ License</a>.
  </div>

</body>
</html>
