package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
    private static final int SOCKET_PORT = 12245;
    private static final int SLEEP = 5000;
    private static final String DEFAULT_TOPIC = "tec502/pbl2/fog";
    /*------------------------------------------------------------------------*/

    private static final List<PatientDevice> patientDevices
            = Collections.synchronizedList(new ArrayList());

    private static final ArrayList<FogHandler> fogHandler = new ArrayList<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private static int listLength;
    private static ServerSocket serverFog;

    /**
     * Faz o controle da criação de novas threads.
     */
    private static int threadCreationControl = -1;

    public static void main(String[] args) {
        listLength = patientDevices.size();

        MQTTClient mqttClient
                = new MQTTClient(
                        "tcp://broker.mqttdashboard.com:1883",
                        null,
                        null
                );
        mqttClient.connect();

        new FogListener(mqttClient, "tec502/pbl2/fog", 0);

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
                        = DEFAULT_TOPIC + "/"
                        + System.currentTimeMillis()
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
     * @return PatientDevice | null
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
     * @param amount int - Quantidade de pacientes requisitados.
     */
    public static void sendToServer(
            String httpMethod,
            String route,
            Socket conn,
            int amount
    ) {
        JSONObject json = patientsDevicesToJSON(httpMethod, route, amount);

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
                            Socket connection = serverFog.accept();

                            ObjectInputStream input
                                    = new ObjectInputStream(connection.getInputStream());

                            JSONObject request = (JSONObject) input.readObject();

                            if (request
                                    .getString("method")
                                    .equals("GET")
                                    && request
                                            .getString("route")
                                            .contains("/patients/")) {
                                String[] temp
                                        = request.getString("route").split("/");

                                sendToServer(
                                        "POST",
                                        "/patients",
                                        connection,
                                        Integer.parseInt(temp[2])
                                );
                            }
                            connection.close();
                        }

                        Thread.sleep(SLEEP);
                    } catch (ClassNotFoundException cnfe) {
                        System.err.println("Servidor não encontrado ou "
                                + "está fora do ar.");
                        System.out.println(cnfe);
                    } catch (IOException ioe) {
                        System.err.println("Classe JSONObject não foi "
                                + "encontrada.");
                        System.out.println(ioe);
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
    }

    /**
     * Monta um JSON com as dados de um número específicos de pacientes.
     *
     * @param httpMethod String - Método HTTP da requisição que será feita.
     * @param route String - Rota para a qual a requisição será feita.
     * @param amount int - Quantidade de pacientes requisitados.
     */
    private static JSONObject patientsDevicesToJSON(
            String httpMethod,
            String route,
            int amount
    ) {
        ArrayList<PatientDevice> temp = new ArrayList<>();
        JSONObject json = new JSONObject();

        /* Definindo os dados que serão enviadas para o servidor. */
        json.put("method", httpMethod); // Método HTTP
        json.put("route", route); // Rota

        /**
         * Verifica se tem a quantidade de pacientes requisitada existe na
         * lista.
         */
        int listSize = (amount > patientDevices.size())
                ? patientDevices.size()
                : amount;

        for (int i = 0; i < listSize; i++) {
            temp.add(patientDevices.get(i));
        }

        json.put("body", temp); // Adicionando o Array no JSON que será enviado

        return json;
    }
}
