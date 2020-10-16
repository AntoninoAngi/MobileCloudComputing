package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class Project implements Parcelable {

    private String name = new String();
    private String description = new String();
    //Both lastModified and dueDate will be DateFormat

    private String projectId = new String();
    private Date creationDate = new Date();
    private Date lastModified = new Date();
    private Calendar dueDate;
    private ArrayList<String> keywords = new ArrayList<String>();
    private ArrayList<User> users = new ArrayList<User>();
    private User admin;
    private boolean favoriteButtonState = false;
    private ImageView projectImage;
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();
    private ArrayList<File> files = new ArrayList<File>();
    private ArrayList<StorageReference> filesRefs = new ArrayList<StorageReference>();
    private ArrayList<StorageReference> picturesRefs = new ArrayList<StorageReference>();

    private boolean individualProject;

    private static SimpleDateFormat dateFormat = dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(projectId);
        out.writeString(name);
        out.writeString(description);
    }
    public static final Parcelable.Creator<Project> CREATOR = new Parcelable.Creator<Project>() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Project(Parcel in) {

        this.projectId = in.readString();
        this.name = in.readString();
        this.description = in.readString();
    }

    public Project(){


    }

    //IMAGE AND USERS GROUP
    public Project(ImageView projectImage, String name, String description, Date lastModified, Calendar dueDate, ArrayList<String> keywords, ArrayList<User> users, User admin, boolean favoriteORnot, ArrayList<Task> tasks, ArrayList<Bitmap> pictures, ArrayList<File> files, boolean individual_project){
        this();
        this.projectImage=projectImage;
        this.name=name;
        this.description=description;
        this.lastModified=lastModified;
        this.creationDate = Calendar.getInstance().getTime();
        this.dueDate=dueDate;
        this.keywords=keywords;
        this.users=users;
        this.admin=admin;
        this.favoriteButtonState=favoriteORnot;
        this.tasks=tasks;
        this.pictures=pictures;
        this.files=files;
        this.individualProject = individual_project;

    }

    public Project(String name, String description, Date lastModified, Calendar dueDate, ArrayList<String> keywords, ArrayList<User> users, User admin, boolean favoriteORnot, ArrayList<Task> tasks, ArrayList<Bitmap> pictures, ArrayList<File> files, boolean individiual_project){
        this();
        this.name=name;
        this.description=description;
        this.lastModified=lastModified;
        this.dueDate=dueDate;
        this.keywords=keywords;
        this.users=users;
        this.admin=admin;
        this.favoriteButtonState=favoriteORnot;
        this.tasks=tasks;
        this.files=files;
        this.creationDate = Calendar.getInstance().getTime();
        this.pictures=pictures;

        this.individualProject = individiual_project;

    }

    //NO IMAGE AND INDIVIDUAL
    public void setProjectId(String projectId) {

        this.projectId = projectId;
    }

    public ArrayList<Bitmap> getPictures() {
        return pictures;
    }

    public ArrayList<StorageReference> getPicturesRefs() {
        return picturesRefs;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public ArrayList<StorageReference> getFilesRefs() {
        return filesRefs;
    }

    public String getProjectId(){

        if(this.projectId.startsWith("-")){
            this.projectId = projectId.substring(1);
        }
        return  this.projectId;
    }

    public JSONObject ToPostJson(){
        JSONObject project = new JSONObject();
        try {


            project.put("administrator", this.admin.getUserID());
            project.put("badge", getBase64String());
            project.put("name", this.name);
            project.put("description", this.description);
            project.put("creation_date", dateFormat.format(creationDate));
            project.put("deadline", dateFormat.format(dueDate.getTime()));
            project.put("individual_project", this.individualProject);
            project.put("last_update", dateFormat.format(lastModified));




            JSONArray usersJsonArray = new JSONArray();
            for(User user:users){
                usersJsonArray.put(user.getUserID());
            }
            project.put("users_index", usersJsonArray);


            JSONArray jsonKeywords = new JSONArray();
            for (int i = 0; i < this.keywords.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put(String.valueOf(i),this.keywords.get(i));
                jsonKeywords.put(keywords.get(i));
            }

            project.put("keywords",jsonKeywords);



        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return project;

    }


    private String getBase64String() {

        if(projectImage == null) return "";
        Bitmap bitmap = ((BitmapDrawable)projectImage.getDrawable()).getBitmap();



        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    private static Bitmap getBitmapFromString(String encodedString){

        byte[] decodedBytes = Base64.decode(encodedString, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);



    }

    public boolean isFavoriteButtonState() {
        return favoriteButtonState;
    }

    public ImageView getProjectImage() {
        return projectImage;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date  getLastModifiedDate() {
        return lastModified;
    }

    public Boolean getFavoriteButtonState() {
        return favoriteButtonState;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public User getAdmin() {
        return admin;
    }


    public ArrayList<Task> getTasks() {
        return tasks;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setFavoriteButtonState(boolean favoriteButtonState) {
        this.favoriteButtonState = favoriteButtonState;
    }

    public void setProjectImage(ImageView projectImage) {
        this.projectImage = projectImage;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setFavoriteButtonState(Boolean state) {
        this.favoriteButtonState = state;
    }

    public void setLastModifiedDate(Date lastModified) {
        this.lastModified = lastModified;
        setLastUpdateToDB(lastModified);
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public boolean isIndividualProject(){return this.individualProject;}


    static Comparator<Project> compareNames() {
        return new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                if (p1.getName() == null || p2.getName() == null) {
                    return 0;
                }
                return p1.getName().compareTo(p2.getName());
            // compare using attribute 1
        };
    };
    }

    static Comparator<Project> compareDueDates() {
        return new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                if (p1.getDueDate() == null || p2.getDueDate() == null) {
                    return 0;
                }
                return p1.getDueDate().compareTo(p2.getDueDate());
                // compare using attribute 1
            };
        };
    }

    static Comparator<Project> compareModifiedDates() {

        return new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                Calendar c1 = Calendar.getInstance();
                c1.setTime(p1.getLastModifiedDate());
                Calendar c2 = Calendar.getInstance();
                c2.setTime(p2.getLastModifiedDate());
                if (c1 == null || c2 == null) {
                    return 0;
                }
                return (c1.compareTo(c2));
                // compare using attribute 1
            };
        };
    }


    public static ArrayList<Project> FromJsonArray(JSONArray array, Context context){
        ArrayList<Project> projects= new ArrayList<>();

        try{
            for (int i = 0 ; i < array.length(); i++) {
                projects.add(FromJson(array.getJSONObject(i), context));
            }
        }
        catch (Exception ex){

        }
        return  projects;

    }

    public static Project FromJson(JSONObject object, Context context){
        if(object == null) return null;
        Project project = null;
        try{
            project = new Project();
            if (object.has("project_id"))
            {
                String id  = object.getString("project_id");
                if(id.startsWith("-")){
                    id = id.substring(1);
                    if(id.startsWith("-"))
                    {
                        id = id.substring(1);
                    }
                }
                project.projectId = id;
            }

            if (object.has("name"))project.name = object.getString("name");
            //getUSerbyUsername
            //project.admin = object.getString("administrator");
            if (object.has("description"))project.description = object.getString("description");

            if(object.has("badge")){
                String image = object.getString("badge");
                Bitmap decodedBitmap = getBitmapFromString(image);
                if (decodedBitmap != null && !decodedBitmap.equals("")){
                     project.projectImage = new ImageView(context);
                    project.projectImage.setImageBitmap(decodedBitmap);

                }
            }


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            if (object.has("creation_date")){
                                project.creationDate =  dateFormat.parse(object.getString("creation_date"));
            }

            if (object.has("deadline")){
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateFormat.parse(object.getString("deadline")));
                project.dueDate = cal;
            }
            if(object.has("last_update")){
                project.lastModified = dateFormat.parse(object.getString("last_update"));
            }

            if(object.has("individual_project"))
            {
                project.individualProject = Boolean.parseBoolean(object.getString("individual_project"));
            }

            if(object.has("administrator")){

                 String user_id = object.getString("administrator");
                 // we need only the ID for now to make admin checks
                 User admin = new User(user_id, "", "", "", new ArrayList<String>());
                 project.admin = admin;
            }

            project.tasks = new ArrayList<Task>();
            project.users = new ArrayList<User>();

            if (object.has("users_index")){

                JSONObject jsonObject = object.getJSONObject("users_index");

                ArrayList<String> ids = new ArrayList<>();

                Iterator<String> keys = jsonObject.keys();

                while(keys.hasNext()) {
                    String key = keys.next();
                    ids.add(key);
                }
                project.setUsers(ids.toArray(new String[0]));

            }

          if (object.has("tasks"))
        {

                JSONObject jsonObject = object.getJSONObject("tasks");

                ArrayList<String> ids = new ArrayList<>();

                Iterator<String> keys = jsonObject.keys();

                while(keys.hasNext()) {
                    String key = keys.next();
                    ids.add(key);
                }
                project.setTasks(ids.toArray(new String[0]));

            }


            //TODO set this here properly
            project.pictures = new ArrayList<>();

            project.files = new ArrayList<>();




        }
        catch (Exception ex)
        {
            Log.d("Project to json", ex.getMessage());
        }

        return project;
    }


    public void UpdateTask(){

    }


    public void setLastUpdateToDB(Date date){


        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

       rootRef.child("projects").child("-"+this.getProjectId()).child("last_update").setValue(dateFormat.format(lastModified));


    }


    public void setTasks(final String [] idArray ){


        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference usersdRef = rootRef.child("tasks");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> usernames = new ArrayList();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {



                    if(ds.getKey() != null) {

                        Task task = Task.getUserFromDataFrame(ds);
                        for (String s : idArray){
                            if(s.equals(task.getID()))
                                tasks.add(task);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        usersdRef.addListenerForSingleValueEvent(eventListener);

    }


    // we can use firebase here
    public void setUsers(final String idArray []){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference usersdRef = rootRef.child("users");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> usernames = new ArrayList();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {



                    if(ds.getKey() != null) {

                        User user = User.getUserFromDataFrame(ds);
                        for (String s : idArray){
                            if(s.equals(user.getUserID()))
                            users.add(user);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        usersdRef.addListenerForSingleValueEvent(eventListener);

    }



}

