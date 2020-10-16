package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapterTask extends RecyclerView.Adapter<CustomAdapterTask.ViewHolder> implements Filterable {


    private List<Task> taskList;
    private List<Task> taskListCopy;
    private Context context;
    private ViewHolder holderGeneral;
    private  Activity activity;


    private Project project;



    private BackendService backendService;
    private IResult mResultCallback;
    private String TAG = this.getClass().getName();
    public CustomAdapterTask(Project project, Context context) {
        Log.d("CustomAdapterTask", "CustomAdapterTask() constructor called");
        this.taskList = project.getTasks();
        this.context = context;
        this.project = project;
        taskListCopy = new ArrayList<>(taskList);
    }

    public CustomAdapterTask(Project project, Context context, Activity activity)
    {
        this(project, context);
        this.activity = activity;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.task, viewGroup, false);

        return new ViewHolder(v);
    }

    public void onPrepareOptionsMenu(Menu menu, FirebaseAuth mAuth, int i)
    {
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {

        final Task task = taskList.get(i);

        this.holderGeneral=holder;

        holder.taskDescription.setText((task.getDescription()));

        holder.checkBox.setChecked(task.getisChecked());


        if( holder.checkBox.isChecked()){
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else if(!holder.checkBox.isChecked()){
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.checkBox.isChecked()) {
                    holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    taskList.get(i).setChecked(false);
                    taskList.get(i).setStatus("completed");
                } else if (holder.checkBox.isChecked()) {
                    holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    taskList.get(i).setChecked(true);
                    if (!project.isIndividualProject()) {
                        taskList.get(i).setStatus("on-going");
                    } else {
                        taskList.get(i).setStatus("pending");
                    }
                }

                try {
                    Task task = taskList.get(i);
                    URL url = new URL("https", "mcc-fall-2019-g01-258815.appspot.com/task", String.valueOf(task.getBackendId()));
                    JSONObject jsonObject = task.toJsonStatus();
                    backendService = new BackendService(mResultCallback, ProjectTask.getAppContext());
                    backendService.putDataVolley("PUT_TASK", url.toString(), jsonObject);
                    Log.d(this.getClass().getCanonicalName(), "sending " + url.toString());
                } catch (Exception ex) {

                }



            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ProjectPage.getAppContext(), holder.itemView);
                popupMenu.inflate(R.menu.task_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.addmemberTask:
                                Intent intent = new Intent(ProjectTask.getAppContext(), AddMember.class);
                                intent.putExtra("user_list", project.getUsers());
                                Task task = ProjectTask.getProject().getTasks().get(i);
                                intent.putExtra("task_id", task.getBackendId());
                                ProjectTask.getActivity().startActivityForResult(intent,ProjectTask.RequestCodes.AddMember.getValue());
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    void initVolleyCallback() {
        mResultCallback = new IResult() {


            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try{

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
        return taskList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    public Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Task> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length()==0){
                filteredList.addAll(taskListCopy);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Task task : taskListCopy){
                    if(task.getDescription().toLowerCase().contains(filterPattern)){
                        filteredList.add(task);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            taskList.clear();
            taskList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };



    public class ViewHolder extends RecyclerView.ViewHolder{

        public CheckBox checkBox;

        public TextView taskDescription;


        public ViewHolder(final View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkbox_task);

            taskDescription = itemView.findViewById(R.id.taskDescription);

            itemView.setOnClickListener(new View.OnClickListener() { //TODO instead of sending just the name, send the whole Project class
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(ProjectPage.getAppContext(), itemView);
                    popupMenu.inflate(R.menu.task_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.addmemberTask:
                                    Intent intent = new Intent(ProjectTask.getAppContext(), AddMember.class);
                                    intent.putExtra("user_list", project.getUsers());
                                    Task task = ProjectTask.getProject().getTasks().get(item.getItemId());
                                    intent.putExtra("task_id", task.getID());
                                    ProjectTask.getActivity().startActivityForResult(intent,ProjectTask.RequestCodes.AddMember.getValue());
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });


        }
    }

}
