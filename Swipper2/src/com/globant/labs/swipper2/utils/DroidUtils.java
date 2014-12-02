package com.globant.labs.swipper2.utils;

import android.content.Context;

public class DroidUtils {

	public static int dpToPx(float dp, Context context) {
		float scale = context.getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (dp * scale + 0.5f);
		return dpAsPixels;
	}

}
