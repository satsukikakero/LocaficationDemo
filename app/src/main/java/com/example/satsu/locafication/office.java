package com.example.satsu.locafication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class office extends Activity {

    public TextView mon,tue,wen,thu,fri,total;
    public String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Program";
    public Spinner weekMenu;
    public boolean reset=false,load=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.office);


        mon= (TextView) findViewById(R.id.Monday);
        tue= (TextView) findViewById(R.id.Tuesday);
        wen= (TextView) findViewById(R.id.Wednesday);
        thu= (TextView) findViewById(R.id.Thursday);
        fri= (TextView) findViewById(R.id.Friday);
        total= (TextView) findViewById(R.id.totalHours);


        weekMenu= (Spinner) findViewById(R.id.weekMenu);
        loadHours();

        weekMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                makeAllNull();
                String[] fullWeek;
                String [] test;
                int needToReset[]=new int[5];
                int leng,toReset=0;
                if(position==0) {
                    test=buttonLoad("Week1Hours");
                    if(test.length==0){
                        load=true;
                    }
                    if(load || test[0].equals("Error")) {
                        weekHours("Week1", 1);
                        load=false;
                    }
                    fullWeek = buttonLoad("Week1");
                    hoursMade("Week1Hours");
                    needToReset[0]=test.length;
                }
                else if(position==1){
                    test=buttonLoad("Week2Hours");
                    if(test.length==0){
                        load=true;
                    }
                    if(load || test[0].equals("Error")) {
                        weekHours("Week2", 2);
                        load=false;
                    }
                    fullWeek = buttonLoad("Week2");
                    hoursMade("Week2Hours");
                    needToReset[1]=test.length;
                }
                else if(position==2){
                    test=buttonLoad("Week3Hours");
                    if(test.length==0){
                        load=true;
                    }
                    if(load || test[0].equals("Error")) {
                        weekHours("Week3", 3);
                        load=false;
                    }
                    fullWeek = buttonLoad("Week3");
                    hoursMade("Week3Hours");
                    needToReset[2]=test.length;
                }
                else if(position==3){
                    test=buttonLoad("Week4Hours");
                    if(test.length==0 || test[0].equals("Error")){
                        load=true;
                    }
                    if(load || test[0].equals("Error")) {
                        weekHours("Week4", 4);
                        load=false;
                    }
                    fullWeek = buttonLoad("Week4");
                    hoursMade("Week4Hours");
                    needToReset[3]=test.length;
                }
                else {
                    test=buttonLoad("Week5Hours");
                    if(test.length==0){
                        load=true;
                    }
                    if(load || test[0].equals("Error")) {
                        weekHours("Week5", 5);
                        load=false;
                    }
                    fullWeek = buttonLoad("Week5");
                    hoursMade("Week5Hours");
                    needToReset[4]=test.length;
                }

                for(int b=0;b<needToReset.length;b++){
                    toReset=toReset + needToReset[b];
                }

                if(toReset==35){
                    load=true;
                }

                leng=fullWeek.length;
                if(leng==6){
                    leng--;
                }
                else if(leng==7){
                    leng=leng-2;
                }
                for(int i=0;i<leng;i++){
                    if(i==0){
                        mon.setText(fullWeek[i]);
                    }
                    else if(i==1){
                        tue.setText(fullWeek[i]);
                    }
                    else if(i==2){
                        wen.setText(fullWeek[i]);
                    }
                    else if(i==3){
                        thu.setText(fullWeek[i]);
                    }
                    else{
                        fri.setText(fullWeek[i]);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadHours() {
        for(int i=1;i<6;i++) {
            reset=true;
            weekHours("Week" + Integer.toString(i), i);
        }
    }

    public void weekHours(String strWeek,int ind){
        int first,last;
        String toSave;
        String [] eachDay=buttonLoad(strWeek);
        if(!eachDay[0].equals("Error") && eachDay[0].length()>1) {
            for (int i = 0; i < eachDay.length; i++) {
                first = eachDay[i].indexOf("-");
                last = eachDay[i].indexOf("(", first);
                toSave = eachDay[i].substring(first + 2, last - 1);
                buttSave(toSave, ind);
            }
        }
    }

    public void hoursMade(String strWeek){
        int pos,min=0,hours=0,count;
        String [] textLoad=buttonLoad(strWeek);
        int charPosition[][] = new int[textLoad.length][2];
        if(!textLoad[0].equals("Error")) {
            for (int i = 0; i < textLoad.length; i++) {
                count = 0;
                pos = 0;
                for (int j = 0; j < textLoad[i].length(); j++) {
                    if (textLoad[i].charAt(j) == ':') {
                        charPosition[i][pos] = j;
                        pos++;
                        count++;
                    }
                }
                if (count > 1) {
                    hours = hours + Integer.parseInt(textLoad[i].substring(0, charPosition[i][0]));
                    min = min + Integer.parseInt(textLoad[i].substring(charPosition[i][0] + 1, charPosition[i][1]));
                } else {
                    min = min + Integer.parseInt(textLoad[i].substring(0, 2));
                }
            }
            if((60-min)<0){
                min=Math.abs(min);
                int tmin=min%60;
                hours=hours+(min/60);
                min=tmin;
            }
            if(hours>=20){
                total.setText("Hours to work off : 0 hours");
            }
            else{
                if(min==0)
                    total.setText("Hours to work off : "+Integer.toString(20-hours)+" hours");
                else {
                    total.setText("Hours to work off : " + Integer.toString(19 - hours) + " hours " + Integer.toString(60 - min)+ " minutes");
                }
            }
        }
    }

    public String [] buttonLoad(String name){
        File file=new File(path+name+".txt");
        String[] loadText;
        if(file.exists()) {
            loadText = Load(file);
            return loadText;
        }
        else{
            loadText=new String[1];
            loadText[0]="Error";
        }
        return loadText;
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

    public void buttSave(String saveStr,int windex){
        try{

            File file = new File(path + "Week" + Integer.toString(windex) + "Hours.txt");
            //if file doesnt exists, then create it

            if(!file.exists() || reset){
                String [] temp=new String [1];
                temp[0]=saveStr;
                Save(file, temp);
                reset=false;
                return;
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(path + "Week" + Integer.toString(windex) + "Hours.txt",true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("\n" + saveStr);
            bufferWritter.close();

        }catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
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

    public void makeAllNull(){
        mon.setText("No record found");
        tue.setText("No record found");
        wen.setText("No record found");
        thu.setText("No record found");
        fri.setText("No record found");
    }
}
