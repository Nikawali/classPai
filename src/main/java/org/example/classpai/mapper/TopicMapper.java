package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classpai.entity.Topic;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    @Select("SELECT t.*, u.user_name AS userName, " +
            "(SELECT COUNT(*) FROM reply r WHERE r.topic_id = t.topic_id) AS replyCount " +
            "FROM topic t LEFT JOIN user u ON t.user_id = u.user_id " +
            "WHERE t.course_id = #{courseId} " +
            "ORDER BY t.is_pinned DESC, t.create_time DESC")
    List<Topic> findTopicsByCourseId(@Param("courseId") Long courseId);
}
