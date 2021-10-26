package utils;

import java.util.List;
import models.PatientDevice;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe para transformar a lista de pacientes em JSON.
 *
 * @author Allan Capistrano
 */
public class PatientToJson {

    /**
     * Transforma a lista de pacientes no formato JSON.
     *
     * @param patientDevicesList List<PatientDevice> - Lista de pacientes.
     * @param hasStatusCode
     * @return JSONObject
     */
    public static JSONObject handle(
            List<PatientDevice> patientDevicesList,
            boolean hasStatusCode
    ) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if (hasStatusCode) {
            json.put("statusCode", 200);
        }

        for (PatientDevice patientDevice : patientDevicesList) {
            JSONObject patientDeviceJson = new JSONObject();

            patientDeviceJson.put("name",
                    patientDevice.getName());
            patientDeviceJson.put("deviceId",
                    patientDevice.getDeviceId());
            patientDeviceJson.put("isSeriousConditionLabel",
                    patientDevice.getIsSeriousConditionLabel());
            patientDeviceJson.put("fogServer", patientDevice.getFogServer());

            jsonArray.put(patientDeviceJson);
        }

        json.put("data", jsonArray);

        return json;
    }

    /**
     * Transforma a lista de pacientes no formato JSON.
     *
     * @param patientDevicesList List<PatientDevice> - Lista de pacientes.
     * @param httpMethod String - Método HTTP da requisição que será feita.
     * @param route String - Rota para a qual a requisição será feita.
     * @param amount int - Quantidade de dispositivos de pacientes.
     * @return JSONObject
     */
    public static JSONObject handle(
            List<PatientDevice> patientDevicesList,
            String httpMethod,
            String route,
            int amount
    ) {
        JSONObject json = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        /* Definindo os dados que serão enviadas para o servidor. */
        json.put("method", httpMethod); // Método HTTP
        json.put("route", route); // Rota

        for (int i = 0; i < amount; i++) {
            JSONObject patientDeviceJson = new JSONObject();

            patientDeviceJson.put("name",
                    patientDevicesList.get(i).getName());
            patientDeviceJson.put("deviceId",
                    patientDevicesList.get(i).getDeviceId());
            patientDeviceJson.put("bodyTemperature",
                    patientDevicesList.get(i).getBodyTemperature());
            patientDeviceJson.put("respiratoryFrequency",
                    patientDevicesList.get(i).getRespiratoryFrequency());
            patientDeviceJson.put("bloodOxygenation",
                    patientDevicesList.get(i).getBloodOxygenation());
            patientDeviceJson.put("bloodPressure",
                    patientDevicesList.get(i).getBloodPressure());
            patientDeviceJson.put("heartRate",
                    patientDevicesList.get(i).getHeartRate());
            patientDeviceJson.put("isSeriousCondition",
                    patientDevicesList.get(i).isIsSeriousCondition());
            patientDeviceJson.put("isSeriousConditionLabel",
                    patientDevicesList.get(i).getIsSeriousConditionLabel());
            patientDeviceJson.put("patientSeverityLevel",
                    patientDevicesList.get(i).getPatientSeverityLevel());
            patientDeviceJson.put("fogServer",
                    patientDevicesList.get(i).getFogServer());

            jsonArray.put(patientDeviceJson);
        }

        json.put("body", jsonArray); // Adicionando o Array no JSON que será enviado

        return json;
    }

    /**
     * Transforma o dispositivo do paciente no formato JSON.
     *
     * @param patient PatientDevice - Dispositivo do paciente.
     * @return JSONObject
     */
    public static JSONObject handle(PatientDevice patient) {
        JSONObject json = new JSONObject();
        json.put("statusCode", 200);

        JSONObject jsonPatient = new JSONObject();
        jsonPatient.put("name", patient.getName());
        jsonPatient.put("deviceId", patient.getDeviceId());
        jsonPatient.put("bodyTemperature", patient.getBodyTemperature());
        jsonPatient.put("respiratoryFrequency", patient.getRespiratoryFrequency());
        jsonPatient.put("bloodOxygenation", patient.getBloodOxygenation());
        jsonPatient.put("bloodPressure", patient.getBloodPressure());
        jsonPatient.put("heartRate", patient.getHeartRate());
        jsonPatient.put("isSeriousCondition", patient.isIsSeriousCondition());
        jsonPatient.put("isSeriousConditionLabel", patient.getIsSeriousConditionLabel());
        jsonPatient.put("patientSeverityLevel", patient.getPatientSeverityLevel());
        jsonPatient.put("fogServer", patient.getFogServer());

        json.put("data", jsonPatient);

        return json;
    }
}
