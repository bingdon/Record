package com.bing.fre;

public class Mathfre {
	static {
	System.loadLibrary("FFT");
}
	public native double processSampleData(byte[] sample, int sampleRate);
}
