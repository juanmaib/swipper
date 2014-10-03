package com.globant.labs.swipper2.drawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

import com.globant.labs.swipper2.R;

public class CategoryMapper {

	public static DrawerCategoryItem getCategoryDisplay(String category) {
		DrawerCategoryItem cDisplay = new DrawerCategoryItem(category);
		cDisplay.setColor(colorMap.get(category));
		return cDisplay;
	}
	
	@SuppressLint("ResourceAsColor")
	public static List<DrawerCategoryItem> getStaticCategories() {
		ArrayList<DrawerCategoryItem> list = new ArrayList<DrawerCategoryItem>();

		DrawerCategoryItem cDisp = new DrawerCategoryItem("Food");
		cDisp.setColor(R.color.food);
		list.add(cDisp);

		cDisp = new DrawerCategoryItem("Gas");
		cDisp.setColor(R.color.gas);
		list.add(cDisp);

		cDisp = new DrawerCategoryItem("Taxi");
		cDisp.setColor(R.color.taxi);
		list.add(cDisp);
		
		cDisp = new DrawerCategoryItem("Car Rental");
		cDisp.setColor(R.color.carrental);
		list.add(cDisp);

		cDisp = new DrawerCategoryItem("Lodging");
		cDisp.setColor(R.color.lodging);
		list.add(cDisp);
				
		return list;
	}
	
	private static final Map<String, Integer> colorMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Lodging", R.color.lodging);
		map.put("Car Rental", R.color.carrental);
		map.put("Food", R.color.food);
		map.put("Gas", R.color.gas);
		map.put("Taxi", R.color.taxi);
		colorMap = map;
	}
	
	private static final Map<String, Integer> markerMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Lodging", R.drawable.marker_lodging);
		map.put("Car Rental", R.drawable.marker_carrental);
		map.put("Food", R.drawable.marker_food);
		map.put("Gas", R.drawable.marker_gas);
		map.put("Taxi", R.drawable.marker_taxi);
		markerMap = map;
	}
	
	private static final Map<String, Integer> iconMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Lodging", R.drawable.icon_lodging);
		map.put("Car Rental", R.drawable.icon_carrental);
		map.put("Food", R.drawable.icon_food);
		map.put("Gas", R.drawable.icon_gas);
		map.put("Taxi", R.drawable.icon_taxi);
		iconMap = map;
	}
	
	private static final Map<String, Integer> textMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Lodging", R.string.lodging);
		map.put("Car Rental", R.string.carrental);
		map.put("Food", R.string.food);
		map.put("Gas", R.string.gas);
		map.put("Taxi", R.string.taxi);
		textMap = map;
	}
	
	public static int getCategoryColor(String category) {
		return colorMap.get(category);
	}
	
	public static int getCategoryMarker(String category) {
		return markerMap.get(category);
	}
	
	public static int getCategoryIcon(String category) {
		return iconMap.get(category);
	}
	
	public static int getCategoryText(String category) {
		return textMap.get(category);
	}
}
