package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import models.PatientDevice;
import org.json.JSONObject;
import utils.PatientToJson;

/**
 * Classe que lida com as requisições do Servidor e do Monitoramento de Pacientes.
 * 
 * @author Allan Capistrano e João Erick Barbosa
 */
public class FogRequestHandler implements Runnable  {

    private final Socket connection;
    private final ObjectInputStream input;
    private JSONObject received;
    
    /**
     * Método construtor.
     *
     * @param connection Socket - Conexão com o Client.
     * @throws IOException
     */
    public FogRequestHandler(Socket connection) throws IOException {
        this.connection = connection;

        this.input = new ObjectInputStream(connection.getInputStream());
    }

    @Override
    public void run() {
        try {
            /* Requisição recebida. */
            this.received = (JSONObject) this.input.readObject();

            /* Processandos a requisição. */
            this.processRequests(this.received);

            /* Finalizando as conexões. */
            input.close();
            connection.close();
        } catch (IOException ioe) {
            System.err.println("Erro de Entrada/Saída.");
            System.out.println(ioe);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Classe JSONObject não foi encontrada.");
            System.out.println(cnfe);
        }
    }
    
    /**
     * Processa as requisições que são enviados ao servidor.
     *
     * @param httpRequest JSONObject - Requisição HTTP.
     */
    private void processRequests(JSONObject request) {
        
        if (request
                .getString("method")
                .equals("GET")
                && request
                        .getString("route")
                        .contains("/patients")) {
            

            String[] temp = request.getString("route").split("/");

            sendToServer(
                    "POST",
                    "/patients",
                    Integer.parseInt(temp[2]),
                    connection
            );
        } else if (request
                .getString("method")
                .equals("GET")
                && request
                        .getString("route")
                        .contains("/patient")) {
            
            String[] temp = request.getString("route").split("/");

            sendToMonitoring(
                    temp[2],
                    connection
            );
        }
    }
    
    /**
     * Envia para o servidor uma requisição.
     *
     * @param httpMethod String - Método HTTP da requisição que será feita.
     * @param route String - Rota para a qual a requisição será feita.
     * @param amount int - Quantidade de dispositivos de pacientes.
     * @param conn Socket - Conexão que é realizada com o servidor.
     */
    private static void sendToServer(
            String httpMethod,
            String route,
            int amount,
            Socket conn
    ) {
        amount = amount > Fog.patientDeviceListSize()
                ? Fog.patientDeviceListSize()
                : amount;

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
     * Envia para o monitoramento de pacientes uma requisição.
     *
     * @param deviceId String - Identificador do dispositivo.
     * @param conn Socket - Conexão que é realizada com o servidor.
     */
    private static void sendToMonitoring(
            String deviceId,
            Socket conn
    ) {

        JSONObject json = patientDeviceToJSON(deviceId);

        try {
            ObjectOutputStream output
                    = new ObjectOutputStream(conn.getOutputStream());
            
            if(json == null){
                json.put("error", "Paciente não encontrado.");
            }

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
     * Monta um JSON com os dados de um número específicos de pacientes.
     *
     * @param httpMethod String - Método HTTP da requisição que será feita.
     * @param route String - Rota para a qual a requisição será feita.
     * @param amount int - Quantidade de dispositivos de pacientes.
     */
    private static JSONObject patientsDevicesToJSON(
            String httpMethod,
            String route,
            int amount
    ) {
        List<PatientDevice> temp = Fog.patientDevices;

        return PatientToJson.handle(temp, httpMethod, route, amount);
    }
    
    /**
     * Monta um JSON com os dados de um paciente específico.
     *
     * @param deviceId String - Identificador do dispositivo.
     */
    private static JSONObject patientDeviceToJSON(
            String deviceId
    ) {
        List<PatientDevice> temp = Fog.patientDevices;
        
        for (int i = 0; i < temp.size(); i++) {
            if(temp.get(i).getDeviceId().equals(deviceId))
                return PatientToJson.handle(temp.get(i));
        }

        return null;
        
    }
    
    
    
}
