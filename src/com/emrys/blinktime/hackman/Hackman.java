package com.emrys.blinktime.hackman;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Hackman extends Activity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private static final String MESSAGE_AHEAD = "F";
    private static final String MESSAGE_BACK = "B";
    private static final String MESSAGE_LEFT = "L";
    private static final String MESSAGE_RIGHT = "R";

    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    private ImageButton moveAhead, moveBack, moveLeft, moveRight = null;
    private TextView showAction = null;

    private boolean isTouched = false;
    private String currentCode = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) setupChat();
        }
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
//            showAction.setText(currentCode);
            String face = null;
            if (currentCode == MESSAGE_AHEAD) {
                face = "前";
            } else if (currentCode == MESSAGE_BACK) {
                face = "后";
            } else if (currentCode == MESSAGE_LEFT) {
                face = "左";
            } else if (currentCode == MESSAGE_RIGHT) {
                face = "右";
            }

            showAction.setText(face);
        }
    };

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        moveAhead = (ImageButton) findViewById(R.id.bt_ahead);
        moveBack = (ImageButton) findViewById(R.id.bt_back);
        moveLeft = (ImageButton) findViewById(R.id.bt_left);
        moveRight = (ImageButton) findViewById(R.id.bt_right);
        showAction = (TextView) findViewById(R.id.tv_show_action);

        moveAhead.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        currentCode = MESSAGE_AHEAD;
                        new Thread(myButtonThread).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        currentCode = null;
                        uiHandler.sendEmptyMessage(0);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        moveBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        currentCode = MESSAGE_BACK;
                        new Thread(myButtonThread).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        currentCode = null;
                        uiHandler.sendEmptyMessage(0);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        moveLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        currentCode = MESSAGE_LEFT;
                        new Thread(myButtonThread).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        currentCode = null;
                        uiHandler.sendEmptyMessage(0);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        moveRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        currentCode = MESSAGE_RIGHT;
                        new Thread(myButtonThread).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        currentCode = null;
                        uiHandler.sendEmptyMessage(0);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

    private Runnable myButtonThread = new Runnable() {
        @Override
        public void run() {
            while (isTouched) {
                uiHandler.sendEmptyMessage(1);
                try {
                    sendMessage(currentCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) mChatService.stop();
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);

//            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

//    private TextView.OnEditorActionListener mWriteListener =
//            new TextView.OnEditorActionListener() {
//                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
//                        String message = view.getText().toString();
//                        sendMessage(message);
//                    }
//                    return true;
//                }
//            };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                            mInTextView.setText("");    // clear the text
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
//                byte[] writeBuf = (byte[]) msg.obj;
//                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    mInTextView.setText(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        return false;
    }

}
