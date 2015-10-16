<?php 
 	$u = "admin";
 	$p = "12345";

	$username = $_POST['username'];
	$password = $_POST['password'];
	
	//$arg1 = "user:" . $username;
	//$arg2 = "pass:" . $password;

	if (strcmp($u, $username) == 0 && strcmp($p, $password) == 0) {
		header('Location: https://localhost:80/page1.html');
		exit;
	} else {
		header('Location: https://localhost:80/index.html');
		exit;
	}
?> 