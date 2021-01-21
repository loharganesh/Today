package app.splitbit.today.Model;

public class Todo {

    private String taskname,key,date;
    private String image = "notavailable";
    private long timestamp;
    private boolean done;

    public Todo(){

    }

    public Todo(String taskname, String key, String date, String image, long timestamp, boolean done) {
        this.taskname = taskname;
        this.key = key;
        this.date = date;
        this.image = image;
        this.timestamp = timestamp;
        this.done = done;
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public boolean equals(@androidx.annotation.Nullable Object obj) {
        Todo transaction = (Todo) obj;
        return key.matches(transaction.getKey());
    }

}
