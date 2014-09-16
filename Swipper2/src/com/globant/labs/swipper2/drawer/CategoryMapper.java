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
		cat.setId("54049180a90a2a0000a37d91");
		DrawerCategoryItem cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.food);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Gas");
		cat.setId("54049181a90a2a0000a37db2");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.gas);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Taxi");
		cat.setId("54049193a90a2a0000a38206");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.taxi);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Car Rental");
		cat.setId("54049180a90a2a0000a37d8f");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.carrental);
		list.add(cDisp);
		
		cat = new Category();
		cat.setName("Lodging");
		cat.setId("5404917fa90a2a0000a37d8b");
		cDisp = new DrawerCategoryItem(cat);
		cDisp.setColor(R.color.lodging);
		list.add(cDisp);
				
		return list;
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
