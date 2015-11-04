<?php
session_start ();
$error = '';
$baseName = "./userdata";
if (isset ( $_POST ['submitLoginCredentials'] )) {
	if (empty ( $_POST ['userName'] )) {
		$error = "No user name provided";
	} else if (empty ( $_POST ['password'] )) {
		$error = "No password provided";
	} else {
		// Define $userName and $password
		$userName = $_POST ['userName'];
		$cleaneduserName = preg_replace ( '/[^a-zA-Z0-9_-]/', '//', $userName );
		if ($cleaneduserName != $userName) {
			$error = "Illegal characters in user name";
		} else {
			$password = $_POST ['password'];
			// $hash = password_hash($password, PASSWORD_DEFAULT);
			$userFile = $baseName . "/" . $userName . "/passwd.txt";
			// echo "No illegal characters in user name; cwd is " . getcwd() . ", filename is \"" . $userFile . "\"\n";
			if (file_exists ( $userFile )) {
				$userFP = fopen ( $userFile, "r" );
				$expectedHash = fread ( $userFP, 999 );
				$expectedHash = trim ( $expectedHash );
				// echo "Expected hash is \"" . $expectedHash . "\"\n";
				if (password_verify ( $password, $expectedHash )) {
					$_SESSION ['login_user'] = $userName; // Initializing Session
					header ( "location: selectproject.php" ); // Redirect to selectproject.php page
				}
			}
			$error = "userName or Password is invalid";
		}
	}
}
?>
