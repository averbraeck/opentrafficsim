<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="../angular-1.4.7/angular.min.js"></script>
<script src="../jquery-2.1.4/jquery-2.1.4.min.js"></script>
<script src="../bootstrap-4.0.0-alpha/bootstrap.min.js"></script>
<link rel="stylesheet" type="text/css"
	href="../bootstrap-4.0.0-alpha/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="network.css">
<script src="include.js"></script>
</head>

<body>
	<div id="main" ng-app>
		<div class="include" data-include="menu.html"></div>
		<div class="include" data-include="toolbar.html"></div>

		<div class="container-fluid">
			<div class="row">
				<div class="col-sm-12">
					<h2>GTU definition</h2>
				</div>
			</div>
			<div class="col-sm-3">
				<b>Car</b><br>- Create new Car<br>- Station car<br>- Electrical car<br>
				<br> <b>Truck</b><br>- Create new Truck<br>- Standard truck<br>
			</div>
			<div class="col-sm-9">
				<form>
					Enter data about the selected GTU type here

					<?php
					include ("common.php");
					makeSelector ( "Maximum speed", "maxSpeed", "Speed" );
					makeSelector ( "Maximum acceleration", "maxAcceleration", "Acceleration" );
					makeSelector ( "Length", "length", "Length" );
					makeSelector ( "Height", "height", "Length" );
					?>
				</form>
			</div>
		</div>
	</div>
</body>
</html>