package fi.delektre.ringdataread;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
//import org.reactivestreams.Subscription;

import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import fi.delektre.ringdataread.util.HexString;
import fi.delektre.ringdataread.util.RingCommand;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
//import io.reactivex.subjects.PublishSubject;
//import rx.Observable;
//import rx.Subscription;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.subjects.PublishSubject;

import android.support.design.widget.Snackbar;

//import static com.trello.rxlifecycle.RxLifecycle.bindUntilEvent;
//import static com.trello.rxlifecycle.android.ActivityEvent.DESTROY;

//import static com.trello.rxlifecycle2.RxLifecycle.bindUntilEvent;
import static com.trello.rxlifecycle2.android.ActivityEvent.DESTROY;
import static com.trello.rxlifecycle2.android.ActivityEvent.PAUSE;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity
        implements LifecycleProvider<ActivityEvent> {
    private final String TAG = "RINGA";
    private RxBleClient rxBleClient = null;
    private RxBleDevice rxBleDevice = null;
    //    private Subscription scanDisposable = null;
    private Disposable scanDisposable = null;
    private Disposable connSubscription = null;
    private Disposable ackDisposable = null;
    private Disposable cmdSubscription = null;

    private Disposable connectionObservable;
    //private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();

    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

    private boolean mConnected = false;

    private static final int PERMISSION_REQUEST_ALL = 1000;
    private final int PERMISSION_REQUEST_LOCATION = 1002;

    private final long DATA_RETRIEVAL_PERIOD = 10000L;
    private final int DATA_BLOCKS = 256;
    private final int DATA_BLOCKS_SIZE = 20;
    private byte dataBlocks[] = new byte[DATA_BLOCKS * DATA_BLOCKS_SIZE];

    private Timer dataTaskTimer = null;
    private Timer commandTaskTimer = null;

    private DataTimer dataUpdater = new DataTimer();

    class DataTimer extends TimerTask {
        public void run() {
            // queryDeviceData();
            fetchData();
        }
    }

    @ViewById(R.id.textViewLogger)
    View rootLayout;

    @ViewById(R.id.textViewLogger)
    TextView loggerView;

    @ViewById(R.id.progressBar)
    ProgressBar progressBar;

    @Click(R.id.startButton)
    void clickStartButton() {
        if (!mConnected) {
            if (!isScanning()) {
                messageAppend("Find a ring device");
                findDevice();
            } else {
                messageAppend("scan already ongoing");
            }

        } else {
            doRingStart();
            messageAppend("Start ring recording");
        }
    }

    @Click(R.id.buttonClear)
    void clearLogScreen() {
        loggerView.setText("");
        lineCount = 0;
    }

    @Bean
    RingCommand commandHelper;
    private final int REQUEST_ENABLE_BT = 579;

    @AfterViews
    void setupScreen() {
        loggerView.setText("");
        setTitle("RingDataRead: not connected");
        runOnce();
    }

    private boolean runOnceRunned = false;

    @UiThread
    void runOnce() {
        Log.d(TAG, "runOnce()");
        if (runOnceRunned)
            return;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            sendUserNotification("BLE Error", "No Bluetooth device found!");
            finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetoothAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothAdapter, REQUEST_ENABLE_BT);
            }
        }

        progressBar.setMax(100);

        rxBleClient = RxBleClient.create(this);
        RxBleLog.setLogLevel(RxBleLog.DEBUG);

        Log.d(TAG, "rxBleClient object created");

        if (rxBleDevice != null) {
            Log.d(TAG, "runOnce(): rxBleDevice has been created");
            triggerConnect();
        } else {
            Log.d(TAG, "runOnce(): rxBleDevice has not been set -> findDevice");
            findDevice();
        }
        /*
        if (rxBleDevice != null) {
            connectionObservable = prepareConnectionObservable();
        } else {
            findDevice();
        }
*/

        runOnceRunned = true;
    }

    private void askPermissions() {
        Log.d(TAG, "askPermissions()");

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        //Manifest.permission.INTERNET
                },
                PERMISSION_REQUEST_ALL);

    }

    private void handleBleScanException(BleScanException bleScanException) {
        final String text;

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                text = "Bluetooth is not available";

                break;
            case BleScanException.BLUETOOTH_DISABLED:
                text = "Enable bluetooth and try again";

                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                text = "On Android 6.0 location permission is required. Implement Runtime Permissions";

                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                text = "Location services needs to be enabled on Android 6.0";

                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                text = "Scan with the same filters is already started";

                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                text = "Failed to register application for bluetooth scan";

                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                text = "Scan with specified parameters is not supported";

                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                text = "Scan failed due to internal error";

                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                text = "Scan cannot start due to limited hardware resources";

                break;
            case BleScanException.UNDOCUMENTED_SCAN_THROTTLE:
                text = String.format(
                        Locale.getDefault(),
                        "Android 7+ does not allow more scans. Try in %d seconds",
                        secondsTill(bleScanException.getRetryDateSuggestion())
                );
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                text = "Unable to start scanning";
                break;
        }
        Log.w("EXCEPTION", text, bleScanException);
        sendUserNotification("BLE Error", text);
    }

    private long secondsTill(Date retryDateSuggestion) {
        return TimeUnit.MILLISECONDS.toSeconds(retryDateSuggestion.getTime() - System.currentTimeMillis());
    }

    private void findDevice() {
        if (rxBleClient == null) {
            Log.e(TAG, "Missing BLE client, creating");
            rxBleClient = RxBleClient.create(this);
        }

        updateProgressBar(0);
        /*
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        //.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                        .build(),
                new ScanFilter.Builder()
                        .setDeviceName("RINGA")
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::clearSubscription)
                .take(1)
                .subscribe(this::onScanSuccess, this::onScanFailure);
                */
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build(),
                new ScanFilter.Builder()
                        .setDeviceName("RINGA")
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::disposeScanner)
                .subscribe(
                        this::onScanSuccess,
                        this::onScanFailure
                );
        //scanDisposable.disposeScanner();
    }

    @UiThread
    void onScanSuccess(ScanResult scanResult) {
        Log.e(TAG, "Found: " + scanResult);
        rxBleDevice = scanResult.getBleDevice();
        String macAddr = scanResult.getBleDevice().getMacAddress();

        //if (macAddr != null) {
        //    prefs.bleAddr().put(macAddr);
        //}
        //prefs.bleName().put(scanResult.getBleDevice().getName());

        Log.d(TAG, "create observable connection state change observer");
        rxBleDevice.observeConnectionStateChanges()
                .compose(bindUntilEvent(DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);

        //connectionObservable = prepareConnectionObservable();
        triggerConnect();

        messageAppend("Found: " + macAddr + " with RSSI = " + scanResult.getRssi());
        //buttonQuery.setEnabled(true);
        //onConnect();
        if (scanDisposable != null)
            scanDisposable.dispose();

        updateProgressBar(100);
        mConnected = true;
    }

    private void onScanFailure(Throwable throwable) {

        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
        mConnected = false;
    }

    private void clearSubscription() {
        if (scanDisposable != null) {
            scanDisposable = null;
        }
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        Log.v(TAG, "onConnectionStateChange( " + newState + ")");
        messageAppend(newState.toString());
    }

    private Disposable connectionDisposable = null;

    private void triggerDisconnect() {
        Log.v(TAG, "triggerDisconnect()");
        if (connectionDisposable != null) {
            connectionDisposable.dispose();
            connectionDisposable = null;
        }
    }

    private void disposeScanner() {
        if (scanDisposable != null) {
            scanDisposable.dispose();
            scanDisposable = null;
        }
    }

    private void disposeConnector() {
        if (connectionDisposable != null) {
            connectionDisposable.dispose();
            connectionDisposable = null;
        }
    }

    private void triggerConnect() {
        Log.v(TAG, "triggerConnect()");
        if (isConnected()) {
            Log.e(TAG, "We already are connected, skip connect");
        } else {
            /*
            connectionObservable = rxBleDevice.establishConnection(false)
                    .compose(bindUntilEvent(PAUSE))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onConnectSuccess, this::onConnectFailure);
*/
            connectionDisposable = rxBleDevice.establishConnection(false)
                    .compose(bindUntilEvent(PAUSE))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onConnectSuccess, this::onConnectFailure);
        }
    }



    /*
    private Observable<RxBleConnection> prepareConnectionObservable() {
        if (dataTaskTimer == null) {
            dataTaskTimer = new Timer();
            dataTaskTimer.schedule(dataUpdater, DATA_RETRIEVAL_PERIOD, DATA_RETRIEVAL_PERIOD);
        }
        return rxBleDevice.observeConnectionStateChanges()
                .subscribe(
                        this::onConnectionStateChange,
                        this::onConnectFailure
                );
        */
        /*
        rxBleDevice
                .observeConnectionStateChanges()
                .compose(bindUntilEvent(DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
*/
        /*
        return rxBleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                // .compose(bindUntilEvent(PAUSE))
                .doOnUnsubscribe(this::clearSubscription)
                .compose(new ConnectionSharingAdapter());
               */
        /*
        Disposable disposable = rxBleDevice.establishConnection(false)
                .subscribe(
                        this::onConnectSuccess,
                        this::onConnectFailure
                );
        disposable.disposeScanner();
        return null;
        */
    //}

    private RxBleConnection activeConnection = null;

    private void onConnectSuccess(RxBleConnection connection) {
        Log.v(TAG, "Successfully connected to: " + connection.toString());
        activeConnection = connection;
        onConnect();
    }

    private void onConnectFailure(Throwable throwable) {
        Log.e(TAG, "onConnectFailure(): " + throwable);
        sendUserNotification("Connection failure", throwable.toString());
        activeConnection = null;
    }

    private int currentPointer = 0;

    private void onData(byte bytes[]) {
        Log.v(TAG, "onData(): " + HexString.bytesToHex(bytes));
        int index;
        if (bytes.length == 0) {
            Log.e(TAG, "onData() called with zero length byte array!");
            return;
        }
        for (index = 0; index < 20; index++) {
            dataBlocks[index + DATA_BLOCKS_SIZE * currentPointer] = bytes[index];
        }
        currentPointer++;
        int value = bytes[0] + (bytes[1] << 8);
        lastSegmentId[0] = bytes[0];
        lastSegmentId[1] = bytes[1];
        // doRingAck(value);
        if (currentPointer >= DATA_BLOCKS) {
            clearSubscriptions();
        }
        //    fetchData();
    }

    private byte lastSegmentId[] = {0, 0};

    public void fetchData() {
        Log.e(TAG, "fetchData()");
        if (connectionObservable != null) {
            //for (int i = 0; i < DATA_BLOCKS; i++) {
            /*
                connectionObservable
                        .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(AppConst.UUID_DATA_CHARACTERISTIC)
                        .doOnNext(bytes -> {
                            onData(bytes);
                        })
                        .flatMap(bytes -> rxBleConnection.writeCharacteristic(AppConst.UUID_ACK_CHARACTERISTIC, lastSegmentId))
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(null,
                                this::onReadFailure);
                                */
            //}


        }
    }

    void setupNotification() {
        Log.d(TAG, "setupNotification()");
        rxBleDevice.establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(AppConst.UUID_DATA_CHARACTERISTIC))
                .flatMap(notificationObservable -> notificationObservable)
                .subscribe(
                        bytes -> {
                            onData(bytes);
                        },
                        this::onReadFailure
                );
    }

    /*
    @Extra
    void sendCommandToRing(String command) {
        if (command == null) {
            Log.e(TAG, "Null command send?? Why??");
        } else {
            switch (command) {
                case AppConst.RING_CMD_RESET:
                    doRingReset();
                    break;
                case AppConst.RING_CMD_CALIBRATE:
                    doRingCalibrate();
                    break;
                case AppConst.RING_CMD_START:
                    doRingStart();
                    break;
                default:
            }
        }
    }
*/
    private void clearSubscriptions() {
        Log.d(TAG, "clearSubscriptions()");
        if (scanDisposable != null) {
            scanDisposable.dispose();
        }
        if (connSubscription != null) {
            connSubscription.dispose();
            onDisconnect();
        }
    }

    @Receiver(actions = AppConst.RING_CMD_CALIBRATE)
    protected void doRingCalibrate() {
        Log.d(TAG, "doRingCalibrate()");
        sendRingCommand(AppConst.RING_CMD_CALIBRATE, 0);
    }

    @Receiver(actions = AppConst.RING_CMD_RESET)
    protected void doRingReset() {
        Log.d(TAG, "doRingReset()");
        sendRingCommand(AppConst.RING_CMD_RESET, 0);
    }

    @Receiver(actions = AppConst.RING_CMD_REBOOT)
    protected void doRingReboot() {
        Log.d(TAG, "doRingReboot()");
        sendRingCommand(AppConst.RING_CMD_REBOOT, 0);
        clearSubscriptions();
    }


    @Receiver(actions = AppConst.RING_CMD_START)
    protected void doRingStart() {
        Log.d(TAG, "doRingStart()");
        sendRingCommand(AppConst.RING_CMD_START, 0);
    }


    // @Receiver(actions = AppConst.RING_CMD_ACK)
    protected void doRingAck(int value) {
        sendAck(value);
    }

    /**
     * Check that user responded YES to enable BLE request
     *
     * @param resultCode response from user interface dialog
     */
    @OnActivityResult(REQUEST_ENABLE_BT)
    void onEnableBTResult(int resultCode) {
        if (resultCode != RESULT_OK) {
            finish();
        }
    }

    @UiThread
    void onConnect() {
        Log.v(TAG, "onConnect()");
        setTitle("RingDataRead: connected");
        setupNotification();
    }

    @UiThread
    void onDisconnect() {
        Log.v(TAG, "onDisconnect()");
        setTitle("RingDataRead: not connected");
        clearSubscriptions();
    }

    @UiThread
    void updateProgressBar(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    @CallSuper
    public void onPause() {
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        super.onPause();

        if (isScanning())
            scanDisposable.dispose();
    }

    @Override
    @CallSuper
    protected void onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        if (isScanning())
            scanDisposable.dispose();
        //if (scanDisposable != null && !scanDisposable.isUnsubscribed())
        //    scanDisposable.unsubscribe();
        super.onDestroy();
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(ActivityEvent.CREATE);

        askPermissions();



        runOnce();
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(ActivityEvent.RESUME);
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<ActivityEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject);
    }


    //public String getCSV() {
        /*
        return id + "; " + ledRed + "; " + ledGreen + "; " + ledBlue + "; " +
                ledNIR + "; " + ledYellow + "; " + temp1 + "; " + temp2 + "; " +
                steps + "; " + timeValue + "; " + pos_lat + "; " + pos_long + "; " +
                pos_alt + "; " + syncstatus + "; " + measureId;
                */
    //}

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
            writer.write(getCSVHeader() + "\n");
            for (index = 0; index < DATA_BLOCKS; index++) {
                writer.write(index);
                writer.write((dataBlocks[index * DATA_BLOCKS + 0] + (dataBlocks[index * DATA_BLOCKS + 1] << 8)) + "; "); // seq#
                writer.write((dataBlocks[index * DATA_BLOCKS + 2] + (dataBlocks[index * DATA_BLOCKS + 3] << 8)) + "; "); // red
                writer.write((dataBlocks[index * DATA_BLOCKS + 4] + (dataBlocks[index * DATA_BLOCKS + 5] << 8)) + "; "); // green
                writer.write((dataBlocks[index * DATA_BLOCKS + 6] + (dataBlocks[index * DATA_BLOCKS + 7] << 8)) + "; "); // blue
                writer.write((dataBlocks[index * DATA_BLOCKS + 8] + (dataBlocks[index * DATA_BLOCKS + 9] << 8)) + "; "); // nir
                writer.write((dataBlocks[index * DATA_BLOCKS + 10] + (dataBlocks[index * DATA_BLOCKS + 11] << 8)) + "; "); // yellow

                writer.write(
                        (dataBlocks[index * DATA_BLOCKS + 12] +
                                (dataBlocks[index * DATA_BLOCKS + 13] << 8) +
                                (dataBlocks[index * DATA_BLOCKS + 14] << 16) +
                                (dataBlocks[index * DATA_BLOCKS + 15] << 24))
                                + "; "); // yellow

                writer.write((dataBlocks[index * DATA_BLOCKS + 16] + (dataBlocks[index * DATA_BLOCKS + 9] << 17)) + "; "); // temp1
                writer.write((dataBlocks[index * DATA_BLOCKS + 18] + (dataBlocks[index * DATA_BLOCKS + 9] << 19)) + "; "); // temp2

                writer.write("\n");
                rows++;
            }

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
        return rxBleDevice != null && rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void sendUserNotification(String title, String content) {
        Log.v(TAG, "sendUserNotication( " + title + ", " + content + " )");
        Snackbar.make(rootLayout, content, Snackbar.LENGTH_LONG).show();
    }

    private void onReadFailure(Throwable throwable) {
        Log.v(TAG, "onReadFailure(): " + throwable.toString());
        Snackbar.make(rootLayout, "Read error: " + throwable, Snackbar.LENGTH_SHORT).show();
        messageAppend("Read error: " + throwable.toString());
    }

    private void onWriteFailure(Throwable throwable) {
        Log.e(TAG, "onWriteFailure(): " + throwable.toString());
        Snackbar.make(rootLayout, "Write error: " + throwable, Snackbar.LENGTH_SHORT).show();
        messageAppend("Write error: " + throwable.toString());
    }

    private void onWriteSuccess(@NonNull int bytecount) {
        messageAppend("BLE: (" + bytecount + " bytes) sent successfully.");
        Log.v(TAG, "onWriteSuccess( " + bytecount + " )");
    }

    private void disposeCommand() {
        Log.v(TAG, "disposeCommand()");
        if (cmdSubscription != null) {
            cmdSubscription.dispose();
            cmdSubscription = null;
        }
    }

    private void disposeAck() {
        Log.v(TAG, "disposeAck()");
        if (ackDisposable != null) {
            ackDisposable.dispose();
            ackDisposable = null;
        }
    }

    private void sendRingCommand(String cmd, int value) {
        Log.v(TAG, "sendRingCommand( " + cmd + ")");
        // commandRetry.addCommand(cmd);

        commandHelper.setCommand(cmd);

        if (isConnected() && commandHelper.isOk()) {


            cmdSubscription = rxBleDevice.establishConnection(true)
                    .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_CMD_CHARACTERISTIC,
                            commandHelper.getSequence(value)))
                    .doFinally(this::disposeCommand)
                    .subscribe(
                            wroteBytes -> {
                                onWriteSuccess(wroteBytes.length);
                            },
                            this::onWriteFailure
                    );

