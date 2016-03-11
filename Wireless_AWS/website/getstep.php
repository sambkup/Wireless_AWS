<!DOCTYPE html>
<html>
<head>
<style>
table {
  width: 100%;
  border-collapse: collapse;
}

table, td, th {
  border: 1px solid black;
  padding: 5px;
}

th {text-align: left;}
</style>
</head>
<body>

  <?php
  $seqNum = intval($_GET['seqNum']);
  $name = strval($_GET['name']);

  $con = mysqli_connect('localhost','root','maggie','wirelessdb');
  if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
  }

  mysqli_select_db($con,"ajax_demo");
  $sql="SELECT * FROM steps WHERE name = '".$name."' AND seqNum = $seqNum;";
  $result = mysqli_query($con,$sql);




  echo "<table>
  <tr>
  <th>lattitude</th>
  <th>longitude</th>
  <th>vector_x</th>
  <th>vector_y</th>
  <th>name</th>
  <th>seqNum</th>
  </tr>";
  $outp = "[";
  while($row = mysqli_fetch_array($result)) {
    echo "<tr>";
    echo "<td>" . $row['lattitude'] . "</td>";
    echo "<td>" . $row['longitude'] . "</td>";
    echo "<td>" . $row['vector_x'] . "</td>";
    echo "<td>" . $row['vector_y'] . "</td>";
    echo "<td>" . $row['name'] . "</td>";
    echo "<td>" . $row['seqNum'] . "</td>";
    echo "</tr>";
      if ($outp != "[") {$outp .= ",";}
      $outp .= '{"lattitude":"'  . $row["lattitude"] . '",';
      $outp .= '{"longitude":"'  . $row["longitude"] . '",';
      $outp .= '{"vector_x":"'  . $row["vector_x"] . '",';
      $outp .= '"vector_y":"'   . $row["vector_y"]        . '",';
      $outp .= '"name":"'. $row["name"]     . '"}';
      $outp .= '"seqNum":"'. $row["seqNum"]     . '"}';
  }
  $outp .="]";
  echo "</table>";
  echo "<p>$outp</p>";
  mysqli_close($con);
  ?>
</body>
</html>
