# Smart Door Lock System 🔐 (Arduino + NodeMCU + Android App)

This project is a **Smart Door Lock System** that allows locking and unlocking a door using:
- A **Bluetooth-connected Android app** (HC-05 module)
- A **WiFi-enabled NodeMCU ESP8266** for online notifications (e.g., Telegram) *(optional upgrade)*

The system uses a **solenoid lock** controlled by a relay, and the commands `U` (Unlock) and `L` (Lock) are sent wirelessly.

---

## 📌 Features

✅ Lock/Unlock using Android App via Bluetooth  
✅ Real-time command response  
✅ Optional online alerts using ESP8266 NodeMCU  
✅ Secure control — only predefined commands allowed  
✅ Future-ready design for smart home automation

---

## 🛠 Hardware Components

| Component | Purpose |
|----------|---------|
| Arduino UNO | Main controller for lock mechanism |
| HC-05 Bluetooth Module | Wireless command receiver |
| NodeMCU ESP8266 | Internet alerts & IoT capability |
| Solenoid Lock | Physical locking mechanism |
| Relay Module | Controls lock power |
| Power Supply | 12V/5A recommended |
| Jumper Wires | Connections |

---

## 📂 Project Structure

```plaintext
Smart-Door-Lock-System/
├── Arduino/
│   └── Smart_Door_Lock.ino               ← Arduino UNO code for solenoid + stepper + HC-05
├── NodeMCU/
│   └── nodemcu_notification_code.ino     ← ESP8266 code for Telegram notifications
├── Android-App/
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/
│   │   │       │   └── com/yourcompany/yourapp/
│   │   │       │       ├── MainActivity.java
│   │   │       │       ├── LoginActivity.java
│   │   │       │       ├── LogManager.java
│   │   │       │       └── LogEntry.java
│   │   │       └── res/
│   │   │           ├── layout/
│   │   │           └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle                         ← Android project build file
├── README.md                                ← Project overview & instructions
└── LICENSE                                  ← MIT License (or as chosen)

