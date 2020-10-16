package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class PicturesPage extends Fragment {

    private static RecyclerView recyclerView;
    private FloatingActionButton addPictureButton;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private File imageFile;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Uri> images = new ArrayList<>();

    private Project project;
    private static CustomAdapterPicture adapter;

    private StorageReference mStorageRef;


    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pictures_fragment,container,false);

        project = ProjectTask.getProject();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this.getContext(), 2);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new CustomAdapterPicture(ProjectTask.getProject().getPictures(), ProjectTask.getProject().getPicturesRefs(), getAppContext());
        recyclerView.setAdapter(adapter);
        addPictureButton = view.findViewById(R.id.photo_bottom_new);

        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Photo_from_camera_or_gallery();
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

    public void Photo_from_camera_or_gallery() {

        AlertDialog.Builder adb = new AlertDialog.Builder(this.getContext());

        //Set a title for alert dialog
        adb.setTitle("Select Options");

        //Initialize a new String Array
        final String[] Choises = new String[]{
                // Those color names are supported by
                //Android parseColor() method
                "Camera", "Gallery", "Cancel",
        };
        //It will render a single choice traditional list on alert dialog

        final boolean[] uploaded = {false};

        adb.setItems(Choises, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //This 'which' argument carry the list selected item's index position
                String clickedItemValue = Arrays.asList(Choises).get(which);
                //Set the TextView text color corresponded to the user selected color
                switch (clickedItemValue) {
                    case "Camera":
                        Intent pictureIntent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);


                        if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    MY_CAMERA_REQUEST_CODE);
                        }

                        if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                Toast.makeText(getContext(), "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),"com.example.android.provider", photoFile);
                                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(pictureIntent,MY_CAMERA_REQUEST_CODE);
                            }
                        }
                        uploaded[0] =true;
                        break;
                    case "Gallery":
                        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent2.setType("image/*");
                        String[] mimeTypes = {"image/jpeg", "image/png"};
                        intent2.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                        startActivityForResult(intent2, 1889);
                        uploaded[0]=true;
                        break;
                    case "Cancel":
                        uploaded[0]=false;
                        break;
                }
            }
        });
        adb.show();

    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFile = image;
        return image;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getAppContext(), "Camera permission granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(getAppContext(), "Camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            Cursor returnCursor = getContext().getContentResolver().query(imageUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            //------
            File file = new File(returnCursor.getString(nameIndex));

            String picturePath = "";
            StorageReference fileUrl = null;
            switch (requestCode) {
                case 1889: //from gallery

                    fileUrl = uploadFile(file, imageUri);

                    Bitmap bitmap=null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ProjectTask.addPicture(bitmap, fileUrl);
                    break;
                case MY_CAMERA_REQUEST_CODE: //from camera

                    fileUrl = uploadFile(file, imageUri);

                    Bitmap bm = null;
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(imageFile));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ProjectTask.addPicture(bm, fileUrl);

                   // images.add(Uri.fromFile(imageFile));
                    break;
                default:
                    break;
            }


        }else if(resultCode == 0) {
            Toast.makeText(getAppContext(), "No photo chosen", Toast.LENGTH_SHORT).show();
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

}
