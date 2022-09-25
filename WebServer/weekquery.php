<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');


    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달

        $email=$_POST['email'];
        $date=$_POST['date'];

        if(empty($email)){
            $errMSG = "기기명을 입력하세요.";
        }
        else if(empty($date)){
            $errMSG = "날짜를 입력하세요.";
        }

        if(!isset($errMSG)) { // 모두 입력이 되었다면 
			$sql="SELECT SUM(intake) as sum FROM water WHERE email = '$email' AND DATE(intake_date) = DATE('$date')";
			$stmt = $con->prepare($sql);
			$stmt->execute();
 
			if ($stmt->rowCount() > 0){
				$row=$stmt->fetch(PDO::FETCH_ASSOC);
				extract($row);
				$result = $row["sum"];
				
				if(empty($result))
					$result = "0";
				
				if (!$android) {
					echo $result;
					} 
				else {
					echo $result;
					// 안드로이드에 전달하기 위해 json 형태로 변환
					// header('Content-Type: application/json; charset=utf8');
					// $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
					// echo $json;
					}
				}
        }
	}

?>


<?php 
    if (isset($errMSG)) echo $errMSG;

 $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
   
    if( !$android )
    {
?>
    <html>
       <body>

            <form action="<?php $_PHP_SELF ?>" method="POST">
                email: <input type = "text" name = "email" />
                date: <input type = "text" name = "date" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>