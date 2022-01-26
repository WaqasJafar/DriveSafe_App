package com.example.fyp.iDriveSafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

public class Signup_Form extends AppCompatActivity {
    EditText last_name, first_name,E_mail, e_password, c_password;
    Button register;

    AwesomeValidation awesomeValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup__form);
        first_name = findViewById(R.id.first_name);
        E_mail = findViewById(R.id.E_mail);
        e_password = findViewById(R.id.e_password);
        c_password = findViewById(R.id.c_password);

        register = findViewById(R.id.register);

        // Initiaalize Validation Style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        // Create Validation for User Name

        awesomeValidation .addValidation(this,R.id.first_name, RegexTemplate.NOT_EMPTY,R.string.accept);


        // Create Validation for E-mail
        awesomeValidation.addValidation(this, R.id.E_mail, Patterns.EMAIL_ADDRESS,R.string.accept);

        // Create Validation for Password
        awesomeValidation.addValidation(this,R.id.e_password, ".{6,}", R.string.accept);

        // Create Validation for Conform Passwprd

        awesomeValidation.addValidation(this,R.id.c_password, R.id.e_password,R.string.accept);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check Validation

                if (awesomeValidation.validate()){

                    Toast.makeText(getApplicationContext(),"Form Validate Successfully...", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Signup_Form.this, com.example.fyp.iDriveSafe.Welcome.class);
                    startActivity(intent);

                }
                else {

                    Toast.makeText(getApplicationContext(), "Validation Faild.",Toast.LENGTH_SHORT).show();
                }
            }
        });







    }
}
