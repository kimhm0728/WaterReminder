<?php
include "../inc/admin_session.php";

/* DB 연결 */
include "../inc/dbcon.php";

/* 쿼리 작성 */
$sql = "SELECT email
		FROM users";

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
?>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterReminder 사용자 방문 순위</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
    <link rel="stylesheet" href="../css/table.css">
	<link rel="stylesheet" href="../css/main.css">
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
<div style="float:right; margin-top:15px; margin-right:359.5px;">
    <p style="margin-bottom:5px;">사용자 총 <?php echo $num; ?>명</p>
    <table>
        <tr class="title">
            <td>번호</td>
            <td>이메일</td>
            <td>이름</td>
            <td>성별</td>
            <td>나이</td>
            <td>최근 접속 일자</td>
            <td>방문 수</td>
        </tr>

        <?php
        // for($i = 1; $i <= $num; $i++){
        /* $i = 1;
        while($array = mysqli_fetch_array($result)){ */

        /* paging : 시작 번호 = (현재 페이지 번호 - 1) * 페이지 당 보여질 데이터 수 */
        $start = ($page - 1) * $list_num;

        /* paging : 쿼리 작성 - limit 몇번부터, 몇개 */
        $sql = "SELECT a.idx, a.email, a.nickname, a.gender, a.age, 
				a.recent_visit, IFNULL( b.cnt, 0 ) AS cnt
				FROM (
				SELECT *
				FROM users
				WHERE del_yn = 'N'
				)a
				LEFT JOIN (
				SELECT u.email as email, COUNT( visit_date ) AS cnt
				FROM users u, visit v
				WHERE u.del_yn = 'N'
				AND u.email = v.email
				GROUP BY email
				)b ON a.email = b.email
				ORDER BY cnt DESC
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
            <td><?php echo $array["cnt"]; ?></td>
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
    <a href="users_rank.php?page=1">이전</a>
    <?php } else{ ?>
    <a href="users_rank.php?page=<?php echo ($page-1); ?>">이전</a>
    <?php };?>

    <?php
    /* pager : 페이지 번호 출력 */
    for($print_page = $s_pageNum; $print_page <= $e_pageNum; $print_page++){
    ?>
    <a href="users_rank.php?page=<?php echo $print_page; ?>"><?php echo $print_page; ?></a>
    <?php };?>

    <?php
    /* paging : 다음 페이지 */
    if($page >= $total_page){
    ?>
    <a href="users_rank.php?page=<?php echo $total_page; ?>">다음</a>
    <?php } else{ ?>
    <a href="users_rank.php?page=<?php echo ($page+1); ?>">다음</a>
    <?php };?>

    </p>
</div>
</body>
</html>