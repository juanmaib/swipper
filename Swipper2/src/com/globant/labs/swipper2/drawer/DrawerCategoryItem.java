package com.globant.labs.swipper2.drawer;


public class DrawerCategoryItem extends DrawerItem {

	protected String category;
	protected int color;

	public DrawerCategoryItem(String cat) {
		category = cat;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getName() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
}
