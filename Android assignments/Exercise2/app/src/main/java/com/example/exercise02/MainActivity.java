package com.example.exercise02;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button rotazione = findViewById(R.id.rotate);
        rotazione.setEnabled(false);

        ImageView immagine = (ImageView) findViewById(R.id.imageView);
        immagine.setTag("0");

    }



    public void onTextViewClicked(View v){
        //Get widgets reference from XML layout
        ImageView immagine = (ImageView) findViewById(R.id.imageView);

        //Initialize a new AlertDialog Builder
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        //Set a title for alert dialog
        adb.setTitle("Select Options");

        //Initialize a new String Array
        final String[] Choises = new String[]{
                // Those color names are supported by
                //Android parseColor() method
                "Camera","Gallery","Cancel",
        };

        //Set a list of items to be displayed in the dialog as the content
        //It will render a single choice traditional list on alert dialog
        adb.setItems(Choises, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //This 'which' argument carry the list selected item's index position
                String clickedItemValue = Arrays.asList(Choises).get(which);
                //Set the TextView text color corresponded to the user selected color
                switch (clickedItemValue){
                    case "Camera":
                        CameraPickUp();
                        break;
                    case "Gallery":
                        GalleryPickUp();
                        break;
                    case "Cancel":
                        break;
                }
            }
        });

        //Display the Alert Dialog on app interface
        adb.show();
    }

    public void CameraPickUp(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }



    private static final int GALLERY_REQUEST_CODE = 1889;

    public void GalleryPickUp() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        ImageView immagine = (ImageView) findViewById(R.id.imageView);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData return the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    immagine.setImageURI(selectedImage);
                    immagine.setTag("1");
                    break;
                case 0:
                    Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                    immagine.setImageBitmap(bitmap);
                    immagine.setTag("1");
                    break;
            }

        if (immagine.getTag() == "1") {
            Button rotazione = findViewById(R.id.rotate);
            rotazione.setEnabled(true);
        }

    }

    public void rotateBtnclick(View viev){
        ImageView immagine = (ImageView) findViewById(R.id.imageView);

        immagine.setRotation(immagine.getRotation() + 90);
    }

}