package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class ProjectTask extends AppCompatActivity {

    private TabLayout tabLayout;
    private static Context context;
    private String projectName;
    private ViewPager viewPager;

    private static ArrayList<Task> taskList;
    private static ArrayList<Bitmap> pictureList;
    private static ArrayList<File> fileList;
    private static ArrayList<StorageReference> fileRefList;
    private static ArrayList<StorageReference> picturesRefList;
    private static Activity activity;
    private static  String TAG  = ProjectTask.class.getName();

    private  BackendService backendService;
    private IResult mResultCallback;

    private static Project project;
    private static int position=-1;

    private SectionPageAdapter sectionPageAdapter;

    public enum RequestCodes {
        AddMember(1), AddTask(2);

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
        setContentView(R.layout.project_task);

        //ProjectTask.context= getApplicationContext();
        context = getApplicationContext();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.projtasks_pb_toolbar);
        setSupportActionBar(myToolbar);

        sectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);
        activity = this;

        initVolleyCallback();
        tabLayout = (TabLayout) findViewById(R.id.tabss);
        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        Bundle bundle = getIntent().getExtras();
        projectName = bundle.getString("chosenProject");
        getSupportActionBar().setTitle(""+projectName);
        setupTabIcons();

        for(int i=0; i<ProjectPage.getProjectsList().size();i++){
            if(ProjectPage.getProjectsList().get(i).getName().equals(projectName)){
                project=ProjectPage.getProjectsList().get(i);
                position=i;
            }
        }

        taskList = project.getTasks();
        pictureList = project.getPictures();
        picturesRefList = project.getPicturesRefs();
        fileList = project.getFiles();
        fileRefList = project.getFilesRefs();

    }

    public static Project getProject(){
        return project;
    }

    public static void addTask(Task task){
        taskList.add(task);
        project.setLastModifiedDate(new Date());
        TaskPages.updateAdapter();
        ProjectPage.updateProjects();

    }


    public static void addPicture(Bitmap bitmap, StorageReference fileRef){
        pictureList.add(bitmap);
        picturesRefList.add(fileRef);
        project.setLastModifiedDate(new Date());
        PicturesPage.updateAdapter();
        ProjectPage.updateProjects();
    }

    public static void addFile(File file, StorageReference fileRef){
        fileList.add(file);
        fileRefList.add(fileRef);
        project.setLastModifiedDate(new Date());
        FilesPage.updateAdapter();
        ProjectPage.updateProjects();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setText("Tasks");
        tabLayout.getTabAt(1).setText("Pictures");
        tabLayout.getTabAt(2).setText("Files");
    }

    public void setupViewPager(ViewPager viewPager){
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new TaskPages(),"");
        adapter.addFragment(new PicturesPage(),"");
        adapter.addFragment(new FilesPage(),"");
        viewPager.setAdapter(adapter);
    }
    public static Context getAppContext() {
        return ProjectTask.context;
    }

    public static Activity getActivity(){
        return activity;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // do now put the user to a task and the task to the user in the project
        if(requestCode == RequestCodes.AddMember.getValue()){


            String task_Id = data.getExtras().getString("task_id");
            ArrayList<User>users = data.getExtras().getParcelableArrayList("members");
            backendService = new BackendService(mResultCallback, this);
            try{
                String path = String.format("project/%s/%s", project.getProjectId(),task_Id);
                URL url = new URL("https", "mcc-fall-2019-g01-258815.appspot.com", path);

                JSONArray jsonArray = new JSONArray();
                for (User user : users){
                    jsonArray.put(user.ToJson());
                }

                backendService = new BackendService(mResultCallback, this);
                backendService.putDataVolley("PUT_USER_TASK",url.toString(), jsonArray );

            }
            catch (Exception ex){

            }

        }


    }

   private void initVolleyCallback() {
        mResultCallback = new IResult() {


            @Override
            public void notifySuccess(String requestType, JSONObject response) {

                Log.d(TAG, response.toString());

            }
            @Override
            public void notifySuccess(String requestType, JSONArray response) {
                // no array expected here
                Log.d(TAG, response.toString());
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!");
            }
        };
    }
}
