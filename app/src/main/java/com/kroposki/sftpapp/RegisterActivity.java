package com.kroposki.sftpapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Since the Login & Register activities have similar fields, buttons, etc. it helps to name them this way
    private EditText registerUsernameInput, registerEmailInput, registerPasswordInput, registerConfirmPasswordInput;
    private Button registerRegisterBtn;
    private Button registerLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Register Inputs
        registerUsernameInput = findViewById(R.id.registerUsernameField);
        registerEmailInput = findViewById(R.id.registerEmailField);
        registerPasswordInput = findViewById(R.id.registerPasswordField);
        registerConfirmPasswordInput = findViewById(R.id.registerConfirmPasswordField);

        //Register Buttons
        registerRegisterBtn = findViewById(R.id.btn_signup);
        registerLoginBtn = findViewById(R.id.btn_link_login);

        /**
         * Handles input validation, and submits the user's registration information.
         */
        registerRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registerUsernameInput.getText().toString().isEmpty()
                        || registerEmailInput.getText().toString().isEmpty()
                        || registerPasswordInput.getText().toString().isEmpty()
                        || registerPasswordInput.getText().toString().isEmpty()) {
                    makeToast("Please make sure all fields are properly filled.");
                } else if(!passwordsMatch(registerConfirmPasswordInput.getText().toString(), registerPasswordInput.getText().toString())) {
                    makeToast("Passwords do not match.");
                } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmailInput.getText().toString()).matches()) {
                    makeToast("Email is incorrectly formatted.");
                } else if(!(registerPasswordInput.getText().length() >= 8)) {
                    makeToast("Password must be a minimum of 8 characters.");
                } else {
                    submitRegistrationInfo();
                }
            }
        });

        //OnClick handles directing the user to the LoginActivity if they already have an account
        registerLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * Helper method for the register button's OnClick
     */
    private void submitRegistrationInfo() {
        registerUser(registerUsernameInput.getText().toString(), registerEmailInput.getText().toString(), registerPasswordInput.getText().toString());
    }

    private boolean passwordsMatch(String password, String confirmPassword) {
        if(password.equals(confirmPassword)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Handles user registration via sending a POST request to the Web Server at registerURL
     * @param name - user input name
     * @param email - user input email
     * @param password - user input password
     */
    private void registerUser(final String name, final String email, final String password) {
        // Tag used to cancel the request
        String cancelRequest = "register";

        //Create a new StringRequest to submit a POST to the server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.registerURL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    //If no error occurs, then the user is successfully registered and is sent to the UserActivity
                    if (!error) {
                        String user = jObj.getJSONObject("user").getString("name");
                        Toast.makeText(getApplicationContext(), "Hi " + user +", You successfully registered!", Toast.LENGTH_SHORT).show();

                        //Create intent to direct user to UserActivity with an intent containing the user's username (which must be unique)
                        Intent intent = new Intent(RegisterActivity.this, UserActivity.class);
                        intent.putExtra("username", user);
                        startActivity(intent);
                        finish();
                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            //Since a POST request is being made, Volley's getParams() must be overridden in order to
            //make a POST to the web server.
            @Override
            protected Map<String, String> getParams() {

                //Store parameters as Key-Value pair in a HashMap
                Map<String, String> params = new HashMap();

                params.put("username", name.trim());
                params.put("email", email);
                params.put("password", password);

                return params;
            }};

        // Add to RequestQueue
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, cancelRequest);
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

//end file
}