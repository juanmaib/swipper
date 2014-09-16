package com.globant.labs.swipper2.drawer;

import com.globant.labs.swipper2.models.Category;

public class DrawerCategoryItem extends DrawerItem {

	protected Category category;
	protected int color;

	public DrawerCategoryItem(Category cat) {
		category = cat;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public String getName() {
		return category.getName();
	}
	
	public String getId() {
		return category.getId();
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
}
