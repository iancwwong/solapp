/*

  Official code to be run on the Sol device
  
  Connections:
    Bluetooth Module (HC-06):
      - Module TX to D10
      - Module RX to D11
      - Module Vcc to 5V
      - Module GND to GND
    
    UV Sensor (ML8511):
      - Sensor 3.3 to both 3.3V and pin A1
      - Sensor GND to GND
      - Sensor OUT to pin A0
      - Sensor EN to 3.3V
    
    Mini Pushbutton:
      - Arduino pin 2 to a pushbutton pin
      - Arduino GND to the corresponding pushbutton pin

    LED:
      - Arduino pin 12 to LED anode (longer leg)
      - Arduino GND to LED cathode (shorter leg)
  
  Adapted from code in the SparkFun codebase, licensed as beerware
  https://github.com/sparkfun/ML8511_Breakout

  Written by New South Solutions

*/

#include <SoftwareSerial.h>

// pin definitions
#define UV_PIN A0 // pin for uv sensor output
#define REF_3V3 A1 // reference 3.3V power on the Arduino board
#define LED_PIN 12 // pin for led
#define BUTTON_PIN 2 // pin for pushbutton

// time definitions
#define ONE_SECOND 1000 // milliseconds
#define SECONDS_PER_MINUTE 60
#define MINUTES_PER_HOUR 60
#define SECONDS_PER_HOUR SECONDS_PER_MINUTE*MINUTES_PER_HOUR
#define MAX_MINUTES 6*MINUTES_PER_HOUR // maximum storage size for arduino uno
#define TIMER_LIMIT_SECONDS 3*SECONDS_PER_HOUR // timer limit before blinking led and sending reminder message

// definitions for received bluetooth commands
#define SYNC '0'
#define SINGLE_READ '1'

// creates a virtual bluetooth serial port
SoftwareSerial BT(10, 11);

// storage arrays for data logging
float uvHistory[MAX_MINUTES];
float uvMinute[SECONDS_PER_MINUTE];

// timers and counter
byte seconds = 0;
unsigned int minutes = 0;
unsigned long timerSeconds = 0;
unsigned long refMillis = 0;

// output variable for led
int ledOut = LOW;

// flag for whether a reminder was sent after 3 hours have passed since reset
bool note_sent = false;

// stores incoming character from bluetooth
char bt_in;

void setup() {
  // initialise UV sensor pins
  pinMode(UV_PIN, INPUT);
  pinMode(REF_3V3, INPUT);
  
  // initialise button pins
  pinMode(BUTTON_PIN, INPUT);
  digitalWrite(BUTTON_PIN, HIGH); // active low
  
  // initialise led pin
  pinMode(LED_PIN, OUTPUT);
  
  // set the data rate for the Bluetooth port
  BT.begin(9600);
  
  // store reference time
  refMillis = millis();
}

void loop() {
  // one second passed
  if((unsigned long)(millis() - refMillis) >= ONE_SECOND) {
    refMillis = millis(); // get new reference time
    timerSeconds++; // increment led timer
    uvMinute[seconds++] = readUV(); // get and store new uv value
    
    // calculate minute average if 60 seconds passed, and reset seconds count
    if(seconds == SECONDS_PER_MINUTE) {
      if(minutes < MAX_MINUTES)
        uvHistory[minutes++] = arrayAverage(uvMinute, SECONDS_PER_MINUTE);
      seconds = 0;
    }
    
    // timer is past limit
    if(timerSeconds >= TIMER_LIMIT_SECONDS) {
      // send reminder message if not sent yet
      if(!note_sent) {
        BT.println("#note&");
        note_sent = true;
      }
      
      // blink the led
      if(ledOut == LOW) ledOut = HIGH;
      else ledOut = LOW;
      digitalWrite(LED_PIN, ledOut);
    }
  }
  
  // if the button is pressed, reset led timer and reminder message flag, turn off led
  if (buttonIsPressed()) {
    timerSeconds = 0;
    note_sent = false;
    ledOut = LOW;
    digitalWrite(LED_PIN, ledOut);
  }
  
  // check if a command was sent over bluetooth
  if(BT.available()) {
    bt_in = BT.read();
    // data dump if sync was requested
    if(bt_in == SYNC) {
      sendAllData();
      // reset array indices
      seconds = 0;
      minutes = 0;
      refMillis = millis();
    } else if(bt_in == SINGLE_READ) {
      sendSingle(readUV());
    }
  }
}

// function to read the state of the button
bool buttonIsPressed() {
  return !digitalRead(BUTTON_PIN); // active low
}

// format: #n+n+...+n+~
void sendAllData() {
  BT.print('#');
  for(int i = 0;i < minutes;i++) {
    BT.print(uvHistory[i]);// mW/cm^2
    BT.print('+');
  }
  BT.print('~');
  BT.println();
}

// format: #n$
void sendSingle(float val) {
  BT.print('#');
  BT.print(val);
  BT.print('$');
  BT.println();
}

// reads the uv sensor pin and converts the voltage into mW/cm^2
float readUV() {
  int uvLevel = averageAnalogRead(UV_PIN, 8);
  int refLevel = averageAnalogRead(REF_3V3, 8);
  
  //Use the 3.3V power pin as a reference to get a very accurate output value from sensor
  float outputVoltage = 3.3 / refLevel * uvLevel;
  
  float uvIntensity = mapfloat(outputVoltage, 0.99, 2.8, 0.0, 15.0); //Convert the voltage to a UV intensity level
  if(uvIntensity < 0) uvIntensity = 0;
  if(uvIntensity > 15) uvIntensity = 15;
  return uvIntensity;
}

//Takes an average of readings on a given pin
int averageAnalogRead(int pin, byte numReads) {
  unsigned int total = 0;
  
  for(int i = 0 ; i < numReads; i++)
    total += analogRead(pin);

  return total/numReads;
}

//The Arduino Map function but for floats
//From: http://forum.arduino.cc/index.php?topic=3922.0
float mapfloat(float x, float in_min, float in_max, float out_min, float out_max) {
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

// returns the average value of an array of floats
float arrayAverage(float *a, int n) {
  float total = 0;
  for(int i = 0;i < n;i++) {
    total += a[i];
  }
  return total/n;
}
