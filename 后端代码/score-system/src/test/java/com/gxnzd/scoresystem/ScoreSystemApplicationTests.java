package com.gxnzd.scoresystem;

import com.alibaba.excel.EasyExcel;
import com.gxnzd.scoresystem.entity.ScoreDetail;
import com.gxnzd.scoresystem.entity.Student;
import com.gxnzd.scoresystem.entity.Teacher;
import com.gxnzd.scoresystem.entity.User;
import com.gxnzd.scoresystem.mapper.LoginMapper;
import com.gxnzd.scoresystem.mapper.StudentMapper;
import com.gxnzd.scoresystem.mapper.TeacherMapper;
import com.gxnzd.scoresystem.service.QuantitativeService;
import com.gxnzd.scoresystem.service.SchoolService;
import com.gxnzd.scoresystem.service.ScoreDetailService;
import com.gxnzd.scoresystem.service.TeacherService;
import com.gxnzd.scoresystem.utils.CalculateScore;
import com.gxnzd.scoresystem.utils.easyExcel.TeacherExcelReadListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class ScoreSystemApplicationTests {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private LoginMapper loginMapper;
    @Autowired
    private ScoreDetailService scoreDetailService;
    @Autowired
    private QuantitativeService quantitativeService;

    @Test
    void contextLoads() {
        List<Teacher> teachers = teacherMapper.getTeacherList();
//        teachers.forEach(System.out::println);
        String name = "E:/aaa.xlsx";
        EasyExcel.write(name,Teacher.class).sheet("aaa").doWrite(teachers);
        List<Student> students = studentMapper.getStudentList();
        students.forEach(System.out::println);
    }

    @Test
    void aa() throws IOException {
        Resource resource = new ClassPathResource("upload/teacher.xlsx");
        TeacherExcelReadListener terl = new TeacherExcelReadListener(schoolService,teacherService);
        EasyExcel.read(resource.getInputStream(), Teacher.class, terl).doReadAll();
        System.out.println(terl.getResult());
    }

    @Test
    void insertTeacher() {
        ScoreDetail scoreDetail = scoreDetailService.getById(1);
        System.out.println("scoreDetail = " + scoreDetail);
        scoreDetail.setQName("A");
        CalculateScore calculateScore = new CalculateScore(quantitativeService, scoreDetail);
        ScoreDetail scoreDetail1 = calculateScore.calculateDetail();

        ScoreDetail scoreDetail2 = new ScoreDetail();
        scoreDetail2.setUsualScore(scoreDetail1.getUsualScore());
        scoreDetail2.setSkillScore(scoreDetail1.getSkillScore());
        scoreDetail2.setDisScore(scoreDetail1.getDisScore());
        System.out.println("scoreDetail1 = " + scoreDetail1);
    }

    @Test
    void test1() {
        String id = "2202020101";
        System.out.println(Long.parseLong(id));
        User user = loginMapper.studentLogin(Long.valueOf(id));
        System.out.println(user);
    }

}
