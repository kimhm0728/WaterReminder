<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');


    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달

        $email=$_POST['email'];
        $nickname=$_POST['nickname'];
		$gender=$_POST['gender'];
		$age=$_POST['age'];

        if(empty($email) || empty($nickname)){
            $errMSG = "이메일 또는 닉네임을 입력하세요.";
        }
        else {
			if(empty($gender) && empty($age)){ // gender, age가 입력되지 않은 경우
				// SQL문을 실행하여 데이터를 MySQL 서버의 water 테이블에 저장
				$stmt = $con->prepare('INSERT INTO users(email, nickname) VALUES(:email, :nickname)');
				$stmt->bindParam(':email', $email);
				$stmt->bindParam(':nickname', $nickname);
				}
			else if(empty($gender)){ // gender만 입력되지 않은 경우
				$stmt = $con->prepare('INSERT INTO users(email, nickname, age) VALUES(:email, :nickname, :age)');
				$stmt->bindParam(':email', $email);
				$stmt->bindParam(':nickname', $nickname);
				$stmt->bindParam(':age', $age);
				}
			else if(empty($age)){ // age만 입력되지 않은 경우
				$stmt = $con->prepare('INSERT INTO users(email, nickname, gender) VALUES(:email, :nickname, :gender)');
				$stmt->bindParam(':email', $email);
				$stmt->bindParam(':nickname', $nickname);
				$stmt->bindParam(':gender', $gender);
				}
			else if(!isset($errMSG)) { // 모두 입력된 경우
				$stmt = $con->prepare('INSERT INTO users(email, nickname, gender, age) VALUES(:email, :nickname, :gender, :age)');
				$stmt->bindParam(':email', $email);
				$stmt->bindParam(':nickname', $nickname);
				$stmt->bindParam(':gender', $gender);
				$stmt->bindParam(':age', $age);
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
                nickname: <input type = "text" name = "nickname" />
				gender: <input type = "text" name = "gender" />
				age: <input type = "text" name = "age" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>
