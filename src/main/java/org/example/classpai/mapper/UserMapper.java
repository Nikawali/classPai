package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classpai.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
