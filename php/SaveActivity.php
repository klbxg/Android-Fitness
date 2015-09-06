<?php
	
	$activity = $_POST["activity"];
	$username = $_POST["username"];
	$distance = $_POST["distance"];
	$time = $_POST["time"];
	$speed = $_POST["speed"];
	$activityName = "activities/" . $username . time() . ".JPG";

	$decodedImage = base64_decode("$activity");
	file_put_contents($activityName, $decodedImage);

	$con = mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");
	$statement = mysqli_prepare($con, "INSERT INTO activities (owner, distance, time, speed, activityImage) VALUES (?, ?, ?, ?, ?)");
	mysqli_stmt_bind_param($statement, "sssss", $username, $distance, $time, $speed, $activityName); 
	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>