  <?php
  # get the last 5 steps
  $name = strval($_GET['name']);

  # TODO: sanitize the inputs!!

  $con = mysqli_connect('localhost','root','maggie','wirelessdb');
  if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
  }

  mysqli_select_db($con,"ajax_demo");
  $sql="SELECT * FROM steps WHERE name = '".$name."' ORDER BY time DESC LIMIT 5;";
  #$sql="SELECT * FROM steps WHERE name = '".$name."' AND seqNum = $seqNum;";
  $result = mysqli_query($con,$sql);

  $outp = "[";
  while($row = mysqli_fetch_array($result)) {
    if ($outp != "[") {$outp .= ",";}
    $outp .= '{"lattitude":"'  . $row["lattitude"] . '",';
    $outp .= '"longitude":"'  . $row["longitude"] . '",';
    $outp .= '"vector_x":"'  . $row["vector_x"] . '",';
    $outp .= '"vector_y":"'   . $row["vector_y"]        . '",';
    $outp .= '"name":"'. $row["name"]     . '",';
    $outp .= '"timestamp":"'. $row["time"]     . '"}';
  }
  $outp .="]";
  echo ($outp);
  mysqli_close($con);
  ?>
