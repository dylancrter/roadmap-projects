import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TaskTracker {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar <command> [arguments]");
            return;
        }

        String command = args[0];
        switch (command) {
            case "add":
                if (args.length != 2) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar add <description>");
                    return;
                }
                addTask(args[1]);
                break;
            case "update":
                if (args.length < 3) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar update <taskID> <description>");
                    return;
                }
                updateTask(Integer.parseInt(args[1]), args[2]);
                break;
            case "delete":
                if (args.length != 2) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar delete <taskID>");
                    return;
                }
                deleteTask(Integer.parseInt(args[1]));
                break;
            case "mark-in-progress":
                if (args.length != 2) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar mark-in-progress <taskID>");
                    return;
                }
                markTaskStatus(Integer.parseInt(args[1]), "in-progress");
                break;
            case "mark-done":
                if (args.length != 2) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar mark-done <taskID>");
                    return;
                }
                markTaskStatus(Integer.parseInt(args[1]), "done");
                break;
            case "mark-todo":
                if (args.length != 2) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar mark-done <taskID>");
                    return;
                }
                markTaskStatus(Integer.parseInt(args[1]), "todo");
                break;
            case "list":
                if (args.length == 1) {
                    listTasks(null, true);
                } else if (args.length == 2) {
                    listTasks(args[1], false);
                } else {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar list <taskID>");
                    return;
                }
                break;
            case "delete-all":
                if (args.length != 1) {
                    System.out.println("Usage: java -jar target/Task-Tracker-CLI-1.0-SNAPSHOT.jar delete-all");
                    return;
                }
                deleteAllTasks();
                System.out.println("Deleted all tasks");
                break;
            default:
                System.out.println("Command not recognized. Please try again.");
        }
    }

    private static void addTask(String description) {
        Task task = new Task();
        task.setDescription(description);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        task.setCreatedAt(dateTimeFormatter.format(LocalDateTime.now()));
        task.setUpdatedAt(dateTimeFormatter.format(LocalDateTime.now()));
        task.setStatus("in-progress");

        List<Task> tasks = readTasksFromFile();
        int id = tasks.size() + 1;
        task.setId(id);
        tasks.add(task);

        writeTasksToFile(tasks);
        System.out.println("Task added successfully (ID: " + task.getId() + ")");
    }

    private static void updateTask(int id, String newDescription) {
        List<Task> tasks = readTasksFromFile();
        if (id > tasks.size() || id <= 0) {
            System.out.println("Task ID " + id + " not found");
            return;
        }
        Task task = tasks.get(id - 1);
        task.setDescription(newDescription);
        writeTasksToFile(tasks);
        System.out.println("Task updated successfully (ID: " + task.getId() + ")");
    }

    private static void deleteTask(int id) {
        List<Task> tasks = readTasksFromFile();
        if (id > tasks.size() || id <= 0) {
            System.out.println("Task ID " + id + " not found");
            return;
        }
        Task task = tasks.get(id - 1);
        tasks.remove(task);
        writeTasksToFile(tasks);
        System.out.println("Task deleted successfully (ID: " + task.getId() + ")");
    }

    private static void markTaskStatus(int id, String status) {
        List<Task> tasks = readTasksFromFile();
        if (id > tasks.size() || id <= 0) {
            System.out.println("Task ID " + id + " not found");
            return;
        }
        Task task = tasks.get(id - 1);
        switch (status) {
            case "in-progress" -> {
                task.setStatus("in-progress");
                writeTasksToFile(tasks);
            }
            case "done" -> {
                task.setStatus("done");
                writeTasksToFile(tasks);
            }
            case "todo" -> {
                task.setStatus("todo");
                writeTasksToFile(tasks);
            }
            default -> {
                System.out.println("Status not recognized. Please try again.");
                return;
            }
        }
        System.out.println("Task status updated successfully (ID: " + task.getId() + ")");
    }

    private static void listTasks(String status, boolean all) {
        List<Task> tasks = readTasksFromFile();
        if (all) {
            if (tasks.isEmpty()) {
                System.out.println("No tasks found");
            }
            for (Task task : tasks) {
                System.out.println(task);
            }
        } else {
            for (Task task : tasks) {
                if (task.getStatus().equals(status)) {
                    System.out.println(task);
                }
            }
        }
    }

    private static List<Task> readTasksFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.readValue(getTasksFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void writeTasksToFile(List<Task> tasks) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(getTasksFile(), tasks);
        } catch (IOException e) {
            System.err.println("Error writing tasks to file: " + e.getMessage());
        }
    }

    private static File getTasksFile() {
        String filepath = "src/main/resources/tasks.json";
        return new File(filepath);
    }

    private static void deleteAllTasks() {
        List<Task> tasks = new ArrayList<>();
        writeTasksToFile(tasks);
    }
}