package com.gxnzd.scoresystem;

import com.gxnzd.scoresystem.utils.easyExcel.TeacherExcelReadListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@MapperScan("com.gxnzd.scoresystem.mapper")
public class ScoreSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreSystemApplication.class, args);
    }

}
