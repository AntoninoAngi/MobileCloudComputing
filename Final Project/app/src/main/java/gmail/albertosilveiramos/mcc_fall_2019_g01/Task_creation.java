package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.android.volley.VolleyLog.TAG;
import static gmail.albertosilveiramos.mcc_fall_2019_g01.ProjectTask.getActivity;

public class Task_creation extends AppCompatActivity {
    private static final int REQUEST_MICROPHONE = 1;

    //TODO backend part to add a task to the corresponding project

    private Button buttonImport;
    private EditText description_text;
    private ImageView immagine;
    private EditText deadline;
    private EditText userAssign;
    private Button save_button;
    private ToggleButton setTaskDueDate;
    private String dueDate="";
    private Calendar taskDueDateCal = Calendar.getInstance();
    private boolean hasPickedADate=false;

    private Project project;
    //private Task t;
    private FirebaseUser user;
    private IResult mResultCallback;
    private  BackendService backendService;
    private FloatingActionButton addTaskButton;
    private Task newTask;
    private ImageButton voice_button;
    private Button translateButton;
    private boolean connected;
    private Translate translate;
    private String originalText;

    private ArrayList<User>selectedUser;

    public enum RequestCodes {
        AddMember(1), UploadPhto(1889);

        private final int value;
        private RequestCodes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.new_task_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("New task");
        initVolleyCallback();
        project=ProjectTask.getProject();
        user = FirebaseAuth.getInstance().getCurrentUser();

        newTask = new Task();

        addTaskButton = (FloatingActionButton)findViewById(R.id.fab_addtask);
        buttonImport = (Button) findViewById(R.id.upload_button);
        description_text = (EditText) findViewById(R.id.descr_text);
        immagine = (ImageView) findViewById(R.id.immagine);
        deadline = (EditText) findViewById(R.id.projadd_ev_datepaciker);
        voice_button = (ImageButton) findViewById(R.id.voice_button);
        translateButton = findViewById(R.id.translateButton);
        //checkPermission();

        selectedUser = new ArrayList<>();

        if(project.isIndividualProject()){ //if it is not a group project
            userAssign = (EditText) findViewById(R.id.userAssign);
        }else{
            userAssign = (EditText) findViewById(R.id.userAssign);
            userAssign.setVisibility(View.INVISIBLE);
            userAssign.setEnabled(false);
        }

        if (project.getAdmin().getEmail().equals(user.getEmail()) && project.getUsers()!=null){
            userAssign.setVisibility(View.VISIBLE);
            userAssign.setEnabled(true);
        }

