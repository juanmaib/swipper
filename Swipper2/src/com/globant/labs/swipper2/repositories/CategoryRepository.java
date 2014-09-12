package com.globant.labs.swipper2.repositories;

import com.globant.labs.swipper2.models.Category;
import com.strongloop.android.loopback.ModelRepository;

public class CategoryRepository extends ModelRepository<Category> {
	public CategoryRepository() {
		super("category", "categories", Category.class);
	}
	
}
