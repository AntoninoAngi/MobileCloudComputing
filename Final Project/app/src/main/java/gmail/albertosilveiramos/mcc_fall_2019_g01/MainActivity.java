package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passField;
    private Button loginButton;
    private Button signUpButton;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore db;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        if (mAuth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            mAuth.signOut();
        }

        emailField = (EditText) findViewById(R.id.email);
        passField = (EditText) findViewById(R.id.password);

        signUpButton = (Button) findViewById(R.id.signUp);

        loginButton = (Button) findViewById(R.id.login);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startSignIn();



            }
        });


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){ //if the user is logged in and it goes directly to the next page
                    Intent intent = new Intent(MainActivity.this, ProjectPage.class);
                    startActivity(intent);

                }
            }
        };

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Login");

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Sign_up.class));
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(authListener);
    }

    private void startSignIn(){

        final String email = emailField.getText().toString();
        String password = passField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity.this, "Insert an email address", Toast.LENGTH_LONG).show();

        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(MainActivity.this, "Insert a password", Toast.LENGTH_LONG).show();

        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) { //the user is NOT successfully logged in
                        Toast.makeText(MainActivity.this, "Sign In Problem, try again" + task.getException(), Toast.LENGTH_LONG).show();
                    } else {
                        db.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                String username = task.getResult().getDocuments().get(0).get("username").toString();
                                Toast.makeText(MainActivity.this,"Log in successfully "+username, Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            });
        }
    }




}
