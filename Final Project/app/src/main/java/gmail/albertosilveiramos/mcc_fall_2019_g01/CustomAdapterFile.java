package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapterFile extends RecyclerView.Adapter<CustomAdapterFile.ViewHolder> implements Filterable {


    private List<File> fileList;
    private List<StorageReference> fileRefList;
    private List<File> pictureListCopy;
    private Context context;
    private ViewHolder holderGeneral;




    public CustomAdapterFile(List<File> fileList, List<StorageReference> fileRefList, Context context) {
        this.fileList = fileList;
        this.fileRefList = fileRefList;
        this.context = context;
        pictureListCopy = new ArrayList<>(fileList);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.file, viewGroup, false);

        return new ViewHolder(v);
    }

    public void onPrepareOptionsMenu(Menu menu, FirebaseAuth mAuth, int i)
    {

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {

        final File file = fileList.get(i);
        final StorageReference storRef = fileRefList.get(i);

        this.holderGeneral=holder;

        //Read the path file, if its .pdf, add the pdf image, etc

        //Change this, read the path file

        holder.fileName.setText(storRef.getName());
        holder.storRef = storRef;

       // holder.picture.setImageBitmap(crop);
        if(storRef.getName().contains(".pdf")){
            holder.fileType.setImageResource(R.drawable.pdf_icon);
        } else if(storRef.getName().contains(".doc")){
            holder.fileType.setImageResource(R.drawable.text_icon);
        } else if(storRef.getName().contains(".mp3") || storRef.getName().contains(".mpg") || storRef.getName().contains(".mpeg") || storRef.getName().contains(".mp4")){
            holder.fileType.setImageResource(R.drawable.video_icon);
        }



    }





    @Override
    public int getItemCount() {
        return fileList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    public Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<File> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length()==0){
                filteredList.addAll(fileList);
            }
            else {

            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            fileList.clear();
            fileList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView fileType;
        public TextView fileName;
        public StorageReference storRef;


        public ViewHolder(final View itemView) {
            super(itemView);

             fileType = (ImageView) itemView.findViewById(R.id.fileType);
             fileName = (TextView) itemView.findViewById(R.id.fileName);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

           downloadFile(storRef);
        }

    }

    private void downloadFile(StorageReference storageRef) {

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), storageRef.getName());
        Toast.makeText(context, "Download in progress...", Toast.LENGTH_SHORT).show();
        Log.d("File download location", localFile.getAbsolutePath());

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context, "File has been downloaded in your external storage directory", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Download failed, did you set storage authorisation for this app?", Toast.LENGTH_SHORT).show();
            }
        });

    }

}


