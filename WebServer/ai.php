<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');
	header('Content-Type: text/html; charset=UTF-8'); 

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

	putenv("LANG=ko_KR.UTF-8");
	setlocale(LC_ALL, 'ko_KR.utf8');


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 받아옴
        $email=$_POST['email'];
		if(empty($email)){
            $errMSG = "이메일을 입력하세요.";
        }
		else {
			exec("python lstm.py ".$email." ", $output);

			if(!$android) {
				$result = implode("\n", $output)."\n";
				$result = (int)(substr($result, -2));
				echo $result;
			}
			else {
				// 결과값을 안드로이드에 전송
				$result = implode("\n", $output)."\n";
				$result = (int)(substr($result, -2));
				echo $result;
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
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>