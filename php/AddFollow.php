<?php
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];
	$wantfollow = $_POST["wantfollow"];
	
	$statement = mysqli_prepare($con, "INSERT INTO followInfo (username, followedUser) VALUES (?, ?)");
	mysqli_stmt_bind_param($statement, "ss", $username, $wantfollow); 
	mysqli_stmt_execute($statement);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>