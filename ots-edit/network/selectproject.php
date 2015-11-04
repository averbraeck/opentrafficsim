<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
include ('login.php');

if (empty ( $_SESSION ['login_user'] )) {
	header ( "location: index.php" ); // Redirect to index.php page
}
?>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>OTS - Select Project</title>
</head>
<body>
	<div id="main">
		<h1>OTS - login</h1>
		<?php
		$projectGroups = $baseName . "/" . $_SESSION ['login_user'] . "/projectgroups/";
		$projects = scandir ( $projectGroups );
		if (count ( $projects ) > 2) {
			echo "Select a project group\n";
			echo "<ol>\n";
			for ($i = 2; $i < count($projects); $i++) {
				echo "<li>$projects[$i]</li>\n";
			}
			echo "</ol>\n";
		} else {
			echo "You currently have no project groups.";
		}
		
		?>
		<hr />
		<form action="createprojectgroup.php" method="post">
			<table>
				<tr>
					<td><label>Create new project group. Name:</label></td>
					<td><input id="name" name="projectGroupName"
						placeholder="projectgroupname" type="text"></td>
					<td>&nbsp;</td>
					<td><input name="submitNewProjectGroupName" type="submit"
						value=" Create new project group "></td>
				</tr>
			</table>
		</form>
		<hr />
		<a href="logout.php">logout</a>

	</div>
</body>
</html>
