<?php

	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];
	//$username = "vivi";

	$statement = mysqli_prepare($con, "SELECT * FROM followInfo WHERE username = '$username'");
	mysqli_stmt_execute($statement);
	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $id, $username, $followedUser);

	$user = array();
	$users = array();
	$i = 0;

	while(mysqli_stmt_fetch($statement)) {
		$users[$i++] = $followedUser;
	}

	$user[followedUser] = $users;
	echo json_encode($user);
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>
