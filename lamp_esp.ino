#include <ESP8266WiFi.h>
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"

#define WLAN_SSID       "EgriHuliganok"
#define WLAN_PASS       "b8vjjchEdt5m"
#define AIO_SERVER      "192.168.0.200"
#define AIO_SERVERPORT  1883                  
#define AIO_USERNAME    ""
#define AIO_KEY         ""

const int ledPin = D4;
String val;
uint32_t delayMS;

WiFiClient client;
Adafruit_MQTT_Client mqtt(&client, AIO_SERVER, AIO_SERVERPORT, AIO_USERNAME, AIO_KEY);
Adafruit_MQTT_Subscribe bedroom_lamp = Adafruit_MQTT_Subscribe(&mqtt, AIO_USERNAME "bedroom/lamp");

void MQTT_connect(); 

void setup() {
  Serial.begin(115200); 
  delay(10);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, HIGH);

  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) 
   {
    delay(500);
    Serial.print(".");
   }
  
  mqtt.subscribe(&bedroom_lamp); 
  MQTT_connect();
  Adafruit_MQTT_Subscribe *subscription;   
}

void loop() 
  {
   Adafruit_MQTT_Subscribe *subscription; 
    while(subscription = mqtt.readSubscription(10)){
     String message((char *)bedroom_lamp.lastread);
     if(message == "on"){
       Serial.println("got on msg");
        digitalWrite(ledPin, LOW);
     }
     if(message == "off"){
       Serial.println("got off msg");
        digitalWrite(ledPin, HIGH);
     }     
      delay(10);
    }
  }

void MQTT_connect() {  
  int8_t ret;
  if (mqtt.connected()) {
    return;
  }
  Serial.print("Connecting to MQTT... ");
  uint8_t retries = 3;
  while ((ret = mqtt.connect()) != 0) { 
    Serial.println(mqtt.connectErrorString(ret));
    Serial.println("Retrying MQTT connection in 5 seconds...");
    mqtt.disconnect();
    delay(5000);  // wait 5 seconds
    retries--;
    if (retries == 0) {  
      while (1);
    }
  }
  Serial.println("MQTT Connected!");
 }