/*
            cmdSubscription = connectionObservable.flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_CMD_CHARACTERISTIC,
                    commandHelper.getSequence(value)))
                    .subscribe(wroteBytes -> { onWriteSuccess(wroteBytes.length);},
                            this::onWriteFailure);
*/
            /*
            rxBleDevice.establishConnection(false)
                    .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_CMD_CHARACTERISTIC,
                            commandHelper.getSequence(value)))
                    .subscribe(wroteBytes -> { onWriteSuccess(wroteBytes.length); },
                            this::onWriteFailure
                    );
                    */
/*
            connectionObservable
                    .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_CMD_CHARACTERISTIC,
                            commandHelper.getSequence(value)))
                    .subscribe(
                            bytes -> onWriteSuccess(bytes.length),
                            this::onWriteFailure
                    );
                    */
        } else {
            Log.e(TAG, "Not connected, skip sending command");
        }
    }

    private void sendAck(int value) {
        Log.v(TAG, "sendAck( " + value + ")");
        if (isConnected()) {
            ackDisposable = rxBleDevice.establishConnection(false)
                    .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_ACK_CHARACTERISTIC,
                            commandHelper.getSequence(value)))
                    .doFinally(this::disposeAck)
                    .subscribe(
                            wroteBytes -> {
                                onWriteSuccess(wroteBytes.length);
                            },
                            this::onWriteFailure
                    );
            /*
            connectionObservable
                    .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AppConst.UUID_ACK_CHARACTERISTIC,
                            commandHelper.getSequence(value)))
                    .subscribe(
                            bytes -> onWriteSuccess(bytes.length),
                            this::onWriteFailure
                    );
*/
        } else {
            Log.e(TAG, " not connected, skip ACK");
        }
    }
}
