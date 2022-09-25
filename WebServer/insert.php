<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');


    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 MySQL에 값을 전달

        $email=$_POST['email'];
        $intake=$_POST['intake'];

        if(empty($email)){
            $errMSG = "이메일을 입력하세요.";
        }
        else {
			if(empty($intake)){ // intake가 입력되지 않았다면 기본갑인 0으로 insert
				$stmt = $con->prepare('INSERT INTO water(email) VALUES(:email)');
				$stmt->bindParam(':email', $email);
				}
			if(!isset($errMSG)) { // 모두 입력이 되었다면 
				// SQL문을 실행하여 데이터를 MySQL 서버의 water 테이블에 저장
				$stmt = $con->prepare('INSERT INTO water(email, intake) VALUES(:email, :intake)');
				$stmt->bindParam(':email', $email);
				$stmt->bindParam(':intake', $intake);
			}
			try {
				if($stmt->execute()) {
					$successMSG = "새로운 행을 추가했습니다.";
				}
				else {
					$errMSG = "행 추가 에러";
				}
			} catch(PDOException $e) {
						die("Database error: " . $e->getMessage()); 
						}
		}

    }

?>


<?php 
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

 $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
   
    if( !$android )
    {
?>
    <html>
       <body>

            <form action="<?php $_PHP_SELF ?>" method="POST">
                email: <input type = "text" name = "email" />
                Intake: <input type = "text" name = "intake" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>