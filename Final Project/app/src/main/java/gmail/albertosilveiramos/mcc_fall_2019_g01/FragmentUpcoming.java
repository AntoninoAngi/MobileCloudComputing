package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentUpcoming extends Fragment {

    private static final String TAG="FragmentUpcoming";
    private static RecyclerView recyclerView;
    private static CustomAdapter adapter;


    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upcoming_fragment,container,false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_upcoming);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));


        adapter = new CustomAdapter(ProjectPage.getProjectsList(), getAppContext(), getActivity());
        recyclerView.setAdapter(adapter);


        if(ProjectPage.getProjectsList().size()>1){
            Collections.sort(ProjectPage.getProjectsList(), Project.compareDueDates());
        }




        return view;

    }
    public static Context getAppContext() {
        return ProjectPage.getAppContext();
    }

    public static void updateAdapter(){
        adapter.notifyDataSetChanged();
    }


}
