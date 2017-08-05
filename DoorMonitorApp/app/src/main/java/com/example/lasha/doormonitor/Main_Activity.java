package com.example.lasha.doormonitor;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static com.example.lasha.doormonitor.R.id.button2;
import static com.example.lasha.doormonitor.R.id.button;


public class Main_Activity extends AppCompatActivity implements View.OnClickListener {
    public static final String ip = "192.168.8.108";
    public static final String imgIp="192.168.8.100";
    public static final int portNum = 80;
    public static final int portNumImg =1250;
    ImageView img;
    static  String path;
    Bitmap b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mai);
        img=(ImageView) findViewById(R.id.imageView2);
        Button click = (Button) findViewById(button);
        Button getImg=(Button)findViewById(button2);
        click.setOnClickListener(this);
        getImg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==button){
        new Thread() {
            @Override
            public void run(){
                try {
                    Socket socket=new Socket(ip,portNum);
                    BufferedWriter br = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    br.write("open");
                    br.newLine();
                    br.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();}
        if(view.getId()== R.id.button2){
            new Thread() {
                @Override
                public void run(){
                    try {
                        Socket socket=new Socket(imgIp,portNumImg);
                        ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
                        byte [] buffer=(byte [])ois.readObject();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

                        path=saveToInternalStorage(bitmap);
                        loadImageFromStorage(path);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }


    }
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"img.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 0, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "img.jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img.setImageBitmap(b);
                    img.setAdjustViewBounds(true);
                }
            });


        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }
}
