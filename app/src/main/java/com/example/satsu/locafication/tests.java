package com.example.satsu.locafication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class tests extends Activity {
    Calendar cal= Calendar.getInstance();
    TextView date;
    EditText exam;
    Button examButt;
    public String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Program";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests);

        exam= (EditText) findViewById(R.id.exam);
        examButt= (Button) findViewById(R.id.examButt);
        date= (TextView) findViewById(R.id.selectedDate);
        Button datePicker= (Button) findViewById(R.id.datePick);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(tests.this, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }
    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if((monthOfYear+1)<10) {
                date.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
            }
            else{
                date.setText(dayOfMonth+"."+(monthOfYear+1)+"."+year);
            }
        }
    };

    public void ButtonSave(View view){
        if(!(exam.length()<1) && !date.getText().toString().equals("Pick a date for your exam")) {
            File file;
            file = new File(path + date.getText() + ".txt");
            String[] saveText = String.valueOf(exam.getText()).split(System.getProperty("line.separator"));
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            Save(file, saveText);
            finish();
        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong Input!\nFill everything!", Toast.LENGTH_LONG).show();
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
}
