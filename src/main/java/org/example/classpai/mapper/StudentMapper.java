package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classpai.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
