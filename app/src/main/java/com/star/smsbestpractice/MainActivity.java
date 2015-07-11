package com.star.smsbestpractice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 0;

    public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";

    private TextView mSenderTextView;
    private TextView mContentTextView;

    private IntentFilter mReceiveIntentFilter;
    private MessageReceiver mMessageReceiver;

    private EditText mToEditText;
    private EditText mMsgInputEditText;

    private IntentFilter mSendFilter;
    private SendStatusReceiver mSendStatusReceiver;

    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSenderTextView = (TextView) findViewById(R.id.sender);
        mContentTextView = (TextView) findViewById(R.id.content);

        mReceiveIntentFilter = new IntentFilter();
        mReceiveIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
//        mReceiveIntentFilter.setPriority(100);
        mMessageReceiver = new MessageReceiver();

        registerReceiver(mMessageReceiver, mReceiveIntentFilter);

        mToEditText = (EditText) findViewById(R.id.to);
        mMsgInputEditText = (EditText) findViewById(R.id.msg_input);

        mSendFilter = new IntentFilter();
        mSendFilter.addAction(SENT_SMS_ACTION);

        mSendStatusReceiver = new SendStatusReceiver();

        registerReceiver(mSendStatusReceiver, mSendFilter);

        mSendButton = (Button) findViewById(R.id.send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager smsManager = SmsManager.getDefault();

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        MainActivity.this, REQUEST_CODE, new Intent(SENT_SMS_ACTION), 0
                );

                smsManager.sendTextMessage(mToEditText.getText().toString(), null,
                       mMsgInputEditText.getText().toString(), pendingIntent, null );
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mSendStatusReceiver);

        super.onDestroy();
    }

    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            Object[] pdus = (Object[]) bundle.get("pdus");

            SmsMessage[] messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            String address = messages[0].getOriginatingAddress();

            StringBuffer fullMessage = new StringBuffer();

            for (int i = 0; i < messages.length; i++) {
                fullMessage.append(messages[i].getMessageBody());
            }

            mSenderTextView.setText(address);
            mContentTextView.setText(fullMessage);

//            abortBroadcast();
        }
    }

    private class SendStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == RESULT_OK) {
                Toast.makeText(context, "Send succeeded", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Send failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}


