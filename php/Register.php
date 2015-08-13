<?php
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$name = $_POST["name"];
	$age = $_POST["age"];
	$email = $_POST["email"];
	$username = $_POST["username"];
	$password = $_POST["password"];

	$statement = mysqli_prepare($con, "INSERT INTO userinfo (name, age, email, username, password) VALUES (?, ?, ?, ?, ?)");
	mysqli_stmt_bind_param($statement, "sisss", $name, $age, $email, $username, $password); 
	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>