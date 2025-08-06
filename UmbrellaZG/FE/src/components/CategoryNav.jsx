import React from 'react';
import { Button } from '@/components/ui/button';

const CategoryNav = ({ categories = [], activeCategory, onCategoryChange }) => {
  // 确保categories是数组类型
  const safeCategories = Array.isArray(categories) ? categories : [];

  return (
    <div className="flex flex-wrap gap-2 mb-8">
      <Button
        variant={activeCategory === null ? 'default' : 'outline'}
        onClick={() => onCategoryChange(null)}
      >
        全部
      </Button>
      {safeCategories.map((category) => (
        <Button
          key={category.id}
          variant={activeCategory === category.id ? 'default' : 'outline'}
          onClick={() => onCategoryChange(category.id)}
        >
          {category.name}
        </Button>
      ))}
    </div>
  );
};

export default CategoryNav;
