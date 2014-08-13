package com.globant.labs.swipper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import com.globant.labs.swipper.fragments.sections.EsculturaItem;
import com.globant.labs.swipper.logic.esculturas.Evento;
import com.globant.labs.swipper.net.FileCache;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

public class Utils {

	public static final String LAT1 = "lat1";
	public static final String LAT2 = "lat2";
	public static final String LONG1 = "long1";
	public static final String LONG2 = "long2";

	public static final int GPS_NOT_TURNED_FF = 3;
	public static final String RESISTENCIARTE_APP = "com.jmv.codigociudadano.resistenciarte";
	public static final String FIRST_TIME = RESISTENCIARTE_APP + "first_time";
	public static final String LAST_UPDATE = "last_update";
	public static final String LAST_PAGE = "last_page";
	public static final String FILE_NAME = "systemTB";
	public static final String CONTENTS = "contents";

	public static String EMPRESA_SELECTED = "empresa_selected";
	public static String CURRENT_ESCULTURA = "escultura_actual";
	public static final int GPS_NOT_TURNED_ON = 2;
	public static final String LAST_NID = "last_nid";

	public static ArrayList<Evento> getWebLocations(Context context) {
		ArrayList<Evento> list = new ArrayList<Evento>();

		try {

			// Find the directory for the SD Card using the API
			// *Don't* hardcode "/sdcard"
			File sdcard = Environment.getExternalStorageDirectory();
			File dir = new File(sdcard.getAbsolutePath() + "/resistenciarte");
			// Get the text file
			File file = new File(dir, FILE_NAME);

			StringBuilder text = new StringBuilder();
			String contents;
			if (file.exists()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					while ((line = br.readLine()) != null) {
						text.append(line);
						text.append('\n');
					}
				} catch (IOException e) {
					// You'll need to add proper error handling here
				}
				contents = text.toString();
			} else {
				SharedPreferences prefs = context.getSharedPreferences(
						Utils.RESISTENCIARTE_APP, 0);
				contents = prefs.getString(Utils.CONTENTS, "error");
			}

			if (contents.equalsIgnoreCase("error")) {
				return list;
			}

			String[] linesText = contents.split("\n");

			int size = linesText.length;
			for (int i = 0; i < size; i++) {
				String string = linesText[i];
				if (string.indexOf("#")==-1){
					String[] eventData = string.split(",");
					Evento event = new Evento(eventData[0],eventData[1], eventData[2], eventData[3], eventData[4], eventData[5]);
					DateTimeFormatter dateStringFormat = DateTimeFormat
							.forPattern("dd-MM-yyyy HH:mm");
					DateTime time = dateStringFormat.parseDateTime(String
							.valueOf(
									event.getDate()
											.replaceAll("\\s+", "")
											+ " "
											+ event.getHora_fin()
													.replaceAll("\\s+", ""))
							.trim());
					
					DateTime current = new DateTime();
					if (time.isAfter(current.getMillis())){
						list.add(event);
					}
				}
				
			}

		} catch (Exception e) {
			return new ArrayList<Evento>();
		}

		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T tranformAccordingType(Class<T> type, Object object) {

		if (type.isAssignableFrom(String.class)) {
			return (T) object;
		} else if (type.isAssignableFrom(Date.class)) {
			String[] date = String.valueOf(object).split(
					Constants.DATE_SEPARATOR);
			int year = Integer.parseInt(date[2].trim());
			int month = Integer.parseInt(date[1].trim());
			int day = Integer.parseInt(date[0].trim());
			Date d;
			Calendar cal = GregorianCalendar.getInstance();
			cal.set(year, month, day);
			d = cal.getTime();
			return (T) d;
		} else if (type.isAssignableFrom(double.class)
				|| (type.isAssignableFrom(Double.class))) {
			return (T) Double.valueOf(String.valueOf(object).trim()
					.replaceAll(Constants.COMMA, Constants.DOT));
		} else if (type.isAssignableFrom(float.class)
				|| type.isAssignableFrom(Float.class)) {
			return (T) Double.valueOf(String.valueOf(object).trim()
					.replaceAll(Constants.COMMA, Constants.DOT));
		} else if (type.isAssignableFrom(int.class)
				|| type.isAssignableFrom(Integer.class)) {
			// takeout all spaces
			return (T) Integer.valueOf(String.valueOf(object).trim());
		}
		return null;
		// To change body of generated methods, choose Tools | Templates.
	}

	public static <T> T[] copyArray(T[] vector) {
		T[] another = (T[]) Array.newInstance(vector.getClass()
				.getComponentType(), vector.length);
		System.arraycopy(vector, 0, another, 0, vector.length);
		return another;
	}

	public static Bitmap getRefelection(Bitmap image) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {

			// The gap we want between the reflection and the original image
			final int reflectionGap = 0;

			// Get your bitmap from drawable folder
			Bitmap originalImage = image;

			int width = originalImage.getWidth();
			int height = originalImage.getHeight();

			// This will not scale but will flip on the Y axis
			Matrix matrix = new Matrix();
			matrix.preScale(1, -1);

			/*
			 * Create a Bitmap with the flip matix applied to it. We only want
			 * the bottom half of the image
			 */

			Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
					height / 2, width, height / 2, matrix, false);

