<?php
include "../inc/admin_session.php";

/* DB 연결 */
include "../inc/dbcon.php";

/* 쿼리 작성 */
$sql = "select * from users;";
		
/* 쿼리 전송 */
$result = mysqli_query($dbcon, $sql);

/* 결과 가져오기 */
// $array = mysqli_fetch_array($result); // for문
// $num = mysqli_num_rows($result);

/* paging : 전체 데이터 수 */
$num = mysqli_num_rows($result);

/* paging : 한 페이지 당 데이터 개수 */
$list_num = 5;

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

/* 쿼리 작성 */
$sql = "select count(*) as cnt from alarm;";

/* 쿼리 전송 */
$result = mysqli_query($dbcon, $sql);

/* 결과 가져오기 */
$array = mysqli_fetch_array($result);

$sum = $array["cnt"];

?>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterReminder 이상치 분석</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
    <link rel="stylesheet" href="../css/table.css">
	<link rel="stylesheet" href="../css/main.css">
	<link rel="stylesheet" href="../css/menu.css">
	<link rel="stylesheet" href="../css/graph.css">
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
				<li><a href="">이상치 분석</a></li>		
                <li><a href="users_rank.php">사용자 방문 순위</a></li>   
				<li><a href="users_intake.php">사용자별 섭취량</a></li>	 				
            </ul>
        </li>  
    </ul>
</div>
<div style="margin-top:15px;">
    <p style="margin-bottom:5px">이상치 총 <?php echo $sum; ?>번 발생</p>
	<h1 style="margin-bottom: 5px;">사용자별 이상치 분석</h1>
	<p class="btn_wrap" style="margin: 10px 0 10px 0;">
		<a href="" class="bar" style="font-weight: bold;">사용자별</a>
		<a href="users_alarm_week.php">요일별</a>
    </p>
    <table>
        <tr class="title">
            <td>번호</td>
            <td>이메일</td>
            <td>이름</td>
            <td>성별</td>
            <td>나이</td>
            <td>최근 접속 일자</td>
            <td>이상치 발생 횟수</td>
        </tr>

        <?php
        // for($i = 1; $i <= $num; $i++){
        /* $i = 1;
        while($array = mysqli_fetch_array($result)){ */

        /* paging : 시작 번호 = (현재 페이지 번호 - 1) * 페이지 당 보여질 데이터 수 */
        $start = ($page - 1) * $list_num;

        /* paging : 쿼리 작성 - limit 몇번부터, 몇개 */
        $sql = "SELECT u.email, u.nickname, u.gender, u.age, u.recent_visit, COUNT( a.alarm_date ) AS cnt
				FROM alarm a, users u
				WHERE a.email = u.email
				GROUP BY a.email 
				order by u.idx asc
				limit $start, $list_num;";

        /* paging : 쿼리 전송 */
        $result = mysqli_query($dbcon, $sql);

        /* paging : 글번호 */
        $cnt = $start + 1;

        /* paging : 회원정보 가져오기(반복) */
        while($array = mysqli_fetch_array($result)){
        ?>
        <tr class="brd">
            <!-- <td><?php echo $i; ?></td> -->
            <td><?php echo $cnt; ?></td>
            <td><?php echo $array["email"]; ?></td>
            <td><?php echo $array["nickname"]; ?></td>
            <td><?php echo $array["gender"]; ?></td>
            <td><?php echo $array["age"]; ?></td>
            <td><?php echo $array["recent_visit"]; ?></td>
			<td><?php echo $array["cnt"]; ?>번</td>
        </tr>
        <?php  
            /* $i++; */
            /* paging */
            $cnt++;
        }; 
        ?>
    </table>
	<p class="pager" style="margin-top:10px">

    <?php
    /* paging : 이전 페이지 */
    if($page <= 1){
    ?>
    <a href="users_alarm.php?page=1">이전</a>
    <?php } else{ ?>
    <a href="users_alarm.php?page=<?php echo ($page-1); ?>">이전</a>
    <?php };?>

    <?php
    /* pager : 페이지 번호 출력 */
    for($print_page = $s_pageNum; $print_page <= $e_pageNum; $print_page++){
    ?>
    <a href="users_alarm.php?page=<?php echo $print_page; ?>"><?php echo $print_page; ?></a>
    <?php };?>

    <?php
    /* paging : 다음 페이지 */
    if($page >= $total_page){
    ?>
    <a href="users_alarm.php?page=<?php echo $total_page; ?>">다음</a>
    <?php } else{ ?>
    <a href="users_alarm.php?page=<?php echo ($page+1); ?>">다음</a>
    <?php };?>

    </p>
</div>
</div>
</body>
</html>