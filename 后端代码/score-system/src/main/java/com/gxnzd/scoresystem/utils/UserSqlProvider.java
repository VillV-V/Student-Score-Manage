package com.gxnzd.scoresystem.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.gxnzd.scoresystem.entity.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

public class UserSqlProvider {

    public String searchStudent(@Param("ew") Wrapper<Student> queryWrapper) {
        // 使用 MyBatis 提供的 SQL 构造器来生成 SQL
        String str = charReplace(queryWrapper.getCustomSqlSegment());
        String sql = new SQL() {{
            SELECT("s.*, c.class_name, m.major_name, s1.school_name");
            FROM("student s");
            JOIN("class c on s.class_id = c.class_id");
            JOIN("major m on c.major_id = m.major_id");
            JOIN("school s1 on m.school_id = s1.school_id");
            if (str != "") {
                WHERE(str);
            }
            ORDER_BY("s.student_id asc");
        }}.toString();
        return sql;
    }

    public String searchTeacher(@Param("ew") Wrapper<Teacher> queryWrapper) {
        // 使用 MyBatis 提供的 SQL 构造器来生成 SQL
        String str = charReplace(queryWrapper.getCustomSqlSegment());
        String sql = new SQL() {{
            SELECT("t.*, s.school_name");
            FROM("teacher t");
            JOIN("school s on t.school_id = s.school_id");
            if (str != "") {
                WHERE(str);
            }
            ORDER_BY("t.teacher_id asc");
        }}.toString();
        return sql;
    }

    public String searchClass(@Param("ew") Wrapper<StuClass> queryWrapper) {
        // 使用 MyBatis 提供的 SQL 构造器来生成 SQL
        String str = charReplace(queryWrapper.getCustomSqlSegment());
        String sql = new SQL() {{
            SELECT("c.*, m.major_name");
            FROM("class c");
            JOIN("major m on c.major_id = m.major_id");
            if (str != "") {
                WHERE(str);
            }
            ORDER_BY("c.class_id asc");
        }}.toString();
        return sql;
    }

    public String searchMajor(@Param("ew") Wrapper<Major> queryWrapper) {
        // 使用 MyBatis 提供的 SQL 构造器来生成 SQL
        String str = charReplace(queryWrapper.getCustomSqlSegment());
        String sql = new SQL() {{
            SELECT("m.*, s.school_name");
            FROM("major m");
            JOIN("school s on m.school_id = s.school_id");
            if (str != "") {
                WHERE(str);
            }
            ORDER_BY("m.major_id asc");
        }}.toString();
        return sql;
    }

    public String searchCourse(@Param("ew") Wrapper<Course> queryWrapper) {
        // 使用 MyBatis 提供的 SQL 构造器来生成 SQL
        String str = charReplace(queryWrapper.getCustomSqlSegment());
        String sql = new SQL() {{
            SELECT("c.*, q.q_id, q.q_name, s.school_id, s.school_name");
            FROM("course c");
            JOIN("quantitative q on c.q_id = q.q_id");
            JOIN("school s on c.school_id = s.school_id");
            if (str != "") {
                WHERE(str);
            }
            ORDER_BY("c.course_id asc");
        }}.toString();
        return sql;
    }

    public String searchCourseByTId(@Param("ew") Wrapper<Course> queryWrapper) {
        // 使用 MyBatis 提供的 SQL 构造器来生成 SQL
        String str = charReplace(queryWrapper.getCustomSqlSegment());
        String sql = new SQL() {{
            SELECT("DISTINCT c.*, s.school_name, q.q_name");
            FROM("class_course cc");
            JOIN("course c on cc.course_id = c.course_id");
            JOIN("school s on c.school_id = s.school_id");
            JOIN("quantitative q on c.q_id = q.q_id");
            if (str != "") {
                WHERE(str);
            }
            ORDER_BY("c.course_id asc");
        }}.toString();
        return sql;
    }

    public String charReplace(String str) {
        return str.replace("WHERE (", "").replace(")", "");
    }

}
