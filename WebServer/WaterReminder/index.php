<?php
    session_start();

    $s_id = isset($_SESSION["s_id"])? $_SESSION["s_id"]:"";
    $s_name = isset($_SESSION["s_name"])? $_SESSION["s_name"]:"";
    // echo "Session ID : ".$s_id." / Name : ".$s_name;
?>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterReminder</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
	<link rel="stylesheet" href="css/main.css">
	<link rel="stylesheet" href="css/edit.css">
	<link rel="stylesheet" href="css/menu.css">
</head>

<header>
	<h1>WaterReminder 관리자 페이지</h1>
    <?php if(!$s_id){/* 로그인 전  */ ?>
    <p>
        <a href="login/login.php" class="bar q">로그인</a>
        <a href="members/join.php" class="q">회원가입</a>
    </p>
	<?php } else{ /* 로그인 후 */ ?>
    <p>'<?php echo $s_name; ?>'님, 안녕하세요.</p>
	<nav>
		<span><a href="" class="bar q">홈으로</a></span>
		<span><a href="members/my_edit.php" class="bar q">내 정보 수정</a></span>
		<span><a href="login/logout.php" class="q">로그아웃</a></span>
	</nav>
</header>

<body>
<div style="float:left;">
    <ul id="navi">
        <li class="group">
            <div class="menutitle">계정 관리</div>
            <ul class="sub">
                <li><a href="members/user.php">전체 사용자</a></li>
                <li><a href="members/admin.php">관리자</a></li>
            </ul>
        </li>
        <li class="group">
            <div class="menutitle">인사이트 분석</div>
            <ul class="sub">
                <li><a href="stat/today_visit.php">일간 분석</a></li>
                <li><a href="stat/week_visit.php">주간 분석</a></li>
				<li><a href="stat/users_alarm.php">이상치 분석</a></li>					
                <li><a href="stat/users_rank.php">사용자 방문 순위</a></li>
				<li><a href="stat/users_intake.php">사용자별 섭취량</a></li>	
            </ul>
        </li>  
    </ul>
</div>
<div style="float:right; margin-top:50px; margin-right:600px;">
    <p>어플 사용자를 관리할 수 있는 관리자 전용 페이지입니다.</p>
	<p>원하는 메뉴를 선택하세요.</p>
</div>
    <?php }; ?>    
</body>
</html>