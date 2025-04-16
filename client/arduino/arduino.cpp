#define LED_PIN 6
#define SENSOR_PIN A0

String greenhouseId = "";
unsigned long lastSendTime = 0;
const unsigned long SEND_INTERVAL = 60000;

void setup() {
  Serial.begin(9600);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);
  while (!Serial);
  Serial.println("READY");
}

void loop() {
  if (Serial.available()) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (greenhouseId == "" && cmd.startsWith("ID:")) {
      greenhouseId = cmd.substring(3);
      Serial.println("[ID SET] " + greenhouseId);
    }
    else if (cmd == "update_request" || cmd == "start_watering") {
      handleCommand(cmd);
    }
  }

  if (millis() - lastSendTime > SEND_INTERVAL && greenhouseId != "") {
    sendSensorData();
    lastSendTime = millis();
  }
}

void handleCommand(const String& cmd) {
  if (cmd == "update_request") {
    sendSensorData();
  }
  else if (cmd == "start_watering") {
    Serial.println("Start watering");
  }
}

void sendSensorData() {
  int waterLevel = readWaterSensor();
  Serial.println(greenhouseId + " " + String(0) + " " + String(waterLevel));
}

int readWaterSensor() {
  int val = analogRead(SENSOR_PIN);
  int output = map(val, 0, 1023, 255, 0);
  analogWrite(LED_PIN, output);
  return output;
}