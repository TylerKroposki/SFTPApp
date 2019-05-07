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

/**
 * @author Tyler Kroposki
 * Handles User Login for the application
 * Users enter their credentials into the fields, and then they are authenticated with a remote Web Server
 * by using a POST request. The web server returns whether the user has been authenticated or not,
 * and if not, what the correlating issue is.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText loginInputEmail, loginInputPassword;
    private Button loginLoginBtn;
    private Button loginRegisterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Retrieve Views
        loginInputEmail = findViewById(R.id.loginUsernameField);
        loginInputPassword = findViewById(R.id.loginPasswordField);
        loginLoginBtn = findViewById(R.id.loginLoginBtn);
        loginRegisterBtn = findViewById(R.id.loginRegisterBtn);

        //Handles the Login Button's onClick. If the button is pressed, then the current input in the text fields is what gets utilized in
        //this listener.
        loginLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Verify there's input in the views
                if(!loginInputEmail.getText().toString().isEmpty() && !loginInputPassword.getText().toString().isEmpty()) {

                    //Attempt to login
                    loginUser(loginInputEmail.getText().toString(), loginInputPassword.getText().toString());
                } else {
                    Toast.makeText(LoginActivity.this, "Please make sure both fields are properly completed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //If the user doesn't have an account, the user can click this button and be directed to the RegisterActivity
        loginRegisterBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Handles user login by sending a request to AWS EC2 server for a JSON response on whether the user has been authenticated or not.
     * @param username - the user's email
     * @param password - the user's password
     */
    private void loginUser(final String username, final String password) {
        String cancelRequest = "login";

        //Main Listener: Waits for a JSON response from server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.loginURL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {

                    //Since the response is going to be in JSON format, it's made into a JSONObject here so it can be used
                    JSONObject jObj = new JSONObject(response);

                    //Retrieve if an error occurred
                    boolean isError = jObj.getBoolean("error");

                    //If there's no error, then the user is authenticated and directed to the UserActivity
                    if (!isError) {

                        //Retrieve the username
                        String user = jObj.getJSONObject("user").getString("name");

                        //Create intent to direct user to UserActivity with an intent containing the user's username (which must be unique)
                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                        intent.putExtra("username", user);
                        startActivity(intent);
                        finish();

                    } else {

                        //Tell the user what the error was
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Please contact an administrator.", Toast.LENGTH_LONG).show();
                }
            }
            //Error Listener
        }, new Response.ErrorListener() {

            //Alert user of what the issue is via a Toast
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            //Since a POST request is being made, Volley's getParams() must be overridden in order to
            //make a POST to the web server.
            @Override
            protected Map<String, String> getParams() {

                //Key value pair stored in a HashMap and returned
                Map<String, String> params = new HashMap();
                params.put("username", username);
                params.put("password", password);
                return params;
            }

        };
        //Add to RequestQueue
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, cancelRequest);
    }


//end file
}


