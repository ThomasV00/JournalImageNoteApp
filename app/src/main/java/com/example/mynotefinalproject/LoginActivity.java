package com.example.mynotefinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mynotefinalproject.util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createAccount;

    private AutoCompleteTextView emailAddres;
    private EditText password;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth= FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.login_progress);

        emailAddres=findViewById(R.id.email);
        password=findViewById(R.id.password);
        loginButton=findViewById(R.id.email_sign_in_button_login);
        createAccount=findViewById(R.id.create_acct_button);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginRmailPasswordUsers(emailAddres.getText().toString().trim()
                ,password.getText().toString().trim());

            }


        });
    }

    private void loginRmailPasswordUsers(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
           firebaseAuth.signInWithEmailAndPassword(email,password)
                   .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           FirebaseUser user=firebaseAuth.getCurrentUser();
                           assert user != null;
                           String currentUserId=user.getUid();

                           collectionReference
                                   .whereEqualTo("userId",currentUserId)
                                   .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                       @Override
                                       public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                          if(error!=null){
                                              return;
                                          }
                                           assert value != null;
                                           if(!value.isEmpty()){
                                               progressBar.setVisibility(View.INVISIBLE);
                                               for(QueryDocumentSnapshot snapshot: value){
                                                   JournalApi journalApi= JournalApi.getInstance();
                                                   journalApi.setUsername(snapshot.getString("username"));
                                                   journalApi.setUserId(currentUserId);

                                                   //go to ListActivity
                                                   startActivity(new Intent(LoginActivity.this
                                                   ,PostJournalActivity.class));


                                               }
                                           }
                                       }
                                   });

                       }
                   }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {

                   progressBar.setVisibility(View.INVISIBLE);
                   Toast.makeText(LoginActivity.this
                           ,"please enter email and password",
                           Toast.LENGTH_LONG).show();

               }
           });


        }else {
            Toast.makeText(LoginActivity.this,"Please enter email and password"
            ,Toast.LENGTH_LONG).show();
        }
    }
}