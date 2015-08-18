<?php
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];
	$otherUsername = $_POST["otherUsername"];
	// $username = "vivi";
	// $otherUsername = "nin";
	
	$statement = mysqli_prepare($con, "SELECT * FROM followInfo WHERE username = ? AND followedUser = ?");
	mysqli_stmt_bind_param($statement, "ss", $username, $otherUsername); 
	mysqli_stmt_execute($statement);

	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $id, $username, $followedUser);

	$user = array();

	while(mysqli_stmt_fetch($statement)) {
		$user[username] = $username;
	}

	echo json_encode($user);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>