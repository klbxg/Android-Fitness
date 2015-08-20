<?php

	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$followedName = $_POST["friendname"];
	$offset = $_POST["offset"];
	// $offset = 2;
	// $followedName = array();	
	// $followedName[2] = "vivi";
	// $followedName[1] = "nini";
	// $followedName[0] = "mengyye";

	$statement = mysqli_prepare($con, "SELECT * FROM shareContent WHERE owner IN ('" . implode("','", $followedName) . "') ORDER BY time DESC LIMIT $offset, 5") or die(mysqli_error($con));
	
	mysqli_stmt_execute($statement);
	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $owner, $feeling, $picName, $id, $time);

	$content = array();
	$contentAll = array();
	$contentReturn = array();
	$i = 0;
	while(mysqli_stmt_fetch($statement)) {
		$content[feeling] = $feeling;
		$content[picName] = $picName;
		$content[time] = $time;
		$content[owner] = $owner;

		$contentAll[$i++] = $content;
		
	}
	$contentReturn[contents] = $contentAll;

	echo json_encode($contentReturn);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>