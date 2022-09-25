<?php
session_start();

/* 로그인 사용자 */
$s_idx = $_SESSION["s_idx"];

/* DB 연결 */
include "../inc/dbcon.php";

/* 쿼리 작성 */
$sql = "select * from admin where idx=$s_idx;";

/* 쿼리 전송 */
$result = mysqli_query($dbcon, $sql);

/* 결과 가져오기 */
$array = mysqli_fetch_array($result);

?>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterReminder 내 정보 수정</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
	<link rel="stylesheet" href="../css/main.css">
	<link rel="stylesheet" href="../css/menu.css">
	<link rel="stylesheet" href="../css/edit.css">
    <script type="text/javascript">
        function edit_check(){
            
            var pwd = document.getElementById("pwd");
            var repwd = document.getElementById("repwd");
            var mobile = document.getElementById("mobile");


            if(pwd.value){
                var pwd_len = pwd.value.length;
                if( pwd_len < 4 || pwd_len > 8){
                    var err_txt = document.querySelector(".err_pwd");
                    err_txt.textContent = "* 비밀번호는 4~8글자만 입력할 수 있습니다.";
                    pwd.focus();
                    return false;
                };
            };

            if(pwd.value){
                if(pwd.value != repwd.value){
                    var err_txt = document.querySelector(".err_repwd");
                    err_txt.textContent = "비밀번호를 확인해 주세요.";
                    repwd.focus();
                    return false;
                };
            };

            if(mobile.value){
                var reg_mobile = /^[0-9]+$/g;
                if(!reg_mobile.test(mobile.value)){
                    var err_txt = document.querySelector(".err_mobile");
                    err_txt.textContent = "전화번호는 숫자만 입력할 수 있습니다.";
                    mobile.focus();
                    return false;
                };
            };

            frm.action="edit_ok.php?u_idx=<?php echo $s_idx; ?>"
        };

        function change_email(){
            var email_dns = document.getElementById("email_dns");
            var email_sel = document.getElementById("email_sel");

            var idx = email_sel.options.selectedIndex;

            var sel_txt = email_sel.options[idx].value;
            email_dns.value = sel_txt;
        };

        function del_check(){
            var i = confirm("정말 삭제하시겠습니까?\n삭제한 아이디는 복원하실 수 없습니다.");

            if(i == true){
                location.href = "admin_delete.php?u_idx=<?php echo $s_idx; ?>";
            };
        };
    </script>
</head>

<header>
	<h1>WaterReminder 관리자 페이지</h1>
		<p>'<?php echo $array["name"]; ?>'님, 안녕하세요.</p>
	<nav>
		<span><a href="/waterreminder/index.php" class="bar q">홈으로</a></span>
		<span><a href="my_edit.php" class="bar q">내 정보 수정</a></span>
		<span><a href="../login/logout.php" class="q">로그아웃</a></span>
	</nav>
</header>

<body>
<div style="display: flex;">
<div style="margin-right:30px;">
    <ul id="navi">
        <li class="group">
            <div class="menutitle">계정 관리</div>
            <ul class="sub">
                <li><a href="user.php">전체 사용자</a></li>
                <li><a href="admin.php">관리자</a></li>
            </ul>
        </li>
        <li class="group">
            <div class="menutitle">인사이트 분석</div>
            <ul class="sub">
                <li><a href="../stat/today_visit.php">일간 분석</a></li>
                <li><a href="../stat/week_visit.php">주간 분석</a></li>
				<li><a href="../stat/users_alarm.php">이상치 분석</a></li>   
                <li><a href="../stat/users_rank.php">사용자 방문 순위</a></li>  
				<li><a href="../stat/users_intake.php">사용자별 섭취량</a></li>    	  				
            </ul>
        </li>  
    </ul>
</div>
<div style="margin-top:15px;">
    <form name="edit_form" action="edit_ok.php" method="post" onsubmit="return edit_check()">
        <fieldset>
            <legend>내 정보 수정</legend>
            <input type="hidden" name="u_idx" value="<?php echo $u_idx; ?>">
            <p>
                <div class="txt">이름</div>
                <?php echo $array["name"]; ?>
            </p>

            <p>
                <div class="txt">아이디</div>
                <?php echo $array["id"]; ?>
            </p>

            <p>
                <label for="pwd" class="txt">비밀번호</label>
                <input type="password" name="pwd" id="pwd" class="pwd">
                <br>
                <span class="err_pwd">* 비밀번호는 4~8글자만 입력할 수 있습니다.</span>
            </p>

            <p>
                <label for="repwd" class="txt">비밀번호 확인</label>
                <input type="password" name="repwd" id="repwd" class="repwd">
                <br>
                <span class="err_repwd"></span>
            </p>

            <?php
                // explode("기준 문자", "어떤 문장에서");
                $email = explode("@", $array["email"]);
            ?>
            <p>
                <label for="" class="txt">이메일</label>
                <input type="text" name="email_id" id="email_id" class="email_id" value="<?php echo $email[0]; ?>"> @ 
                <input type="text" name="email_dns" id="email_dns" class="email_dns" value="<?php echo $email[1]; ?>"> 
                <select name="email_sel" id="email_sel" class="email_sel" onchange="change_email()">
                    <option value="">직접 입력</option>
                    <option value="naver.com">naver.com</option>
                    <option value="daum.net">daum.net</option>
                    <option value="gmail.com">gmail.com</option>
                </select>
            </p>
            
            <p>
                <label for="mobile" class="txt">전화번호</label>
                <input type="text" name="mobile" id="mobile" class="mobile" value="<?php echo $array["mobile"]; ?>">
                <br>
                <span class="err_mobile">"-"없이 숫자만 입력</span>
            </p>

            <p class="btn_wrap" style="margin-top: 20px;">
                <button type="button" class="btn" onclick="history.back()">이전으로</button>
                <button type="button" class="btn" onclick="location.href='../index.php'">홈으로</button>
                <button type="button" class="btn" onclick="del_check()">탈퇴</button>
                <button type="submit" class="btn">정보수정</button>
            </p>
        </fieldset>
    </form>
</div>
</div>
</body>
</html>