        save_button = (Button) findViewById(R.id.save_task_new);
        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        voice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });





        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null)
                    description_text.setText(matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        setTaskDueDate = (ToggleButton) findViewById(R.id.arrow_down_task);
        setTaskDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dueDate="";
                handleDate();
            }
        });


        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(project.getUsers().size()== 0){

                    Toast.makeText(getApplicationContext(), "There are no users in you project", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(Task_creation.this, AddMember.class);
                    intent.putExtra("user_list", project.getUsers());
                    startActivityForResult(intent, 0);
                }

            }
        });


        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData(v);
            }
        });

        voice_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        description_text.setHint("You will see input here");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        description_text.setText("");
                        description_text.setHint("Listening...");
                        break;
                }
                return false;
            }
        });

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInternetConnection()) {

                    //If there is internet connection, get translate service and start translation:
                    getTranslateService();
                    translate();

                } else {

                    //If not, display "no connection" warning:
                    Toast.makeText(Task_creation.this, "There is no internet connection. Try again later", Toast.LENGTH_LONG).show();
                }

            }
        });
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
                taskDueDateCal.set(Calendar.YEAR,year);
                taskDueDateCal.set(Calendar.MONTH,month);
                taskDueDateCal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
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
                deadline.setText(dueDate);

                taskDueDateCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                taskDueDateCal.set(Calendar.MINUTE,minute);
                taskDueDateCal.set(Calendar.SECOND,00);

                hasPickedADate=true;

            }
        },HOUR,MINUTE,is24HFormat);
        timePickerDialog.show();
    }

    public boolean verifyDate( ){
        Calendar c = Calendar.getInstance();
        if(taskDueDateCal.get(Calendar.YEAR)<=c.get(Calendar.YEAR) &&
                taskDueDateCal.get(Calendar.MONTH)<=c.get(Calendar.MONTH) &&
                taskDueDateCal.get(Calendar.DAY_OF_MONTH)<=c.get(Calendar.DAY_OF_MONTH) &&
                taskDueDateCal.get(Calendar.HOUR_OF_DAY)<=c.get(Calendar.HOUR_OF_DAY) &&
                taskDueDateCal.get(Calendar.MINUTE)<=c.get(Calendar.MINUTE)){
            return false;

        }
        else{
            return true;
        }

    }
    public void translate() {

        //Get input text to be translated:
        originalText = description_text.getText().toString();
        Translation translation = translate.translate(originalText, Translate.TranslateOption.targetLanguage("it"), Translate.TranslateOption.model("base"));
        String translatedText = translation.getTranslatedText();

        //Translated text and original text are set to TextViews:
        description_text.setText(translatedText);

    }

    public void Upload_photo(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent, RequestCodes.UploadPhto.getValue());
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RequestCodes.UploadPhto.getValue()) {


            if (resultCode == Activity.RESULT_OK)
                switch (requestCode) {
                    case 1889:
                        Uri selectedImage = data.getData();
                        immagine.setImageURI(selectedImage);
                        runTextRecognition();
                        break;
                    default:
                        break;
                }
        }
        else if (requestCode == RequestCodes.AddMember.getValue()){

            ArrayList<User>users = data.getParcelableArrayListExtra("members");
            // Making now request
            selectedUser.addAll(users);
            String member = "";
            for(User user: selectedUser){
                member += user.getUsername() +"\n";

            }
            userAssign.setText(member);
            if(users != null){
                newTask.setUsers(users);
            }


        }
    }

    public void runTextRecognition(){
        BitmapDrawable drawable = (BitmapDrawable) immagine.getDrawable();
        Bitmap selectedImage = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(selectedImage);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();


        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processTextRecognitionResult(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        description_text.setText(texts.getText());
    }

    public void SaveData(View view) {

        final Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

        try {
            cal.setTime(sdf.parse(deadline.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (!verifyDate()) {
            Toast.makeText(this, "You did not enter a deadline date or a valid one", Toast.LENGTH_SHORT).show();

        /*
        We do not assign tasks to users
        }else if (userAssign.getText().toString().isEmpty() && project.getUsers()!=null){
            Toast.makeText(this, "You did not insert any valid username", Toast.LENGTH_SHORT).show();
         */
        }

        // Up here data is valid
        if (description_text.getText().toString().isEmpty()) {
            Toast.makeText(this, "You did not insert any description", Toast.LENGTH_SHORT).show();
        }


        boolean dataIsvalid = verifyDate() && !description_text.getText().toString().isEmpty();

        if (dataIsvalid) {

            String status;
            if (project.isIndividualProject()) { //At least 1 user is assigned
                status = "on-going";

            } else { //Group projects with no users assigned
                status = "pending";
            }

            newTask = new Task(project, "on-going", description_text.getText().toString(), cal, false);
            newTask.setChecked(false);
            newTask.setStatus(status);
            newTask.setDescription(description_text.getText().toString());
            newTask.setDueDate(cal);
            ProjectTask.addTask(newTask);


            try{
                URL url = new URL("https", "mcc-fall-2019-g01-258815.appspot.com/project", String.valueOf(project.getProjectId()));
                JSONObject jsonObject = newTask.ToJson();
                backendService = new BackendService(mResultCallback, this);
                backendService.postDataVolley("POST_TASK",url.toString(), jsonObject);
                Log.d(this.getClass().getCanonicalName(),  "sending "+ url.toString());
            }

            catch(Exception ex){

            }
            Intent intent = new Intent(Task_creation.this, ProjectTask.class);
            intent.putExtra("chosenProject", project.getName());
            intent.putExtra("members", selectedUser);

            startActivity(intent);




        }


    }

    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.credentials)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }




       private void initVolleyCallback() {
            mResultCallback = new IResult() {


                @Override
                public void notifySuccess(String requestType, JSONObject response) {
                    try{
                        String task_id = response.get("task_id").toString();
                        if(task_id.startsWith("-")){
                            task_id.substring(1);

                        }
                        newTask.setID(task_id);

                        if(selectedUser.size()> 0){
                            String task_Id = newTask.getBackendId();

                            backendService = new BackendService(mResultCallback, ProjectTask.getAppContext());
                            try {
                                String path = String.format("project/%s/%s", project.getProjectId(), task_Id);
                                URL url = new URL("https", "mcc-fall-2019-g01-258815.appspot.com", path);

                                JSONArray jsonArray = new JSONArray();
                                for (User user : selectedUser) {
                                    jsonArray.put(user.ToJson());
                                }
                                backendService.putDataVolley("PUT_USER_TASK", url.toString(), jsonArray);

                            }
                            catch (Exception ex){
                                Log.d(TAG, "PUT_USER_TASK");
                            }
                        }



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

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);

        }
    }

    public boolean checkInternetConnection() {

        //Check internet connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Means that we are connected to a network (mobile or wi-fi)
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }

    }






