#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>

// WiFi Credentials
const char* ssid = "";        // Replace with your WiFi SSID
const char* password = ""; // Replace with your WiFi password

// Telegram Bot Token and Chat ID
const char* botToken = ""; // Replace with your Telegram Bot Token
const char* chatID = ""; // Replace with your Telegram Chat ID

WiFiClientSecure client;
String message;

void setup() {
  Serial.begin(9600); // Communication with Arduino
  delay(1000);

  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);

  // Wait until connected to WiFi
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");
}

void loop() {
  if (Serial.available()) {
    String command = Serial.readStringUntil('\n'); // Read command from Arduino
    command.trim(); // Remove any extra spaces or newlines

    if (command == "LOCKED") {
      message = "The door has been locked!";
    } else if (command == "UNLOCKED") {
      message = "The door has been unlocked!";
    }else {
      return; // Ignore unknown commands
    }

    sendTelegramMessage(message);
  }
}

void sendTelegramMessage(String message) {
  if (WiFi.status() == WL_CONNECTED) {
    client.setInsecure(); // Skip SSL certificate validation
    String url = "https://api.telegram.org/bot" + String(botToken) + "/sendMessage?chat_id=" + String(chatID) + "&text=" + message;

    Serial.println("Sending Telegram notification...");
    if (client.connect("api.telegram.org", 443)) {
      Serial.println("Connected to Telegram server.");
      client.println("GET " + url + " HTTP/1.1");
      client.println("Host: api.telegram.org");
      client.println("Connection: close");
      client.println();

      // Wait for the response
      while (client.connected() || client.available()) {
        if (client.available()) {
          String line = client.readStringUntil('\n');
          Serial.println(line); // Print server response for debugging
        }
      }
      client.stop();
      Serial.println("Notification sent!");
    } else {
      Serial.println("Failed to connect to Telegram server.");
    }
  } else {
    Serial.println("WiFi not connected.");
  }
}
