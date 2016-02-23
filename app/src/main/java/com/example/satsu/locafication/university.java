package com.example.satsu.locafication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;


public class university extends Activity {

    public EditText editText;
    public TextView textView;
    public Button save,editProgram,test;
    public CharSequence name;

    public String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Program";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.university);

        editText= (EditText) findViewById(R.id.editText);
        textView= (TextView) findViewById(R.id.dayProgram);
        test= (Button) findViewById(R.id.dispTests);
        editProgram= (Button) findViewById(R.id.editP);
        save= (Button) findViewById(R.id.save);
        final Spinner weekDays= (Spinner) findViewById(R.id.spinner);
        final int indexOfDay=findIndex();
        weekDays.post(new Runnable() {
            @Override
            public void run() {
                weekDays.setSelection(indexOfDay-1);
            }
        });
        weekDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView myText = (TextView) view;
                    name = myText.getText();
                    buttonLoad();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        File dir=new File(path);
        dir.mkdirs();
    }

    private int findIndex() {
        int day =Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day-1;
    }

    public void buttonSave(View view){
        if(!(editText.length()<1)) {
            File file = new File(path + name + ".txt");
            String[] saveText = String.valueOf(editText.getText()).split(System.getProperty("line.separator"));

            editText.setText("");

            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

            Save(file, saveText);
            buttonLoad();
        }
        else{
            Toast.makeText(getApplicationContext(), "Don't leave empty text fields!", Toast.LENGTH_LONG).show();
        }
    }
    public void tryTOWork(View view){
        editText.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        editProgram.setVisibility(View.GONE);
        test.setVisibility(View.GONE);
        textView.setText("Enter new record!");
    }
    public void buttonLoad(){
        File file=new File(path+name+".txt");
        if(!file.exists()) {
            editText.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            test.setVisibility(View.GONE);
            editProgram.setVisibility(View.GONE);

            textView.setText("There is no record!");
        }
        else {
            editProgram.setVisibility(View.VISIBLE);
            test.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            String[] loadText = Load(file);

            String finalString = "";

            for (int i = 0; i < loadText.length; i++) {
                finalString += loadText[i] + System.getProperty("line.separator");
            }
            textView.setText(finalString);
        }
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


    public static String[] Load(File file)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        int anzahl=0;
        try
        {
            while ((br.readLine()) != null)
            {
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try
        {
            while((line=br.readLine())!=null)
            {
                array[i] = line;
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }
    public void displayTests(View tst) {
        if(tst.getId()==R.id.dispTests){
            Intent test = new Intent(university.this,tests.class);
            startActivity(test);
        }
    }
}
