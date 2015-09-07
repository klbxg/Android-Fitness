<?php
	
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];
	$offset = $_POST["offset"];

	// $username = "vivi";
	// $offset = "2";

	$statement = mysqli_prepare($con, "SELECT * FROM activities WHERE owner = '$username' ORDER BY time DESC LIMIT $offset, 5");
	mysqli_stmt_execute($statement);
	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $owner, $distance, $time, $speed, $activityImage, $timeStamp);

	$content = array();
	$contentAll = array();
	$contentReturn = array();
	$i = 0;
	while(mysqli_stmt_fetch($statement)) {
		$content[activityImage] = $activityImage;
		$content[timeStamp] = $timeStamp;

		$contentAll[$i++] = $content;
		
	}
	$contentReturn[contents] = $contentAll;

	echo json_encode($contentReturn);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>
