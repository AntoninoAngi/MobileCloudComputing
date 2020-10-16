package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;



public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements Filterable {


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<Project> projectList;
    private List<Project> projectListCopy;
    private Context context;
    private ViewHolder holderGeneral;
    private Project currentChosen;

    private BackendService backendService;
    private IResult mResultCallback;
    private String TAG = this.getClass().getSimpleName();

    private ArrayList<ImageView> userPicsList = new ArrayList<>();
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

    private  Activity activity;
    public CustomAdapter(List<Project> projectList, Context context) {
        this.projectList = projectList;
        this.context = context;
        projectListCopy = new ArrayList<>(projectList);
        initVolleyCallback();
    }

    public CustomAdapter(List<Project> projectList, Context context, Activity activity)
    {
        this(projectList, context);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.project_card, viewGroup, false);

        return new ViewHolder(v);
    }






    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {

        final Project project = projectList.get(i);

        this.holderGeneral=holder;

        if(project.getProjectImage()!=null){
            project.getProjectImage().invalidate();
            BitmapDrawable drawable = (BitmapDrawable) project.getProjectImage().getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            // holder.imageView.setImageBitmap(bitmap);

            Bitmap crop=null;

            if (bitmap.getWidth() >= bitmap.getHeight()){

                crop = Bitmap.createBitmap(
                        bitmap,
                        bitmap.getWidth()/2 - bitmap.getHeight()/2,
                        0,
                        bitmap.getHeight(),
                        bitmap.getHeight()
                );

            }else{

                crop = Bitmap.createBitmap(
                        bitmap,
                        0,
                        bitmap.getHeight()/2 - bitmap.getWidth()/2,
                        bitmap.getWidth(),
                        bitmap.getWidth()
                );
            }

            holder.imageView.setImageBitmap(crop);

        }else{
            Bitmap largeIcon = BitmapFactory.decodeResource(ProjectPage.getAppContext().getResources(), R.drawable.folderdefault);
            holder.imageView.setImageBitmap(largeIcon);

        }

        holder.project_name.setText((project.getName()));

        Date lastModified = project.getLastModifiedDate();
        CharSequence MonthDay= DateFormat.format("MMM d",lastModified);
        CharSequence currentDay= DateFormat.format("MMM d",Calendar.getInstance());

        if(MonthDay.equals(currentDay)){
            MonthDay="Today";
        }

        holder.projectModifiedDate.setText("Last Modified: "+MonthDay);

        holder.favorite_button.setChecked(project.getFavoriteButtonState());

        holder.favorite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //It changes the state automatically when pressed.

                if (!holder.favorite_button.isChecked()) {
                    //Verify if its in the favorites list and remove if that's the case
                    ProjectPage.removeProjectFromFavorites(project);


                } else if (holder.favorite_button.isChecked()) {
                    //Add to the favorites list
                    ProjectPage.addProjectToFavorites(project);


                }
            }});


        holder.textOpt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!project.isIndividualProject() && ((ProjectPage)activity).getCurrentUser().getUserID().equals(project.getAdmin().getUserID())){
                    PopupMenu popupMenu = new PopupMenu(ProjectPage.getAppContext(), holder.textOpt);
                    popupMenu.inflate(R.menu.project_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            User current_user = ((ProjectPage)activity).getCurrentUser();
                            switch (item.getItemId()) {
                                case R.id.action_settings:
                                    //TODO function to generate pdf
                                    generatePdf(project.getProjectId());
                                    //Toast.makeText(context, "PDF generated!", Toast.LENGTH_LONG).show();
                                    break;
                                case R.id.delete_project:


                                    if (projectList.get(i).getAdmin().getUserID().equals(current_user.getUserID())) {
                                        try {
                                            Project proj = projectList.get(i);
                                            String projectid = projectList.get(i).getProjectId();

                                            URL url = new URL ("https", "mcc-fall-2019-g01-258815.appspot.com/project", projectid);
                                            backendService = new BackendService(mResultCallback, ProjectPage.getAppContext());
                                            backendService.deleteDataVolley("DELETECALL", url.toString());
                                            projectList.remove(i);

                                            notifyDataSetChanged();

                                            String nameToRemove = proj.getName();
                                            for(int a=0; a<projectList.size(); a++){
                                                if(projectList.get(a).getName().equals(nameToRemove)){
                                                    projectList.remove(a);
                                                }
                                            }
                                            for(int b=0; b<ProjectPage.getFavoriteProjectsList().size(); b++){
                                                if(ProjectPage.getFavoriteProjectsList().get(b).getName().equals(nameToRemove)){
                                                    ProjectPage.getFavoriteProjectsList().remove(b);
                                                }
                                            }

                                            if(projectList.size()==0){
                                                ProjectPage.updateAllDeletedManually(true);
                                            }
                                            ProjectPage.updateProjects();
                                            Toast.makeText(context, proj.getName() + " deleted!", Toast.LENGTH_LONG).show();
                                        }
                                        catch (Exception ex){

                                        }

                                    }else{
                                        Toast.makeText(context, "Cannot delete that project because the current user is not the admin", Toast.LENGTH_LONG).show();
                                    }
                                    break;

                                case R.id.addmember:
                                    Intent intent = new Intent(activity, AddMember.class);
                                    Project project = projectList.get(i);
                                    intent.putExtra("project", project);
                                    intent.putExtra("filter_list", project.getUsers());
                                    intent.putExtra("current_user", current_user);
                                    activity.startActivityForResult(intent, ProjectPage.RequestCodes.AddMember.getValue() );
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
                else if(!project.isIndividualProject() && !((ProjectPage)activity).getCurrentUser().getUserID().equals(project.getAdmin().getUserID())){
                    PopupMenu popupMenu = new PopupMenu(ProjectPage.getAppContext(), holder.textOpt);
                    popupMenu.inflate(R.menu.project_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            User current_user = ((ProjectPage)activity).getCurrentUser();
                            switch (item.getItemId()) {
                                case R.id.action_settings:
                                    //TODO function to generate pdf
                                    generatePdf(project.getProjectId());

                                    break;
                                case R.id.delete_project:
                                    Toast.makeText(context, "Only the administrator is able to delete the project.", Toast.LENGTH_LONG).show();

                                    break;
                                case R.id.addmember:
                                    Toast.makeText(context, "Only the administrator is able to add members.", Toast.LENGTH_LONG).show();

                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
                else{
                    PopupMenu popupMenu = new PopupMenu(ProjectPage.getAppContext(), holder.textOpt);
                    popupMenu.inflate(R.menu.project_menu_individual);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            User current_user = ((ProjectPage)activity).getCurrentUser();
                            switch (item.getItemId()) {
                                case R.id.delete_project:

                                    if (projectList.get(i).getAdmin().getUserID().equals(current_user.getUserID())) {
                                        try {
                                            Project proj = projectList.get(i);
                                            String projectid = projectList.get(i).getProjectId();

                                            URL url = new URL ("https", "mcc-fall-2019-g01-258815.appspot.com/project", projectid);
                                            backendService = new BackendService(mResultCallback, ProjectPage.getAppContext());
                                            backendService.deleteDataVolley("DELETECALL", url.toString());
                                            projectList.remove(i);
                                            notifyDataSetChanged();


                                            String nameToRemove = proj.getName();
                                            for(int a=0; a<projectList.size(); a++){
                                                if(projectList.get(a).getName().equals(nameToRemove)){
                                                    projectList.remove(a);
                                                }
                                            }
                                            for(int b=0; b<ProjectPage.getFavoriteProjectsList().size(); b++){
                                                if(ProjectPage.getFavoriteProjectsList().get(b).getName().equals(nameToRemove)){
                                                    ProjectPage.getFavoriteProjectsList().remove(b);
                                                }
                                            }


                                            if(projectList.size()==0){
                                                ProjectPage.updateAllDeletedManually(true);
                                            }
                                            ProjectPage.updateProjects();
                                            Toast.makeText(context, proj.getName() + " deleted!", Toast.LENGTH_LONG).show();
                                        }
                                        catch (Exception ex){

                                        }

                                    }else{
                                        Toast.makeText(context, "Cannot delete that project because the current user is not the admin", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }



            }


        });

        if(project.getPictures() != null && project.getPictures().size()>0 || project.getFiles() != null && project.getFiles().size()>0){
            holder.mediaIcon.setVisibility(View.VISIBLE);
        }else{
            holder.mediaIcon.setVisibility(View.INVISIBLE);
        }


        if(!project.isIndividualProject()){

            if(project.getUsers().size()>=1 &&project.getUsers().get(0)!=null && holder.userPic1.getVisibility()==View.INVISIBLE){
                getUserPhoto(project,0,holder);
            }
            else if(project.getUsers().size()>=2 && project.getUsers().get(1)!=null && holder.userPic2.getVisibility()==View.INVISIBLE  ){
                getUserPhoto(project,1,holder);
            }
            else if(project.getUsers().size()>=3 && project.getUsers().get(2)!=null && holder.userPic3.getVisibility()==View.INVISIBLE ){
                getUserPhoto(project,2,holder);
            }
        }



        currentChosen = project;

    }

    public void getUserPhoto(Project project, final int position, final ViewHolder holder){
        db.collection("users")
                .whereEqualTo("id_string", project.getUsers().get(position).getUserID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            task.getResult().getDocuments().get(0).getString("image");
                            final String photo_url;
                            if(task.getResult().getDocuments().get(0).getString("image").compareTo("")==0)
                                photo_url = "default.png";
                            else{
                                photo_url =task.getResult().getDocuments().get(0).getString("image");
                            }
                            mStorageRef.child(photo_url).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if(position==0){
                                        holder.userPic1.setVisibility(View.INVISIBLE);
                                        Picasso.get().load(uri.toString()).fit().into(holder.userPic1);
                                        holder.userPic1.setVisibility(View.VISIBLE);
                                    }
                                    else if(position==1){
                                        holder.userPic2.setVisibility(View.INVISIBLE);
                                        Picasso.get().load(uri.toString()).fit().into( holder.userPic2);
                                        holder.userPic2.setVisibility(View.VISIBLE);
                                    }
                                    else if(position==2){
                                        holder.userPic3.setVisibility(View.INVISIBLE);
                                        Picasso.get().load(uri.toString()).fit().into(holder.userPic3);
                                        holder.userPic3.setVisibility(View.VISIBLE);

                                    }
                                }
                            });
                        }
                    }
                });
    }


    void initVolleyCallback() {
        mResultCallback = new IResult() {


            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try{

                    ((ProjectPage)activity).refreshProjects();
                    Log.d(TAG, response.get("response").toString());
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
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!" + error);
            }
        };
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    public Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Project> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length()==0){
                filteredList.addAll(projectListCopy);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Project project : projectListCopy){
                    if(project.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(project);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            projectList.clear();
            projectList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };



    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;

        public CardView background_cardView;

        public TextView project_name;

        public TextView projectModifiedDate;

        public ToggleButton favorite_button;

        public TextView textOpt;

        public ImageView mediaIcon;

        public ImageView userPic1;

        public ImageView userPic2;

        public ImageView userPic3;


        public FirebaseAuth mAuth;

        public ViewHolder(final View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

            mAuth = FirebaseAuth.getInstance();

            background_cardView = itemView.findViewById(R.id.background_cardView);

            project_name = itemView.findViewById(R.id.project_name);

            projectModifiedDate = itemView.findViewById(R.id.last_modified);

            favorite_button = itemView.findViewById(R.id.favorite_button);

            textOpt = itemView.findViewById(R.id.txtOptionDigit);

            mediaIcon =itemView.findViewById(R.id.mediaIcon);

            userPic1 = itemView.findViewById(R.id.userPic1);

            userPic2 = itemView.findViewById(R.id.userPic2);

            userPic3 = itemView.findViewById(R.id.userPic3);



            itemView.setOnClickListener(new View.OnClickListener(){ //TODO instead of sending just the name, send the whole Project class
                @Override public void onClick(View v) {

                    Intent intent = new Intent(itemView.getContext(), ProjectTask.class);
                    intent.putExtra("chosenProject", project_name.getText());
                    itemView.getContext().startActivity(intent);

                }
            });


        }
    }

    public String getLastValueOfString(String s){
        String[] bits = s.split(" ");
        String lastOne = bits[bits.length-1];
        return lastOne;
    }

    public void generatePdf(String id) {

            id = "-"+id;

            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }

            final DatabaseReference mDatabaseRefProj = FirebaseDatabase.getInstance().getReference("projects").child(id);
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            mDatabaseRefProj.child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        final String names = dataSnapshot.getValue().toString();
                        mDatabaseRefProj.child("users_index").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final Map<String, String> users_id = (Map<String, String>) dataSnapshot.getValue();
                                final ArrayList<String> users = new ArrayList<>();
                                for (String f : users_id.keySet()) {
                                    db.collection("users").whereEqualTo("id_string", f).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                users.add(task.getResult().getDocuments().get(0).getString("username"));
                                                if (users_id.size() == users.size()) {
                                                    mDatabaseRefProj.child("tasks").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.getValue()!=null) {
                                                                final Map<String, String> tasks = (Map<String, String>) dataSnapshot.getValue();
                                                                final Map<String, String> task = new HashMap<String, String>();
                                                                for (final String g : tasks.keySet()) {
                                                                    final DatabaseReference mDatabaseRefProj2 = FirebaseDatabase.getInstance().getReference("tasks").child(g);
                                                                    mDatabaseRefProj2.child("status").addValueEventListener(new ValueEventListener() {
                                                                        String gi = g;

                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                            final String f = (String) dataSnapshot.getValue();
                                                                            //System.out.println(f);
                                                                            mDatabaseRefProj2.child("deadline").addValueEventListener(new ValueEventListener() {
                                                                                String fi = gi;
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                    String date = (String) dataSnapshot.getValue();
                                                                                    System.out.println(date);
                                                                                    task.put(fi, f + "," + date);
                                                                                    if (task.size() == tasks.size()) {

                                                                                        PdfDocument document = new PdfDocument();
                                                                                        // crate a page description
                                                                                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
                                                                                        // start a page
                                                                                        PdfDocument.Page page = document.startPage(pageInfo);
                                                                                        Canvas canvas = page.getCanvas();
                                                                                        Paint paint = new Paint();
                                                                                        paint.setColor(Color.GREEN);
                                                                                        canvas.drawCircle(70, 450, 400, paint);
                                                                                        paint.setColor(Color.RED);
                                                                                        canvas.drawCircle(70, 50, 300, paint);
                                                                                        paint.setColor(Color.WHITE);
                                                                                        paint.setTextSize(20);
                                                                                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                                        canvas.drawText("Project: " + names, 10, 50, paint);
                                                                                        paint.setColor(Color.WHITE);
                                                                                        paint.setTextSize(10);
                                                                                        canvas.drawText("List of users:", 17, 70, paint);
                                                                                        int i;
                                                                                        for (i = 0; i < users.size(); i++) {
                                                                                            canvas.drawText(users.get(i), 27 + (i * 7), 82 + (i * 10), paint);
                                                                                        }
                                                                                        int actualPosition = 82 + (i * 10) + 20;
                                                                                        paint.setColor(Color.WHITE);
                                                                                        paint.setTextSize(10);
                                                                                        canvas.drawText("List of tasks:", 19, actualPosition, paint);
                                                                                        actualPosition += 20;
                                                                                        Object id[] = task.keySet().toArray();
                                                                                        for (i = 0; i < task.size(); i++) {
                                                                                            canvas.drawText("Task id: " + id[i].toString(), 29 + (i * 7), actualPosition + (i * 20), paint);
                                                                                            canvas.drawText("Status : " + task.get(id[i].toString().split(",")[0]), 29 + (i * 7), actualPosition + (i * 20) + 10, paint);
                                                                                        }

                                                                                        //canvas.drawt
                                                                                        // finish the page
                                                                                        document.finishPage(page);
// draw text on the graphics object of the page
                                                                                        // Create Page 2
                                                                                        pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 2).create();
                                                                                        page = document.startPage(pageInfo);
                                                                                        canvas = page.getCanvas();
                                                                                        paint = new Paint();
                                                                                        paint.setColor(Color.BLUE);
                                                                                        canvas.drawCircle(200, 130, 200, paint);
                                                                                        document.finishPage(page);
                                                                                        // write the document content
                                                                                        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/";
                                                                                        File file = new File(directory_path);
                                                                                        if (!file.exists()) {
                                                                                            file.mkdirs();
                                                                                        }
                                                                                        String targetPdf = directory_path + names + ".pdf";
                                                                                        File filePath = new File(targetPdf);
                                                                                        try {
                                                                                            filePath.createNewFile();
                                                                                        } catch (IOException e) {
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                        try {
                                                                                            document.writeTo(new FileOutputStream(filePath));
                                                                                            //Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_LONG).show();
                                                                                        } catch (IOException e) {
                                                                                            Log.e("main", "error " + e.toString());
                                                                                            //Toast.makeText(CustomAdapter.this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                        // close the document
                                                                                        document.close();
                                                                                        Toast.makeText(context, "PDF generated!", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                        }


                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }
                                                            } else {
                                                                PdfDocument document = new PdfDocument();
                                                                // crate a page description
                                                                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
                                                                // start a page
                                                                PdfDocument.Page page = document.startPage(pageInfo);
                                                                Canvas canvas = page.getCanvas();
                                                                Paint paint = new Paint();
                                                                paint.setColor(Color.GREEN);
                                                                canvas.drawCircle(70, 450, 400, paint);
                                                                paint.setColor(Color.RED);
                                                                canvas.drawCircle(70, 50, 300, paint);
                                                                paint.setColor(Color.WHITE);
                                                                paint.setTextSize(20);
                                                                paint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                canvas.drawText("Project: " + names, 10, 50, paint);
                                                                paint.setColor(Color.WHITE);
                                                                paint.setTextSize(10);
                                                                canvas.drawText("List of users:", 17, 70, paint);
                                                                int i;
                                                                for (i = 0; i < users.size(); i++) {
                                                                    canvas.drawText(users.get(i), 27 + (i * 7), 82 + (i * 10), paint);
                                                                }
                                                                int actualPosition = 82 + (i * 10) + 20;
                                                                paint.setColor(Color.WHITE);
                                                                paint.setTextSize(10);
                                                                canvas.drawText("No tasks associated with this project", 19, actualPosition, paint);


                                                                //canvas.drawt
                                                                // finish the page
                                                                document.finishPage(page);
// draw text on the graphics object of the page
                                                                // Create Page 2
                                                                pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 2).create();
                                                                page = document.startPage(pageInfo);
                                                                canvas = page.getCanvas();
                                                                paint = new Paint();
                                                                paint.setColor(Color.BLUE);
                                                                canvas.drawCircle(200, 130, 200, paint);
                                                                document.finishPage(page);
                                                                // write the document content
                                                                String directory_path = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/";
                                                                File file = new File(directory_path);
                                                                if (!file.exists()) {
                                                                    file.mkdirs();
                                                                }
                                                                String targetPdf = directory_path + names + ".pdf";
                                                                File filePath = new File(targetPdf);
                                                                try {
                                                                    filePath.createNewFile();
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                try {
                                                                    document.writeTo(new FileOutputStream(filePath));
                                                                    //Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_LONG).show();
                                                                } catch (IOException e) {
                                                                    Log.e("main", "error " + e.toString());
                                                                    //Toast.makeText(MainActivity.this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
                                                                }
                                                                // close the document
                                                                document.close();
                                                                Toast.makeText(context, "PDF generated!", Toast.LENGTH_LONG).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    } else {
                        //Toast.makeText(MainActivity.this, "Error finding project", Toast.LENGTH_LONG).show();
                    }}
                @Override
                public void onCancelled (@NonNull DatabaseError databaseError){

                }

            });

        }


}
