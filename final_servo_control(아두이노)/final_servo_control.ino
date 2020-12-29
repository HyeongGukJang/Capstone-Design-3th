#include <SoftwareSerial.h>  // 블루투스 통신을 위한 SoftwareSerial 라이브러리를 불러온다.
#include <Servo.h>   //서보 라이브러리를 불러온다.
#define BT_TX 2
#define BT_RX 3

SoftwareSerial BTSerial(2, 3); // SoftwareSerial(RX, TX)
int data;
int currentVar1 = 0;
int currentVar2 = 0;
int servoState1 = 0;
int servoState2 = 0;

Servo myservo1; // 서보를 제어할 서보 오브젝트를 만듭니다.
Servo myservo2; // 서보를 제어할 서보 오브젝트를 만듭니다.

void setup (){
  myservo1.attach(9); // 서보모터를 9번 핀에 연결
  myservo2.attach(8); // 서보모터를 8번 핀에 연결
  myservo1.write(6); // 기본 서보모터1의 각도를 20도로 설정
  myservo2.write(174); // 기본 서보모터2의 각도를 20도로 설정
  BTSerial.begin(9600);//블루투스 통신속도 설정 HC=06
  Serial.begin(9600);//시리얼 통신 설정
  Serial.println("Hi smoker!!");     
}
void loop (){
  if (BTSerial.available()){ // 블루투스로 데이터 수신
    data = BTSerial.read();
    Serial.write(data); // 수신된 데이터 시리얼 모니터로 출력

    if(data == '1'){  // 열기 버튼을 눌렀을 경우
      if(servoState1 <= 180){  // 서보모터1의 각도가 175이하일 경우
        servoState1 += 179; 
        currentVar1 = constrain(servoState1,2,);//서보모터가 양 끝각에서의 떨림현상이 발생하는걸 방지
        myservo1.write(currentVar1); // 서보모터1의 각도를 150도 늘려 준다.
        delay(700); //딜레이 0.7초
        
        servoState2 -= 179; 
        currentVar2 = constrain(servoState2,2,180);
        myservo2.write(currentVar2); // 서보모터2의 각도를 120도 늘려 준다.
        delay(5000); //딜레이 5초뒤에 자동 잠김기능

        servoState2 += 179;
        currentVar2 = constrain(servoState2,1000,2);//서보모터가 양 끝각에서의 떨림현상이 발생하는걸 방지
        myservo2.write(currentVar2); //서보모터1의 각도를 120도 줄여 준다.
        delay(700); //서보모터 안정화를 위한 딜레이
        
        servoState1 -= 180;
        currentVar1 = constrain(servoState1,2,1000);//서보모터가 양 끝각에서의 떨림현상이 발생하는걸 방지
        myservo1.write(currentVar1); //서보모터1의 각도를 150도 줄여 준다
        delay(100); //서보모터 안정화를 위한 딜레이
        

        }
      }
    if(servoState1 >= 180){ //만약 서보모터1의 각도가 150 이상일때
      if(data == '1'){ // 열기 버튼을 누른다면
        Serial.println("Please close the case first"); //메세지출력
      }
    }

    if(data == '2'){  // 재충전 버튼을 눌렀을 경우
        //서보모터1의 각도가 173이하일 경우
        servoState1 += 180;
        currentVar1 = constrain(servoState1,2,1000); //서보모터가 양 끝각에서의 떨림현상이 발생하는걸 방지
        myservo1.write(currentVar1); //서보모터1의 각도를 175도 늘려 준다.
        delay(100); //서보모터 안정화를 위한 딜레이
    }
    }
  }      
      
    
