#include <Keypad.h>
#include <LiquidCrystal.h>
#include <SoftwareSerial.h>
#include <String.h>
#define DEBUG true  
LiquidCrystal lcd(12, 11, 5, 4, 3, 2);
char challenge[4];

const byte numRows= 4; //number of rows on the keypad
const byte numCols= 4; //number of columns on the keypad

//keymap defines the key pressed according to the row and columns just as appears on the keypad
char keymap[numRows][numCols]= 
{
{'1', '2', '3', 'A'}, 
{'4', '5', '6', 'B'}, 
{'7', '8', '9', 'C'},
{'*', '0', '#', 'D'}
};
int x=0,password_attempt=0;
const int green = 8;
int piezoStatus = LOW;
char password[6] = {'1', '2', '3', '4', '5', '6'};

//Code that shows the the keypad connections to the arduino terminals
byte rowPins[numRows] = {A0,A1,A2,A3}; //Rows 0 to 3
byte colPins[numCols]= {A4,A5,7,6}; //Columns 0 to 3
Keypad myKeypad= Keypad(makeKeymap(keymap), rowPins, colPins, numRows, numCols);

//initializes an instance of the Keypad class
SoftwareSerial esp8266(9,13);


void setup() {
Serial.begin(9600);
esp8266.begin(9600); // your esp's baud rate might be different
//starting a server

  sendCommand("AT+RST\r\n",2000,DEBUG); // reset module
  sendCommand("AT+CWMODE=1\r\n",1000,DEBUG); // configure as access point
  sendCommand("AT+CWJAP=\"D4G\",\"NoFreeInternet\"\r\n",3000,DEBUG);
  sendCommand("AT+CIFSR\r\n",3000,DEBUG); // get ip address
  sendCommand("AT+CIPMUX=1\r\n",1000,DEBUG); // configure for multiple connections
  sendCommand("AT+CIPSERVER=1,80\r\n",1000,DEBUG); // turn on server on port 80
  Serial.println("Server Ready");
pinMode(A0, INPUT_PULLUP);
digitalWrite(A0, HIGH);
pinMode(A1, INPUT_PULLUP);
digitalWrite(A1, HIGH);
pinMode(A2, INPUT_PULLUP);
digitalWrite(A2, HIGH);
pinMode(A3, INPUT_PULLUP);
digitalWrite(A3, HIGH);
pinMode(A4, INPUT_PULLUP);
digitalWrite(A4, HIGH);
pinMode(A5, INPUT_PULLUP);
digitalWrite(A5, HIGH);
pinMode(green, OUTPUT);
digitalWrite(green,LOW);

lcd.begin(16, 2);
lcd.setCursor(0, 0);
lcd.print("A-Enter password.");
lcd.setCursor(0, 1);
 //Print a message to the LCD.
lcd.print("B-Change password.");

  }
 bool challengeSend=false;
void loop() {
    //////////////////////////////////////////////////server part////////////////
 
  if(esp8266.available() && challengeSend==false) // check if the esp is sending a message 
  { 
     if(esp8266.find("+IPD,")){
    delay(1000); 
    int connectionId  = esp8266.read()-48; 
    sendChallenge(connectionId);
    challengeSend=true;}}
      if(esp8266.available()){
          while(!esp8266.find("+IPD,"));
       int connectionId  = esp8266.read()-48; 
       boolean condition=authenticate(connectionId);
       boolean conditionOne=authorize();
       if(condition && conditionOne){ 
      Serial.println("Authentication success"); 
      int count=0;
      int timer=0;
         while(true){
          if(count==2){
          break;
          }
         while(!esp8266.find("+IPD,"));
     int connectionId  = esp8266.read()-48;   
      if(esp8266.find("open")){
                Serial.println("door open");
               digitalWrite(green,!digitalRead(green));
               count++;
        }
        
         }
     
    }else{
       Serial.println("Authentication error");
      }
      challengeSend=false;
  }
//use keyboard when no one is sending data
while(!esp8266.available()){
    Serial.println("no data");
    char keypressed = myKeypad.getKey();
 if (keypressed == 'A'){
 boolean pw_correct = enter_password();
 if(pw_correct){
  digitalWrite(green, HIGH);
  menu(1000);}
  }
else if (keypressed == 'B'){
  boolean change_pw = change_password();
  }
 else if(keypressed == 'C'){  
  digitalWrite(green, LOW);
  } 
  }

}
//////////////////////////////////////////////////////keypad and display/////////////////////////////////////////////////

void clear_display(int time){ //clear display with delay time
  delay(time);
  lcd.clear();
}

void menu(int time){     //go to menu with delay time and clear display
  password_attempt=0; //set password attemted to 0
  clear_display(time);
  lcd.setCursor(0, 0);
lcd.print("A-Enter password.");
lcd.setCursor(0, 1);
  // Print a message to the LCD.
lcd.print("B-Change password.");
  }

