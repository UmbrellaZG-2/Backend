import React from 'react';
import { Badge } from '@/components/ui/badge';

const TagCloud = ({ tags = [], onTagClick }) => {
  // 确保tags是数组类型
  const safeTags = Array.isArray(tags) ? tags : [];

  return (
    <div className="mb-8">
      <h3 className="text-lg font-semibold mb-4">标签</h3>
      <div className="flex flex-wrap gap-2">
        {safeTags.map((tag) => (
          <Badge 
            key={tag.id} 
            variant="secondary" 
            className="cursor-pointer hover:bg-blue-200 transition-colors"
            onClick={() => onTagClick && onTagClick(tag)}
          >
            {tag.name} ({tag.articleCount})
          </Badge>
        ))}
      </div>
    </div>
  );
};

export default TagCloud;
