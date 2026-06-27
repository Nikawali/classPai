package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classpai.entity.SubmitFile;
import org.springframework.stereotype.Repository;


@Mapper
public interface SubmitFileMapper extends BaseMapper<SubmitFile> {
}
