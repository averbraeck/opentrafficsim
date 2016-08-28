<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<?php
include ('login.php');

if (empty ( $_SESSION ['login_user'] )) {
	header ( "location: index.php" ); // Redirect to index.php page
}
if (isset ( $_POST ['submitNewProjectGroupName'] )) {
	if (empty ( $_POST ['projectGroupName'] )) {
		$error = "No project group name provided";
	}
	$projectGroupName = $_POST ['projectGroupName'];
	$cleanedProjectGroupName = preg_replace ( '/[^a-zA-Z0-9_-]/', '//', $projectGroupName );
	if ($cleanedProjectGroupName != $projectGroupName) {
		$error = "Illegal characters in user project group name; use only letters, digits, underscores and hypens.";
	}
	$projectGroupDir = $baseName . "/" . $_SESSION ['login_user'] . "/projectgroups/" . $projectGroupName;
	if (file_exists ( $projectGroupDir )) {
		$error = "Project group \"" . $projectGroupName . "\" already exists";
	}
	else if (!mkdir($projectGroupDir, 0755))
	{
		$error = "Failed to create project group (mkdir failed for \"". $projectGroupDir . "\")";
	}
	if ('' == $error) {
		echo "<title>New project group \"" . $projectGroupName . "\" created</title></head>
			<body>New project group \"" . $projectGroupName . "\" created.<hr><a href=\"selectproject.php\">Continue</a>";
	} else {
		echo "<title>Project group not created</title></head><body>$error<hr/><a href=\"selectproject.php\">Continue</a>";
	}
}
?>