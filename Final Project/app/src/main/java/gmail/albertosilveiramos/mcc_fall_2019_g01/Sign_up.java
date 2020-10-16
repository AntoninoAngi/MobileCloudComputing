package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

public class Sign_up extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText email;
    private EditText pass;
    private Button signup;
    private FirebaseAuth firebaseAuth;
    private ImageView image;
    private EditText musername;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private Uri uriSavedImage;
    private String username;
    private String Uri_saved;
    private File imageFile;
    private static final int MY_CAMERA_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_sign_in);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Sign Up");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.password);
        signup = (Button) findViewById(R.id.signUp);
        musername = (EditText) findViewById(R.id.DisplayName);
        image = (ImageButton) findViewById(R.id.image);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");


        firebaseAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                final String email2 = email.getText().toString();
                final String password = pass.getText().toString();
                final String username2 = musername.getText().toString();

                if (TextUtils.isEmpty(email2)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Sign_up.this, "Insert an email address", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(password)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Sign_up.this, "Insert a password", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(username2)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Sign_up.this, "Insert a valid username", Toast.LENGTH_LONG).show();
                } else { //if email + password + username are inserted

                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final Map<String, String> user = new HashMap<>();
                    user.put("username", username2);
                    user.put("email", email2);
                    user.put("password", password);


                    db.collection("users").whereEqualTo("username",username2).get().addOnCompleteListener(
                            new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.getResult().isEmpty()) {
                                        db.collection("users").whereEqualTo("email", email2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.getResult().isEmpty()) {
                                                    firebaseAuth.createUserWithEmailAndPassword(user.get("email"), user.get("password")).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if(task.isSuccessful()){
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(Sign_up.this, "Registered successfully", Toast.LENGTH_LONG).show();
                                                                musername.setText("");
                                                                email.setText("");
                                                                pass.setText("");
                                                                String user_id = firebaseAuth.getCurrentUser().getUid();
                                                                mDatabaseRef.child(user_id).child("username").setValue(username2);
                                                                mDatabaseRef.child(user_id).child("password").setValue(password);
                                                                mDatabaseRef.child(user_id).child("email").setValue(email2);

                                                                //DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                                                                if (!image.getTag().equals("0")) {
                                                                    uploadFile();
                                                                    //Map newUser = new HashMap();
                                                                    //newUser.put("username", user.get("username"));
                                                                    user.put("image", Uri_saved);
                                                                    mDatabaseRef.child(user_id).child("image").setValue(Uri_saved);
                                                                    //current_user_db.setValue(newUser);
                                                                } else {
                                                                    //Map newUser = new HashMap();
                                                                    //newUser.put("username", user.get("username"));
                                                                    user.put("image", "");
                                                                    mDatabaseRef.child(user_id).child("image").setValue("");
                                                                    //current_user_db.setValue(newUser);
                                                                }
                                                                user.put("id_string", user_id);
                                                                db.collection("users")
                                                                        .add(user).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                    }
                                                                });
                                                            } else {
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(Sign_up.this, "Registered failed. Please try again" + task.getException(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }

                                                    });
                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(Sign_up.this, "Email already used, choose another one", Toast.LENGTH_LONG).show();
                                                    email.setText("");
                                                }
                                            }
                                        });


                                    }else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(Sign_up.this, "Username already used, choose another one", Toast.LENGTH_LONG).show();
                                        musername.setText("");
                                        int cont = 0;
                                        final ArrayList<String> list = new ArrayList<>();
                                        while(list.size()<3 && cont<12){
                                            final int id = cont;
                                            db.collection("users").whereEqualTo("username", username2+id)
                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        int bt = id;
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.getResult().isEmpty()){
                                                        list.add(username2+bt);
                                                        if(list.size()==3){
                                                            Toast.makeText(Sign_up.this, "You can use for example "+list.get(0)+" or "+
                                                                    list.get(1)+" or "+list.get(2), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                }
                                            });
                                            cont++;

                                        }
                                    }
                                }
                            });

                }
            }});
    }



    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                uriSavedImage = data.getData();
            }
            String picturePath = "";
            switch (requestCode) {
                case 1889: //from gallery
                    //data.getData return the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    image.setImageURI(selectedImage);
                    image.setTag("1");
                    break;
                case MY_CAMERA_REQUEST_CODE: //from camera
                    Picasso.get().load(imageFile).into(image);
                    uriSavedImage = Uri.fromFile(imageFile);
                    image.setTag("1");
                    break;
                default:
                    break;
            }
        }else if(resultCode == 0) {
            Toast.makeText(Sign_up.this, "No photo chosen", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExstention (Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (uriSavedImage != null) {
            String id = System.currentTimeMillis() + "." + getFileExstention(uriSavedImage);
            final StorageReference fileReference = mStorageRef.child(id);
            Uri_saved = id;
            fileReference.putFile(uriSavedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Sign_up.this, "Photo profile successfully uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(Sign_up.this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }


    public void Photo_from_camera_or_gallery(View view) {
        AlertDialog.Builder adb = new AlertDialog.Builder(image.getContext());

        //Set a title for alert dialog
        adb.setTitle("Select Options");

        //Initialize a new String Array
        final String[] Choises = new String[]{
                // Those color names are supported by
                //Android parseColor() method
                "Camera", "Gallery", "Cancel",
        };
        //It will render a single choice traditional list on alert dialog

        adb.setItems(Choises, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //This 'which' argument carry the list selected item's index position
                String clickedItemValue = Arrays.asList(Choises).get(which);
                //Set the TextView text color corresponded to the user selected color
                switch (clickedItemValue) {
                    case "Camera":
                        Intent pictureIntent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);


                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    MY_CAMERA_REQUEST_CODE);
                        }

                        if(pictureIntent.resolveActivity(getPackageManager()) != null){
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                Toast.makeText(Sign_up.this, "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(getApplication().getApplicationContext(),"com.example.android.provider", photoFile);
                                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(pictureIntent,MY_CAMERA_REQUEST_CODE);
                            }
                        }
                        break;
                    case "Gallery":
                        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent2.setType("image/*");
                        String[] mimeTypes = {"image/jpeg", "image/png"};
                        intent2.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                        startActivityForResult(intent2, 1889);
                        break;
                    case "Cancel":
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
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFile = image;
        return image;
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }




}
