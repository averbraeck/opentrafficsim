<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
include ('login.php');

if (isset ( $_SESSION ['login_user'] )) {
	header ( "location: selectproject.php" );
}
?>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>OTS - Login</title>
</head>
<body>
	<!-- adapted from http://www.formget.com/login-form-in-php/ -->
	<div id="main">
		<h1>OTS - login</h1>
		<div id="login">
			<form action="" method="post">
				<table>
					<tr>
						<td><label>User name:</label></td>
						<td><input id="name" name="userName" placeholder="name"
							type="text"></td>
					</tr>
					<tr>
						<td><label>Password:</label></td>
						<td><input id="password" name="password" placeholder="**********"
							type="password"></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><input name="submitLoginCredentials" type="submit"
							value=" Login "></td>
					</tr>
				</table>
			</form>
			<span><?php echo $error; ?></span>
		</div>
	</div>
</body>
</html>
