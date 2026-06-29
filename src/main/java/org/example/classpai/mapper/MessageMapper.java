package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.example.classpai.entity.Message;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /** 获取某作业未提交的学生ID列表 */
    @Select("SELECT uc.user_id FROM user_course uc " +
            "WHERE uc.course_id = (SELECT course_id FROM homework WHERE hw_id = #{hwId}) " +
            "AND uc.role = 'student' " +
            "AND uc.user_id NOT IN (" +
            "  SELECT DISTINCT student_id FROM homework_submit WHERE hw_id = #{hwId}" +
            ")")
    List<Long> findUnsubmittedStudentIds(@Param("hwId") Long hwId);

    /** 查询当前用户的所有私信（含发送者和接收者姓名） */
    @Select("SELECT m.*, su.user_name AS sender_name, ru.user_name AS receiver_name " +
            "FROM message m " +
            "LEFT JOIN user su ON m.sender_id = su.user_id " +
            "LEFT JOIN user ru ON m.receiver_id = ru.user_id " +
            "WHERE m.receiver_id = #{userId} OR m.sender_id = #{userId} " +
            "ORDER BY m.create_time DESC")
    @Results({
            @Result(column = "sender_name", property = "senderName"),
            @Result(column = "receiver_name", property = "receiverName")
    })
    List<Message> findMessagesByUserId(@Param("userId") Long userId);
}
