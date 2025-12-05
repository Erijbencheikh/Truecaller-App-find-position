<?php
require_once 'config.php';

// Connect to the database
$con = mysqli_connect($server, $user, $mp, $database, $port);

// Check connection
if (mysqli_connect_errno()) {
    die(json_encode(["success" => 0, "message" => "Connection failed: " . mysqli_connect_error()]));
}

// Retrieve parameters from POST or GET
$pseudo = $_POST['pseudo'] ?? $_GET['pseudo'] ?? null;
$numero = $_POST['numero'] ?? $_GET['numero'] ?? null;
$longitude = $_POST['longitude'] ?? $_GET['longitude'] ?? null;
$latitude = $_POST['latitude'] ?? $_GET['latitude'] ?? null;

// Check if all parameters are provided
if ($pseudo && $numero && $longitude && $latitude) {
    $sql = "INSERT INTO Position (pseudo, numero, longitude, latitude)
            VALUES ('$pseudo', '$numero', '$longitude', '$latitude')";
    
    if (mysqli_query($con, $sql)) {
        $response["success"] = 1;
        $response["message"] = "Position added successfully";
    } else {
        $response["success"] = 0;
        $response["message"] = "Error: " . mysqli_error($con);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Missing parameters";
}

echo json_encode($response);

// Close connection
mysqli_close($con);
?>
