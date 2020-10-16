package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class FilesPage extends Fragment {

    private static RecyclerView recyclerView;
    private static CustomAdapterFile adapter;
    private FloatingActionButton addFileButton;
    private StorageReference mStorageRef;



    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.files_fragment,container,false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_file);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));


        adapter = new CustomAdapterFile(ProjectTask.getProject().getFiles(), ProjectTask.getProject().getFilesRefs(), getAppContext());
        recyclerView.setAdapter(adapter);

        addFileButton = view.findViewById(R.id.add_file);

        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ProjectTask.addFile(new File(""));
                filePath();
            }
        });


        return view;

    }

    public void filePath(){
        PopupMenu popupMenu = new PopupMenu(ProjectPage.getAppContext(), addFileButton);
        popupMenu.inflate(R.menu.files_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.addpdf:
                        addPDF();
                        break;
                    case R.id.addtext:
                        addDOC();
                        break;
                    case R.id.addmusic:
                        addMP3();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    public void addPDF(){
        Toast.makeText(getContext(), "PDF", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
    }

    public void addDOC(){
        Toast.makeText(getContext(), "TEXT", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Text File"), 1);
    }

    public void addMP3(){
        Toast.makeText(getContext(), "VIDEO", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 1);

    }


    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                    //------
                    Uri returnUri = result.getData();
                    Cursor returnCursor = getContext().getContentResolver().query(returnUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    //------
                    File file = new File(returnCursor.getString(nameIndex));

                    StorageReference fileUrl = uploadFile(file, returnUri);

                    ProjectTask.addFile(file, fileUrl);

            }
        }
    }


    // Upload file to storageRoot/uploads/project_id/
    private StorageReference uploadFile(File file, Uri uri) {

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference fileRef = mStorageRef.child(ProjectTask.getProject().getProjectId()).child(file.getName());
        Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
        UploadTask uploadTask = fileRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getContext(), "Unsuccessful upload", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Successful upload", Toast.LENGTH_SHORT).show();
            }
        });

        return fileRef;
    }

    private void downloadFile(StorageReference storageRef) {

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), storageRef.getName());
        Log.d("FileLocation", localFile.getAbsolutePath());

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "File has been downloaded in your external storage directory", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getContext(), "Download failed, dit you set storage authorisation?", Toast.LENGTH_SHORT).show();
            }
        });

    }


    public static Context getAppContext() {
        return ProjectTask.getAppContext();
    }

    public static void updateAdapter(){
        adapter.notifyDataSetChanged();
    }

}
