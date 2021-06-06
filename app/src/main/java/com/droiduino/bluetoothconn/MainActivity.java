package com.droiduino.bluetoothconn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private BluetoothDevice bluetoothDevice;
    public static BluetoothSocket mmSocket;
    public BluetoothAdapter mBluetoothAdaptater;

    private String deviceName = null;
    private String mConnectedDeviceName = null;

    private String deviceAddress;
    public static Handler handler;

    public static ConnectedThreadOLD connectedThreadOLD;
    public static CreateConnectThread createConnectThread;

    private Button button_send;
    private EditText send_value;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;


    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * The Handler that gets information back from the Data class
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Context activity = getApplicationContext();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        // STATE_CONNECTED = 3
                        case Data.STATE_CONNECTED:
                            setStatus("CONNECTED");
//                            mConversationArrayAdapter.clear();
                            break;
                        // STATE_CONNECTING = 2
                        case Data.STATE_CONNECTING:
                            setStatus("CONNECTING...");
                            break;
                        // STATE_LISTEN = 1
                        case Data.STATE_LISTEN:
                        // STATE_NONE = 0
                        case Data.STATE_NONE:
                            setStatus("NOT CONNECTED");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    setStatus("WRITE: "+writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    setStatus("READ: "+readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ((TextView) findViewById(R.id.levelValue)).setText(sharedPreferences.getString("levels","3"));
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data data = Data.getInstance();
        data.setContext(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdaptater = BluetoothAdapter.getDefaultAdapter();
        Data.getInstance().setmAdapter(mBluetoothAdaptater);
        Data.getInstance().setmHandler(mHandler);

        // UI Initialization
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Button buttonSetting = findViewById(R.id.buttonSetting);
        this.button_send = findViewById(R.id.buttonSend);
        this.send_value = findViewById(R.id.message_value);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


//        final TextView textViewInfo = findViewById(R.id.textViewInfo);
//        final Button buttonToggle = findViewById(R.id.buttonToggle);
//        buttonToggle.setEnabled(false);
//        final ImageView imageView = findViewById(R.id.imageView);
//        imageView.setBackgroundColor(getResources().getColor(R.color.colorOff));
//        final TextView levelValue = findViewById(R.id.levelValue);
//        Data.getInstance().setAffichage_main(levelValue);
//        Data.getInstance().getAffichage_main().setText("TEST");

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
//            createConnectThread.start();
        }


    // todo: à remplacer

        /*
        Second most important piece of Code. GUI Handler
         */
//        handler = new Handler(Looper.getMainLooper()) {
//
//            @Override
//            public void handleMessage(Message msg){
//                switch (msg.what){
//                    case CONNECTING_STATUS:
//                        switch(msg.arg1){
//                            case 1:
//                                toolbar.setSubtitle("Connected to " + deviceName);
//                                TextView angleView = findViewById(R.id.angleValue);
////                                angleView.setText("READY");
//                                progressBar.setVisibility(View.GONE);
//                                buttonConnect.setEnabled(true);
////                                buttonToggle.setEnabled(true);
//                                break;
//                            case -1:
//                                toolbar.setSubtitle("Device fails to connect");
//                                progressBar.setVisibility(View.GONE);
//                                buttonConnect.setEnabled(true);
//                                break;
//                        }
//                        break;
//
//                    case MESSAGE_READ:
//                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
//                        TextView angleView = findViewById(R.id.angleValue);
//                        Log.d("Handler:Message_READ", arduinoMsg);
//                        switch (arduinoMsg.toLowerCase()){
//                            case "led is turned on":
//                                angleView.setText(arduinoMsg.toLowerCase());
////                                imageView.setBackgroundColor(getResources().getColor(R.color.colorOn));
////                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
//                                break;
//                            case "led is turned off":
//                                angleView.setText(arduinoMsg.toLowerCase());
////                                imageView.setBackgroundColor(getResources().getColor(R.color.colorOff));
////                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
//                                break;
//
//                            default:
//                                angleView.setText(arduinoMsg.toUpperCase());
//                                Log.d("handle :","msg : "+arduinoMsg);
//
//                                break;
//                        }
//                        break;
//                }
//            }
//        };

//        Data.getInstance().setmHandler(handler);

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
//        button_send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Move to adapter list
//                String message = send_value.getText().toString();
//                Log.d("button_send",message);
////                Data.getInstance().getmConnectedThread().write(message);
//            }
//        });
            //todo: à réutiliser
        // Button to ON/OFF LED on Arduino Board
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmdText = "null";
                String content = send_value.getText().toString();
               /* switch (btnState){
                    case "turn on":
//                        buttonToggle.setText("Turn Off");
                        Log.d("ONCLICK","Turn Off");
                        // Command to turn on LED on Arduino. Must match with the command in Arduino code
                        cmdText = "<turn on>";
                        break;
                    case "turn off":
//                        buttonToggle.setText("Turn On");
                        Log.d("ONCLICK","Turn On");
                        // Command to turn off LED on Arduino. Must match with the command in Arduino code
                        cmdText = "<turn off>";
                        break;
                }*/
                // Send command to Arduino board
                cmdText = content;
                connectedThreadOLD.write(cmdText);
                Log.d("SEND MSG",content);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        if(!mBluetoothAdaptater.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }else{
            setupChat();
            Data.getInstance().start();
        }
    }


    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {


        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {


            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
            BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
                    Log.e("LISTENER","CONNECTED");
                    Data.getInstance().getAffichage_main().setText("Connected at "+bluetoothProfile.getConnectedDevices().get(0).getName());
                }

                @Override
                public void onServiceDisconnected(int i) {
                    Log.d("LISTENER","DISCONNECTED");
                }
            };
//            Context context =
            Context context = Data.getInstance().getContext();
            bluetoothAdapter.getProfileProxy(context,serviceListener, BluetoothProfile.GATT);

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device"+connectException.getMessage());
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThreadOLD = new ConnectedThreadOLD(mmSocket);
//            Data.getInstance().setmConnectedThread(connectedThreadOLD);
            connectedThreadOLD.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThreadOLD extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThreadOLD(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                Log.d("THREAD RUN","JE SUIS CO");
                //todo: envoyer niveau
                //todo: classe singleton avec message reçu et message à envoyer
                //todo: relier le click reset avec un msg socket reset
                //todo: Singleton avec liste FIFO et une socket en attribut msg en attente si pas , faire un objet pour chaque type de truc à envoyer (2)
//                this.write();
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    Log.d("INFO:WHILE(TRUE)","JUST BEFORE TEST");
                    if (buffer[bytes] == '\n'){
//                        angleValue.setText(buffer[bytes]);
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);

                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        Log.d("setStatus","SUBTITLE:"+subTitle.toString());
        Context activity = getApplicationContext();
        if (null == activity) {
            return;
        }
        final TextView angleView = findViewById(R.id.angleValue);
        if (null == angleView) {
            return;
        }
        angleView.setText(subTitle);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        Context context = getApplicationContext();
        if (context == null) {
            return;
        }


        // Initialize the compose field with a listener for the return key
//        optionnel
//        send_value.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        button_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget

                TextView textView = findViewById(R.id.message_value);

                String message = textView.getText().toString();
                Log.d(TAG,"ONCLICK:BUTTONSEND"+message);
                sendMessage(message);

            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
//        mChatService = new BluetoothChatService(context, mHandler);


        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
//        setStatus(message); // for debugging purpose
        // Check that we're actually connected before trying anything
        if (Data.getInstance().getState() != Data.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            Data.getInstance().write(send);

            Log.e(TAG,"sendMessage: "+message);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            send_value.setText(mOutStringBuffer);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Context activity = getApplicationContext();
                    if (activity != null) {
                        Toast.makeText(activity, R.string.bt_not_enabled_leaving,
                                Toast.LENGTH_SHORT).show();
//                        activity.finish();/todo:ENDAPP
                    }
                }
        }
    }
    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdaptater.getRemoteDevice(address);
        // Attempt to connect to the device
       Data.getInstance().connect(device, secure);
    }
}
