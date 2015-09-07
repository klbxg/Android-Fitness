<?php
	
	$feeling = $_POST["name"];
	$image = $_POST["image"];
	$username = $_POST["username"];
	$picName = "pictures/" . $username . time() . ".JPG";

	$decodedImage = base64_decode("$image");
	file_put_contents($picName, $decodedImage);

	$con = mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");
	$statement = mysqli_prepare($con, "INSERT INTO shareContent (owner, feeling, picName) VALUES (?, ?, ?)");
	mysqli_stmt_bind_param($statement, "sss", $username, $feeling, $picName); 
	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>