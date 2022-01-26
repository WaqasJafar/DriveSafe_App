package com.example.fyp.iDriveSafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

public class Login_Form extends AppCompatActivity {
    EditText E_mail,password;
    Button btn_login;
    TextView signuptext;
    AwesomeValidation awesomeValidation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login__form);

        getSupportActionBar().setTitle("Login Form");

        E_mail = findViewById(R.id.E_mail);
        password= findViewById(R.id.password);

        btn_login = findViewById(R.id.btn_login);
        signuptext = findViewById(R.id.signuptext);


        // Initiaalize Validation Style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        // Create Validation for E-mail
        awesomeValidation.addValidation(this, R.id.E_mail, Patterns.EMAIL_ADDRESS,R.string.accept);

        // Create Validation for Password
        awesomeValidation.addValidation(this,R.id.e_password, ".{6,}", R.string.accept);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check Validation

                if (awesomeValidation.validate()){

                    Toast.makeText(getApplicationContext(),"Form Validate Successfully...", Toast.LENGTH_SHORT).show();

                    {

                        Intent intent = new Intent(Login_Form.this, com.example.fyp.iDriveSafe.Welcome.class);
                        startActivity(intent);
                    }
                }

                else {
                    Toast.makeText(getApplicationContext(), "Validation Faild.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        signuptext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check Validation
                    Intent intent=new Intent(Login_Form.this, com.example.fyp.iDriveSafe.Signup_Form.class);
                    startActivity(intent);
            }
        });
    }
    public void btn_signupForm(View view) {
        startActivity(new Intent(getApplicationContext(), com.example.fyp.iDriveSafe.Signup_Form.class));


    }





}
