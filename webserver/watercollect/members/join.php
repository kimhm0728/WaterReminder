<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WaterCollect 회원가입</title>
	<style>
		@import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&display=swap');
	</style>
	<link rel="stylesheet" href="../css/main.css">
	<link rel="stylesheet" href="../css/menu.css">
	<link rel="stylesheet" href="../css/edit.css">
    <script type="text/javascript">
        function form_check(){
            
            // 객체 생성
            var uname = document.getElementById("uname");
            var uid = document.getElementById("uid");
            var pwd = document.getElementById("pwd");
            var repwd = document.getElementById("repwd");
            var mobile = document.getElementById("mobile");

            if(uname.value == ""){
                var err_txt = document.querySelector(".err_name");
                err_txt.textContent = "이름을 입력하세요.";
                uname.focus();
                return false;
            };

            if(uid.value == ""){
                var err_txt = document.querySelector(".err_id");
                err_txt.textContent = "아이디를 입력하세요.";
                uid.focus();
                return false;
            };
            var uid_len = uid.value.length;
            if( uid_len < 4 || uid_len > 12){
                var err_txt = document.querySelector(".err_id");
                err_txt.textContent = "아이디는 4~12글자만 입력할 수 있습니다.";
                uid.focus();
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

            if(pwd.value != repwd.value){
                var err_txt = document.querySelector(".err_repwd");
                err_txt.textContent = "비밀번호를 확인해 주세요.";
                repwd.focus();
                return false;
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

        };

        function change_email(){
            var email_dns = document.getElementById("email_dns");
            var email_sel = document.getElementById("email_sel");

            var idx = email_sel.options.selectedIndex;

            var sel_txt = email_sel.options[idx].value;
            email_dns.value = sel_txt;
        };

        function id_search(){
            window.open("search_id.php", "", "width=600, height=250, left=0, top=0");
        };

    </script>
</head>

<header>
	<h1>WaterCollect 관리자 페이지</h1>
	<nav>
		<span><a href="../index.php" class="bar q">홈으로</a></span>
		<span><a href="../login/login.php" class="q">로그인</a></span>
	</nav>
</header>

<body>
    <form name="join_form" action="insert.php" method="post" onsubmit="return form_check()">
        <fieldset style="margin:15px 0 0 15px;">
            <legend>회원가입</legend>
            <p>
                <label for="u_name" class="txt">이름</label>
                <input type="text" name="u_name" id="u_name" class="u_name">
                <br>
                <span class="err_name"></span>
            </p>

            <p>
                <label for="uid" class="txt">아이디</label>
                <input type="text" name="u_id" id="u_id" class="u_id" readonly>
                <button type="button" class="btn" onclick="id_search()">아이디 중복확인</button>
                <br>
                <span class="err_id">* 아이디는 4~12글자만 입력할 수 있습니다.</span>
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

            <p>
                <label for="" class="txt">이메일</label>
                <input type="text" name="email_id" id="email_id" class="email_id"> @ 
                <input type="text" name="email_dns" id="email_dns" class="email_dns"> 
                <select name="email_sel" id="email_sel" class="email_sel" onchange="change_email()">
                    <option value="">직접 입력</option>
                    <option value="naver.com">naver.com</option>
                    <option value="daum.net">daum.net</option>
                    <option value="gmail.com">gmail.com</option>
                </select>
            </p>
            
            <p>
                <label for="mobile" class="txt">전화번호</label>
                <input type="text" name="mobile" id="mobile" class="mobile">
                <br>
                <span class="err_mobile">"-"없이 숫자만 입력</span>
            </p>

            <p class="btn_wrap" style="margin-top: 20px;">
                <button type="button" class="btn" onclick="history.back()">이전으로</button>
                <button type="submit" class="btn">회원가입</button>
            </p>
        </fieldset>
    </form>
    <p>
    </p>
</body>
</html>