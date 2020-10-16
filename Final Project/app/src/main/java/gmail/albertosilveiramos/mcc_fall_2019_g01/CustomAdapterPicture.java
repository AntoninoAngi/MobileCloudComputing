package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
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

public class CustomAdapterPicture extends RecyclerView.Adapter<CustomAdapterPicture.ViewHolder> implements Filterable {


    private List<Bitmap> pictureList;
    private List<StorageReference> fileRefList;
    private List<Bitmap> pictureListCopy;
    private Context context;
    private ViewHolder holderGeneral;




    public CustomAdapterPicture(List<Bitmap> pictureList, List<StorageReference> fileRefList, Context context) {
        this.pictureList = pictureList;
        this.fileRefList = fileRefList;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.image_card, viewGroup, false);

        return new ViewHolder(v);
    }

    public void onPrepareOptionsMenu(Menu menu, FirebaseAuth mAuth, int i)
    {

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {

        final Bitmap picture = pictureList.get(i);

        this.holderGeneral=holder;

        //Make the image fit the cardlayout
        Bitmap crop=null;

        if (picture.getWidth() >= picture.getHeight()){

            crop = Bitmap.createBitmap(
                    picture,
                    picture.getWidth()/2 - picture.getHeight()/2,
                    0,
                    picture.getHeight(),
                    picture.getHeight()
            );

        }else{

            crop = Bitmap.createBitmap(
                    picture,
                    0,
                    picture.getHeight()/2 - picture.getWidth()/2,
                    picture.getWidth(),
                    picture.getWidth()
            );
        }

        holder.picture.setImageBitmap(crop);
        holder.storRef = fileRefList.get(i);

    }





    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }


    public Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Bitmap> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length()==0){
                filteredList.addAll(pictureListCopy);
            }
            else {

            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            pictureList.clear();
            pictureList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView picture;
        public StorageReference storRef;


        public ViewHolder(final View itemView) {
            super(itemView);

            picture = (ImageView) itemView.findViewById(R.id.imageView_);

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
