package me.relex.camerafilter.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Util {
	public static final int PREVIEW_PIX_FORMAT_NV21	= 0;
	public static final int PREVIEW_PIX_FORMAT_YV12	= 1;
	
	public static final int ENCODE_PIX_FORMAT_YUV420P	= 0;
	public static final int ENCODE_PIX_FORMAT_NV12	= 1;
	
	private final static int ONE_MAGABYTE = 1048576;
	private final static int ONE_KILOBYTE = 1024;
	
	private final static String PREF_NAME = "settings";
	
	// int to byte 
    public static byte[] IntToByte(int number) { 
        int temp = number; 
        byte[] b = new byte[4]; 
        for (int i = 0; i < b.length; i++) { 
            b[i] = new Long(temp & 0xff).byteValue();
            temp = temp >> 8; // right shift 8
        } 
        return b; 
    }
    
    // byte to int 
    public static int ByteToInt(byte[] b) { 
        int s = 0; 
        int s0 = b[0] & 0xff;// MSB
        int s1 = b[1] & 0xff; 
        int s2 = b[2] & 0xff; 
        int s3 = b[3] & 0xff; 
 
        // s0 unchange
        s1 <<= 8; 
        s2 <<= 16; 
        s3 <<= 24;  
        s = s0 | s1 | s2 | s3;
        return s;
    }
    
    public static String getFileSize(long size) {
	    String strFilesize;
		if (size > ONE_MAGABYTE)
			strFilesize = String.format("%.3f MB",
					(float) size / (float) ONE_MAGABYTE);
		else if (size > ONE_KILOBYTE)
			strFilesize = String.format("%.3f kB",
					(float) size / (float) ONE_KILOBYTE);
		else
			strFilesize = String.format("%d Byte", size);
		
		return strFilesize;
    }
    
    public static boolean writeSettingsInt(Context ctx, String key, int value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // create it if NOT exist
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putInt(key, value);
    	return editor.commit();
	}
	
	public static int readSettingsInt(Context ctx, String key) {
		return readSettingsInt(ctx, key, 0);
	}
	
	public static int readSettingsInt(Context ctx, String key, int default_value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // create it if NOT exist
    	return settings.getInt(key, default_value);
	}
	
    public static boolean writeSettingsBoolean(Context ctx, String key, boolean isOn) {
		SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // create it if NOT exist
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean(key, isOn);
    	return editor.commit();
	}
	
    public static Boolean readSettingsBoolean(Context ctx, String key) {
    	return readSettingsBoolean(ctx, key, false);
	}
    
	public static Boolean readSettingsBoolean(Context ctx, String key, boolean default_value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // create it if NOT exist
    	return settings.getBoolean(key, default_value);
	}
}
