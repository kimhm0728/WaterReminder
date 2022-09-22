<?php

$u_idx = $_GET["u_idx"];
// echo $idx;
// exit;


/*  DB 접속 */
include "../inc/dbcon.php";


/* 쿼리 작성 */
$sql = "update users set del_yn = 'Y' where idx=$u_idx;";
// echo $sql;
// exit;

/* 데이터베이스에 쿼리 전송 */
mysqli_query($dbcon, $sql);


/* DB(연결) 종료 */
mysqli_close($dbcon);


/* 리디렉션 */
echo "
    <script type=\"text/javascript\">
        alert(\"정상처리 되었습니다.\");
        location.href = \"user.php\";
    </script>
";
?>