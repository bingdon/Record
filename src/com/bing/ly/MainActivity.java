package com.bing.ly;

import com.bing.fre.Mathfre;
import com.bing.ly.tools.RealDoubleFFT;
import com.bing.ly.tools.Spectrum;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button recored;
	private int frequency=8000;
	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord audioRecord;
	private RealDoubleFFT tRsform;
	private Thread mThread;
	private boolean startre=false;
	private static final String TAG="MainActivity";
	TextView dbtext,fertext;
	String dString;
	int fre=0;
	float newfre=0;
	Mathfre mathfre;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		
		mathfre=new Mathfre();
		
		recored=(Button)findViewById(R.id.startrecod);
		dbtext=(TextView)findViewById(R.id.db);
		fertext=(TextView)findViewById(R.id.frequence);
		
		recored.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startre=true;
				mThread=new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						onRecored();
					}
				});
				mThread.start();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void onRecored(){
		int buffersize=AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
		tRsform=new RealDoubleFFT(buffersize);
		audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, buffersize);
		short [] buffer=new short[buffersize];
		byte []  mybuffer=new byte[buffersize];
		double[] buffer0=new double[buffersize];
		audioRecord.startRecording();
		while (startre) {
			int lylength=audioRecord.read(buffer, 0,buffersize);
			int okl=audioRecord.read(mybuffer, 0, buffersize);
			mathfre.processSampleData(mybuffer, frequency);
			Log.i(TAG, "大小:"+buffersize);
//			Spectrum spectrum=new Spectrum(mybuffer, frequency);
//			try {
//				newfre=spectrum.getFrequency();
//				Log.i(TAG, "实时频率:"+newfre);
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
			
			long v = 0;
			int db = -90;
			for (int i = 0; i < lylength; i++) {
				v += buffer[i] * buffer[i];
				buffer0[i]=(double)buffer[i]/Short.MAX_VALUE;
				
				
			}
			if (v != 0) {
				db = (int) (20 * Math.log10(Math.sqrt(v/lylength) / 32768f));
				db=db+90;
				dString="声强"+db;
//				dbtext.setText(""+db+"分贝");
			}
			tRsform.ft(buffer0);
			informationch(buffer0);
			Message msg=new Message();
			msg.what=0;
			msg.obj=dString;
			myHandler.sendMessage(msg);
		}
		audioRecord.stop();
	}
	
	public void informationch(double[] ds ){
		double maxv=(ds[0]*ds[0]);
		Log.i(TAG, "是否运行");
//		double maxv0=ds;
		for (int i = 0; i < ds.length; i++) {
//			maxv=Math.abs(ds[0][0]);
			if (maxv<=power(ds[i])) {
				maxv=power(ds[i]);
				fre=(i*frequency)/640;
				Log.i(TAG, "幅值："+maxv);
			}
			if (i==ds.length-1&fre!=0) {
				
				Log.i(TAG, "频率:"+fre);
//				fertext.setText("频率:"+fre+"Hz");
			}
//			Log.i(TAG, "���"+ds[0][i]);
		}
		
//		Log.i(TAG, "频率:"+fre);
		
	}
	
	protected void onPause(){
		super.onPause();
		startre=false;
	}
	@SuppressWarnings("deprecation")
	protected void onDestory(){
		super.onDestroy();
		startre=false;
		mThread.destroy();
	}
	
	private Handler myHandler =new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case 0:
				String str=msg.obj.toString();
				dbtext.setText(str+"分贝");
				break;
			}
			super.handleMessage(msg);
			}
	};
	
	public double power(double i){
		return i*i;
	}
}
