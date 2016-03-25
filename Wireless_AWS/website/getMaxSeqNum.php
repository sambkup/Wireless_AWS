<?php
$name = strval($_GET['name']);

$con = mysqli_connect('localhost','root','maggie','wirelessdb');
if (!$con) {
  die('Could not connect: ' . mysqli_error($con));
}

mysqli_select_db($con,"ajax_demo");
$sql="SELECT max(seqNum) AS seqNum FROM steps WHERE name = '".$name."';";
$result = mysqli_query($con,$sql);

$outp = "[";
while($row = mysqli_fetch_array($result)) {
  if ($outp != "[") {$outp .= ",";}
  $outp .= '{"seqNum":"'. $row["seqNum"]     . '"}';
}
$outp .="]";
echo ($outp);
mysqli_close($con);
?>
