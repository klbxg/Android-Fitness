<?php
	
	$con=mysqli_connect("mysql4.000webhost.com", "a2635382_fitness", "woaiwo+0555", "a2635382_fitDB");

	$username = $_POST["username"];

	$statement = mysqli_prepare($con, "SELECT * FROM shareContent WHERE owner = '$username' ORDER BY time DESC LIMIT 5");
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

		$contentAll[$i++] = $content;
		
	}
	$contentReturn[contents] = $contentAll;

	echo json_encode($contentReturn);
	
	mysqli_stmt_close($statement);

	mysqli_close($con);
?>
