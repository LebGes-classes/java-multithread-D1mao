package Multithread;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Worker> workers = Worker.readWorkersFromExcel();
        Worker.assignTasksToWorkers(workers);
        List<Thread> workerThreads = new ArrayList<>();
        for (Worker worker : workers) {
            Thread workerThread = new Thread(worker);
            workerThreads.add(workerThread);
            workerThread.start();
        }

        for (Thread workerThread : workerThreads) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Worker.writeWorkersToExcel(workers);
        Worker.writeTasksToExcel(workers);
        System.out.println("Данные о работниках и задачах успешно записаны в Excel-файл.");
    }
}
