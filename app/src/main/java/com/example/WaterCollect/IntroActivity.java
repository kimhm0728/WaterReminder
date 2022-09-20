package com.example.WaterCollect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ImageButton loginButton, logoutButton;
    private TextView nickName;
    private ImageView profileImage;

    private Handler handler;
    private Runnable runnable;

    private static String email;
    private static String nickname;
    private static String gender;
    private static String age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        loginButton = findViewById(R.id.login);
        logoutButton = findViewById(R.id.logout);
        nickName = findViewById(R.id.nickname);
        profileImage = findViewById(R.id.profile);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

        // 카카오가 설치되어 있는지 확인 하는 메서드또한 카카오에서 제공 콜백 객체를 이용함
        Function2<OAuthToken, Throwable, Unit> callback = new  Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                updateKakaoLoginUi();
                // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
                if(oAuthToken != null) {
                    //로그인 성공 시 db에 사용자 데이터를 보냄
                    final AccountInserter task = new AccountInserter();
                    task.execute("http://" + MainActivity.getIpAddress() + "/accountInsert.php", email, nickname, gender, age);
                }
                if (throwable != null) {

                }
                updateKakaoLoginUi();
                return null;
            }
        };
        // 로그인 버튼
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(IntroActivity.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(IntroActivity.this, callback);
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(IntroActivity.this, callback);
                }
            }
        });
        // 로그아웃 버튼
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        updateKakaoLoginUi();
                        return null;
                    }
                });
            }
        });
        updateKakaoLoginUi();
    }

    private void updateKakaoLoginUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어있으면
                if (user != null) {
                    email = user.getKakaoAccount().getEmail();
                    nickname = user.getKakaoAccount().getProfile().getNickname();
                    if(String.valueOf(user.getKakaoAccount().getGender()).equals("FEMALE"))
                        gender = "여";
                    else
                        gender = "남";
                    age = String.valueOf(user.getKakaoAccount().getAgeRange());
                    age = age.substring(4, 6)+"대";

                    // 유저의 어카운트정보에 이메일
                    Log.d(TAG, "invoke: nickname " + email);
                    // 유저의 어카운트 정보의 프로파일에 닉네임
                    Log.d(TAG, "invoke: email " + nickname);
                    // 유저의 어카운트 파일의 성별
                    Log.d(TAG, "invoke: gerder " + gender);
                    // 유저의 어카운트 정보에 나이
                    Log.d(TAG, "invoke: age " + age);

                    nickName.setText("  " + nickname + "님, 안녕하세요.");

                    Glide.with(profileImage).load(user.getKakaoAccount().
                            getProfile().getProfileImageUrl()).circleCrop().into(profileImage);
                    loginButton.setVisibility(View.GONE);
                    logoutButton.setVisibility(View.VISIBLE);

                    handler.postDelayed(runnable, 3000);
                } else {
                    // 로그인이 되어 있지 않다면 위와 반대로
                    nickName.setText(null);
                    profileImage.setImageBitmap(null);
                    loginButton.setVisibility(View.VISIBLE);
                    logoutButton.setVisibility(View.GONE);
                    handler.removeCallbacks(runnable);
                }
                return null;
            }
        });
    }

    public static String getEmail() {
        return email;
    }

}
