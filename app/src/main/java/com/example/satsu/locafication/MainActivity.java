package com.example.satsu.locafication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //Declaring variables needed
    private Button uniCord,offCord;
    private TextView textView,trainTime;
    private LocationManager locationManager;
    private LocationListener locationListener;
    public String [] uniCords,offCords;
    public String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Program";
    public Chronometer chrono;
    public boolean toStartChro = true,isFirstTime=true,saveTime;
    public long timeWhenStopped = 0;
    public String weekDayName = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis());
    public int currDay;
    public boolean reset = false;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //onCreate initializing needed variables
        textView = (TextView) findViewById(R.id.textView);
        uniCord= (Button) findViewById(R.id.uniCords);
        offCord= (Button) findViewById(R.id.offCords);
        chrono= (Chronometer) findViewById(R.id.chronomet);
        Calendar calendar=Calendar.getInstance();
        currDay=calendar.get(Calendar.DAY_OF_MONTH);
        trainTime= (TextView) findViewById(R.id.trainTime);
        String [] setT=buttonLoad("Hour");
        trainTime.setText(setT[0]);

        //Every time location changes do:
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String weekDayNameNow = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis());
                String [] isFileOpen;
                Calendar cale=Calendar.getInstance();
                cale.add(Calendar.DAY_OF_MONTH, 1);
                String date=cale.get(Calendar.DAY_OF_MONTH)+"."+(cale.get(Calendar.MONTH)+1)+"."+cale.get(Calendar.YEAR);
                textView.setText(location.getLatitude() + "\n" + location.getLongitude());
                uniCords=buttonLoad("University");
                offCords=buttonLoad("Office");
                trainLeaves();
                isFileOpen=buttonLoad(date);
                if(!isFileOpen[0].equals("Error")){ //if there is something in file or file is created
                    testNotification(isFileOpen);
                }
                if(!weekDayName.equals(weekDayNameNow)){ //when next day comes
                    saveWeek(weekDayName,currDay);
                    currDay=(cale.get(Calendar.DAY_OF_MONTH))-1;
                    weekDayName=weekDayNameNow;
                    toStartChro=true;
                    isFirstTime=true;
                }
                if(!(offCords[0].equals("Error")) && testIfInRange(Double.parseDouble(offCords[1]),Double.parseDouble(offCords[0]),location.getLongitude(),location.getLatitude())) { //test if in range of work
                    if(toStartChro && isFirstTime) { //if chronometer is started for the first time
                        startChro();
                        toStartChro = false;
                        isFirstTime = false;
                        saveTime=true;
                    }
                    else if(toStartChro && !isFirstTime){ //if chronometer was stopped resume it
                        resumeChro();
                        toStartChro=false;
                        saveTime=true;
                    }
                }
                else{
                    if(saveTime) { //stop chronometer if not in range of work
                        stopChro();
                        toStartChro = true;
                        saveTime=false;
                    }
                }
                if(!(uniCords[0].equals("Error")) && testIfInRange(Double.parseDouble(uniCords[1]), Double.parseDouble(uniCords[0]), location.getLongitude(), location.getLatitude())) { //test if in range of university
                    createNotification();
                }
                if(weekDayNameNow.equals("Friday")){ //if day is Friday and you have made at least 15 hours at work,notification to buy a ticket
                    String [] tmp=buttonLoad("FirstDay");
                    if(!tmp[0].equals("Error")) {
                        int t = Integer.parseInt(tmp[0]);
                        t++;
                        weekHours("Week" + Integer.toString(t));
                    }
                }
            }

            private void trainLeaves() { // function to notify when does train leave
                String [] isOpen=buttonLoad("Hour");
                int temporal=0;
                if(!isOpen[0].equals("Error")){
                    for(int i=0;i<isOpen[0].length();i++){
                        if(isOpen[0].charAt(i)==':'){
                            temporal=i;
                        }
                    }
                }
                Calendar rightNow = Calendar.getInstance();
                int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                int min = rightNow.get(Calendar.MINUTE);
                if(min+40>=60){
                    min=min+40;
                    min=min-60;
                    if(hour+1==24) {
                        hour=0;
                    }
                    else{
                        hour = hour + 1;
                    }

                }
                else {
                    min=min+40;
                }
                if(!(isOpen[0].equals("Error")) && hour==Integer.parseInt(isOpen[0].substring(0,temporal)) && min==Integer.parseInt(isOpen[0].substring(temporal+1,isOpen[0].length()))) { // if 40 min are left till train leaves notify user
                    String trainTime = isOpen[0];
                    Notification(trainTime,"Your train will leave!","Train leaves",4);
                    File file =new File(path +"Hour.txt");
                    isOpen[0]="Error";
                    Save(file,isOpen);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
                Intent intent =new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            },10);
            return;
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 0, locationListener); // from where to get your coordinates
        }

    }
    private void testNotification(String [] examName) { //notification is user has a test!Notifies only if there is a day difference for the test
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        String day=cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        String finalString="";
        for (int i = 0; i < examName.length; i++) {
            finalString += examName[i] + System.getProperty("line.separator");
        }
        Notification(finalString,"Tomorrow is "+day,"Tomorrow is " + day + " you have exam!",1);

    }

    private void createNotification() { //Notification for what program you have today at university
        Calendar cal=Calendar.getInstance();
        String day=cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        File file=new File(path+day+".txt");
        if(file.exists()) {
        String [] program=Load(file);
            String finalString = "";
            for (int i = 0; i < program.length; i++) {
                finalString += program[i] + System.getProperty("line.separator");
            }
            Notification(finalString, "Today is " + day, "Today is " + day, 0);
        }
    }

    private void Notification(String finalString,String setT,String title,int num) { //as many notifications were made in order to minimize code this function was created
        Intent intent =new  Intent(MainActivity.this,university.class);
        PendingIntent pIntent=PendingIntent.getActivity(this, 0, intent, 0);
        Notification noti= new Notification.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                .setTicker(setT)
                .setContentTitle(title)
                .setContentText(finalString)
                .setSmallIcon(R.mipmap.logo)
                .setContentIntent(pIntent).getNotification();
        noti.flags=Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(num, noti);
    }

    public void displayUni(View uni) { // Open activity university
        if(uni.getId()==R.id.disUniversity){
            Intent university = new Intent(MainActivity.this,university.class);
            startActivity(university);
        }
    }

    public void displayOffice(View office) { // Open activity office
        if(office.getId()==R.id.disOffice) {
            Intent dispOffice = new Intent(MainActivity.this,office.class);
            startActivity(dispOffice);
        }
    }
    public void displayTravel(View travel) { // Open activity travel
        if(travel.getId()==R.id.disTravel) {
            Intent dispTravel = new Intent(MainActivity.this,travel.class);
            startActivity(dispTravel);
        }
    }

    public void saveWeek(String DayName,int theDay) { // saves days for a certain week(a day contains : day name,hours spend at work,date)
        int weekIndex=0;
        boolean toLoop=true;
        String [] isFileOpen=buttonLoad("FirstDay");

        if(isFileOpen[0].equals("Error")){
            String [] fill=emptyLineToAdd();
            String[] day = new String[1];
            day[0] = "0";
            File file1 = new File(path + "FirstDay.txt");
            File file2 = new File(path + "Week1.txt");
            Save(file1, day);
            if(fill.length>0){
                Save(file2, fill);
            }
            weekIndex=0;
        }
        else {
            File file1 = new File(path + "FirstDay.txt");
            String[] openedWeek;
            String[] weekToOpen = buttonLoad("FirstDay");
            while (toLoop) {
                openedWeek = buttonLoad("Week"+ Integer.toString(Integer.parseInt(weekToOpen[0]) + 1));
                if (openedWeek.length < 7 && !openedWeek[0].equals("Error")) {
                    weekIndex = Integer.parseInt(weekToOpen[0]);
                    break;
                }
                else {
                    if(Integer.parseInt(weekToOpen[0])==4){
                        weekToOpen[0]="0";
                        reset=true;
                        Save(file1, weekToOpen);
                        break;
                    }
                    else {
                        reset = true;
                        weekToOpen[0] = Integer.toString(Integer.parseInt(weekToOpen[0]) + 1);
                        Save(file1, weekToOpen);
                        weekIndex = Integer.parseInt(weekToOpen[0]);
                        break;
                    }
                }
            }
        }
        weekOpen("Week"+Integer.toString(weekIndex+1),DayName,theDay);
    }

    private String [] emptyLineToAdd() { // if the program is installed on day different from monday adds empty lines for the days that have passed before installation this week
        String [] textToReturn;
        int ind=getIndexOfDay();
        if(ind>1){
            textToReturn=new String [ind-1];
            fillEmpty(textToReturn,ind);
            return textToReturn;
        }
        textToReturn = new String[0];
        return textToReturn;
    }

    private String[] fillEmpty(String[] textToReturn, int ind) { // addition for the emptyLineToAdd function
        for(int i=0;i<ind-1;i++){
            textToReturn[i]=" ";
        }
        return textToReturn;
    }

    public void weekOpen(String week,String today,int theDay){ // open a week to save in
        Calendar cal=Calendar.getInstance();
        int currMonth=(cal.get(Calendar.MONTH)+1);
        try{
            File file = new File(path + week+".txt");
            //if file doesnt exists, then create it
            if(!file.exists() || reset==true){
                String saveStr = today + " " + "(" + theDay + "." + currMonth + ")" + " - " + chrono.getText() + " (Hou:Min:Sec)";
                String[] saveText = String.valueOf(saveStr).split(System.getProperty("line.separator"));
                Save(file, saveText);
                reset=false;
                return;
            }
            String saveStr ="\n"+today + " " + "(" + theDay + "." + currMonth + ")" + " - " + chrono.getText() + " (Hou:Min:Sec)";
            //true = append file
            FileWriter fileWritter = new FileWriter(path + week + ".txt",true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(saveStr);
            bufferWritter.close();

        }catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }
    }
    public void buttSave(View view){ // save cords for university or office
        File file;
        if(uniCord.getId()==view.getId()) {
            file = new File(path + "University.txt");
        }
        else{
            file = new File(path + "Office.txt");
        }
        String [] saveText=String.valueOf(textView.getText()).split(System.getProperty("line.separator"));
        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
        Save(file, saveText);
        uniCord.setVisibility(View.GONE);
        offCord.setVisibility(View.GONE);
    }
    public String [] buttonLoad(String name){ // loads a file
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
    public static void Save(File file, String[] data) // save file
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
    public static String[] Load(File file) // load file
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

    public void showButtonCord(View view){
        if(!textView.getText().equals("Coordinates")) {
            uniCord.setVisibility(View.VISIBLE);
            offCord.setVisibility(View.VISIBLE);
            return;
        }
        Toast.makeText(MainActivity.this, "Wait for Locafication to find you!\nAnd try again!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public boolean testIfInRange(double finalLong,double finalLat,double yourLong,double yourLat){ // test function if in range of some location
        double longitude=(finalLong-yourLong);
        double latitude=(finalLat-yourLat);
        if((longitude<=0.001 && longitude>=-0.001) && (latitude>=-0.001 && latitude<=0.001)){
            return true;
        }
        return false;
    }

    public void startChro(){
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
    }

    public void stopChro(){
        chrono.stop();
        timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
    }

    public void resumeChro(){
        chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chrono.start();
    }

    public int getIndexOfDay() { // helping function to know index of day
        if(weekDayName.equals("Monday")){
            return 1;
        }
        else if(weekDayName.equals("Tuesday")){
            return 2;
        }
        else if(weekDayName.equals("Wednesday")){
            return 3;
        }
        else if(weekDayName.equals("Thursday")){
            return 4;
        }
        else if(weekDayName.equals("Friday")){
            return 5;
        }
        else if(weekDayName.equals("Saturday")){
            return 6;
        }
        else
            return 7;
    }

    public void hoursMade(String [] strWeek){ // if certain hours are made at work go buy a ticket
        int pos,min=0,hours=0,count;
        int charPosition[][] = new int[strWeek.length][2];
        for(int i=0;i<strWeek.length;i++){
            count=0;pos=0;
            for( int j=0; j<strWeek[i].length(); j++ ) {
                if(strWeek[i].charAt(j) == ':' ) {
                    charPosition[i][pos]=j;
                    pos++;
                    count++;
                }
            }
            if(count>1){
                hours=hours+Integer.parseInt(strWeek[i].substring(0, charPosition[i][0]));
                min=min+Integer.parseInt(strWeek[i].substring(charPosition[i][0] + 1, charPosition[i][1]));
            }
            else{
                min=min+Integer.parseInt(strWeek[i].substring(0, 2));
            }
        }
        if((60-min)<0){
            min=Math.abs(min);
            hours=hours+(min/60);
        }
        if(hours>=15){
            Notification("Go take a ticke for home!","Ticket","Take ticket",5);
        }
    }

    public void weekHours(String strWeek){ // function for extracting time spend at work
        int first,last;
        String [] toSend=new String[4];
        String help;
        String [] eachDay=buttonLoad(strWeek);
        if(eachDay[0].length()>1) {
            for (int i = 0; i < eachDay.length; i++) {
                first = eachDay[i].indexOf("-");
                last = eachDay[i].indexOf("(", first);
                help = eachDay[i].substring(first + 2, last - 1);
                if (eachDay.length == 4) {
                    toSend[i] = help;
                }
            }
            hoursMade(toSend);
        }
    }
}
