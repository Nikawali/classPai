package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.classpai.entity.Submission;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
}
