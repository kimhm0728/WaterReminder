#include "HX711.h"  // HX711 로드쉘 관련함수 호출
#include <SoftwareSerial.h>
#include <stdlib.h>
#define calibration_factor -7050.0  // 로드쉘 스케일 값 선언
#define DOUT 3 // 엠프 데이터 아웃 핀 넘버 선언
#define CLK 2  // 엠프 클락 핀 넘버
#define RX 8
#define TX 7
HX711 scale(DOUT, CLK);  // 엠프 핀 선언 

SoftwareSerial BTSerial(RX, TX); // serial 객체 생성 
// 아두이노의 RX은 코드의 TX, 아두이노의 TX은 코드의 RX

int LED1 = 12; // LED 핀 설정
int LED2 = 13;
long weightPre; // HX711 데이터 저장
long weight;
int water;

void setup() {
  Serial.begin(9600); // 시리얼 통신속도 설정
  BTSerial.begin(9600); // 소프트웨어 시리얼 시작
  Serial.println("HX711 scale TEST");
  
  scale.set_scale(calibration_factor);  // 스케일 지정
  scale.tare();  // 스케일 설정
  Serial.println("Readings:");
  
  // LED output 설정
  pinMode(LED1, OUTPUT); 
  pinMode(LED2, OUTPUT);

  delay(1000);
}

void loop() {
  // HX711 값 읽음
  weightPre = scale.get_units();
  delay(60000);
  weight = scale.get_units();
  
  if(weight < weightPre && weightPre - weight > 3) { 
    // 무게가 줄어든 경우 물을 섭취한 것이라고 판단
    water = weightPre - weight;
  }
  else {
    water = 0;
    }
  
  // String 변환
  String waterStr = String(water);
  Serial.println(waterStr);
  
  BTSerial.println(waterStr); // 블루투스에 데이터 전송
    
  
  // HX711 데이터에 따라 led on/off 
  if(weight > 300){ // 무게가 300g 이상이면
    digitalWrite(LED1, HIGH);
  }
  else if(weight > 100) { // 무게가 300g 이하 100g 이상이면
    digitalWrite(LED2, HIGH);
    digitalWrite(LED1, LOW);
  }
  else { // 무게가 100g 이하이면
    digitalWrite(LED1, LOW);
    digitalWrite(LED2, LOW);
    }  
}
