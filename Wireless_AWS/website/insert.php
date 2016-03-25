<?php
# get the last 5 steps
$name = strval($_GET['name']);
$lattitude = strval($_GET['latt']);
$longitude = strval($_GET['long']);
$vector_x = strval($_GET['vecx']);
$vector_y = strval($_GET['vecy']);

# TODO: sanitize the inputs!!

$con = mysqli_connect('localhost','root','maggie','wirelessdb');
if (!$con) {
  die('Could not connect: ' . mysqli_error($con));
}

mysqli_select_db($con,"ajax_demo");
#$sql="SELECT * FROM steps WHERE name = '".$name."' ORDER BY time DESC LIMIT 5;";
$sql="INSERT INTO steps VALUES(".$lattitude.",".$longitude.",".$vector_x.",".$vector_y.",'".$name."',now());";
#$sql="SELECT * FROM steps WHERE name = '".$name."' AND seqNum = $seqNum;";
$result = mysqli_query($con,$sql);

echo ($result);
mysqli_close($con);
?>
