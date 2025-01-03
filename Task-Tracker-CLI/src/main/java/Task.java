public class Task {

    private int id;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;

    public Task(String description, String status, String createdAt, String updatedAt) {
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Task() {}

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() { return this.createdAt; }

    public String getUpdatedAt() { return this.updatedAt; }

    public void setId(int id) { this.id = id; }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) { this.status = status; }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String toString() {
        return "ID: " + id + "\nDescription: " + description + "\nStatus: " + status + "\n" + "Created At: " +
                createdAt + "\n" + "Updated At: " + updatedAt + "\n";
    }

}
