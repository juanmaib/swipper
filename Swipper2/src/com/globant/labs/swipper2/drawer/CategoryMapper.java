package com.globant.labs.swipper2.drawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import com.globant.labs.swipper2.R;
import com.globant.labs.swipper2.models.Category;

public class CategoryMapper {

	public static DrawerCategoryItem getCategoryDisplay(Category category) {
		DrawerCategoryItem cDisplay = new DrawerCategoryItem(category);
		cDisplay.setColor(colorMap.get(category.getName()));
		return cDisplay;
	}
	
	@SuppressLint("ResourceAsColor")
	public static List<DrawerCategoryItem> getStaticCategories() {
		ArrayList<DrawerCategoryItem> list = new ArrayList<DrawerCategoryItem>();
		
		Category cat = new Category();
		cat.setName("Food");
		cat.setId("5419c50d4405590000442957");
		DrawerCategoryItem cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.food);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Gas");
		cat.setId("5419c50e4405590000442978");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.gas);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Taxi");
		cat.setId("5419c5174405590000442dc8");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.taxi);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Car Rental");
		cat.setId("5419c50d4405590000442955");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.carrental);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Lodging");
		cat.setId("5419c50d4405590000442951");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.lodging);
		list.add(cDisp);
				
		return list;
	}
	
	private static final Map<String, Integer> colorMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("5419c50d4405590000442951", R.color.lodging);
		map.put("5419c50d4405590000442955", R.color.carrental);
		map.put("5419c50d4405590000442957", R.color.food);
		map.put("5419c50e4405590000442978", R.color.gas);
		map.put("5419c50f44055900004429d8", R.color.lodging);
		map.put("5419c5174405590000442dc8", R.color.taxi);
		colorMap = map;
	}
	
	private static final Map<String, Integer> markerMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("5419c50d4405590000442951", R.drawable.marker_lodging);
		map.put("5419c50d4405590000442955", R.drawable.marker_carrental);
		map.put("5419c50d4405590000442957", R.drawable.marker_food);
		map.put("5419c50e4405590000442978", R.drawable.marker_gas);
		map.put("5419c50f44055900004429d8", R.drawable.marker_lodging);
		map.put("5419c5174405590000442dc8", R.drawable.marker_taxi);
		markerMap = map;
	}
	
	private static final Map<String, Integer> iconMap;
	static {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("5419c50d4405590000442951", R.drawable.icon_lodging);
		map.put("5419c50d4405590000442955", R.drawable.icon_carrental);
		map.put("5419c50d4405590000442957", R.drawable.icon_food);
		map.put("5419c50e4405590000442978", R.drawable.icon_gas);
		map.put("5419c50f44055900004429d8", R.drawable.icon_lodging);
		map.put("5419c5174405590000442dc8", R.drawable.icon_taxi);
		iconMap = map;
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
}
