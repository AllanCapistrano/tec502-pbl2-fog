package main;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import models.PatientDevice;
import mqtt.FogListener;
import mqtt.MQTTClient;

/**
 * Fog responsável pela comunicação com os dispositivos e com o servidor.
 *
 * @author Allan Capistrano e João Erick Barbosa
 */
public class Fog {

    /*-------------------------- Constantes ----------------------------------*/
    private static final int REQUEST_COUNT = 5;
    public static final String MQTT_ADDRESS = "tcp://broker.mqttdashboard.com:1883";
    public static final String SOCKET_ADDRESS = "localhost";
    public static int SOCKET_PORT = 12240;
    private static final String DEFAULT_TOPIC = "tec502/pbl2/fog/";
    private static final int QOS = 0;
    /*------------------------------------------------------------------------*/

    private static final String regions[]
            = {"Norte", "Sul"};

    public static final List<PatientDevice> patientDevices
            = Collections.synchronizedList(new ArrayList());

    private static final ArrayList<FogHandler> fogHandler = new ArrayList<>();
    private static final ArrayList<FogRequestHandler> fogRequest = new ArrayList<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private static int listLength;
    private static ServerSocket serverFog;

    /**
     * Faz o controle da criação de novas threads.
     */
    private static int threadCreationControl = -1;

    public static void main(String[] args) {
        Scanner keyboardInput = new Scanner(System.in);
        int regionIndex = 0;

        System.out.println("Digite o número que corresponde a região dessa Fog: ");
        System.out.println("1 - Norte");
        System.out.println("2 - Sul");
        System.out.print("> ");

        try {
            regionIndex = keyboardInput.nextInt() - 1;
            SOCKET_PORT += regionIndex;
        } catch (Exception e) {
            System.err.println("Erro ao dar entrada nas opções");
            System.exit(0);
        }

        listLength = patientDevices.size();

        MQTTClient mqttClient
                = new MQTTClient(
                        MQTT_ADDRESS,
                        null,
                        null
                );
        mqttClient.connect();

        new FogListener(
                mqttClient,
                DEFAULT_TOPIC + regions[regionIndex],
                QOS,
                regions[regionIndex]
        );

        /**
         * Iniciando o servidor Fog que recebe requisições do servidor
         * principal.
         */
        initializeServerFog();

        /**
         * Cria a Thread para receber as requisições.
         */
        receiveRequest();

        while (true) {
            listLength = patientDevices.size();

            /* A cada 5 dispositivos uma nova thread é criada */
            if (listLength % REQUEST_COUNT == 0
                    && listLength != threadCreationControl) {
                threadCreationControl = listLength;

                FogListener.clientTopic
                        = DEFAULT_TOPIC
                        + regions[regionIndex]
                        + "/" + System.currentTimeMillis()
                        + "/" + threadCreationControl;

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
     * Retorna a lista de dispositivos dos pacientes.
     *
     * @return List<PatientDevice>
     */
    public static List<PatientDevice> getPatientsDevicesList() {
        return patientDevices;
    }

    /**
     * Verifica se o dispositivo do paciente está presente na lista.
     *
     * @param deviceId String - Id do dispositivo
     * @return boolean
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
     * Verifica se o dispositivo do paciente está presente na lista e o retorna.
     *
     * @param deviceId String - Id do dispositivo
     * @return PatientDevice | null
     */
    public static PatientDevice getDevicePatient(String deviceId) {
        return (patientDevices.stream()
                .filter(
                        patientDevice -> deviceId.equals(
                                patientDevice.getDeviceId()
                        )
                )
                .findFirst()
                .orElse(null));
    }

    /**
     * Inicializa o servidor da Fog.
     */
    public static void initializeServerFog() {
        try {
            serverFog = new ServerSocket();
            InetAddress addr = InetAddress.getByName(SOCKET_ADDRESS);
            InetSocketAddress inetSocket = new InetSocketAddress(addr, SOCKET_PORT);
            serverFog.bind(inetSocket);
        } catch (BindException be) {
            System.err.println("A porta já está em uso.");
            System.out.println(be);
        } catch (IOException ioe) {
            System.err.println("Erro de Entrada/Saída.");
            System.out.println(ioe);
        }
    }

    /**
     * Recebe as requisições do servidor principal.
     */
    public static void receiveRequest() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        /* Caso tenha algum dispositivo para ser enviado para o servidor. */
                        if (patientDeviceListSize() > 0) {
                            
                            /* Serviço que lida com as requisições utilizando threads. */
                            FogRequestHandler requestHandler = new FogRequestHandler(serverFog.accept());
                            fogRequest.add(requestHandler);

                            /* Executando as threads. */
                            pool.execute(requestHandler);
                        }
                    } catch (IOException ioe) {
                        System.err.println("Classe JSONObject não foi "
                                + "encontrada.");
                        System.out.println(ioe);
                    }
                }
            }
        });

        /* Finalizar a thread de requisição quando fechar o programa. */
        thread.setDaemon(true);
        /* Iniciar a thread de requisições. */
        thread.start();
    }

}
