package Multithread;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Worker implements Runnable{
    public String name;
    public final int id;
    public int workedHours;
    public int afkHours = 1;
    public LinkedList<Task> tasks;
    public final int MAX_WORK_HOURS = 8;
    public int statistic;


    private static final String WORKERS_FILE = "workers.xlsx";
    private static final String TASKS_FILE = "tasks.xlsx";

    public int getAfkHours() {
        return afkHours;
    }

    public int getStatistic() {
        return statistic;
    }

    public int getWorkedHours() {
        return workedHours;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getName(){
        return name;

    }

    public int getId() {
        return id;
    }


    public Worker(String name, int id, int workedHours, int afkHours, int statistic) {
        this.name = name;
        this.id = id;
        this.tasks = new LinkedList<>();
        this.workedHours = workedHours;
        this.afkHours = afkHours;
        this.statistic = statistic;
    }

    public static List<Worker> readWorkersFromExcel() {
        List<Worker> workers = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(WORKERS_FILE); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                String name = row.getCell(0).getStringCellValue();
                int id = (int) row.getCell(1).getNumericCellValue();
                int workedHours = (int) row.getCell(2).getNumericCellValue();
                int afkHours = (int) row.getCell(3).getNumericCellValue();
                int statistic = (int) row.getCell(4).getNumericCellValue();
                workers.add(new Worker(name, id, workedHours, afkHours, statistic));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return workers;
    }

    public static void assignTasksToWorkers(List<Worker> workers) {
        try (FileInputStream fis = new FileInputStream(TASKS_FILE); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell idCell = row.getCell(0);
                Cell taskCell = row.getCell(1);
                Cell hoursCell = row.getCell(2);
                if (idCell != null && idCell.getCellType() == CellType.NUMERIC &&
                        taskCell != null && taskCell.getCellType() == CellType.STRING &&
                        hoursCell != null && hoursCell.getCellType() == CellType.NUMERIC) {
                    int workerId = (int) idCell.getNumericCellValue();
                    String taskDescription = taskCell.getStringCellValue();
                    int taskHours = (int) hoursCell.getNumericCellValue();

                    Worker worker = findWorkerById(workers, workerId);
                    if (worker != null) {
                        Task task = new Task(taskDescription, taskHours);
                        worker.addTask(task);
                    } else {
                        System.out.println("Рабочий с id " + workerId + " не найден.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Рабочий " + name + " начал работать.");

        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Task currentTask = iterator.next();
            System.out.println("Рабочий " + name + " начал задачу " + currentTask.name);

            int remainingHours = currentTask.hours;
            while (remainingHours > 0) {
                int hoursToWork = Math.min(remainingHours, MAX_WORK_HOURS);


                for (int i = 0; i < hoursToWork; i++) {
                    workedHours++;
                    remainingHours -=1;
                    System.out.println("Рабочий " + name + " работал над задачей " + currentTask.name + " 1 час. Осталось часов: " + remainingHours);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (remainingHours > 0) {
                    System.out.println("Задача " + currentTask.name + " остаётся на завтра. Осталось часов: " + remainingHours);
                    afkHours +=16;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            currentTask.hours = remainingHours;
            currentTask.complete();
            System.out.println("Задача " + currentTask.name + " завершена рабочим " + name);
        }
        statistic = (workedHours/afkHours)*100;
        System.out.println("Рабочий " + name + " выполнил все задачи.");
    }

    public static void writeWorkersToExcel(List<Worker> workers) {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(WORKERS_FILE)) {
            Sheet sheet = workbook.createSheet("Workers");
            int rownum = 0;

            for (Worker worker : workers) {
                Row row = sheet.createRow(rownum++);
                row.createCell(0).setCellValue(worker.getName());
                row.createCell(1).setCellValue(worker.getId());
                row.createCell(2).setCellValue(worker.getWorkedHours());
                row.createCell(3).setCellValue(worker.getAfkHours());
                row.createCell(4).setCellValue(worker.getStatistic());
            }

            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTasksToExcel(List<Worker> workers) {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(TASKS_FILE)) {
            Sheet sheet = workbook.createSheet("Tasks");
            int rowNum = 0;

            for (Worker worker : workers) {
                for (Task task : worker.getTasks()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(worker.getId());
                    row.createCell(1).setCellValue(task.getName());
                    row.createCell(2).setCellValue(task.getHours());
                }
            }

            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Worker findWorkerById(List<Worker> workers, int id) {
        for (Worker worker : workers) {
            if (worker.getId() == id) {
                return worker;
            }
        }
        return null;
    }
    public void addTask(Task task) {
        this.tasks.add(task);
    }

}
