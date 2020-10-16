package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class AddProject extends AppCompatActivity {

    private TextView projectName;
    private TextView projectDescription;
    private TextView projectDueDate;
    private TextView keywordsAdded;
    private Switch individualORGroup;

    private ImageView projectImage;
    private TextView keywords;
    private ToggleButton selectDueDate;
    private Uri mImageuri;
    public boolean uploadedImage=false;
    public FirebaseAuth mAuth;
    IResult mResultCallback = null;
    BackendService mVolleyService;
    private String TAG = "AddProject";
    private User admin;

    private boolean hasPickedADate=false;

    private String dueDate="";

    private ArrayList<String> keywordsList = new ArrayList<>();

    private Calendar projectDueDateCal = Calendar.getInstance();


    private Project newProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        setupToolBar();
        initVolleyCallback();

        this.admin = getIntent().getExtras().getParcelable("current_user");

        projectName = (TextView) findViewById(R.id.projadd_ev_name);

        projectDescription = (TextView) findViewById(R.id.projadd_ev_description);


        projectImage = (ImageView) findViewById(R.id.imageProject);

        projectDueDate = (TextView) findViewById(R.id.projadd_ev_datepaciker);

        keywords = (TextView) findViewById(R.id.projadd_tv_add_key);

        selectDueDate = (ToggleButton) findViewById(R.id.arrow_down);

        keywordsAdded = (TextView) findViewById(R.id.keywordsToDisplay);

        individualORGroup = (Switch) findViewById(R.id.individualORgroup);

        mAuth = FirebaseAuth.getInstance();

        projectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(projectImage.getContext());

                adb.setTitle("Select Options");

                //Initialize a new String Array
                final String[] Choises = new String[]{"Gallery", "Cancel"};
                //It will render a single choice traditional list on alert dialog

                adb.setItems(Choises, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String clickedItemValue = Arrays.asList(Choises).get(which);
                        switch (clickedItemValue) {
                            case "Gallery":
                                Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent2.setType("image/*");
                                String[] mimeTypes = {"image/jpeg", "image/png"};
                                intent2.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                                startActivityForResult(intent2, 1889);
                                uploadedImage=true;
                                break;
                            case "Cancel":
                                uploadedImage=false;
                                break;

                        }
                    }
                });
                adb.show();
            }
        });

           keywords.setOnKeyListener(new View.OnKeyListener() {
                   public boolean onKey(View v, int keyCode, KeyEvent event) {
                       // If the event is a key-down event on the "enter" button
                       if (event.getAction() == KeyEvent.ACTION_DOWN &&
                           keyCode == KeyEvent.KEYCODE_ENTER && keywords.getText().toString().equals("")) {
                                          Toast.makeText(AddProject.this, "Please type a keyword", Toast.LENGTH_SHORT).show();

                       } else if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                           (keyCode == KeyEvent.KEYCODE_ENTER) && !keywords.getText().toString().equals("") && keywordsList.size()<3 ) {
                                          keywordsList.add(keywords.getText().toString());
                                          Toast.makeText(AddProject.this, "Added keyword: " + keywords.getText().toString(), Toast.LENGTH_SHORT).show();
                                          if(keywordsList.size()==1){
                                              keywordsAdded.setText(keywords.getText().toString());
                                          }
                                          else{
                                              keywordsAdded.setText(keywordsAdded.getText().toString()+"\n"+keywords.getText().toString());
                                          }
                                          keywords.setText("");
                                          return true;
                       }
                       else if((event.getAction()==KeyEvent.ACTION_DOWN)&& (keyCode==KeyEvent.KEYCODE_ENTER)&& keywordsList.size()==3){
                              Toast.makeText(AddProject.this,"You can only add 3 keywords",Toast.LENGTH_SHORT).show();

                       }
                       return false;
                   }
           } );

        selectDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dueDate="";
                handleDate();  
            }
        });
    }

    public boolean verifyProjectValue(){
        if(projectName.getText().toString().equals("")){
           Toast.makeText(AddProject.this, "Please give a name to the project", Toast.LENGTH_SHORT).show();
           return false;
        }
        else if(!validName()){
            Toast.makeText(AddProject.this, "That project name already exists", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(projectDescription.getText().toString().equals("")){
           Toast.makeText(AddProject.this, "Please provide a description to the project", Toast.LENGTH_SHORT).show();
           return false;
        }
        else if(hasPickedADate && !verifyDate()){
           Toast.makeText(AddProject.this, "The due date needs to be valid", Toast.LENGTH_SHORT).show();
           return false;
        }
        else {
            return true;
        }
    }




    public boolean verifyDate( ){
        Calendar c = Calendar.getInstance();
        if(projectDueDateCal.get(Calendar.YEAR)<=c.get(Calendar.YEAR) &&
                       projectDueDateCal.get(Calendar.MONTH)<=c.get(Calendar.MONTH) &&
                       projectDueDateCal.get(Calendar.DAY_OF_MONTH)<=c.get(Calendar.DAY_OF_MONTH) &&
                       projectDueDateCal.get(Calendar.HOUR_OF_DAY)<=c.get(Calendar.HOUR_OF_DAY) &&
                       projectDueDateCal.get(Calendar.MINUTE)<=c.get(Calendar.MINUTE)){
                   return false;

        }
        else{
            return true;
        }

    }


    public boolean validName(){
        for(int i=0; i<ProjectPage.getProjectsList().size();i++){
            if(projectName.getText().toString().equals(ProjectPage.getProjectsList().get(i).getName())){
                return false;
            }
        }
        return true;
    }



    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        mImageuri = data.getData();
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 1889:
                    //data.getData return the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    projectImage.setImageURI(selectedImage);
                    projectImage.setTag("1");
                    uploadedImage = true;
                    break;
                case 0:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    projectImage.setImageBitmap(bitmap);
                    projectImage.setTag("1");
                    uploadedImage=true;
                    break;
            }
    }


    void initVolleyCallback() {
        mResultCallback = new IResult() {


            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try{
                    String project_id = response.get("project_id").toString();
                    if(project_id.startsWith("-")){
                        project_id.substring(1);
                    }
                    newProject.setProjectId(project_id);
                }
                catch (Exception ex){

                }
                finally {
                    Log.d(TAG, "Volley requester " + requestType);
                    Log.d(TAG, "Volley JSON post" + response);
                }

            }
            @Override
            public void notifySuccess(String requestType, JSONArray response) {
                // no array expected here
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!");
            }
        };
    }


    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.projadd_pb_toolbar);

        if (toolbar == null) return;

        setSupportActionBar(toolbar);
        setTitle("New Project");
        toolbar.setTitleTextColor(Color.WHITE);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_project) {

            if(verifyProjectValue())
            {
                ArrayList<User>user_array = new ArrayList<>();
                user_array.add(this.admin);
                if(uploadedImage){
                     newProject = new Project(projectImage,projectName.getText().toString(),
                             projectDescription.getText().toString(), new Date(),projectDueDateCal,keywordsList,user_array,this.admin,
                             false ,new ArrayList<Task>(), new ArrayList<Bitmap>(),
                             new ArrayList<File>(), individualORGroup.isChecked());

                }
                else{

                     newProject = new Project(projectName.getText().toString(),projectDescription.getText().toString(),
                             new Date(),projectDueDateCal,keywordsList,user_array,this.admin, false,
                             new ArrayList<Task>(), new ArrayList<Bitmap>(), new ArrayList<File>() , individualORGroup.isChecked());

                }
                RequestQueue queue = Volley.newRequestQueue(this);


                mVolleyService = new BackendService(mResultCallback,this);
                String url = "https://mcc-fall-2019-g01-258815.appspot.com/project";
                JSONObject sendObj = newProject.ToPostJson();

                Log.d("Betobug", newProject.getDueDate().toString()+" <----");

                mVolleyService.postDataVolley("POSTCALL", url, sendObj);

                Intent intent = new Intent(AddProject.this, ProjectPage.class);
                ProjectPage.addProject(newProject);


                setResult(Activity.RESULT_OK,intent);
                finish();
            }



            return true;
        }
        else if (id == R.id.cancel_creation) {
            Intent intent = new Intent(AddProject.this, ProjectPage.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_proj_menu, menu);
        return true;
    }



    public void handleDate(){
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month+=1;
                dueDate += dayOfMonth+"-"+month+"-"+year+ " ";
               projectDueDateCal.set(Calendar.YEAR,year);
               projectDueDateCal.set(Calendar.MONTH,month);
               projectDueDateCal.set(Calendar.DAY_OF_MONTH,dayOfMonth);

               handleTime();
            }
        }, YEAR,MONTH,DATE);
        datePickerDialog.show();



    }

    public void handleTime(){
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);

        boolean is24HFormat = DateFormat.is24HourFormat(this);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                dueDate+=hourOfDay+":"+minute+":00";
                projectDueDate.setText(dueDate);

                projectDueDateCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                projectDueDateCal.set(Calendar.MINUTE,minute);
                projectDueDateCal.set(Calendar.SECOND,00);

                 hasPickedADate=true;

            }
        },HOUR,MINUTE,is24HFormat);
        timePickerDialog.show();
    }




}
