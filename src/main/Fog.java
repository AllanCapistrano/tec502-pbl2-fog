package main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import models.PatientDevice;
import mqtt.FogListener;
import mqtt.MQTTClient;
import org.json.JSONObject;

/**
 * Fog responsável pela comunicação com os dispositivos e com o servidor.
 *
 * @author Allan Capistrano e João Erick Barbosa
 */
public class Fog {

    /*-------------------------- Constantes ----------------------------------*/
    private static final int REQUEST_COUNT = 5;
    private static final String SOCKET_ADDRESS = "localhost";
    private static final int SOCKET_PORT = 12244;
    private static final int SLEEP = 5000;
    private static final String DEFAULT_TOPIC = "tec502/pbl2/fog";
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

        MQTTClient mqttClient = new MQTTClient("tcp://broker.mqttdashboard.com:1883", null, null);
        mqttClient.connect();

        new FogListener(mqttClient, "tec502/pbl2/fog", 0);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        /* Caso tenha algum dispositivo para ser enviado para o servidor. */
                        if (patientDeviceListSize() != 0) {
                            Socket conn = new Socket(SOCKET_ADDRESS, SOCKET_PORT);

                            sendToServer("POST", "/patients", conn);

                            conn.close();
                        }
                    } catch (UnknownHostException uhe) {
                        System.err.println("Servidor não encontrado ou "
                                + "está fora do ar.");
                        System.out.println(uhe);
                    } catch (IOException ioe) {
                        System.err.println("Erro ao tentar alterar os "
                                + "valores dos sensores.");
                        System.out.println(ioe);
                    }

                    try {
                        Thread.sleep(SLEEP);
                    } catch (InterruptedException ie) {
                        System.err.println("Não foi possível parar a Thread");
                        System.out.println(ie);
                    }
                }
            }
        });

        /* Finalizar a thread de requisição quando fechar o programa. */
        thread.setDaemon(true);
        /* Iniciar a thread de requisições. */
        thread.start();

        while (true) {
            listLength = patientDevices.size();

            /* A cada 5 dispositivos uma nova thread é criada */
            if (listLength % REQUEST_COUNT == 0 && listLength != threadCreationControl) {
                threadCreationControl = listLength;

                FogListener.clientTopic = DEFAULT_TOPIC + "/" + System.currentTimeMillis() + "/" + threadCreationControl;

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

    /**
     * Verifica se o dispositivo do paciente está presente na lista.
     *
     * @param deviceId String - Id do dispositivo;
     * @return PatientDevice | null;
     */
    public static boolean devicePatientExists(String deviceId) {
        return (patientDevices.stream()
                .filter(
                        patientDevice -> deviceId.equals(
                                patientDevice.getDeviceId()
                        )
                )
                .findFirst()
                .orElse(null) != null);
    }

    /**
     * Envia para o servidor uma requisição.
     *
     * @param httpMethod String - Método HTTP da requisição que será feita.
     * @param route String - Rota para a qual a requisição será feita.
     * @param conn Socket - Conexão que é realizada com o servidor.
     */
    public static void sendToServer(
            String httpMethod,
            String route,
            Socket conn
    ) {
        JSONObject json = new JSONObject();

        /* Definindo os dados que serão enviadas para o Server. */
        json.put("method", httpMethod); // Método HTTP
        json.put("route", route); // Rota

        json.put("body", patientDevices); // Adicionando o Array no JSON que será enviado

        try {
            ObjectOutputStream output
                    = new ObjectOutputStream(conn.getOutputStream());

            /* Enviando a requisição para o servidor. */
            output.writeObject(json);

            output.close();
        } catch (IOException ioe) {
            System.err.println("Erro ao tentar enviar os dados dos sensores "
                    + "para o servidor.");
            System.out.println(ioe);
        }
    }
}
