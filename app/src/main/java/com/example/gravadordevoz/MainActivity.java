package com.example.gravadordevoz;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {
    private ImageButton btnGravar, btnPausar, btnPlay, btnParar;
    private TextView txtTitulo, PauseResumeTextView;
    private Button btnArquivo;

    private MediaRecorder recorder = null;

    private MediaPlayer mPlayer;

    private String mFileName = null;

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private static final int PICK_PDF_FILE = 2;

    public int PauseResumeFlag=0, PausedLength;

    private int valor_tempo, valor_total, valor, var = 0, i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTitulo = findViewById(R.id.txtTitulo);
        btnGravar = findViewById(R.id.imgBtnGravar);
        btnPausar = findViewById(R.id.imgBtnPausar);
        btnPlay = findViewById(R.id.imgBtnPlay);
        btnParar = findViewById(R.id.imgBtnParar);
        btnArquivo = findViewById(R.id.btnArquivo);

        btnParar.setBackgroundColor(getResources().getColor(R.color.gray));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.gray));
        btnPausar.setBackgroundColor(getResources().getColor(R.color.gray));

        btnPausar.setEnabled(false);
        btnPlay.setEnabled(false);
        btnParar.setEnabled(false);

        btnGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { gravar(); }
        });

        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pararGravar();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        btnPausar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausarAudio();
            }
        });

        btnArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openFile();
            }
        });

        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private void gravar() {
        if (CheckPermissions()) {
            btnParar.setBackgroundColor(getResources().getColor(R.color.green));
            btnGravar.setBackgroundColor(getResources().getColor(R.color.gray));
            btnPlay.setBackgroundColor(getResources().getColor(R.color.gray));
            btnPausar.setBackgroundColor(getResources().getColor(R.color.gray));

            btnPausar.setEnabled(false);
            btnPlay.setEnabled(false);
            btnParar.setEnabled(true);
            btnGravar.setEnabled(false);

            mFileName = Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + "audio"+i+".3gp";

            recorder = new MediaRecorder();

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            recorder.setOutputFile(mFileName);

            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }

            recorder.start();
            salvarAudio(mFileName);
        } else {
            RequestPermissions();
        }
        i++;
    }

    public void pararGravar() {
        btnParar.setBackgroundColor(getResources().getColor(R.color.gray));
        btnGravar.setBackgroundColor(getResources().getColor(R.color.green));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.green));
        btnPausar.setBackgroundColor(getResources().getColor(R.color.gray));

        recorder.stop();

        recorder.release();
        recorder = null;

        btnParar.setEnabled(false);
        btnPlay.setEnabled(true);
        btnGravar.setEnabled(true);
    }

    public void playAudio() {
        btnParar.setBackgroundColor(getResources().getColor(R.color.gray));
        btnGravar.setBackgroundColor(getResources().getColor(R.color.green));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.gray));
        btnPausar.setBackgroundColor(getResources().getColor(R.color.green));

        btnPausar.setEnabled(true);
        btnPlay.setEnabled(false);

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    botoesStop();
                }
            };
            handler.postDelayed(runnable, mPlayer.getDuration());

            /*handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    botoesStop();
                }
            }, mPlayer.getDuration());*/

        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
        PauseResumeFlag = 0;
    }

    public void pausarAudio() {
        btnPlay.setEnabled(false);
        //btnPausar.setEnabled(false);

        /*if(var == 0){
            Toast.makeText(getApplicationContext(), "O tempo eh xxx", Toast.LENGTH_SHORT).show();

            mPlayer.pause();
            Toast.makeText(getApplicationContext(), "O tempo eh yyy", 5000).show();

            valor_tempo = mPlayer.getCurrentPosition();
            Toast.makeText(getApplicationContext(), "Reproduzido eh"+valor_tempo, 5000).show();

            valor_total = mPlayer.getDuration();
            Toast.makeText(getApplicationContext(), "O total eh"+valor_total, 5000).show();

            valor = valor_total-valor_tempo;
            Toast.makeText(getApplicationContext(), "O valor eh"+valor, 5000).show();

            var = 1;
        }
        if(var == 1){
            mPlayer.seekTo(valor_tempo);
            mPlayer.start();
        }

        mPlayer.seekTo(valor);
        mPlayer.start();*/

        /*mPlayer = null;
        mPlayer.seekTo((mPlayer.getDuration())-valor_tempo);
        mPlayer.start();*/

        /*if (PauseResumeFlag == 0){
            //Pause song
            mPlayer.pause();
            PausedLength = mPlayer.getCurrentPosition();

            //Set flag and Button Text to Resume
            PauseResumeFlag = 1;
        }
        else {
            // Resume song
            mPlayer.seekTo(PausedLength);
            mPlayer.start();

            //Set flag and Button Text to Pause
            PauseResumeFlag = 0;
        }*/

        if (PauseResumeFlag == 0){
            //Toast.makeText(getApplicationContext(), "O tempo eh bbb", Toast.LENGTH_SHORT).show();
            valor_tempo = mPlayer.getCurrentPosition();
            valor_total = mPlayer.getDuration();
            valor = valor_total-valor_tempo;

            mPlayer.pause();
            //Toast.makeText(getApplicationContext(), "O tempo eh ccc", Toast.LENGTH_SHORT).show();

            PausedLength = mPlayer.getCurrentPosition();
            //Toast.makeText(getApplicationContext(), "O tempo eh ddd", Toast.LENGTH_SHORT).show();

            // Set flag and Button Text to Resume
            PauseResumeFlag = 1;
            //Toast.makeText(getApplicationContext(), "O tempo eh eee", Toast.LENGTH_SHORT).show();

            //PauseResumeTextView.setText("Resume");
            //Toast.makeText(getApplicationContext(), "O tempo eh fff", Toast.LENGTH_SHORT).show();
            btnPlay.setEnabled(true);
        }

        // Resume
        else {
            // Resume song
            mPlayer.seekTo(PausedLength);
            mPlayer.start();

            valor_tempo = mPlayer.getCurrentPosition();
            valor_total = mPlayer.getDuration();
            valor = valor_total-valor_tempo;

            // Set flag and Button Text to Pause
            PauseResumeFlag = 0;
            btnPlay.setEnabled(true);

            //PauseResumeTextView.setText("Pause");
        }

        /*Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                btnPausar.setBackgroundColor(getResources().getColor(R.color.gray));
                btnPausar.setEnabled(false);
            }
        };
        handler.postDelayed(runnable, mPlayer.getCurrentPosition());*/
        //Toast.makeText(getApplicationContext(), "O tempo eh aaa", Toast.LENGTH_SHORT).show();
        //int j = 50;
        /*if(mPlayer.getCurrentPosition() == mPlayer.getCurrentPosition()){
            Toast.makeText(getApplicationContext(), "O tempo eh bbb", Toast.LENGTH_SHORT).show();
            btnPausar.setBackgroundColor(getResources().getColor(R.color.gray));
            btnPausar.setEnabled(false);
        }*/

        /*Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                botoesStop();
            }
        };
        handler.postDelayed(runnable, mPlayer.getDuration());*/

        btnParar.setBackgroundColor(getResources().getColor(R.color.gray));
        btnGravar.setBackgroundColor(getResources().getColor(R.color.green));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.green));
        btnPausar.setBackgroundColor(getResources().getColor(R.color.green));

    }

    private void botoesStop(){
        btnParar.setBackgroundColor(getResources().getColor(R.color.gray));
        btnGravar.setBackgroundColor(getResources().getColor(R.color.green));
        btnPlay.setBackgroundColor(getResources().getColor(R.color.green));
        btnPausar.setBackgroundColor(getResources().getColor(R.color.green));

        btnPausar.setEnabled(true);
        btnPlay.setEnabled(true);

    }

    private void salvarAudio(String mFileName){
        File file1 = new File(Environment.getExternalStorageDirectory() + "/Audios_Comp_Movel");
        if(file1.exists()){
            file1.mkdir();
        }

        String nomeArquivo = "Audio.3gp";
        File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Audios_Comp_Movel/" + nomeArquivo);
        try{
            FileOutputStream salvar = new FileOutputStream(file2);
            salvar.write(mFileName.getBytes());
            salvar.close();
            //Toast.makeText(this, "Audio Salvo com sucesso", Toast.LENGTH_SHORT).show();
        }catch (FileNotFoundException e){
            e.printStackTrace();
            //Toast.makeText(this, "Audio nao Encontrado", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "Erro", Toast.LENGTH_SHORT).show();
        }
    }

    /*private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, PICK_PDF_FILE);
    }*/

}