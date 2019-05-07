package com.kroposki.sftpapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bipin.fileselector.FileUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.net.URI;
import java.util.Properties;
import java.util.Vector;

/**
 * @author
 * Class utilizes JSCH library to create an SFTP connection between the app and a remote SFTP server.
 * Users view their directory, and upload a file to their directory if they wish.
 */
public class UserActivity extends AppCompatActivity {

    //The user's username
    private String username;

    private static final int REQUEST_SELECT_FILE = 101;

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Retrieve user information from LoginActivity
        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");

        //Establish RecyclerView
        recyclerView = findViewById(R.id.userRecyclerView);
        manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);

        //Retrieve directory in an asyncTask so that the data retrieval is done in a thread separate from the main
        final AsyncTask<Integer, Void, Void> asyncTask = new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    //Retrieve directory and display it in a RecyclerView
                    retrieveDirectory();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);


        //Opens a file-selector. The selector allows a user to select any file within their filesystem and upload it to their own directory.
        findViewById(R.id.userUploadFileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        //Brings the user to the LoginActivity
        findViewById(R.id.userLogoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Establishes an SFTP connection and displays the User's directory in a RecyclerView
     * @throws Exception
     */
    private void retrieveDirectory() throws Exception {

        //Create connection to SFTP server
        Session session = createSession();

        //Add no StrictHostKeyChecking
        Properties properties = new Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);

        //Connect to server
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        //New SFTP channel
        ChannelSftp sftpChannel = (ChannelSftp) channel;


        //Retrieve Present Working Directory
        String currentDirectory = sftpChannel.pwd();

        //Make directory name equal to the user's name, since only unique users can be stored in the database.
        String dir="/" + username + "/";
        SftpATTRS attrs = null;
        URI uri = new URI(dir);

        //Try to determine if the directory exists already
        try {
            attrs = sftpChannel.stat(currentDirectory + "/" + dir);
        } catch (Exception e) {
            System.out.println(currentDirectory+"/" + dir + " not found");
        }

        //If a directory doesn't exist for the user, then one is created
        if (attrs != null) {

            Log.d("CREATE DIR:", "Directory already exists");
        } else {
            Log.d("CREATE DIR:", "Creating new directory for: " + username);
            sftpChannel.mkdir(dir);
        }

        //Retrieve listing of user's directory
        Vector<LsEntry> files = sftpChannel.ls(uri.getPath());
        displayView(files);

        //Disconnect from SFTP server after the task is completed
        sftpChannel.exit();
        session.disconnect();
    }

    /**
     * UI updates must be done in a thread outside of a thread establishing and utilizing an SFTP connection
     * This method populates the RecyclerView
     * @param entries - the files within the directory
     */
    private void displayView(final Vector<LsEntry> entries) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //These entries will always list the enclosing folders, so this removes them before they're added to the RecyclerView
                entries.remove(0);
                entries.remove(0);

                //Attach an adapter to the RecyclerView, and add borders between each view
                FileAdapter adapter = new FileAdapter(getApplicationContext(), entries);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), manager.getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);

                //Attach Adapter
                recyclerView.setAdapter(adapter);
            }
        });
    }

    /**
     * Opens Bipin's FileSelector with a startActivityForResult
     */
    private void uploadFile() {
        //Open the FileSelector in a new intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        //No specificity in file type selection
        intent.setType("*/*");

        //Start Activity for Result, which is then handled by onActivityResult
        startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), REQUEST_SELECT_FILE);

    }

    /**
     * onActivityResult method for uploadFile's startActivityForResult
     *
     * @param requestCode
     * @param resultCode - Determines if an error occurred in the result or not
     * @param data - Files the user selected
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_FILE:

                    //Holding the session in an array allows it to be accessed from an inner class
                    final Session[] session = {null};

                    //Run SFTP on separate thread
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {

                                session[0] = createSession();

                                Properties properties = new Properties();
                                properties.put("StrictHostKeyChecking", "no");

                                session[0].setConfig(properties);
                                session[0].connect();

                                //Establish connection
                                Channel channel = session[0].openChannel("sftp");
                                channel.connect();

                                //New SFTP channel
                                ChannelSftp sftpChannel = (ChannelSftp) channel;
                                //Try to determine if the directory exists already

                                String currentDirectory = sftpChannel.pwd();
                                String dir="/" + username + "/";
                                SftpATTRS attrs = null;
                                try {
                                    attrs = sftpChannel.stat(currentDirectory + "/" + dir);
                                } catch (Exception e) {
                                    System.out.println(currentDirectory+"/" + dir + " not found");
                                }
                                //If a directory doesn't exist for the user, then one is created
                                if (attrs != null) {

                                    Log.d("CREATE DIR:", "Directory already exists");
                                } else {
                                    Log.d("CREATE DIR:", "Creating new directory for: " + username);
                                    sftpChannel.mkdir(dir);
                                }
                                Uri uri = data.getData();

                                //Retrieve the user's directory
                                String path = FileUtils.getPath(getApplicationContext(), uri);

                                //Put the file in the user's directory
                                sftpChannel.put(path, "/" + username + "/");

                                //Since a new file will be in the directory the directory view is re-retrieved
                                retrieveDirectory();

                            } catch (JSchException e) {
                                e.printStackTrace();
                            } catch (SftpException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
            }
        }
    }
    /** PLEASE REFER TO CONSTANTS.JAVA INTERFACE FOR WHY THE SFTP CREDENTIALS ARE PRESENTED THIS WAY
     * Since multiple methods require an SFTP session in order to function,
     * this method does so.
     * @return session with SFTP server
     */
    private Session createSession() {
        Session session = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, Constants.SFTP_HOST, Constants.SFTP_PORT);
            session.setPassword(Constants.SFTP_PASS);
        } catch (JSchException e) {
            Log.d("", "JSchException Session Error in createSession");
            e.printStackTrace();
        }
        return session;
    }


//end file
}
