package gmail.albertosilveiramos.mcc_fall_2019_g01;

import com.google.firebase.database.DataSnapshot;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Task {


    private  String ID;
    private Project project;
    private String status; //pending, on-going and completed
    private String description;
    private Calendar dueDate;
    private boolean isChecked;
    private ArrayList<User>userArrayList;


    private  static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public Task(){

    }

    public Task(Project project,String status, String description, Calendar dueDate, boolean isChecked){
        this.project=project;
        this.status=status;
        this.description=description;
        this.dueDate=dueDate;
        this.isChecked=isChecked;
        this.userArrayList = new ArrayList<>();
    }

    public Task(Project project,String status, String description, Calendar dueDate, boolean isChecked, ArrayList<User>users){
        this(project, status, description, dueDate , isChecked);
        this.userArrayList = users;

    }


    public void setID(String ID ){this.ID = ID;}
    public String getID(){return  this.ID;}

    public String getBackendId(){
        if(this.ID.startsWith("-")){
            return  this.ID.substring(1);
        }
        else{
            return this.ID;
        }
    }

    public ArrayList<User>getUsers (){return  userArrayList;}
    public void setUsers (ArrayList<User>users){userArrayList = users;}

    public String getStatus() {
        return status;
    }

    public Project getProject() {
        return project;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public boolean getisChecked() {
        return isChecked;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }



    public JSONObject toJsonStatus(){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("status", status);

        }
        catch (Exception ex){

        }

        return jsonObject;


    }


    public JSONObject ToJson()
    {
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("status", status);
            jsonObject.put("description", description);
            jsonObject.put("deadline", dateFormat.format(dueDate.getTime()));

        }
        catch (Exception ex){

        }

        return jsonObject;

    }

    public static Task getUserFromDataFrame(DataSnapshot ds){
        Task task = null;
        try{
            task = new Task();
            task.setID(ds.getKey());
            // encode image later
            task.setStatus(ds.child("status").getValue(String.class));
            task.setDescription(ds.child("description").getValue(String.class));
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(ds.child("deadline").getValue(String.class)));

        }
        catch (Exception ex){

        }

        return task;

    }

}
