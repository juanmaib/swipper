package com.globant.labs.swipper2.drawer;

import android.graphics.Color;

import com.globant.labs.swipper2.models.Category;

public class CategoryDisplay {

	protected Category category;
	protected int color;
	protected boolean checked;
	
	public CategoryDisplay(Category cat) {
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
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
}
