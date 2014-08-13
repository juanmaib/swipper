package com.globant.labs.swipper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.globant.labs.swipper.comps.StorageHelper;
import com.globant.labs.swipper.logic.esculturas.Evento;
import com.globant.labs.swipper.net.HTTPScraper;
import com.globant.labs.swipper.utils.Utils;
import com.globant.labs.swipper.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashActivity extends Activity {

	 //how long until we go to the next activity
    protected int _splashTime = 2000; 


	private SharedPreferences prefs;
	
    private Thread splashTread;


	private ProgressBar progressBar;

	private StorageHelper helper = new StorageHelper();
	
	private View unable_to_get_host;
	private View no_connection_loading;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefs = this.getSharedPreferences(Utils.RESISTENCIARTE_APP, 0);
        
        String lastUpdate = prefs.getString(Utils.LAST_UPDATE, "null");
        
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        
        no_connection_loading = findViewById(R.id.no_connection_loading);
		no_connection_loading.setVisibility(View.GONE);

		unable_to_get_host = findViewById(R.id.unable_to_get_host);
		unable_to_get_host.setVisibility(View.GONE);
        // 
        if (!Utils.isNetworkAvailable(this)&& (lastUpdate.equalsIgnoreCase("null")
        		|| Utils.getDaysCountFromLastUpdate(lastUpdate) != 0)) {
			progressBar.setVisibility(View.GONE);
			unable_to_get_host.setVisibility(View.GONE);
			no_connection_loading.setVisibility(View.VISIBLE);
		} else {
			getFirstTime();
		}
        
    }

    private void removeSplash() {
    	// thread for displaying the SplashScreen
        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized(this){

                            //wait 5 sec
                            wait(_splashTime);
                    }

                } catch(InterruptedException e) {}
                finally {
                    finish();

                    //start a new activity
                    Intent i = new Intent();
                    i.setClass(SplashActivity.this, HomeActivity.class);
                            startActivity(i);
                }
            }
        };

        splashTread.start();
	}

	
    
    public void retryFirst(View view) {
		if (Utils.isNetworkAvailable(this)) {
			getFirstTime();
			no_connection_loading.setVisibility(View.GONE);
		} else {
			no_connection_loading.setVisibility(View.VISIBLE);
		}
	}
    
    private void getFirstTime() {
		String lastUpdate = prefs.getString(Utils.LAST_UPDATE, "null");
		if (lastUpdate.equalsIgnoreCase("null")
				|| Utils.getDaysCountFromLastUpdate(lastUpdate) != 0) {
			final DownloadTask downloadTask = new DownloadTask(this);
			downloadTask
					.execute("https://docs.google.com/document/d/1F28T2pwNwElGka7brpQFQAP7Z0CCIqqaFlGN_bG2kjU/export?format=txt");
		} else {
			removeSplash();
		}

	}
    
 // usually, subclasses of AsyncTask are declared inside the activity class.
 	// that way, you can easily modify the UI thread from here
 	private class DownloadTask extends AsyncTask<String, Integer, String> {

 		private Context context;
 		private PowerManager.WakeLock mWakeLock;

 		public DownloadTask(Context context) {
 			this.context = context;
 		}

 		@Override
 		protected String doInBackground(String... sUrl) {
 			InputStream input = null;
 			StringBuilder contenido = new StringBuilder();
 			BufferedReader in = null;
 			try {

 				input = HTTPScraper.getScraper().fecthHtmlGet(sUrl[0]);
 				if (input == null) {
 					throw new Exception("Unable to resolve host");
 				}

 				// download the file
 				InputStream is = input;
 				in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
 				String str;
 				boolean first = true;
 				while ((str = in.readLine()) != null) {
 					if (!first)
 						contenido.append("\n");
 					first = false;
 					contenido.append(str);
 				}

 			} catch (Exception e) {
 				return "error";
 			} finally {
 				try {
 					if (input != null) {
 						input.close();
 					}
 				} catch (IOException ignored) {
 				}

 				try {
 					if (in != null) {
 						in.close();
 					}
 				} catch (IOException ignored) {
 				}

 			}
 			return contenido.toString();
 		}

 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			// take CPU lock to prevent CPU from going off if the user
 			// presses the power button during download
 			PowerManager pm = (PowerManager) context
 					.getSystemService(Context.POWER_SERVICE);
 			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
 					getClass().getName());
 			mWakeLock.acquire();

 			progressBar.setVisibility(View.VISIBLE);

 		}

 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			super.onProgressUpdate(progress);
 			// if we get here, length is known, now set indeterminate to false
 			progressBar.setProgress(progress[0]);
 		}

 		@Override
 		protected void onPostExecute(String result) {
 			mWakeLock.release();

 			progressBar.setVisibility(View.GONE);

 			progressBar.setProgress(100);

 			String[] resultados = result.split("\n");
 			
 			ArrayList<Evento> ls = new ArrayList<Evento>();
 			for (String evento : resultados) {
				if (evento.indexOf("#") == -1){
					String[] eventData = evento.split(",");
					Evento event = new Evento(eventData[0],eventData[1], eventData[2], eventData[3], eventData[4], eventData[5]);
					ls.add(event);
				}
			}
 			
 			//Eventos!
 			if (ls.size() == 0) {
 				if (Utils.isNetworkAvailable(SplashActivity.this)) {
 					unable_to_get_host.setVisibility(View.VISIBLE);
 					getFirstTime();
 				} else {
 					unable_to_get_host.setVisibility(View.GONE);
 					no_connection_loading.setVisibility(View.VISIBLE);
 				}
 				return;
 			}

 			if (helper.isExternalStorageAvailableAndWriteable()) {
 				File root = android.os.Environment
 						.getExternalStorageDirectory();
 				File dir = new File(root.getAbsolutePath() + "/resistenciarte");
 				dir.mkdirs();
 				File file = new File(dir, Utils.FILE_NAME);

 				try {
 					FileOutputStream f = new FileOutputStream(file);
 					PrintWriter pw = new PrintWriter(f);
 					pw.println(result);
 					pw.flush();
 					pw.close();
 					f.close();
 				} catch (Exception e) {
 					getFirstTime();
 				}

 			}

 			SharedPreferences.Editor editor = prefs.edit();
 			editor.putString(Utils.LAST_UPDATE, Utils.getDateAsString());
 			editor.putString(Utils.CONTENTS, result);
 			editor.commit();

 			removeSplash();
 			Toast.makeText(context, "Datos Actualizados!", Toast.LENGTH_SHORT)
 					.show();
 		}
 	}
}
