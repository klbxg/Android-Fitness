<?php
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];
	$wantunfollow = $_POST["wantunfollow"];
	
	$statement = mysqli_prepare($con, "DELETE FROM followInfo WHERE username = '$username' AND  followedUser = '$wantunfollow'");
	mysqli_stmt_execute($statement);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>