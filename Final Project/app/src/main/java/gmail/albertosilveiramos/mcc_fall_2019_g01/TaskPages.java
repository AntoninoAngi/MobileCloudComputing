package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskPages extends Fragment {

    private static RecyclerView recyclerView;
    private static CustomAdapterTask adapter;
    private Project project;
    private FloatingActionButton addTaskButton;



    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_fragment,container,false);

        project = ProjectTask.getProject();

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_task);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProjectTask.getAppContext()));

        Log.d("TaskPages", "ready to build adapter");
        adapter = new CustomAdapterTask(ProjectTask.getProject(), ProjectTask.getAppContext());
        recyclerView.setAdapter(adapter);

        addTaskButton = view.findViewById(R.id.task_bottom_new);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Task_creation.class);
                getActivity().startActivityForResult(intent, ProjectTask.RequestCodes.AddTask.getValue());
            }
        });


        return view;

    }


    public static Context getAppContext() {
        return ProjectTask.getAppContext();
    }

    public static void updateAdapter(){
        adapter.notifyDataSetChanged();
    }
}
