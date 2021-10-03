package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import models.PatientDevice;

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

    /**
     * Faz o controle da criação de novas threads.
     */
    private static int threadCreationControl = -1;

    public static void main(String[] args) {
        while (true) {
            /* Por enquanto cada thread lida com 5 requisições. */
            if (patientDevices.size() % REQUEST_COUNT == 0 && threadCreationControl != patientDevices.size()) {
                /**
                 * Serviço que lida com as requisições utilizando threads.
                 */
                FogHandler fogThread = new FogHandler(patientDevices.size()); // Trocar tópico.
                fogHandler.add(fogThread);
                
                /**
                 * Executando as threads.
                 */
                pool.execute(fogThread);

                threadCreationControl = patientDevices.size();
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
