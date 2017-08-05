package com.example.lasha.doormonitor;


import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


import static com.example.lasha.doormonitor.R.id.loginButton;
import static com.example.lasha.doormonitor.R.id.nameText;
import static com.example.lasha.doormonitor.R.id.responseText;
import static com.example.lasha.doormonitor.R.id.textView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ip = "192.168.8.108";
    public static final int portNum = 80;
    private Socket socket = null;
    public static final String debugString = "debug";
    EditText name;
    Button login;
    EditText response;
    TextView  text;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(loginButton);
        name = (EditText) findViewById(nameText);
        response=(EditText) findViewById(responseText);
        text=(TextView) findViewById(textView);



        new Thread() {//getting response from server
            @Override
            public void run() {

                try {

                    socket = new Socket(ip, portNum);

                    BufferedWriter br = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    br.write("Request");
                    br.newLine();
                    br.flush();

                    BufferedReader in = new BufferedReader((new InputStreamReader(socket.getInputStream())));
                    final String challenge = in.readLine();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(challenge);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }}.start();

        login.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {




            new Thread() {
                @Override
                public void run() {
                    try {

                        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        BufferedReader in = new BufferedReader((new InputStreamReader(socket.getInputStream())));
                        String uName = name.getText().toString();
                        String challenge = response.getText().toString();

                        br.write("username:" + uName + "\r\n");
                        br.newLine();
                        br.flush();

/*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText("changed");
                        }
                    });
*/

                        br.write("result:" + challenge);
                        br.newLine();
                        br.flush();
                        final String condition = in.readLine();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                text.setText(condition);
                            }
                        });
                        socket.close();
                        if (condition.equals("connect")) {
                            Intent toy = new Intent(LoginActivity.this, Main_Activity.class);
                            startActivity(toy);
                        }
                    } catch (IOException e) {

                    }

                }

            }.start();

    }

}





