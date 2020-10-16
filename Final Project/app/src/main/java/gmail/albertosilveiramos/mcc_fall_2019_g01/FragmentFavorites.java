package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentFavorites extends Fragment {

    private static final String TAG="FragmentFavorites";
    private static RecyclerView recyclerView;
    private static CustomAdapter adapter;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorites_fragment,container,false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_favorites);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));


        adapter = new CustomAdapter(ProjectPage.getFavoriteProjectsList(), getAppContext(), getActivity());
        recyclerView.setAdapter(adapter);


        return view;

    }
    public static Context getAppContext() {
        return ProjectPage.getAppContext();
    }

    public static void updateAdapter(){
        adapter.notifyDataSetChanged();
    }

}
