package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classpai.entity.Reply;

import java.util.List;

@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {

    @Select("SELECT r.*, u.user_name AS userName " +
            "FROM reply r LEFT JOIN user u ON r.user_id = u.user_id " +
            "WHERE r.topic_id = #{topicId} " +
            "ORDER BY r.create_time ASC")
    List<Reply> findRepliesByTopicId(@Param("topicId") Long topicId);
}
