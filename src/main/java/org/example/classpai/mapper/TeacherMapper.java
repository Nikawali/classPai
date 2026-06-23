package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classpai.entity.Teacher;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
