package com.example.exercise1;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    int i = 0;

    EditText inputEdit;
    Button buttonChange;
    TextView change;
    Button buttonCount;
    TextView count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEdit = (EditText) findViewById(R.id.editText);
        buttonChange = (Button) findViewById(R.id.upperLowerButton);
        change = (TextView) findViewById(R.id.textUpperLower);
        buttonCount = (Button) findViewById(R.id.charCount);
        count = (TextView) findViewById(R.id.countChar);

    }

    public void ChangeClick(View v){

        change.setText("");
        change.append("Uppercase/Lowercase : ");

        if (i % 2 == 0) {
            change.append(inputEdit.getText().toString().toUpperCase());
        } else
            change.append(inputEdit.getText().toString().toLowerCase());

        i++;
    }


    public void CountClick(View v){
        count.setText("");
        count.append("Count : ");

        int counter = 0;
        char temp;

        String string = inputEdit.getText().toString();

        for( int j= 0; j < string.length(); j++ ) {

            temp = string.charAt(j);

            if (Character.isLetter(temp) || Character.isDigit(temp)){
                counter++;
            }
        }

        count.append(String.valueOf(counter));
    }
}
