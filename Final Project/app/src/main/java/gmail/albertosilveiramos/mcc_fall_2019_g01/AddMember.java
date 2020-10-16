package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

public class AddMember extends AppCompatActivity {

    private ArrayList<User> arrayuser;
    private ListView mListView;
    private ArrayList<User> selected;
    private ArrayAdapter adapter;

    private Project project;
    private  Toolbar toolbar;
    private User current_user;
    private boolean isTaskAddMember = false;


    private String taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        setupToolBar();

        mListView  = findViewById(R.id.lvMember);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        selected = new ArrayList<>();

        FloatingActionButton addButton = findViewById(R.id.proj_fab_addproj);

        boolean hasExtra = getIntent().hasExtra("user_list");
        addButton.setClickable(true);
        if(hasExtra){
            ArrayList<User>users = getIntent().getExtras().getParcelableArrayList("user_list");

            taskId = getIntent().getExtras().getString("task_id");
            isTaskAddMember = true;
            arrayuser  = users;
            selected = new ArrayList<>();

            String [] names  = new String[arrayuser.size()];
            for (int i = 0;i < names.length; i++){
                names[i] = users.get(i).getUsername();
            }

            adapter = new ArrayAdapter<String>( AddMember.this,
                    android.R.layout.simple_list_item_1, names  );
            mListView.setAdapter(adapter);

        }

        else{
            isTaskAddMember = false;
            final ArrayList<User>filterlist = getIntent().getExtras().getParcelableArrayList("filter_list");
            project = (Project) getIntent().getExtras().getParcelable("project");
            current_user = getIntent().getExtras().getParcelable("current_user");



            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

            DatabaseReference usersdRef = rootRef.child("users");

            ValueEventListener eventListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {
                    arrayuser = new ArrayList<>();
                    ArrayList<String> usernames = new ArrayList();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {


                        if (ds.getKey() != null ) {

                            User user = User.getUserFromDataFrame(ds);

                            if(User.Compare(filterlist, user))continue;

                            // filter current user from selection
                            if (current_user != null && current_user.getUserID().equals(user.getUserID()) || user == null || user.getUsername() == null)
                                continue;

                            usernames.add(user.getUsername());
                            arrayuser.add(user);
                        }
                    }

                    String[] arr = usernames.toArray(new String[0]);
                    adapter = new ArrayAdapter<String>(AddMember.this,
                            android.R.layout.simple_list_item_1, arr);
                    mListView.setAdapter(adapter);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            usersdRef.addListenerForSingleValueEvent(eventListener);

        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProjectPage.updateProjects();
                Intent intent = null;
                if(isTaskAddMember){
                    intent = new Intent(AddMember.this, ProjectTask.class);
                    intent.putExtra("task_id", taskId);
                }
                else{
                    intent = new Intent(AddMember.this, ProjectPage.class);
                    intent.putExtra("project", project);
                }

                intent.putParcelableArrayListExtra("members", selected);

                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                final String member = (String) mListView.getItemAtPosition(position);

                //boolean match = selected.stream().filter(u -> u.getUsername().equals(member)).findFirst().isPresent();
                User match = null;
                for(User user: selected){
                    if(user.getUsername().equals(member)){
                        match = user;
                    }
                }

                if(match != null){
                    selected.remove(match);
                    Log.d("Remove", member);
                    view.setBackgroundColor(Color.WHITE);
                }
                else {
                    User new_sel = null;
                    for(User user: arrayuser){
                        if(user.getUsername() == member){
                            new_sel = user;
                        }
                    }

                    selected.add(new_sel);
                    Log.d("Selected", member);
                    view.setBackgroundColor(Color.argb(100,102, 178, 255));
                }

            }

        });
    }



    private void setupToolBar() {
        toolbar = (Toolbar) findViewById(R.id.projadd_pb_toolbar);
        if (toolbar == null) return;
        setSupportActionBar(toolbar);
        setTitle("Add member");
        toolbar.setTitleTextColor(Color.WHITE);
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
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        return true;
    }

}
