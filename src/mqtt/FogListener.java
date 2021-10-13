package mqtt;

import java.util.Collections;
import main.Fog;
import models.PatientDevice;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import utils.ComparePatients;

/**
 * Lida com a mensagem recebida pelo Broker MQTT.
 *
 * @author Allan Capistrano
 */
public class FogListener implements IMqttMessageListener {

    /*-------------------------- Constantes ----------------------------------*/
    private static final String MQTT_ADDRESS = "tcp://broker.mqttdashboard.com:1883";
    private static final String DEFAULT_PUBLISH_TOPIC = "tec502/pbl2/fog/";
    private static final String RESPONSE_TOPIC = "tec502/pbl2/patientDevice/";
    private static final int QOS = 0;
    /*------------------------------------------------------------------------*/

    public static String clientTopic;

    private final MQTTClient clientMQTT;
    private final String sensorsTopic;
    private String region;

    /**
     * Método construtor.
     *
     * @param clientMQTT MQTTClient Cliente MQTT conectado com o Broker.
     * @param topic String - Tópico para realizar a inscrição.
     * @param qos int - Qualidade do serviço.
     * @param region String - Região a qual a Fog pertence.
     */
    public FogListener(MQTTClient clientMQTT, String topic, int qos, String region) {
        this.sensorsTopic = topic;
        this.clientMQTT = clientMQTT;
        this.region = region;

        /**
         * Se inscreve no tópico.
         */
        this.clientMQTT.subscribe(qos, this, topic);
    }
    
    /**
     * Método construtor.
     *
     * @param clientMQTT MQTTClient Cliente MQTT conectado com o Broker.
     * @param topic String - Tópico para realizar a inscrição.
     * @param qos int - Qualidade do serviço.
     */
    public FogListener(MQTTClient clientMQTT, String topic, int qos) {
        this.sensorsTopic = topic;
        this.clientMQTT = clientMQTT;

        /**
         * Se inscreve no tópico.
         */
        this.clientMQTT.subscribe(qos, this, topic);
    }

    /**
     * Este método é chamado quando chega uma mensagem do servidor.
     *
     * @param topic String - Tópico em que a mensagem foi publicada.
     * @param msg MqttMessage - Mensagem.
     * @throws Exception - Em caso de erro, o cliente será encerrado.
     */
    @Override
    public void messageArrived(String topic, MqttMessage msg) throws Exception {
        /**
         * Caso tenha recebido a mensagem certa, responde com o tópico que o
         * dispositivo deverá publicar.
         */
        if (topic.equals(DEFAULT_PUBLISH_TOPIC + this.region)) {
            this.response();
        } else if (topic.equals(this.sensorsTopic) && !this.sensorsTopic.equals("")) {
            JSONObject json = new JSONObject(new String(msg.getPayload()));

            this.jsonHandler(json);

            System.out.println(json);
        }
    }

    /**
     * Responde para o dispositivo qual o tópico que ele deve publicar.
     */
    private void response() {
        MQTTClient response = new MQTTClient(MQTT_ADDRESS, null, null);
        response.connect();

        JSONObject json = new JSONObject();

        json.put("topic", clientTopic);

        response.publish(RESPONSE_TOPIC + this.region, json.toString().getBytes(), QOS);
    }

    /**
     * Lida com o JSON enviado pelo dispositivo de sensores dos pacientes.
     *
     * @param json JSONObject - JSON recebido.
     */
    private void jsonHandler(JSONObject json) {
        String name = json.getString("userName");
        String deviceId = json.getString("id");
        float bodyTemperature = json.getFloat("temperature");
        int respiratoryFrequency = json.getInt("respiratoryFrequency");
        float bloodOxygen = json.getFloat("bloodOxygen");
        int bloodPressure = json.getInt("bloodPressure");
        int heartRate = json.getInt("heartRate");

        int listLength = Fog.patientDeviceListSize();

        if (listLength == 0 || !Fog.devicePatientExists(deviceId)) {
            Fog.addPatientDevice(
                    new PatientDevice(
                            name,
                            bodyTemperature,
                            respiratoryFrequency,
                            bloodOxygen,
                            bloodPressure,
                            heartRate,
                            deviceId
                    )
            );
        } else {
            for (int i = 0; i < listLength; i++) {
                if (Fog.getPatientDevice(i).getDeviceId().equals(deviceId)) {
                    Fog.getPatientDevice(i).setBodyTemperature(bodyTemperature);
                    Fog.getPatientDevice(i).setRespiratoryFrequency(respiratoryFrequency);
                    Fog.getPatientDevice(i).setBloodOxygenation(bloodOxygen);
                    Fog.getPatientDevice(i).setBloodPressure(bloodPressure);
                    Fog.getPatientDevice(i).setHeartRate(heartRate);
                    Fog.getPatientDevice(i).setPatientSeverityLevel(Fog.getPatientDevice(i).calculatePatientSeverityLevel());

                    break;
                }
            }
        }
        
        Collections.sort(Fog.getPatientsDevicesList(), new ComparePatients());
    }
}
