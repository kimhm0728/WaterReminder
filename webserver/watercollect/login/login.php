<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterCollect 로그인</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
	<link rel="stylesheet" href="../css/main.css">
	<link rel="stylesheet" href="../css/edit.css">
	<link rel="stylesheet" href="../css/menu.css">
    <script type="text/javascript">
        function login_check(){
            var u_id = document.getElementById("u_id");
            var pwd = document.getElementById("pwd");

            if(u_id.value == ""){
                var err_txt = document.querySelector(".err_id");
                err_txt.textContent = "아이디를 입력하세요.";
                u_id.focus();
                return false;
            };
            var u_id_len = u_id.value.length;
            if( u_id_len < 4 || u_id_len > 12){
                var err_txt = document.querySelector(".err_id");
                err_txt.textContent = "아이디는 4~12글자만 입력할 수 있습니다.";
                u_id.focus();
                return false;
            };

            if(pwd.value == ""){
                var err_txt = document.querySelector(".err_pwd");
                err_txt.textContent = "비밀번호를 입력하세요.";
                pwd.focus();
                return false;
            };
            var pwd_len = pwd.value.length;
            if( pwd_len < 4 || pwd_len > 8){
                var err_txt = document.querySelector(".err_pwd");
                err_txt.textContent = "비밀번호는 4~8글자만 입력할 수 있습니다.";
                pwd.focus();
                return false;
            };
        };
    </script>
</head>

<header>
	<h1>WaterCollect 관리자 페이지</h1>
	<nav>
		<span><a href="../index.php" class="bar q">홈으로</a></span>
		<span><a href="../members/join.php" class="q">회원가입</a></span>
	</nav>
</header>

<body>
    <form name="login_form" action="login_ok.php" method="post" onsubmit="return login_check()">
        <fieldset style="margin:15px 0 0 15px;">
            <legend>로그인</legend>
            <p>
                <label for="u_id" class="txt">아이디</label>
                <input type="text" name="u_id" id="u_id" class="u_id" autofocus>
                <br>
                <span class="err_id"></span>
            </p>

            <p>
                <label for="pwd" class="txt">비밀번호</label>
                <input type="password" name="pwd" id="pwd" class="pwd">
                <br>
                <span class="err_pwd"></span>
            </p>

            <p class="btn_wrap" style="margin-top: 20px;">
                <button type="button" class="btn" onclick="history.back()">이전으로</button>
                <button type="submit" class="btn">로그인</button>
            </p>
        </fieldset>
    </form>
</body>
</html>