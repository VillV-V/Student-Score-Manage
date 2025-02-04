package com.gxnzd.scoresystem.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Quantitative;
import com.gxnzd.scoresystem.entity.Score;
import com.gxnzd.scoresystem.entity.ScoreDetail;
import com.gxnzd.scoresystem.service.QuantitativeService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculateScore {

    private QuantitativeService quantitativeService;

    //成绩评价标准
    private String qName;

    //课堂表现成绩
    private float usualScore;
    private String usualItem;

    //技能成绩
    private float skillScore;
    private String skillItem;

    //纪律成绩
    private float disScore;
    private String disItem;

    //总成绩
    private float totalScore;

    public CalculateScore(QuantitativeService quantitativeService, ScoreDetail scoreDetail) {
        this.quantitativeService = quantitativeService;
        this.qName = scoreDetail.getQName();
        this.usualItem = scoreDetail.getUsualItem();
        this.skillItem = scoreDetail.getSkillItem();
        this.disItem = scoreDetail.getDisItem();
    }

    public CalculateScore(QuantitativeService quantitativeService, Score score) {
        this.quantitativeService = quantitativeService;
        this.qName = score.getQName();
        this.usualScore = score.getUsualScore();
        this.skillScore = score.getSkillScore();
        this.disScore = score.getDisScore();
    }

    //计算平时成绩总表的总成绩
    public float calculate() {
        QueryWrapper<Quantitative> wrapper = new QueryWrapper<>();
        wrapper.eq("q_name", qName);
        Quantitative q = quantitativeService.getOne(wrapper);
        this.totalScore = (usualScore * ((float) q.getUsualScore() / 100)) + (skillScore * ((float) q.getSkillScore() / 100)) + (disScore * ((float) q.getDisScore() / 100));
        return Float.parseFloat(String.format("%.2f", totalScore));
    }

    //计算平时成绩明细表的总成绩
    public ScoreDetail calculateDetail() {
        QueryWrapper<Quantitative> wrapper = new QueryWrapper<>();
        wrapper.eq("q_name", qName);
        Quantitative q = quantitativeService.getOne(wrapper);
        float x = calculateScore(q.getUsualScore(), this.usualItem);
        float y = calculateScore(q.getSkillScore(), this.skillItem);
        float z = calculateScore(q.getDisScore(), this.disItem);
        ScoreDetail scoreDetail = new ScoreDetail();
        if(x>100) {
            x = 100;
        }
        if(y>100) {
            y = 100;
        }
        if(z>100) {
            z = 100;
        }
        scoreDetail.setUsualScore(x);
        scoreDetail.setSkillScore(y);
        scoreDetail.setDisScore(z);
        scoreDetail.setTotalScore((x * ((float) q.getUsualScore() / 100)) + (y * ((float) q.getSkillScore() / 100)) + (z * ((float) q.getDisScore() / 100)));
        return scoreDetail;
    }

    //解析字符串，获得成绩
    private float calculateScore(int discount, String str) {
        if (str.equals("无")) {
            return 0;
        }
        float x = 0;
        try {
            x = Float.parseFloat(str);
        } catch (Exception e) {
            String[] arr = str.split("，");
            for (String s : arr) {
                String[] arr2 = s.split("：");
                x += Float.parseFloat(arr2[1]);
            }
        }
        return Float.parseFloat(String.format("%.2f", x));
    }

}