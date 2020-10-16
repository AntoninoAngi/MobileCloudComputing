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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private ImageButton profileImage;
    private TextView emailShown;
    private EditText passShown;
    private StorageReference mStorageRef;
    private Uri mImageuri;
    private TextView passtext;
    private Button modify;
    private Button cancel;
    private Button save;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    private DatabaseReference mDatabaseRef;
    private File imageFile;
    private Uri uriSavedImage;
    private String Uri_saved;
    private int photo_changed;
    private FirebaseFirestore db;
    private DocumentSnapshot ds;

    public void resetPassword() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String oldPass = "totototo";
        final String newPass = "******";

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPass);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Error: password not updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Authentification failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar myToolbar = (Toolbar) view.findViewById(R.id.user_modify_toolbar);

        photo_changed = 0;
        profileImage = (ImageButton) view.findViewById(R.id.accountPhoto);
        emailShown = (TextView) view.findViewById(R.id.emailShown);
        passShown = (EditText) view.findViewById(R.id.PassShown);
        modify = (Button) view.findViewById(R.id.ModifyButton);
        cancel = (Button) view.findViewById(R.id.CancelButton);
        save = (Button) view.findViewById(R.id.SaveButton);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        passtext = (TextView) view.findViewById(R.id.TextViewPass);
        firebaseAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

                 Task d = db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ds = task.getResult().getDocuments().get(0);
                            String u = ds.getString("image");
                            final String photo_url;
                            if(u.compareTo("")==0)
                                photo_url = "default.png";
                            else
                                photo_url = u;
                            mStorageRef.child(photo_url).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri.toString()).fit().into(profileImage);
                                    modify.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            profileImage.setClickable(true);

                                            modify.setVisibility(View.INVISIBLE);
                                            modify.setEnabled(false);

                                            cancel.setVisibility(View.VISIBLE);
                                            cancel.setEnabled(true);

                                            save.setVisibility(View.VISIBLE);
                                            save.setEnabled(true);

                                            passtext.setVisibility(View.VISIBLE);
                                            passShown.setVisibility(View.VISIBLE);
                                            passShown.setEnabled(true);

                                            profileImage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    AlertDialog.Builder adb = new AlertDialog.Builder(profileImage.getContext());

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
                                                                    if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                                                                            != PackageManager.PERMISSION_GRANTED) {
                                                                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                                                MY_CAMERA_REQUEST_CODE);
                                                                    }
                                                                    if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                                                        File photoFile = null;
                                                                        try {
                                                                            photoFile = createImageFile();
                                                                        } catch (IOException ex) {
                                                                            Toast.makeText(getContext(), "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        if (photoFile != null) {
                                                                            Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.android.provider", photoFile);
                                                                            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                                                            startActivityForResult(pictureIntent, MY_CAMERA_REQUEST_CODE);
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
                                            });

                                            save.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (!passShown.getText().toString().equals("") && passShown.getText().toString().length() >= 6) {
                                                        user.updatePassword(passShown.getText().toString());
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("password", passShown.getText().toString());
                                                        String document_name = ds.getId();
                                                        db.collection("users").document(document_name).set(data, SetOptions.merge());
                                                        Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show();
                                                    } else if (passShown.getText().toString().length() < 6) {
                                                        Toast.makeText(getContext(), "Password not long enough, it has to be at least 6 characters!", Toast.LENGTH_SHORT).show();
                                                    }

                                                    if (photo_changed == 1){
                                                        uploadFile();
                                                    }

                                                    modify.setVisibility(View.VISIBLE);
                                                    modify.setEnabled(true);

                                                    cancel.setVisibility(View.INVISIBLE);
                                                    cancel.setEnabled(false);

                                                    save.setVisibility(View.INVISIBLE);
                                                    save.setEnabled(false);

                                                    passtext.setVisibility(View.INVISIBLE);
                                                    passShown.setVisibility(View.INVISIBLE);
                                                    passShown.setEnabled(false);

                                                    profileImage.setClickable(false);

                                                }
                                            });

                                            cancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    modify.setVisibility(View.VISIBLE);
                                                    modify.setEnabled(true);

                                                    cancel.setVisibility(View.INVISIBLE);
                                                    cancel.setEnabled(false);

                                                    save.setVisibility(View.INVISIBLE);
                                                    save.setEnabled(false);

                                                    passtext.setVisibility(View.INVISIBLE);
                                                    passShown.setVisibility(View.INVISIBLE);
                                                    passShown.setEnabled(false);

                                                    profileImage.setClickable(false);
                                                    Toast.makeText(getContext(), "The user profile has not been modified", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            });

                        } else {
                            Toast.makeText(getContext(), "Error while getting the data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        emailShown.setText(user.getEmail());

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
                    profileImage.setImageURI(selectedImage);
                    photo_changed = 1;
                    break;
                case MY_CAMERA_REQUEST_CODE: //from camera
                    Picasso.get().load(imageFile).into(profileImage);
                    uriSavedImage = Uri.fromFile(imageFile);
                    profileImage.setTag("1");
                    photo_changed = 1;
                    break;
                default:
                    break;
            }
        }else if(resultCode == 0) {
            Toast.makeText(getContext(), "No photo chosen", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtention(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (uriSavedImage != null) {
            String id = System.currentTimeMillis() + "." + getFileExtention(uriSavedImage);
            final StorageReference fileReference = mStorageRef.child(id);
            Uri_saved = id;
            fileReference.putFile(uriSavedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("image", Uri_saved);
                            String document_name = ds.getId();
                            db.collection("users").document(document_name).set(data, SetOptions.merge());
                            Toast.makeText(getContext(), "Photo profile successfully uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getContext(), "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}
