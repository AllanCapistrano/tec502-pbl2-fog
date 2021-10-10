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
        
        if (hasStatusCode){
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

            jsonArray.put(patientDeviceJson);
        }

        json.put("data", jsonArray);

        return json;
    }
    
    /**
     * Transforma o dispositivo do paciente no formato JSON.
     *
     * @param patient PatientDevice - Dispositivo do paciente.
     * @return JSONObject
     */
    public static JSONObject handle(PatientDevice patient){
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
          
        json.put("data", jsonPatient);
        
        return json;
    }
}
