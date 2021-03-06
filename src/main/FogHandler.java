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
    private static final int QOS = 0;
    /*------------------------------------------------------------------------*/
    
    private final String clientTopic;
    private final MQTTClient clienteMQTT;
    
    /**
     * Método construtor.
     *
     * @param clientTopic String - Tópico no qual o dispositivo irá publicar os
     * valores medidos pelos sensores.
     */
    public FogHandler(String clientTopic) {
        this.clienteMQTT = new MQTTClient(Fog.MQTT_ADDRESS, null, null);
        this.clienteMQTT.connect();
        
        this.clientTopic = clientTopic;
    }

    @Override
    public void run() {
        /**
         * Iniciando um novo ouvinte, que realizará a assinatura no tópico 
         * em que o dispositivo irá publicar os valores dos sensores.
         */
        new FogListener(this.clienteMQTT, this.clientTopic, QOS);
    }

}
