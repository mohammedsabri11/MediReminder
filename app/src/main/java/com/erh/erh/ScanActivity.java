package com.erh.erh;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ScanActivity extends AppCompatActivity {
    private Button speak;

    private SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    final int requestPermissionID = 1001;
    TextToSpeech tts;
    List<String> dateFormat  ;
    DateTimeFormatter f;
    LocalDate date;
    protected void setDateFormat() {
        Locale.setDefault(Locale.ENGLISH);
        dateFormat = new ArrayList<>() ;
        dateFormat.add("dd/MM/uuuu");
        dateFormat.add("M/d/uu");
        dateFormat.add("d/M/uuuu");

        dateFormat.add("d-M-uu");
        dateFormat.add("dd-MM-uu");
        dateFormat.add("dd-MM-uuuu");
        dateFormat.add("d-M-uuuu");


        dateFormat.add("MM-uuuu");
        dateFormat.add("MM/uuuu");
        dateFormat.add("dd/MMM/yyyy");
        dateFormat.add("dd-MMM-yyyy");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_scan);

        setDateFormat();
        cameraView=(SurfaceView)findViewById(R.id.surfaceView);
        textView=(TextView)findViewById(R.id.text_view);
        speak=(Button)findViewById(R.id.speak);

        startCameraSource();


        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textView.getText().toString();

                if (date != null) {
                    alarm( date);

                    init(text);
                }

            }
        });


    }


    public void init(final String text) {
        // TODO Auto-generated method stub
        tts=new TextToSpeech(ScanActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);

                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        ConvertTextToSpeech(text);
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
    public void extractDate(String input ) {

        Locale.setDefault(Locale.ENGLISH);
        String[] elements = input.split( " " ) ;
        for( int i=0;i< dateFormat.size();i++ )
        {
            if (dateFormat.get(i).equals("MM/uuuu")||dateFormat.get(i).equals("MM-uuuu")) {
                f = new DateTimeFormatterBuilder().parseCaseInsensitive() .appendPattern(dateFormat.get(i)).parseDefaulting(ChronoField.DAY_OF_MONTH,1).toFormatter();//
            }else
                f = new DateTimeFormatterBuilder().parseCaseInsensitive() .appendPattern(dateFormat.get(i)).toFormatter();

            for( String element : elements ) {
                try {
                    LocalDate localDate = LocalDate.parse( element , f ) ;
                    if (localDate != null) {
                        date=localDate;
                        textView.setText(localDate.toString());
                    }
                } catch ( DateTimeParseException e ) {
                    // Ignore the exception. Move on to next element.
                }
            }


        }
    }


    public void alarm(LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar calendarToSchedule = Calendar.getInstance();
        calendarToSchedule.setTimeInMillis(System.currentTimeMillis());
        calendarToSchedule.clear();
        calendarToSchedule.setTime(date);
        Date curDate = new Date();
        Calendar current = Calendar.getInstance();
        current.setTime(curDate);

        int hours = current.get(Calendar.HOUR_OF_DAY);
        int min = current.get(Calendar.MINUTE);
        if(calendarToSchedule.compareTo(current) < 0){
            showMessage( );
            return;
        }

        else if(calendarToSchedule.compareTo(current) == 0){
            showMessage( );
        }

        calendarToSchedule.set(Calendar.HOUR_OF_DAY,hours);
        calendarToSchedule.set(Calendar.MINUTE,min+2);
        calendarToSchedule.set(Calendar.SECOND,00);

        AlarmManager alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        long reminderDateTimeInMilliseconds = 000;
        reminderDateTimeInMilliseconds = calendarToSchedule.getTimeInMillis();

        MyNotificationPublisher receiver = new MyNotificationPublisher();
         IntentFilter filter = new IntentFilter("SET_RECEIVER_ALARM_ACTION");
         registerReceiver(receiver, filter);

         Intent intent = new Intent("SET_RECEIVER_ALARM_ACTION");

        PendingIntent operation = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds, operation);


    }
    public void showMessage( ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Expire Date Detected  ");
        builder.setTitle("Warning").setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                dialog.dismiss();
            }
        });

        AlertDialog dialog  = builder.create();
        dialog.show();

    }

    public  void issDate( String date){
        // 12/21
       String pattern3=  "(\\d{1,2}/\\d{1,2}/\\d{4}|\\d{1,2}/\\d{4}|\\d{1,2}/\\d{2})";
        Matcher m = Pattern.compile(pattern3, Pattern.CASE_INSENSITIVE).matcher(date);
        while (m.find()) {
            //textView.setText(m.group(1));
            extractDate(m.group(1));
        }

    }

    private void ConvertTextToSpeech(String text) {
        // TODO Auto-generated method stub

        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
finish();
        }
        else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(ScanActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                /**
                 * Release resources for cameraSource
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 ){

                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append(" ");
                                }
                                if (stringBuilder != null) {
                                    issDate(stringBuilder.toString() );
                                    extractDate(stringBuilder.toString() );
                                }

                            }
                        });
                    }
                }
            });
        }
    }

}

 /* private void setAlarm(){

        AlarmManager manager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;
        PendingIntent pendingIntent;
        long reminderDateTimeInMilliseconds = 000;

        myIntent = new Intent(this,MyNotificationPublisher.class);
        //int minu=current.getTime().getMinutes();
        //int hours=current.getTime().getHours();
        pendingIntent = PendingIntent.getBroadcast(this,0,myIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Date date = new Date();
        Calendar current = Calendar.getInstance();
        current.setTime(date);
        int hours = current.get(Calendar.HOUR_OF_DAY);
        int min = current.get(Calendar.MINUTE);
//TODO : Reminder the user to take medication on the 13th July 2018 at 15:30
// Note: For the month of July the int value will actuall be 6 instead of 7
        Calendar calendarToSchedule = Calendar.getInstance();
        calendarToSchedule.setTimeInMillis(System.currentTimeMillis());
        calendarToSchedule.clear();

//.Set(Year, Month, Day, Hour, Minutes, Seconds);
        calendarToSchedule.set(2021, 02, 10, hours, min+2, 0);


        reminderDateTimeInMilliseconds = calendarToSchedule.getTimeInMillis();


        MyNotificationPublisher receiver = new MyNotificationPublisher();
        IntentFilter filter = new IntentFilter("ALARM_ACTION");
        registerReceiver(receiver, filter);

        Intent intent = new Intent("ALARM_ACTION");
        intent.putExtra("param", "My scheduled action");
        PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, 0);
        // I choose 3s after the launch of my application
        //alarms.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+3000, operation) ;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds,operation);
        }
        else{

            manager.set(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds, operation);
        }


    }
    public void alarm() {

       *//* MyNotificationPublisher receiver = new MyNotificationPublisher();
        IntentFilter filter = new IntentFilter("ALARM_ACTION");
        registerReceiver(receiver, filter);*//*
        // String dayOfTheWeek = (String) DateFormat.format("EEEE", date); // Thursday


        Calendar calendarToSchedule = Calendar.getInstance();
        calendarToSchedule.setTimeInMillis(System.currentTimeMillis());
        calendarToSchedule.clear();
        //cal.set(Integer.parseInt(year) ,Integer.parseInt(month),Integer.parseInt(day),21,20);
        //3/10/2021
        Date date = new Date();
        Calendar current = Calendar.getInstance();
        current.setTime(date);
        int hours = current.get(Calendar.HOUR_OF_DAY);
        int min = current.get(Calendar.MINUTE)+2;


        calendarToSchedule.set(2021, 02, 10, hours, min, 00);


        if(calendarToSchedule.compareTo(current) <= 0){
            //The set Date/Time already passed
            Toast.makeText(getApplicationContext(),
                    "Invalid Date/Time",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),
                    calendarToSchedule.getTimeInMillis()-current.getTimeInMillis()+"",
                    Toast.LENGTH_LONG).show();
            //setAlarm(cal);
        }
        AlarmManager alarms= (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        long reminderDateTimeInMilliseconds = 000;






        reminderDateTimeInMilliseconds = calendarToSchedule.getTimeInMillis();




        Intent intent = new Intent("ALARM_ACTION");

        PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, 0);
        // I choose 3s after the launch of my application
      //  alarms.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+3000, operation) ;

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds,operation);
        }
        else{

            alarms.set(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds, operation);
        }

    }



    */

/*
    public void alarm2(LocalDate localDate) {


        Intent i = new Intent("SET_RECEIVER_ALARM_ACTION");

        i.putExtra("date",localDate.toString());
        sendBroadcast(i);
    }
    public void alarm(LocalDate localDate) {

        MyNotificationPublisher receiver = new MyNotificationPublisher();
        IntentFilter filter = new IntentFilter("ALARM_ACTION");
        registerReceiver(receiver, filter);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String day          = (String) DateFormat.format("dd",    date); // 20
        String month  = (String) DateFormat.format("MM",  date); // Jun
        String year         = (String) DateFormat.format("yyyy", date); // 09-03-2021


        Date curDate = new Date();
        //Calendar current = Calendar.getInstance();
        current.setTime(curDate);
        int hours = current.get(Calendar.HOUR_OF_DAY);
        int min = current.get(Calendar.MINUTE);

        Calendar calendarToSchedule = Calendar.getInstance();

        calendarToSchedule.setTimeInMillis(System.currentTimeMillis());
        calendarToSchedule.clear();
        calendarToSchedule.setTime(date);

        if(calendarToSchedule.compareTo(current) < 0){
            showMessage( );
            return;
        }
        else if(calendarToSchedule.compareTo(current) == 0){
            showMessage( );
        }
        calendarToSchedule.set(Calendar.HOUR_OF_DAY,hours);
        calendarToSchedule.set(Calendar.MINUTE,min+2);
        calendarToSchedule.set(Calendar.SECOND,00);
        AlarmManager alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        long reminderDateTimeInMilliseconds = 000;
        reminderDateTimeInMilliseconds = calendarToSchedule.getTimeInMillis();

        //MyNotificationPublisher receiver = new MyNotificationPublisher();
        // IntentFilter filter = new IntentFilter("ALARM_ACTION");
        // registerReceiver(receiver, filter);
        //Intent intent = new Intent("ALARM_ACTION23");
        Intent intent = new Intent(ScanActivity.this, MyNotificationPublisher.class);
        PendingIntent operation = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds, operation);


    }*/
