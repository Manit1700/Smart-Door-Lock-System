#include <SoftwareSerial.h>

#define RELAY_PIN 8       // Pin connected to the relay module
#define HC05_RX 6         // RX pin of HC-05 Bluetooth module
#define HC05_TX 7         // TX pin of HC-05 Bluetooth module

SoftwareSerial hc05(HC05_RX, HC05_TX);
bool isLocked = true; // Initial state: door is locked

void setup() {
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW); // Ensure the solenoid lock starts in the locked state
  Serial.begin(9600);           // Communication with NodeMCU
  hc05.begin(9600);             // HC-05 communication

  Serial.println("System ready. Waiting for Bluetooth commands...");
}

void loop() {
  if (hc05.available()) {
    char command = hc05.read();
    if (command == 'U') {
      unlockDoor();
    } else if (command == 'L') {
      lockDoor();
    }
  }
}

void unlockDoor() {
  if (!isLocked) {
    return; // Door is already unlocked, do nothing
  }

  digitalWrite(RELAY_PIN, HIGH); // Supply power to unlock
  delay(100); // Small delay to stabilize the relay (optional)
  isLocked = false; // Update state
  notifyESP("UNLOCKED"); // Notify ESP8266
}

void lockDoor() {
  if (isLocked) {
    return; // Door is already locked, do nothing
  }

  digitalWrite(RELAY_PIN, LOW); // Cut power to lock
  delay(100); // Small delay to stabilize the relay (optional)
  isLocked = true; // Update state
  notifyESP("LOCKED"); // Notify ESP8266
}

void notifyESP(const char* status) {
  Serial.println(status); // Send status to NodeMCU (ESP8266)
}