boolean enter_password(){
  password_attempt++;
  if(password_attempt>3){
    wrong_password();
    return false;
    }
   else{ 
   boolean pw_correct = true;
  clear_display(0);
  lcd.setCursor(0, 0);
  lcd.print("Enter Password : ");
  char pw[6];
  int y=0;
  
  while(1&&y!=6){
  pw[y] = myKeypad.getKey();
  if(pw[y]!=NO_KEY){
  lcd.setCursor(y, 1);
  lcd.print("*");
  y++;
  }
  }
  for(int x=0;x<6;x++){
    if(pw[x]!=password[x]){
      pw_correct = false;
      break;
      }
    }

  if(pw_correct){
    //digitalWrite(green, HIGH);    
  clear_display(500);
  lcd.setCursor(0, 0);
  lcd.print("Password valid");
  //digitalWrite(green, LOW);    
    }
  if(!pw_correct){
  //digitalWrite(red, HIGH);    
  clear_display(500);
  lcd.setCursor(0, 0);
  lcd.print("Password invalid");
  clear_display(1000);
 // digitalWrite(red, LOW);    
  pw_correct = enter_password();
    }  
  return pw_correct;
  }
}

void wrong_password(){
  clear_display(500);
  lcd.setCursor(0, 0);
  lcd.print("You entered password 3 times incorrectly!");
  delay(500);
  for (int positionCounter = 0; positionCounter < 25; positionCounter++) {
    lcd.scrollDisplayLeft();
    delay(300);}
menu(1000);
  }

boolean change_password(){
  boolean pw_correct = enter_password();
  boolean change_pw = true;
  if(pw_correct){
    
  clear_display(0);
  lcd.setCursor(0, 0);
  lcd.print("New Password : ");
  int y=0;
    
  lcd.setCursor(0, 1);
  lcd.print("______");
  while(1&&y!=6){
  password[y] = myKeypad.getKey();
  if(password[y]!=NO_KEY){
  lcd.setCursor(y, 1);
  lcd.print(password[y]);
  y++;
  }
  }
  }
  clear_display(500);
  lcd.setCursor(0, 0);
  lcd.print("New PassWord is :");  
  lcd.setCursor(0, 1);
  for (int positionCounter = 0; positionCounter < 6; positionCounter++) {
    lcd.setCursor(positionCounter, 1);
    lcd.print(password[positionCounter]); 
    }
   delay(1000);
   menu(0);
  }  
 ////////////////////////////////////////////////////////////////////////////////////////////////////
 ///////////////////////server authentication///////////////////////////////////////////////////////////
 String sendData(String command, const int timeout, boolean debug)
{
    String response = "";
    
    int dataSize = command.length();
    char data[dataSize];
    command.toCharArray(data,dataSize);
           
    esp8266.write(data,dataSize); // send the read character to the esp8266
    if(debug)
    {
      Serial.println("\r\n====== HTTP Response From Arduino ======");
      Serial.write(data,dataSize);
      Serial.println("\r\n========================================");
    }
    
    long int time = millis();
    
    while( (time+timeout) > millis())
    {
      while(esp8266.available())
      {
        
        // The esp has data so display its output to the serial window 
        char c = esp8266.read(); // read the next character.
        response+=c;
      }  
    }
    
    if(debug)
    {
      Serial.print(response);
    }
    
    return response;
}
 
void sendCIPData(int connectionId, String data)
{
   String cipSend = "AT+CIPSEND=";
   cipSend += connectionId;
   cipSend += ",";
   cipSend +=data.length();
   Serial.println(data.length());
   cipSend +="\r\n";
   sendCommand(cipSend,1000,DEBUG);
   sendData(data,1000,DEBUG);
}
 

String sendCommand(String cmd, const int t, boolean debug)
{
int temp=0,i=0;
  while(1)
  {
    Serial.println(cmd);
    esp8266.println(cmd); 
    while(esp8266.available())
    {
      if(esp8266.find("OK"))  // Wait till wifi module returns OK
      i=8;
    }
    delay(t);
    if(i>5)
    break;
    i++;
  }
  if(i==8){
    Serial.println("OK");
    return "OK";
  }
  else{
    Serial.println("Error");
    return "ERROR";
  }
}

boolean authenticate(int connectionId){
  String uname="";
  int count=0;
  delay(1000);
  esp8266.find("username:"); // advance cursor to "pin="
    while(esp8266.available())
      { 
        count++;
        char c = esp8266.read(); // read the next character.
        if(count==7){//it takes garbage
          break;
          }
        uname+=c;
      }  
       Serial.println(uname);
    if(uname=="lashan"){
      Serial.println("username correct");
      
      return true;
    }
  return false;
 }

boolean authorize(){
     while(!esp8266.find("+IPD,"));
   
    int connectionId=esp8266.read()-48;
   
    esp8266.find("result:");
    int sum=(int)(challenge[3]-48)+(int)(challenge[0]-48);
    Serial.println(sum);
   if(esp8266.read()-48==sum){
   sendCIPData(connectionId,"connect\r\n");    
   return true;
    }
    return false;
    }

void sendChallenge(int connectionId){
  if(esp8266.find("Request")){
        challenge[0]='1';
        challenge[1]=+(char)random(8)+48;
        challenge[2]=+(char)random(8)+48;
        challenge[3]=(char)random(8)+48;
        challenge[4]='\0';  
          
       sendCIPData(connectionId,(String)challenge+"\r\n");
  
    }
  
  
  
  } 
