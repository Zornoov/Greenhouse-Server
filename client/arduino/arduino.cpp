#define ledPin 6
#define sensorPin A0

String greenhouseId = "";
unsigned long lastSendTime = 0;
const unsigned long sendInterval = 60000; // 1 минута

void setup() {
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  while (!Serial);
  Serial.println("READY");
}

void loop() {
  if (Serial.available()) {
    String command = Serial.readStringUntil('\n');
    command.trim();

    if (greenhouseId == "" && command.startsWith("ID:")) {
      greenhouseId = command.substring(3);
      Serial.println("[ID SET] " + greenhouseId);
    }
    else if (command == "update_request") {
      sendSensorData();
    }
    else if (command == "start_watering") {
      // TODO: управление поливом
      Serial.println("Start wat");
    }
  }

  if (greenhouseId != "" && millis() - lastSendTime > sendInterval) {
    sendSensorData();
    lastSendTime = millis();
  }
}

void sendSensorData() {
  int temperature = 0;
  int waterLevel = readSensor();

  String message = greenhouseId + " " + String(temperature) + " " + String(waterLevel);
  Serial.println(message);
}

int readSensor() {
  int sensorValue = analogRead(sensorPin);
  int outputValue = map(sensorValue, 0, 1023, 255, 0);
  analogWrite(ledPin, outputValue);
  return outputValue;
}