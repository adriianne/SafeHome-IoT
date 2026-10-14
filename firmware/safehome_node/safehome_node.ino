/*
 * SafeHome node - 4 LED channels (prototype)
 *
 * LEDs stand in for the four relay channels until the relays and sensors arrive.
 * Database layout, matching what the Android app reads:
 *
 *   /devices/ESP32_001/channels/ch1..ch4/name     String  (set once, never overwritten)
 *   /devices/ESP32_001/channels/ch1..ch4/isOn     Bool    (written by the node)
 *   /devices/ESP32_001/channels/ch1..ch4/command  String  ("ON" | "OFF" | "TOGGLE" | "none", written by the app)
 */

#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>
#include "secrets.h"

// Must match DeviceRepository.DEVICE_ID in the Android app.
const char* NODE_ID = "ESP32_001";

struct Channel {
  const char* id;
  const char* defaultName;
  uint8_t pin;
  bool on;
};

Channel channels[] = {
  { "ch1", "Living Room Light", 4, false },
  { "ch2", "Ceiling Fan", 16, false },
  { "ch3", "Aircon", 17, false },
  { "ch4", "Kitchen Light", 18, false },
};
const int CHANNEL_COUNT = sizeof(channels) / sizeof(channels[0]);

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

bool registered = false;
unsigned long lastPollMillis = 0;
unsigned long lastBeatMillis = 0;

const unsigned long POLL_INTERVAL_MS = 400;
const unsigned long BEAT_INTERVAL_MS = 10000;

String path(const Channel& c, const char* field) {
  return String("/devices/") + NODE_ID + "/channels/" + c.id + "/" + field;
}

void applyOutput(Channel& c, bool on) {
  c.on = on;
  digitalWrite(c.pin, on ? HIGH : LOW);
}

void connectWifi() {
  Serial.printf("[WiFi] Connecting to %s", WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  for (int i = 0; i < 60 && WiFi.status() != WL_CONNECTED; i++) {
    delay(500);
    Serial.print(".");
  }
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println(" FAILED (needs a 2.4 GHz network). Restarting...");
    delay(5000);
    ESP.restart();
  }
  Serial.printf(" OK, IP %s\n", WiFi.localIP().toString().c_str());
}

// Creates each channel with its default name only if it doesn't exist yet, so a name
// the user changed later is kept. isOn is always reset to match the LED (off at boot).
void registerChannels() {
  for (int i = 0; i < CHANNEL_COUNT; i++) {
    Channel& c = channels[i];
    bool hasName = Firebase.RTDB.getString(&fbdo, path(c, "name")) && fbdo.stringData().length() > 0;
    if (!hasName) Firebase.RTDB.setString(&fbdo, path(c, "name"), c.defaultName);
    Firebase.RTDB.setBool(&fbdo, path(c, "isOn"), false);
    Firebase.RTDB.setString(&fbdo, path(c, "command"), "none");
    Serial.printf("[Firebase] %s registered (GPIO %d)\n", c.id, c.pin);
  }
  registered = true;
}

void handleCommand(Channel& c, String command) {
  command.toUpperCase();
  if (command == "ON") applyOutput(c, true);
  else if (command == "OFF") applyOutput(c, false);
  else if (command == "TOGGLE") applyOutput(c, !c.on);
  else {
    Serial.printf("[%s] Unknown command: %s\n", c.id, command.c_str());
    Firebase.RTDB.setString(&fbdo, path(c, "command"), "none");
    return;
  }
  Serial.printf("[%s] -> %s\n", c.id, c.on ? "ON" : "OFF");
  // State first so the app updates, then clear the command so it isn't run twice.
  Firebase.RTDB.setBool(&fbdo, path(c, "isOn"), c.on);
  Firebase.RTDB.setString(&fbdo, path(c, "command"), "none");
}

void pollCommands() {
  for (int i = 0; i < CHANNEL_COUNT; i++) {
    Channel& c = channels[i];
    if (Firebase.RTDB.getString(&fbdo, path(c, "command"))) {
      String command = fbdo.stringData();
      if (command.length() > 0 && command != "none") handleCommand(c, command);
    }
  }
}

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\nSafeHome node - 4 LED channels");

  for (int i = 0; i < CHANNEL_COUNT; i++) {
    pinMode(channels[i].pin, OUTPUT);
    applyOutput(channels[i], false);
  }

  connectWifi();

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.token_status_callback = tokenStatusCallback;
  Firebase.reconnectWiFi(true);
  Firebase.begin(&config, &auth);

  Serial.print("[Firebase] Connecting");
  unsigned long start = millis();
  while (!Firebase.ready() && millis() - start < 15000) {
    Serial.print(".");
    delay(500);
  }
  Serial.println(Firebase.ready() ? " ready" : " (will retry in loop)");
}

void loop() {
  if (!Firebase.ready()) {
    delay(1000);
    return;
  }
  if (!registered) registerChannels();

  unsigned long now = millis();
  if (now - lastPollMillis >= POLL_INTERVAL_MS) {
    lastPollMillis = now;
    pollCommands();
  }
  if (now - lastBeatMillis >= BEAT_INTERVAL_MS) {
    lastBeatMillis = now;
    Serial.printf("[Beat] up %lus  CH1:%d CH2:%d CH3:%d CH4:%d\n", now / 1000,
                  channels[0].on, channels[1].on, channels[2].on, channels[3].on);
  }
  delay(20);
}
