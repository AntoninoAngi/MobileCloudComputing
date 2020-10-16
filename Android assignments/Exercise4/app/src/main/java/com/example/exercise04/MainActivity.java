package com.example.exercise04;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button detect;
    ImageView immagine;
    TextView testo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detect = (Button) findViewById(R.id.detect);
        detect.setEnabled(false);

        immagine = (ImageView) findViewById(R.id.imageView);
        immagine.setTag("0");
        testo = (TextView) findViewById(R.id.showText);
    }

    private static final int GALLERY_REQUEST_CODE = 1889;

    public void GalleryPickUp(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    immagine.setImageURI(selectedImage);
                    immagine.setTag("1"); //if the image is selected the tag is set to 1 so that it doesn't detect null photos
                    break;
                default: break;
            }

        if (immagine.getTag() == "1"){
            detect.setEnabled(true);
        }
    }

    public void runTextRecognition(View v){
        BitmapDrawable drawable = (BitmapDrawable) immagine.getDrawable();
        Bitmap selectedImage = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(selectedImage);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        detect.setEnabled(false);

        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                detect.setEnabled(true);
                processTextRecognitionResult(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                detect.setEnabled(true);
                e.printStackTrace();
            }
        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts){
        testo.setText(texts.getText());
        /*
        List <FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++){
            List <FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++){
                List <FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                   testo.append("" + elements.get(k).getText());
                }
            }
        }
         */
    }
}