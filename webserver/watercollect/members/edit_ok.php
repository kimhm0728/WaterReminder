<?php

/* 이전 페이지에서 값 가져오기 */
$u_idx = $_POST["u_idx"];
$pwd = $_POST["pwd"];
$email = $_POST["email_id"]."@".$_POST["email_dns"];
$mobile = $_POST["mobile"];

// 값 확인
echo "IDX : ".$u_idx."<br>";
echo "비밀번호 : ".$pwd."<br>";
echo "이메일 : ".$email."<br>";
echo "전화번호 : ".$mobile."<br>";
// exit;


/*  DB 접속 */
include "../inc/dbcon.php";


/* 쿼리 작성 */
// update 테이블명 set 필드명=값, 필드명=값, ....;
if(!$pwd){
    $sql = "update users set email='$email', mobile='$mobile' where idx=$u_idx;";
} else{
    $sql = "update users set pwd='$pwd', email='$email', mobile='$mobile' where idx=$u_idx;";
};
/* echo $sql;
exit; */


/* 데이터베이스에 쿼리 전송 */
mysqli_query($dbcon, $sql);


/* DB(연결) 종료 */
mysqli_close($dbcon);


/* 리디렉션 */
echo "
    <script type=\"text/javascript\">
        alert(\"정보가 수정되었습니다.\");
        location.href = \"../index.php\";
    </script>
";
?>