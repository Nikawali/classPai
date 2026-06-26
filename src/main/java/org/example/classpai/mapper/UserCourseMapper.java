package org.example.classpai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classpai.dto.MemberDTO;
import org.example.classpai.entity.UserCourse;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {

    @Select("<script>SELECT course_id, COUNT(*) AS total FROM user_course WHERE course_id IN " +
            "<foreach collection='courseIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND role = 'student' GROUP BY course_id</script>")
    List<Map<String, Object>> countStudentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    /** 查询课程所有成员信息（联表查用户名等） */
    @Select("SELECT u.user_id, u.user_name, uc.role " +
            "FROM user_course uc JOIN user u ON uc.user_id = u.user_id " +
            "WHERE uc.course_id = #{courseId}")
    List<MemberDTO> findMembersByCourseId(@Param("courseId") Long courseId);
}
