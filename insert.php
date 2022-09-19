<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon.php');


    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");


    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달

        $device=$_POST['device'];
        $intake=$_POST['intake'];

        if(empty($device)){
            $errMSG = "기기명을 입력하세요.";
        }
        else if(empty($intake)){
            $errMSG = "섭취량을 입력하세요.";
        }

        if(!isset($errMSG)) // 모두 입력이 되었다면 
        {
            try{
                // SQL문을 실행하여 데이터를 MySQL 서버의 water 테이블에 저장
                $stmt = $con->prepare('INSERT INTO water(device, intake) VALUES(:device, :intake)');
                $stmt->bindParam(':device', $device);
                $stmt->bindParam(':intake', $intake);

                if($stmt->execute())
                {
                    $successMSG = "새로운 행을 추가했습니다.";
                }
                else
                {
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
                Device: <input type = "text" name = "device" />
                Intake: <input type = "text" name = "intake" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>