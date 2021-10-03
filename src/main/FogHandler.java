package main;

import mqtt.FogListener;
import mqtt.MQTTClient;

/**
 * Thread da Fog que lida com as requisições de X dispositivos.
 *
 * @author Allan Capistrano e João Erick Barbosa
 */
public class FogHandler implements Runnable {

    /*-------------------------- Constantes ----------------------------------*/
    private static final String MQTT_ADDRESS = "tcp://broker.mqttdashboard.com:1883";
    private static final String DEFAULT_SUBSCRIBE_TOPIC = "tec502/pbl2/fog";
    private static final int QOS = 0;
    /*------------------------------------------------------------------------*/

    private final String clientTopic;

    /**
     * Método construtor.
     *
     * @param fogId int - Identificador da fog.
     */
    public FogHandler(int fogId) {
        MQTTClient clienteMQTT = new MQTTClient(MQTT_ADDRESS, null, null);
        clienteMQTT.connect();

        this.clientTopic = DEFAULT_SUBSCRIBE_TOPIC + "/" + System.currentTimeMillis() + "/" + fogId;

        new FogListener(clienteMQTT, DEFAULT_SUBSCRIBE_TOPIC, QOS, this.clientTopic);
        new FogListener(clienteMQTT, this.clientTopic, QOS);
    }

    @Override
    public void run() {
    }
    
}
