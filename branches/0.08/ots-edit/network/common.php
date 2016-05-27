	<?php
	function indent($tabs) {
		for($i = 0; $i < $tabs; $i ++) {
			echo "\t";
		}
	}
	function makeSelector($caption, $id, $unit) {
		echo "\n";
		indent ( 5 );
		echo "<div class=\"form-group row\">\n";
		indent ( 6 );
		echo "<label for=\"$id\" class=\"col-sm-3 form-control-label\">$caption</label>\n";
		indent ( 6 );
		echo "<div class=\"col-sm-3\">\n";
		indent ( 7 );
		echo "<input type=\"text\" class=\"form-control form-control-sm\" id=\"$id\" placeholder=\"$caption\">\n";
		indent ( 6 );
		echo "</div>\n";
		indent ( 6 );
		echo "<fieldset class=\"col-sm-2 form-group\">\n";
		indent ( 7 );
		echo "<select class=\"form-control form-control-sm\" id=\"" . $id . "Unit\">\n";
		switch ($unit) {
			case "Length" :
				indent ( 8 );
				echo "<option>m</option>\n";
				indent ( 8 );
				echo "<option>cm</option>\n";
				indent ( 8 );
				echo "<option>dm</option>\n";
				indent ( 8 );
				echo "<option>dam</option>\n";
				indent ( 8 );
				echo "<option>hm</option>\n";
				indent ( 8 );
				echo "<option>km</option>\n";
				indent ( 8 );
				echo "<option>mi</option>\n";
				indent ( 8 );
				echo "<option>y</option>\n";
				indent ( 8 );
				echo "<option>ft</option>\n";
				break;
			
			case "Speed" :
				indent ( 8 );
				echo "<option>m/s</option>\n";
				indent ( 8 );
				echo "<option>km/h</option>\n";
				indent ( 8 );
				echo "<option>mi/h</option>\n";
				indent ( 8 );
				echo "<option>ft/s</option>\n";
				indent ( 8 );
				echo "<option>knot</option>\n";
				break;
			
			case "Acceleration" :
				indent ( 8 );
				echo "<option>m/s2</option>\n";
				indent ( 8 );
				echo "<option>km/h^2</option>\n";
				indent ( 8 );
				echo "<option>mi/h^2</option>\n";
				indent ( 8 );
				echo "<option>y/s^2</option>\n";
				indent ( 8 );
				echo "<option>ft/s^2</option>\n";
				break;
			
			default :
				indent ( 8 );
				echo "<option>ERROR unknown unit \"$unit\"</option>\n";
				break;
		}
		indent ( 7 );
		echo "</select>\n";
		indent ( 6 );
		echo "</fieldset>\n";
		indent ( 5 );
		echo "</div>\n";
	}
	?>
