package utils;

import java.util.Comparator;
import models.PatientDevice;

/**
 * Compara dois pacientes em estado grave com base no nível de crítico dos
 * mesmos
 *
 * @author Allan Capistrano e João Erick Barbosa.
 */
public class ComparePatients implements Comparator<PatientDevice> {

    @Override
    public int compare(PatientDevice pd1, PatientDevice pd2) {
        if (pd1.getPatientSeverityLevel() > pd2.getPatientSeverityLevel()) {
            /* Coloca o mais a esquerda da lista. */
            return -1;
        } else if (pd1.getPatientSeverityLevel() < pd2.getPatientSeverityLevel()) {
            /* Coloca o mais a direita da lista. */
            return 1;
        }

        return 0;
    }
}
