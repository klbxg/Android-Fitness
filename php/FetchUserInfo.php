<?php
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];
	$password = $_POST["password"];

	$statement = mysqli_prepare($con, "SELECT * FROM userinfo WHERE username = ? AND password = ?");
	mysqli_stmt_bind_param($statement, "ss", $username, $password); 
	mysqli_stmt_execute($statement);

	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $id, $name, $age, $email, $username, $password);

	$user = array();
	while(mysqli_stmt_fetch($statement)) {
		$user[name] = $name;
		$user[age] = $age;
		$user[email] = $email;
		$user[username] = $username;
	}

	echo json_encode($user);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>