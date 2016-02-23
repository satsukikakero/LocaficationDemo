package com.example.satsu.locafication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by satsu on 2/10/2016.
 */
public class travel extends Activity {

    public TextView whatToDo;
    public EditText addText;
    public int index=0;
    public Button saveButt;
    public String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Program";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel);

        whatToDo= (TextView) findViewById(R.id.whatToDo);
        addText= (EditText) findViewById(R.id.addText);
        saveButt= (Button) findViewById(R.id.saveHour);
    }

    public void buttonSave(View view){
        String saveStr = addText.getText().toString();
        int count=0,pos=0;
        for(int i=0;i<addText.length();i++){
            if(saveStr.charAt(i)==':') {
                pos = i;
                count++;
            }
        }
        if(addText.length()==5 && count==1 && pos==2) {
            if (index == 0) {
                buttSave(saveStr, "Train");
                whatToDo.setText("What time does your train leave?");
                addText.setText("");
                index++;
            } else {
                buttSave(saveStr, "Hour");
                finish();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong input!\nExample input :\n00:00", Toast.LENGTH_LONG).show();
        }
    }

    public void buttSave(String saveStr,String fileName){
            File file = new File(path + fileName + ".txt");

            String[] temp = new String[1];
            temp[0] = saveStr;
            Save(file, temp);

            Toast.makeText(getApplicationContext(), "Save", Toast.LENGTH_LONG).show();
    }

    public static void Save(File file, String[] data)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());
                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }
}
