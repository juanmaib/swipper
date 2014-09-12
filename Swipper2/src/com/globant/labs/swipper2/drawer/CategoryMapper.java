package com.globant.labs.swipper2.drawer;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Category;

public class CategoryMapper {

	public static CategoryDisplay getCategoryDisplay(Category category) {
		CategoryDisplay cDisplay = new CategoryDisplay(category);
		cDisplay.setColor(colorMap.get(category.getName()));
		return cDisplay;
	}
	
	private static final Map<String, Integer> colorMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Lodging", R.color.lodging);
		map.put("Car Rental", R.color.carrental);
		map.put("Restaurants", R.color.food);
		map.put("Gas Stations", R.color.gas);
		map.put("Lodging Small", R.color.lodging);
		map.put("Taxi", R.color.taxi);
		colorMap = map;
	}
	
	
}
