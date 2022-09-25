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

        if(empty($email) || empty($date)){
            $errMSG = "이메일 또는 날짜를 입력하세요.";
        }

		if(!isset($errMSG)) { // 모두 입력이 되었다면 
			// SQL문을 실행하여 데이터를 MySQL 서버의 water 테이블에 저장
			$stmt = $con->prepare('UPDATE users SET recent_visit = :date WHERE email = :email');
			$stmt->bindParam(':email', $email);
			$stmt->bindParam(':date', $date);
		}
		try {
				if($stmt->execute()) {
					$successMSG = "하나의 행을 수정했습니다.";
				}
				else {
					$errMSG = "날짜 반영 에러";
				}
			} catch(PDOException $e) {
						die("Database error: " . $e->getMessage()); 
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
                date: <input type = "text" name = "date" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>