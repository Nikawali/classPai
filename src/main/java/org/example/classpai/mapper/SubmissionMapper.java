package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classpai.entity.Submission;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    /** 学生：查哪些作业已提交 */
    @Select("<script>SELECT hw_id FROM homework_submit WHERE hw_id IN " +
            "<foreach collection='hwIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND student_id = #{studentId}</script>")
    List<Long> findSubmittedHwIds(@Param("hwIds") List<Long> hwIds, @Param("studentId") Long studentId);

    /** 教师：按作业统计已交人数 */
    @Select("<script>SELECT hw_id, COUNT(*) AS total FROM homework_submit WHERE hw_id IN " +
            "<foreach collection='hwIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY hw_id</script>")
    List<Map<String, Object>> countSubmitByHwIds(@Param("hwIds") List<Long> hwIds);

    /** 教师：按作业统计已批人数（score 不为 NULL） */
    @Select("<script>SELECT hw_id, COUNT(*) AS total FROM homework_submit WHERE hw_id IN " +
            "<foreach collection='hwIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND score IS NOT NULL GROUP BY hw_id</script>")
    List<Map<String, Object>> countGradedByHwIds(@Param("hwIds") List<Long> hwIds);

    /** 教师批阅页：获取课程所有学生及其对该作业的提交状态 */
    @Select("SELECT u.user_id AS student_id, u.user_name, " +
            "hs.submit_id, hs.submit_content, hs.score, hs.submit_time, " +
            "CASE WHEN hs.submit_id IS NOT NULL THEN 1 ELSE 0 END AS submitted " +
            "FROM user_course uc " +
            "JOIN user u ON uc.user_id = u.user_id " +
            "LEFT JOIN homework_submit hs ON hs.student_id = uc.user_id AND hs.hw_id = #{hwId} " +
            "WHERE uc.course_id = (SELECT course_id FROM homework WHERE hw_id = #{hwId}) " +
            "AND uc.role = 'student' " +
            "ORDER BY u.user_id")
    List<Submission> findGradingListByHwId(@Param("hwId") Long hwId);
}
