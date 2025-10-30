package com.erh.erh;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

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
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanActivity extends AppCompatActivity {
    private Button speak;

    private PreviewView cameraView;
    TextView textView;
    final int requestPermissionID = 1001;
    TextToSpeech tts;
    List<String> dateFormat;
    DateTimeFormatter f;
    LocalDate date;
    private TextRecognizer textRecognizer;

    protected void setDateFormat() {
        Locale.setDefault(Locale.ENGLISH);
        dateFormat = new ArrayList<>();
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
        setContentView(R.layout.activity_scan);

        setDateFormat();
        cameraView = findViewById(R.id.surfaceView);
        textView = findViewById(R.id.text_view);
        speak = findViewById(R.id.speak);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraSource();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestPermissionID);
        }


        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textView.getText().toString();

                if (date != null) {
                    alarm(date);

                    init(text);
                }

            }
        });


    }


    public void init(final String text) {
        // TODO Auto-generated method stub
        tts = new TextToSpeech(ScanActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
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

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    public void extractDate(String input) {

        Locale.setDefault(Locale.ENGLISH);
        String[] elements = input.split(" ");
        for (int i = 0; i < dateFormat.size(); i++) {
            if (dateFormat.get(i).equals("MM/uuuu") || dateFormat.get(i).equals("MM-uuuu")) {
                f = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(dateFormat.get(i)).parseDefaulting(ChronoField.DAY_OF_MONTH, 1).toFormatter();//
            } else
                f = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(dateFormat.get(i)).toFormatter();

            for (String element : elements) {
                try {
                    LocalDate localDate = LocalDate.parse(element, f);
                    if (localDate != null) {
                        date = localDate;
                        textView.setText(localDate.toString());
                    }
                } catch (DateTimeParseException e) {
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
        if (calendarToSchedule.compareTo(current) < 0) {
            showMessage();
            return;
        }

        else if (calendarToSchedule.compareTo(current) == 0) {
            showMessage();
        }

        calendarToSchedule.set(Calendar.HOUR_OF_DAY, hours);
        calendarToSchedule.set(Calendar.MINUTE, min + 2);
        calendarToSchedule.set(Calendar.SECOND, 0);

        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        long reminderDateTimeInMilliseconds = 0;
        reminderDateTimeInMilliseconds = calendarToSchedule.getTimeInMillis();

        MyNotificationPublisher receiver = new MyNotificationPublisher();
        IntentFilter filter = new IntentFilter("SET_RECEIVER_ALARM_ACTION");
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);

        Intent intent = new Intent("SET_RECEIVER_ALARM_ACTION");

        PendingIntent operation = PendingIntent.getBroadcast(getBaseContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds, operation);


    }

    public void showMessage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Expire Date Detected  ");
        builder.setTitle("Warning").setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void issDate(String date) {
        // 12/21
        String pattern3 = "(\\d{1,2}/\\d{1,2}/\\d{4}|\\d{1,2}/\\d{4}|\\d{1,2}/\\d{2})";
        Matcher m = Pattern.compile(pattern3, Pattern.CASE_INSENSITIVE).matcher(date);
        while (m.find()) {
            //textView.setText(m.group(1));
            extractDate(m.group(1));
        }

    }

    private void ConvertTextToSpeech(String text) {
        // TODO Auto-generated method stub

        if (text == null || "".equals(text)) {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    private void startCameraSource() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull @SuppressLint("UnsafeExperimentalUsageError") ImageProxy imageProxy) {
                        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                        textRecognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {
                                        textView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                String resultText = visionText.getText();
                                                if (resultText != null) {
                                                    issDate(resultText);
                                                    extractDate(resultText);
                                                }
                                            }
                                        });
                                        imageProxy.close();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        imageProxy.close();
                                    }
                                });
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestPermissionID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraSource();
            } else {
                // Handle permission denial
            }
        }
    }
}