			// Create a new bitmap with same width but taller to fit reflection
			Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
					(height + height / 2), Config.ARGB_8888);
			// Create a new Canvas with the bitmap that's big enough for
			// the image plus gap plus reflection
			Canvas canvas = new Canvas(bitmapWithReflection);
			// Draw in the original image
			canvas.drawBitmap(originalImage, 0, 0, null);
			// Draw the reflection Image
			canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

			// Create a shader that is a linear gradient that covers the
			// reflection
			Paint paint = new Paint();
			LinearGradient shader = new LinearGradient(0,
					originalImage.getHeight(), 0,
					bitmapWithReflection.getHeight() + reflectionGap,
					0x99ffffff, 0x00ffffff, TileMode.CLAMP);
			// Set the paint to use this shader (linear gradient)
			paint.setShader(shader);
			// Set the Transfer mode to be porter duff and destination in
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			// Draw a rectangle using the paint with our linear gradient
			canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
					+ reflectionGap, paint);
			if (originalImage != null && originalImage.isRecycled()) {
				originalImage.recycle();
				originalImage = null;
			}
			if (reflectionImage != null && reflectionImage.isRecycled()) {
				reflectionImage.recycle();
				reflectionImage = null;
			}
			return bitmapWithReflection;
		} else {
			return null;
		}

	}

	public static String getSetMethod(String fieldName) {
		// TODO Auto-generated method stub
		String firstWithCapitalLetter = fieldName.toUpperCase().substring(0, 1);
		String restOfMethodName = fieldName.substring(1, fieldName.length());
		return Constants.SET + firstWithCapitalLetter + restOfMethodName;
	}

	public static String getGetMethod(String fieldName) {
		// TODO Auto-generated method stub
		String firstWithCapitalLetter = fieldName.toUpperCase().substring(0, 1);
		String restOfMethodName = fieldName.substring(1, fieldName.length());
		String methodName = Constants.GET + firstWithCapitalLetter
				+ restOfMethodName;
		return methodName;
	}

	public static void shareEscultura(Activity activity, EsculturaItem escultura) {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		FileCache cache = new FileCache(activity);
		Uri uri = Uri.fromFile(cache.getFile(escultura.getImage()));
		String shareBody = " Buscala en Google Play, Resistenciarte!!";
		sharingIntent.setType("image/*");

		String toShare = Constants.REPLACCER.replaceFirst(
				Constants.PATTERN_REPLACE, escultura.getEscultura().getTitle()
						.trim());

		sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareBody);

		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, toShare);

		activity.startActivity(Intent.createChooser(sharingIntent,
				"Compartilo en..."));
	}
	
	public static void shareNovedad(Activity activity, Evento evento) {
		Intent sharingIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "Voy a ir al evento:"+evento.getNombre()+", Vos venis? #codigociudadano #resistenciarte";
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"La mejor forma de encontrar esculturas en Resistencia! Usa esta app!!");
		sharingIntent
				.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		activity.startActivity(Intent.createChooser(sharingIntent,
				"Compartilo en..."));
	}

	public static Uri getImageUri(Context inContext, Bitmap image) {
		String path = Images.Media.insertImage(inContext.getContentResolver(),
				image, "Title", null);
		return Uri.parse(path);
	}

	public static String toDecimalFormat(Double d) {
		return new DecimalFormat("##.##").format(d);
	}

	static void handleException(Exception ex) {
		System.out.println(Constants.EXCEPCION_OCURRIDA_
				+ ex.getClass().getName() + " " + ex.getMessage());
	}

	public static long getDaysCountFromLastUpdate(String lastUpdate) {
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy",
				Locale.getDefault());
		Date startDate;
		try {
			startDate = formatter.parse(lastUpdate);
		} catch (ParseException e) {
			return 1;
		}
		Date today = new Date();

		long startTime = startDate.getTime();
		long endTime = today.getTime();

		long diffTime = endTime - startTime;

		long diffDays = diffTime / (1000 * 60 * 60 * 24);

		return diffDays;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	public static String getDateAsString() {
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy",
				Locale.getDefault());
		return formatter.format(new Date());
	}

	public static void addTouchEffectoToButtons(View container) {
		ArrayList<View> touchables = container.getTouchables();

		final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

		for (View view : touchables) {
			if (view instanceof Button) {
				view.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN: {
							v.startAnimation(buttonClick);
							Drawable drw = v.getBackground();
							if (drw != null) {
								drw.setColorFilter(0xe0f47521,
										PorterDuff.Mode.SRC_ATOP);
							}
							v.invalidate();
							break;
						}
						case MotionEvent.ACTION_UP: {
							Drawable drw = v.getBackground();
							if (drw != null) {
								drw.clearColorFilter();
							}
							v.invalidate();
							break;
						}
						}
						return false;
					}

				});
			}
		}
	}

	public static <T> void extractFromResponseToObject(T localReg,
			JSONObject jsonObject) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, JSONException,
			NoSuchMethodException {
		Class<?> classToUse = localReg.getClass();
		Field[] fields = classToUse.getDeclaredFields();
		if (fields.length == 0) {
			classToUse = localReg.getClass().getSuperclass();
			fields = classToUse.getDeclaredFields();
		}
		for (Field f : fields) {
			Method method;
			method = classToUse.getDeclaredMethod(
					Utils.getSetMethod(f.getName()), f.getType());
			method.invoke(
					localReg,
					Utils.tranformAccordingType(f.getType(),
							jsonObject.get(f.getName())));
		}
	}

	public static String toCamelCase(String s){
        String[] parts = s.split(" ");
        String camelCaseString = "";
        for (String part : parts){
            if(part!=null && part.trim().length()>0)
           camelCaseString = camelCaseString + toProperCase(part);
            else
                camelCaseString=camelCaseString+part+" ";   
        }
        return camelCaseString;
     }

     static String toProperCase(String s) {
         String temp=s.trim();
         String spaces="";
         if(temp.length()!=s.length())
         {
         int startCharIndex=s.charAt(temp.indexOf(0));
         spaces=s.substring(0,startCharIndex);
         }
         temp=temp.substring(0, 1).toUpperCase() +
         spaces+temp.substring(1).toLowerCase()+" ";
         return temp;

     }
}
