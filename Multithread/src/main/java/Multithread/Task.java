package Multithread;

public class Task {
    public String name;

    public int hours;
    public boolean status;

    public static final String TITLE_OF_TASK_TABLE = "tasks.xlsx";

    public Task(String name, int time) {
        this.hours = time;
        this.status = true;
        this.name = name;
    }
    public int getHours() {
        return hours;
    }

    public String getName() {
        return name;
    }

    public void complete() {
        status = false;
    }


}