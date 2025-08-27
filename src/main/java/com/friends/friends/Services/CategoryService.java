package com.friends.friends.Services;

import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Category.CategoryDto;
import com.friends.friends.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;

    
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(Category::toDto)
                .collect(Collectors.toList());
    }
}
