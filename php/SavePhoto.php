<?php

	$photo = $_POST["photo"];
	$username = $_POST["username"];
	$photoName = "photo/" . $username . ".JPG";

	$decodedPhoto = base64_decode("$photo");
	file_put_contents($photoName, $decodedPhoto);

	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");
	$statement = mysqli_prepare($con, "UPDATE userinfo SET photo = '$photoName' WHERE username = '$username'");
	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>

