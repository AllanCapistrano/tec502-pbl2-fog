package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import models.PatientDevice;
import mqtt.FogListener;
import mqtt.MQTTClient;

/**
 * Fog responsável pela comunicação com os dispositivos.
 *
 * @author Allan Capistrano e João Erick Barbosa
 */
public class Fog {

    /*-------------------------- Constantes ----------------------------------*/
    private static final int REQUEST_COUNT = 5;
    /*------------------------------------------------------------------------*/

    private static final List<PatientDevice> patientDevices
            = Collections.synchronizedList(new ArrayList());

    private static final ArrayList<FogHandler> fogHandler = new ArrayList<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private static int listLength;
    
    /**
     * Faz o controle da criação de novas threads.
     */
    private static int threadCreationControl = -1;

    public static void main(String[] args) {
        listLength = patientDevices.size();

        MQTTClient temp = new MQTTClient("tcp://broker.mqttdashboard.com:1883", null, null);
        temp.connect();

        new FogListener(temp, "tec502/pbl2/fog", 0);

        while (true) {
            listLength = patientDevices.size();

            /* Por enquanto cada thread lida com 5 requisições. */
            if (listLength % REQUEST_COUNT == 0 && listLength != threadCreationControl) {
                threadCreationControl = listLength;

                FogListener.clientTopic = "tec502/pbl2/fog" + "/" + System.currentTimeMillis() + "/" + threadCreationControl;

                /**
                 * Serviço que lida com as requisições utilizando threads.
                 */
                FogHandler fogThread = new FogHandler(FogListener.clientTopic);
                fogHandler.add(fogThread);

                /**
                 * Executando as threads.
                 */
                pool.execute(fogThread);
            }
        }
    }

    /**
     * Adiciona um dispositivo na lista de dispositivos dos pacientes.
     *
     * @param patientDevice PatientDevice - Dispositivo a ser adicionado.
     */
    public static void addPatientDevice(PatientDevice patientDevice) {
        patientDevices.add(patientDevice);
    }

    /**
     * Retorna o tamanho atual da lista de dispositivos de pacientes.
     *
     * @return int
     */
    public static int patientDeviceListSize() {
        return patientDevices.size();
    }

    /**
     * Retorna um dispositivo específico da lista de dispositivos de pacientes,
     * com base na sua posição na mesma.
     *
     * @param index int - Posição do dispositivo na lista
     * @return PatientDevice
     */
    public static PatientDevice getPatientDevice(int index) {
        return patientDevices.get(index);
    }
}
