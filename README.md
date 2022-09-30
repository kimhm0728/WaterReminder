# **WaterReminder**
인공지능을 기반으로 한 개인 맞춤형 컵받침 및 애플리케이션  
호서대학교 졸업작품  
개발 기간 : 2022.08 ~ 2022.09  

## Description
<img src="https://user-images.githubusercontent.com/70271235/193272746-4b124a50-dcf7-4df1-8dd0-7ae8b5df7e74.jpg" width="200" height="400"/> <img src="https://user-images.githubusercontent.com/70271235/193275499-57f8f54a-c6fd-4e61-b779-04fd6b687b71.jpg" width="200" height="400"/> <img src="https://user-images.githubusercontent.com/70271235/193273156-02e46ee7-8be0-4c75-8ae3-e8464a35777c.jpg" width="200" height="400"/>  
<img src="https://user-images.githubusercontent.com/70271235/193272776-1683be69-7a10-40b4-a080-c3ea3803ce5a.jpg" width="200" height="400"/> <img src="https://user-images.githubusercontent.com/70271235/193273260-cf560d04-9060-41fe-a326-e96930c03b15.jpg" width="200" height="400"/> <img src="https://user-images.githubusercontent.com/70271235/193273267-b33772c5-51a5-466b-bb50-d5302b2d8b48.jpg" width="200" height="400"/>   
* 현대인들의 수분 섭취 증진을 돕기 위한 WaterReminder는 아두이노의 무게 센서로 측정한 사용자가 마신 물의 양을 블루투스를 통해 받아온다. 
* 사용자는 자신이 마신 물의 양과 하루 수분 섭취 권장량에 대한 달성 여부, 이전에 마셨던 물 섭취 그래프를 볼 수 있다. 
* 또한 정기적으로 매일 저녁 6시에 보내주는 알림과 사용자의 수분 섭취 패턴을 학습한 인공지능 모델의 알림을 통해 사용자는 건강한 수분 섭취 습관을 만들어갈 수 있다.
* LSTM 알고리즘을 사용한 인공지능 모델은 학습한 수분 섭취 패턴보다 적은 양의 수분 섭취가 감지되면 이상치라고 판단하여 사용자에게 알림을 전송한다.

## System diagram  
<img src="https://user-images.githubusercontent.com/70271235/193276560-5fd650ea-50f9-4ba7-986b-35a44cede9b7.png" width="700" height="400"/>

## Hardware
<img src="https://user-images.githubusercontent.com/70271235/193283247-eb7fcf0a-744a-4a5e-9c17-1870b5b29613.jpg" width="350" height="500"/>

## Environment
* Apache + php + MySQL (Window) : 웹 서버, 관리자 페이지
* Android Studio 11 : 애플리케이션
* Python 3.8 : LSTM
* Arduino Uno R3 : 하드웨어

## Prerequisite
* kakao login API
* Arudino HX711 library
* PhilJay MPAndroidChart
* Android Jetpack WorkManager
* from tensorflow import keras
