<?php
include "../inc/admin_session.php";

/* DB 연결 */
include "../inc/dbcon.php";


/* 쿼리 작성 */
$sql = "SELECT COUNT( visit_date ) AS cnt
		FROM visit
		WHERE visit_date
		BETWEEN DATE_ADD( NOW( ) , INTERVAL -1 WEEK )
		AND NOW( );";
		
/* 쿼리 전송 */
$result = mysqli_query($dbcon, $sql);

/* 결과 가져오기 */
// $array = mysqli_fetch_array($result); // for문
// $num = mysqli_num_rows($result);

$array = mysqli_fetch_array($result);

$sum = $array["cnt"]; // 총 방문 수

/* paging : 전체 데이터 수 */
$num = 7;

/* paging : 한 페이지 당 데이터 개수 */
$list_num = 7;

/* paging : 한 블럭 당 페이지 수 */
$page_num = 3;

/* paging : 현재 페이지 */
$page = isset($_GET["page"])? $_GET["page"] : 1;

/* paging : 전체 페이지 수 = 전체 데이터 / 페이지당 데이터 개수, ceil : 올림값, floor : 내림값, round : 반올림 */
$total_page = ceil($num / $list_num);
// echo "전체 페이지 수 : ".$total_page;

/* paging : 전체 블럭 수 = 전체 페이지 수 / 블럭 당 페이지 수 */
$total_block = ceil($total_page / $page_num);

/* paging : 현재 블럭 번호 = 현재 페이지 번호 / 블럭 당 페이지 수 */
$now_block = ceil($page / $page_num);

/* paging : 블럭 당 시작 페이지 번호 = (해당 글의 블럭번호 - 1) * 블럭당 페이지 수 + 1 */
$s_pageNum = ($now_block - 1) * $page_num + 1;
// 데이터가 0개인 경우
if($s_pageNum <= 0){
    $s_pageNum = 1;
};

/* paging : 블럭 당 마지막 페이지 번호 = 현재 블럭 번호 * 블럭 당 페이지 수 */
$e_pageNum = $now_block * $page_num;
// 마지막 번호가 전체 페이지 수를 넘지 않도록
if($e_pageNum > $total_page){
    $e_pageNum = $total_page;
};
?>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterCollect 주간 분석</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
    <link rel="stylesheet" href="../css/table.css">
	<link rel="stylesheet" href="../css/main.css">
	<link rel="stylesheet" href="../css/menu.css">
	<link rel="stylesheet" href="../css/graph.css">
</head>

<header>
	<h1>WaterCollect 관리자 페이지</h1>
		<p>'<?php echo $s_name; ?>'님, 안녕하세요.</p>
	<nav>
		<span><a href="/watercollect/index.php" class="bar q">홈으로</a></span>
		<span><a href="../members/my_edit.php" class="bar q">내 정보 수정</a></span>
		<span><a href="../login/logout.php" class="q">로그아웃</a></span>
	</nav>
</header>

<body>
<div style="float:left;">
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
                <li><a href="users_rank.php">사용자 방문 순위</a></li>    				
            </ul>
        </li>  
    </ul>
</div>
<div style="float:right; margin-top:15px; margin-right:590.5px;">
    <p style="margin-bottom:5px">총 <?php echo $sum; ?>번 방문</p>
	<h1>주간 방문 수</h1>
<div class="vGraph">
	<ul>
        <?php
        // for($i = 1; $i <= $num; $i++){
        /* $i = 1;
        while($array = mysqli_fetch_array($result)){ */

        /* paging : 시작 번호 = (현재 페이지 번호 - 1) * 페이지 당 보여질 데이터 수 */
        $start = ($page - 1) * $list_num;

        /* paging : 쿼리 작성 - limit 몇번부터, 몇개 */
        $sql = "SELECT a.date, IFNULL( b.cnt, 0 ) AS cnt
			FROM (
			SELECT CURDATE( ) - INTERVAL( a.a )
			DAY AS DATE
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
			SELECT DATE_FORMAT( v.visit_date, '%Y-%m-%d' ) AS DATE, COUNT( v.visit_date ) AS cnt
			FROM users u, visit v
			WHERE u.email = v.email
			AND u.del_yn = 'N'
			GROUP BY DATE
			)b ON a.date = b.date
			limit $start, $list_num;";

        /* paging : 쿼리 전송 */
        $result = mysqli_query($dbcon, $sql);

        /* paging : 글번호 */
        $cnt = $start + 1;

        /* paging : 회원정보 가져오기(반복) */
        while($array = mysqli_fetch_array($result)){
			$cnt = $array["cnt"];
			$ratio = ($cnt/$sum)*100;
        ?>
		<li>
			<span class="gTerm"><?php echo $array["date"]; ?></span>
			<span class="gBar" style="height:<?php echo $ratio; ?>%"><span><?php echo $cnt; ?></span></span>
		</li>
        <?php  
            /* $i++; */
            /* paging */
            $cnt++;
        }; 
        ?>
		 </ul>
</div>
</body>
</html>