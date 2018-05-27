package fi.delektre.ringdataread;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.OutputStreamWriter;

import fi.delektre.ringdataread.util.RingCommand;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import android.support.design.widget.Snackbar;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private String TAG = "RINGA";
    private RxBleClient rxBleClient = null;
    private RxBleDevice rxBleDevice = null;
    private Subscription scanSubscription = null;
    private Subscription connSubscription = null;
    private Subscription cmdSubscription = null;

    private Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();


    private final int DATA_BLOCKS = 256;

    @ViewById(R.layout.activity_main)
    View rootLayout;

    @ViewById(R.id.textViewLogger)
    TextView loggerView;

    @Click(R.id.startButton)
    void clickStartButton() {
        messageAppend("Start clicked - do nothing yet");
    }

    @Click(R.id.buttonClear)
    void clearLogScreen() {
        loggerView.setText("");
        lineCount = 0;
    }

    @Bean
    RingCommand commandHelper;


    @AfterViews
    void setupScreen() {
        loggerView.setText("");
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    }
    */

    public String getCSV() {
        /*
        return id + "; " + ledRed + "; " + ledGreen + "; " + ledBlue + "; " +
                ledNIR + "; " + ledYellow + "; " + temp1 + "; " + temp2 + "; " +
                steps + "; " + timeValue + "; " + pos_lat + "; " + pos_long + "; " +
                pos_alt + "; " + syncstatus + "; " + measureId;
                */
    }

    private String getCSVHeader() {
        return "\"id\"; \"red\"; \"green\"; \"blue\"; \"nir\"; \"yellow\"; \"temp1\"; \"temp2\"; \"steps\"; " +
                "\"time\"; \"latitude\"; \"longitude\"; \"altitude\"; \"sync\"; \"measureId\"";
    }

    void writeDataFile() {

        try {
            int rows = 0;
            int index = 0;

            String outputFilename = "output_" + System.currentTimeMillis() + ".csv";
            OutputStreamWriter writer = new OutputStreamWriter(getApplicationContext().openFileOutput(outputFilename, Context.MODE_APPEND));
            writer.write("\"Measurement data written by Ring\n\"");
            writer.write( getCSVHeader() + "\n");
            for (index=0;index<DATA_BLOCKS;index++) {
                writer.write(index);
                writer.write("\n");
            }
            /*
            for (DataRow row : getAllRows()) {
                writer.write(row.getCSV() + "\n");
                rows++;
            }
            */
            writer.close();

            sendUserNotification("File output", outputFilename + " with " + rows + " rows");
            //}
        } catch (Exception ioe) {
            Log.e(TAG, "File input/output exception", ioe);
            sendUserNotification("File i/o error", ioe.toString());
        }
    }

    private int lineCount = 0;

    @UiThread
    void messageAppend(@NonNull String msg) {
        if (loggerView != null) {
            //runOnUiThread(() -> {
            if (lineCount > 200) {
                lineCount = 0;
                loggerView.setText("");
            }
            loggerView.append(System.currentTimeMillis() + ": " + msg + "\n");
            lineCount++;

            //});
        }
    }

    private boolean isConnected() {
        if (rxBleDevice != null)
            return rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
        return false;
    }

    private void sendUserNotification(String title, String content) {
        Log.d(TAG, "sendUserNotication()");
        Snackbar.make(rootLayout, content, Snackbar.LENGTH_LONG).show();
    }

    private void onReadFailure(Throwable throwable) {
        Snackbar.make(rootLayout, "Read error: " + throwable, Snackbar.LENGTH_SHORT).show();
        messageAppend("Read error: " + throwable.toString());
    }

    private void onWriteFailure(Throwable throwable) {
        Snackbar.make(rootLayout, "Write error: " + throwable, Snackbar.LENGTH_SHORT).show();
        messageAppend("Write error: " + throwable.toString());
    }

    private void onWriteSuccess() {
        messageAppend("Command sent successfully.");
    }

    private void sendRingCommand(String cmd) {
        // Log.d(TAG, "sendRingCommand( " + cmd + ")");
        // commandRetry.addCommand(cmd);

        commandHelper.setCommand(cmd);

        if (isConnected() && commandHelper.isOk()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_CMD_CHARACTERISTIC, commandHelper.getSequence()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bytes -> onWriteSuccess(),
                            this::onWriteFailure
                    );
        }
    }
    }
