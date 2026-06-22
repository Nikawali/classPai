package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classpai.entity.Course;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
