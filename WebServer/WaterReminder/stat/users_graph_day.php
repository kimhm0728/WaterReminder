<?php
include "../inc/admin_session.php";

/* 클릭한 사용자 정보 가져오기 */
$u_idx = $_GET["u_idx"];

/* DB 연결 */
include "../inc/dbcon.php";

/* 쿼리 작성 */
$sql = "SELECT SUM( w.intake ) AS sum, u.nickname AS name
		FROM water w, users u
		WHERE w.intake_date >= DATE_ADD( NOW( ) , INTERVAL -6
		DAY )
		AND u.email = w.email
		AND u.idx = $u_idx;";

/* 쿼리 전송 */
$result = mysqli_query($dbcon, $sql);

/* 결과 가져오기 */
$array = mysqli_fetch_array($result);
$sum = $array["sum"];
$name = $array["name"];

?>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterReminder 사용자별 섭취량</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
	<link rel="stylesheet" href="../css/main.css">
	<link rel="stylesheet" href="../css/graph.css">
	<link rel="stylesheet" href="../css/menu.css">
</head>

<header>
	<h1>WaterReminder 관리자 페이지</h1>
		<p>'<?php echo $s_name; ?>'님, 안녕하세요.</p>
	<nav>
		<span><a href="/waterreminder/index.php" class="bar q">홈으로</a></span>
		<span><a href="../members/my_edit.php" class="bar q">내 정보 수정</a></span>
		<span><a href="../login/logout.php" class="q">로그아웃</a></span>
	</nav>
</header>

<body>
<div style=" display: flex;">
<div style="margin-right:30px;">
    <ul id="navi">
        <li class="group">
            <div class="menutitle">계정 관리</div>
            <ul class="sub">
                <li><a href="../members/user.php">전체 사용자</a></li>
                <li><a href="../members/admin.php">관리자</a></li>
            </ul>
        </li>
        <li class="group">
            <div class="menutitle">인사이트 분석</div>
            <ul class="sub">
                <li><a href="today_visit.php">일간 분석</a></li>
                <li><a href="week_visit.php">주간 분석</a></li>    
                <li><a href="users_alarm.php">이상치 분석</a></li>   
                <li><a href="users_rank.php">사용자 방문 순위</a></li>
				<li><a href="">사용자별 섭취량</a></li>  						
            </ul>
        </li>  
    </ul>
</div>

<div style="margin-top:15px;">
    <p style="margin-bottom:5px">총 <?php if($sum == 0) { echo 0; } else { echo (int)($sum/1000); } ?>L <?php echo $sum%1000; ?>mL 섭취</p>
	<p class="btn_wrap" style="margin: 5px 0 5px 0;">
		<a href="users_intake.php" class="bar">이전으로</a>
		<a href="users_graph_hour.php?u_idx=<?php echo $u_idx; ?>" class="bar">시간별</a>
		<a href="" style="font-weight: bold;">일별</a>
    </p>
	<h1>사용자 '<?php echo $name;?>'님의 일별 섭취량</h1>
<div class="vGraph">
	<ul>
        <?php
        // for($i = 1; $i <= $num; $i++){
        /* $i = 1;
        while($array = mysqli_fetch_array($result)){ */

        /* paging : 쿼리 작성 - limit 몇번부터, 몇개 */
        $sql = "SELECT DATE_FORMAT( a.date, '%Y/%m/%d ' ) AS date, IFNULL( SUM( b.intake ) , 0 ) AS sum
				FROM (
				SELECT DATE_FORMAT( NOW( ) - INTERVAL( a.a )
				DAY , '%Y-%m-%d' ) AS DATE
				FROM (
				SELECT 0 AS a
				UNION ALL SELECT 1
				UNION ALL SELECT 2
				UNION ALL SELECT 3
				UNION ALL SELECT 4
				UNION ALL SELECT 5
				UNION ALL SELECT 6
				) AS a
				)a
				LEFT JOIN (
				SELECT DATE_FORMAT( w.intake_date, '%Y-%m-%d' ) AS DATE, w.intake
				FROM water w, users u
				WHERE u.email = w.email
				AND u.idx = $u_idx
				)b ON a.date = b.date
				GROUP BY a.date;";

        /* paging : 쿼리 전송 */
        $result = mysqli_query($dbcon, $sql);

        /* paging : 회원정보 가져오기(반복) */
        while($array = mysqli_fetch_array($result)){
			$cnt = $array["sum"];
			if($sum == 0) {
					$ratio = 0;
			}
			else {
				$ratio = ($cnt/$sum)*100;
			}
        ?>
		<li>
			<span class="gTerm"><?php echo $array["date"]; ?>　　</span>
			<span class="gBar" style="height:<?php echo $ratio; ?>%"><span><?php echo $cnt; ?>mL</span></span>
		</li>
        <?php  
            /* $i++; */
            /* paging */
            $cnt++;
        }; 
        ?>
		 </ul>
</div>
</div>
</body>
</html>