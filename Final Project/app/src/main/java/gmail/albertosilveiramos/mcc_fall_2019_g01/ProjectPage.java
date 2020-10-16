package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import static gmail.albertosilveiramos.mcc_fall_2019_g01.Notifications.CHANNEL_1_ID;

public class ProjectPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static Context context;


    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.ic_star,
            R.drawable.ic_assignment_24px,
            R.drawable.ic_hourglass_empty_24px
    };


    private static ArrayList<Project> projectsList= new ArrayList<Project>();
    private static ArrayList<Project> favoriteProjectsList= new ArrayList<Project>();
    private static ArrayList<Project> upcomingProjectsList= new ArrayList<Project>();

    private NotificationManagerCompat notificationManager;

    private SectionPageAdapter sectionPageAdapter;
    private ViewPager viewPager;
    private FloatingActionButton addButton;
    private SwipeRefreshLayout swipeToRefresh;
    private DrawerLayout drawer;
    private TextView email3;
    private ArrayAdapter adapter;
    private FirebaseUser firebaseUser;
    private ImageView photo_profile;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageRef;
    private int a=1;
    private static boolean allDeletedManually=false;

    private FirebaseFirestore db;
    private DocumentSnapshot ds;
    private FirebaseAuth user2;

    // This is our real user
    private  static  User user;

    private BackendService backendService;
    private IResult mResultCallback;
    private final String TAG = "ProjectPage";


    public enum RequestCodes {
        AddMember(1), AddProject(2), AddTask(3);

        private final int value;
        private RequestCodes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ResponseCodes{
        GET_PROJECTS_BY_USER("GET_PROHECTS_BY_USER"),
        PUT_USER_TO_PROJECT("PUT_USER_TO_PROJECT");


        private final String value;
        private ResponseCodes(String value) {
            this.value = value;
        }

        public  String getValue() {
            return value;
        }
    }


    public  static  User getCurrentUser(){
         if(user == null){
             //Toast.makeText(this, "", , """user is not initialized");
             return  null;
         }
         return user;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_page);
        this.context= getApplicationContext();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View v = navigationView.getHeaderView(0);
        initVolleyCallback();

        email3 = (TextView) v.findViewById(R.id.email3);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null){
            startActivity(new Intent(ProjectPage.this, MainActivity.class)); //Go back to login page
            finish();
        }
        email3.setText(firebaseUser.getEmail());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.projadd_pb_toolbar);
        setSupportActionBar(myToolbar);

        adapter = new ArrayAdapter<Project>( ProjectPage.this,
                android.R.layout.simple_list_item_1, projectsList);

        photo_profile = v.findViewById(R.id.pallino_foto);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        user2 = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        Task d = db.collection("users")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                               if (task.isSuccessful()) {
                                                   ds = task.getResult().getDocuments().get(0);
                                                   String u = ds.getString("image");
                                                   final String photo_url;
                                                   if (u.compareTo("") == 0)
                                                       photo_url = "default.png";
                                                   else
                                                       photo_url = u;
                                                   mStorageRef.child(photo_url).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                       @Override
                                                       public void onSuccess(Uri uri) {
                                                           Picasso.get().load(uri.toString()).fit().into(photo_profile);
                                                       }
                                                   });
                                               }
                                           }
                                       });




        InitUser();

        drawer = findViewById(R.id.drawer_layout);

        sectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);


        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProjects();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeToRefresh.setRefreshing(false);
                    }
                }, 4000);
            }
        });

        notificationManager = NotificationManagerCompat.from(this);



        addButton = (FloatingActionButton) findViewById(R.id.proj_fab_addproj);

        getSupportActionBar().setTitle("Projects");
        setupTabIcons();


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProjectPage.this, AddProject.class);

                intent.putExtra("current_user", getCurrentUser());
                startActivityForResult(intent, RequestCodes.AddProject.getValue());
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position % 3 != 1) {
                    addButton.hide();
                } else {
                    addButton.show();
                    if(projectsList.size()==0 && !allDeletedManually){ //FOR THE BACKEND
                        //Here, all the projects from the db will be imported
                        //generateProjects();
                        //refreshed = true;
                        refreshProjects();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, myToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    void initVolleyCallback() {
        mResultCallback = new IResult() {


            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try{
                    if (requestType.equals(ResponseCodes.GET_PROJECTS_BY_USER.getValue())){
                        Log.d(TAG, requestType +" was successful");
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
                if(requestType.equals(ResponseCodes.PUT_USER_TO_PROJECT.getValue())){


                }
                else if (requestType.equals((ResponseCodes.GET_PROJECTS_BY_USER.getValue()))){
                    try{
                        if(response.length() > 0 &&  ! response.getJSONObject(0).has("response")){
                            if(projectsList.size()!= Project.FromJsonArray(response, ProjectPage.this).size()){
                                sendNotification("addedToProject");
                            }

                            projectsList.clear();
                            ArrayList<Project>projects =  Project.FromJsonArray(response, ProjectPage.this);
                            getCurrentUser().setFavoriteList(projects);
                            ArrayList<Project>favourites = getCurrentUser().getFavouriteList();
                            getFavoriteProjectsList().clear();
                            getFavoriteProjectsList().addAll(favourites);


                            FragmentFavorites.updateAdapter();
                            projectsList.addAll(projects);

                            Log.d(TAG, String.format("Retrieved %d projects", projectsList.size()));
                            FragmentUpcoming.updateAdapter();
                            FragmentProjects.updateAdapter();

                        }


                    }
                    catch (Exception ex){

                    }


                }

                Log.d(TAG, requestType +" was successful");
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley requester " + error.getMessage());
                Log.d(TAG, "Volley JSON post" + "That didn't work!");
            }
        };
    }


    private  void InitUser(){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference usersdRef = rootRef.child("users").child(firebaseUser.getUid());
        ValueEventListener eventListener = new ValueEventListener() {
        @Override

        public void onDataChange(DataSnapshot dataSnapshot) {

            user = User.getUserFromDataFrame(dataSnapshot);

        }


        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
        usersdRef.addListenerForSingleValueEvent(eventListener);
    }

    public void refreshProjects(){
        backendService = new BackendService(mResultCallback, this);
        try{
            URL url = new URL("https", "mcc-fall-2019-g01-258815.appspot.com", "projects/"+getCurrentUser().getUserID());
            backendService.getDataVolleyArray(ResponseCodes.GET_PROJECTS_BY_USER.getValue(), url.toString() );
            confirmTheImages();
            confirmFavorites();


        }
        catch (Exception ex){

        }

    }

    public void confirmTheImages(){
        for(int i=0; i<projectsList.size();i++){
            projectsList.get(i).setProjectImage(projectsList.get(i).getProjectImage());
        }
        updateProjects();
    }

    public void confirmFavorites(){
        if(getFavoriteProjectsList().size()>0){
            for(int i=0; i<projectsList.size();i++){
                if(projectsList.get(i).getFavoriteButtonState()==false){
                    removeProjectFromFavorites(projectsList.get(i));
                }
            }
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()){
            case R.id.nav_euser:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                break;
            case R.id.logout_user:
                user2.signOut();
                startActivity(new Intent(ProjectPage.this, MainActivity.class)); //Go back to login page
                finish();
                break;
            default: break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


        @Override
        public void onBackPressed(){
            if (drawer.isDrawerOpen(GravityCompat.START)){
                drawer.closeDrawer(GravityCompat.START);
            }else {
                super.onBackPressed();
            }
        }

    public  static  void addProject(Project project){
        projectsList.add(project);
        updateProjects();

    }

    public static void updateProjects(){
        FragmentProjects.updateAdapter();
        FragmentUpcoming.updateAdapter();
        FragmentFavorites.updateAdapter();

    }

    public  void addMembertoProject(ArrayList<User> users, String project_id){

        try {


            JSONArray array = new JSONArray();
            for (User user : users) {
                array.put(user.ToJson());
            }
            URL url = new URL("https", "mcc-fall-2019-g01-258815.appspot.com/project", String.valueOf(project_id));
            JSONArray jsonArray = new JSONArray();
            for (User user : users){
                jsonArray.put(user.ToJson());
            }
            JSONObject users_obj = new JSONObject();
            users_obj.put("users", jsonArray);


            backendService = new BackendService(mResultCallback, this);
            backendService.putDataVolley(ResponseCodes.PUT_USER_TO_PROJECT.getValue(), url.toString(), users_obj);
        }

        catch (Exception ex){
            Log.d(this.TAG, ex.getMessage().toString());
        }

    }

    public static void addProjectToFavorites(Project project){

        project.setFavoriteButtonState(true);

        getCurrentUser().AddFavourites(project);
        favoriteProjectsList.add(project);
        Collections.sort(favoriteProjectsList, Project.compareNames());
        updateProjects();



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(ProjectPage.RequestCodes.AddMember.getValue() == requestCode) {
                if (resultCode == Activity.RESULT_OK) {
                    // get User object
                    Project proj = data.getExtras().getParcelable("project");
                    ArrayList<User> users =  data.getParcelableArrayListExtra("members");
                    for (User user : users)
                    {
                        Log.d("User", user.getUsername());

                    }
                    // get Project object, but it's include not all properties


                    for(Project project: projectsList){
                        if(project.getProjectId().equals(proj.getProjectId())){

                            ArrayList<User> olduser = project.getUsers();
                            ArrayList<User>merged = User.merge(olduser, users);
                            project.setUsers(merged);
                        }
                    }

                    // Now send put request
                    addMembertoProject(users, proj.getProjectId());
                    Log.d("carlitos", "Added 1 more, size: "+"             "+projectsList.get(0) +"             "+ projectsList.get(0).getUsers().size());


                }


        }

        if(RequestCodes.AddProject.getValue() == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                refreshProjects();
            }
        }
    }

    public static void removeProjectFromFavorites(Project project){
        project.setFavoriteButtonState(false);
        getCurrentUser().RemoveFavourites(project);

        for(int i=0; i<favoriteProjectsList.size();i++){
            if(favoriteProjectsList.get(i).getName().equals(project.getName())){
                favoriteProjectsList.remove(favoriteProjectsList.get(i));
               // Collections.sort(favoriteProjecstList);


                Collections.sort(favoriteProjectsList, Project.compareNames());
                FragmentFavorites.updateAdapter();
                break;
            }
        }
        for(int i=0; i<projectsList.size();i++){
            if(projectsList.get(i).getName().equals(project.getName())){
                projectsList.get(i).setFavoriteButtonState(false);
                FragmentProjects.updateAdapter();
                break;
            }
        }

    }

    private void setupToolBar() {

    }

    public static ArrayList<Project> getProjectsList(){
        return projectsList;
    }
    public static ArrayList<Project> getFavoriteProjectsList(){
        return favoriteProjectsList;
    }

    public static Context getAppContext() {
        return context;
    }

    public void setupViewPager(ViewPager viewPager){
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentFavorites(),"");
        adapter.addFragment(new FragmentProjects(),"");
        adapter.addFragment(new FragmentUpcoming(),"");
        viewPager.setAdapter(adapter);

    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu_search, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                //text has changed, apply filter
                if (adapter != null) {
                    //TODO BACKEND support to filter using keywords
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) {
                    adapter.getFilter().filter(query);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static void updateAllDeletedManually(boolean b){
        allDeletedManually=b;
    }


    public void sendNotification(String type) {
        if(type.equals("addedToProject")){
            Notification noti = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_star)
                    .setContentTitle("New Project!")
                    .setContentText("You were added to a new project.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            notificationManager.notify(1, noti);
        }else if(type.equals("assignedToTask")){
            Notification noti = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_star)
                    .setContentTitle("New Task!")
                    .setContentText("You were assigned to a new task.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            notificationManager.notify(1, noti);

        }else if(type.equals("deadlineApproaching")){

        }


    }

}
