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
    
     private static final List<PatientDevice> patientDevices
            = Collections.synchronizedList(new ArrayList());

    private static final ArrayList<FogHandler> fogHandler = new ArrayList<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    /* Faz o controle da criação de novas threads. */
    private static int threadCreationControl = -1;

    public static void main(String[] args) {
        while (true) {
            /* Por enquanto cada thread lida com 5 requisições. */
            if (patientDevices.size() % 5 == 0 && threadCreationControl != patientDevices.size()) {
                /* Serviço que lida com as requisições utilizando threads. */
                FogHandler fogThread = new FogHandler(patientDevices.size()); // Trocar tópico.
                fogHandler.add(fogThread);

                /* Executando as threads. */
                pool.execute(fogThread);
                
                threadCreationControl = patientDevices.size();
            }
        }
    }
}