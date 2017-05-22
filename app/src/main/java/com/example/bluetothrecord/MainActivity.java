package com.example.bluetothrecord;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String TAG = "BluetoothRecord";
	private static String mFileName = null;
	private AudioManager mAudioManager = null;
	private MediaRecorder mRecorder = null;
	private Button startRecordButton;
	private Button stopRecordButton;
	private TextView textView1=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

		startRecordButton = (Button)findViewById(R.id.button1);
		stopRecordButton = (Button)findViewById(R.id.button2);
		textView1=(TextView) this.findViewById(R.id.textView1);
		startRecordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startRecording();
			}
		});
		stopRecordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopRecording();
			}
		});
	}


	//record
	private void startRecording(){

		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/record.3gp";
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		try {
			mRecorder.prepare();
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(TAG, "prepare() failed!");
		}
		if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
			Log.i(TAG, "系统不支持蓝牙录音");
			return;
		}
		Log.i(TAG, "系统支持蓝牙录音");
		mAudioManager.stopBluetoothSco();
		mAudioManager.startBluetoothSco();//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

				if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
					Log.i(TAG, "AudioManager.SCO_AUDIO_STATE_CONNECTED");
					mAudioManager.setBluetoothScoOn(true);  //打开SCO
					Log.i(TAG, "Routing:" + mAudioManager.isBluetoothScoOn());
					mAudioManager.setMode(AudioManager.STREAM_MUSIC);
					mRecorder.start();//开始录音
					Log.d(TAG,"启动录音");
					textView1.setText("正在录音");
					unregisterReceiver(this);  //别遗漏
				}else if (AudioManager.SCO_AUDIO_STATE_CONNECTING==state){
					Log.d(TAG,"SCO_AUDIO_STATE_CONNECTING");
					textView1.setText("启动录音失败");
				}else {//等待一秒后再尝试启动SCO
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mAudioManager.startBluetoothSco();
					Log.i(TAG, "再次startBluetoothSco()");
					textView1.setText("再次startBluetoothSco()");
				}

				/*Log.i(TAG, "AudioManager.SCO_AUDIO_STATE_CONNECTED");
				mAudioManager.setBluetoothScoOn(true);  //打开SCO
				Log.i(TAG, "Routing:" + mAudioManager.isBluetoothScoOn());
				mAudioManager.setMode(AudioManager.STREAM_MUSIC);
				mRecorder.start();//开始录音
				Log.d(TAG,"启动录音");
				unregisterReceiver(this);  //别遗漏*/
			}
//		}, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
		}, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
	}


	private void stopRecording(){
		//mAudioManager.stopBluetoothSco();
		mRecorder.stop();
		mRecorder.reset();
		mRecorder.release();
		mRecorder = null;
		if (mAudioManager.isBluetoothScoOn()) {
			mAudioManager.setBluetoothScoOn(false);
			mAudioManager.stopBluetoothSco();
			textView1.setText("停止录音");
		}
	}
}
