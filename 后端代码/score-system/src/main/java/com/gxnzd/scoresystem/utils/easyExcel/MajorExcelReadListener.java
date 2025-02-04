package com.gxnzd.scoresystem.utils.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Major;
import com.gxnzd.scoresystem.entity.School;
import com.gxnzd.scoresystem.service.MajorService;
import com.gxnzd.scoresystem.service.SchoolService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MajorExcelReadListener extends AnalysisEventListener<Major> {

    private SchoolService schoolService;

    private MajorService majorService;

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存的数据
     */
    private List<Major> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * Excel总行数
     */
    private int totalRows = 0;

    /**
     * 通过校验的总数
     */
    private int successRows = 0;

    /**
     * 错误信息列表
     */
    private final List<String> errorMsgList = new ArrayList<>(BATCH_COUNT);

    private Integer schoolId = null;

    public MajorExcelReadListener(SchoolService schoolService, MajorService majorService) {
        this.schoolService = schoolService;
        this.majorService = majorService;
    }

    @Override
    public void invoke(Major major, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", major);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Integer majorId = major.getMajorId();
        String majorName = major.getMajorName();
        major.setSchoolId(null);
        String schoolName = major.getSchoolName();

        // 只有全部校验通过的对象才能被添加到下一步
        if(majorId == null) {
            if (nameValid(rowIndex, majorName) && schoolValid(rowIndex, schoolName)) {
                if(schoolId != null) {
                    major.setSchoolId(schoolId);
                }
                cachedDataList.add(major);
                this.schoolId = null;
            }
        } else {
            if(!majorIdValid(rowIndex, majorId)) {
                if (nameValid(rowIndex, majorName) && schoolValid(rowIndex, schoolName)) {
                    if(schoolId != null) {
                        major.setSchoolId(schoolId);
                    }
                    cachedDataList.add(major);
                    this.schoolId = null;
                }
            } else {
                // 任意一项有值时也要进行校验
                if(!StringUtils.isBlank(majorName)) {
                    if(!nameValid(rowIndex, majorName)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(schoolName)) {
                    if(!schoolValid(rowIndex, schoolName)) {
                        return;
                    }
                }

                if(schoolId != null) {
                    major.setSchoolId(schoolId);
                }
                cachedDataList.add(major);
                this.schoolId = null;
            }
        }
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            majorService.saveOrUpdateBatch(cachedDataList);
            successRows += cachedDataList.size();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        successRows += cachedDataList.size();
        log.info("所有数据解析完成！全部校验通过的数据有{}条", successRows);
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库saveData();
        if(!cachedDataList.isEmpty()) {
            majorService.saveOrUpdateBatch(cachedDataList);
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof RuntimeException) {
            throw exception;
        }
        int index = context.readRowHolder().getRowIndex() + 1;
        errorMsgList.add("第 " + index + " 行解析错误");
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        int totalRows = context.readSheetHolder().getApproximateTotalRowNumber() - 1;
        int maxNum = 500;
        if (totalRows > maxNum) {
            errorMsgList.add("数据量过大,单次最多上传500条");
            throw new RuntimeException("数据量过大,单次最多上500条");
        }
    }

    public Map<String, Object> getResult() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalRows", totalRows);
        map.put("successRows", successRows);
        map.put("errorMsgList", errorMsgList);
        return map;
    }

    /**
     * 专业编号的校验
     *
     * @param rowIndex 行数
     * @param majorId     专业编号
     */
    private Boolean majorIdValid(Integer rowIndex, Integer majorId) {
        Major major = majorService.getOne(new QueryWrapper<Major>().eq("major_id", majorId));
        if(major == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 姓名的校验
     *
     * @param rowIndex 行数
     * @param name     专业名称
     */
    private Boolean nameValid(Integer rowIndex, String name) {
        if (StringUtils.isBlank(name)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，专业名称不能为空");
            return Boolean.FALSE;
        } else {
            QueryWrapper<Major> wrapper = new QueryWrapper<>();
            wrapper.eq("major_name", name);
            Major major = majorService.getOne(wrapper);
            if (major != null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，该专业已经存在");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 所属学院校验
     *
     * @param rowIndex 行数
     * @param schoolName  所属学院
     */
    private Boolean schoolValid(int rowIndex, String schoolName) {
        // 校验所属学院是否为空
        if (StringUtils.isBlank(schoolName)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，所属学院不能为空");
            return Boolean.FALSE;
        } else {
            Map<String,Integer> map = schoolService.getSchoolList().stream().collect(Collectors.toMap(School::getSchoolName,School::getSchoolId));
            if (map.get(schoolName) == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该学院");
                return Boolean.FALSE;
            }
            this.schoolId = map.get(schoolName);
        }
        return Boolean.TRUE;
    }

}
