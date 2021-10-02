package mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 * Lida com a mensagem recebida pelo Broker MQTT.
 * 
 * @author Allan Capistrano
 */
public class FogListener implements IMqttMessageListener {
    
    private static final String MQTT_ADDRESS = "tcp://broker.mqttdashboard.com:1883";
    private static final String RESPONSE_TOPIC = "tec502/pbl2/patientDevice";
    private static final int QOS = 0;
    
    private final MQTTClient clientMQTT;
    private final String clientTopic;

    public FogListener(MQTTClient clientMQTT, String topic, int qos, String clientTopic) {
        this.clientMQTT = clientMQTT;
        this.clientTopic = clientTopic;
        
        /* Se inscreve no tópico */
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
        if (topic.equals("tec502/pbl2/fog")) {
            this.response();
        } else {
            // TODO
        }
    }
    
    /**
     * Responde para o dispositivo qual o tópico que ele deve publicar.
     */
    private void response() {
        MQTTClient response = new MQTTClient(MQTT_ADDRESS, null, null);
        response.connect();
        
        JSONObject json = new JSONObject();
        
        json.put("topic", this.clientTopic);
        
        response.publish(RESPONSE_TOPIC, json.toString().getBytes(), QOS);
    }
}
