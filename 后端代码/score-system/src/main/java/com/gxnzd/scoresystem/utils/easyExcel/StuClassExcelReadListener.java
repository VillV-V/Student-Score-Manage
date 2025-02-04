package com.gxnzd.scoresystem.utils.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Major;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.service.MajorService;
import com.gxnzd.scoresystem.service.StuClassService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class StuClassExcelReadListener extends AnalysisEventListener<StuClass> {

    private StuClassService stuClassService;

    private MajorService majorService;

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存的数据
     */
    private List<StuClass> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

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

    private Integer majorId = null;

    public StuClassExcelReadListener(StuClassService stuClassService, MajorService majorService) {
        this.stuClassService = stuClassService;
        this.majorService = majorService;
    }

    @Override
    public void invoke(StuClass stuClass, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", stuClass);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Integer classId = stuClass.getClassId();
        String className = stuClass.getClassName();
        String grade = stuClass.getGrade();
        stuClass.setMajorId(null);
        String majorName = stuClass.getMajorName();

        // 只有全部校验通过的对象才能被添加到下一步
        if (classId == null) {
            if (nameValid(rowIndex, className) && gradeValid(rowIndex, grade) && majorValid(rowIndex, majorName)) {
                if(majorId != null) {
                    stuClass.setMajorId(majorId);
                }
                cachedDataList.add(stuClass);
                this.majorId = null;
            }
        } else {
            if(!classIdValid(rowIndex, classId)) {
                if (nameValid(rowIndex, className) && gradeValid(rowIndex, grade) && majorValid(rowIndex, majorName)) {
                    if(majorId != null) {
                        stuClass.setMajorId(majorId);
                    }
                    cachedDataList.add(stuClass);
                    this.majorId = null;
                }
            } else {
                // 任意一项有值时也要进行校验
                if(!StringUtils.isBlank(className)) {
                    if(!nameValid(rowIndex, className)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(grade)) {
                    if(!gradeValid(rowIndex, grade)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(majorName)) {
                    if(!majorValid(rowIndex, majorName)) {
                        return;
                    }
                }

                if(majorId != null) {
                    stuClass.setMajorId(majorId);
                }
                cachedDataList.add(stuClass);
                this.majorId = null;
            }
        }
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            stuClassService.saveOrUpdateBatch(cachedDataList);
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
            stuClassService.saveOrUpdateBatch(cachedDataList);
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
     * 班级编号的校验
     *
     * @param rowIndex 行数
     * @param classId     班级编号
     */
    private Boolean classIdValid(Integer rowIndex, Integer classId) {
        StuClass stuClass =stuClassService.getOne(new QueryWrapper<StuClass>().eq("class_id", classId));
        if(stuClass == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 班级名称的校验
     *
     * @param rowIndex 行数
     * @param name     班级名称
     */
    private Boolean nameValid(Integer rowIndex, String name) {
        if (StringUtils.isBlank(name)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，班级名称不能为空");
            return Boolean.FALSE;
        } else {
            QueryWrapper<StuClass> wrapper = new QueryWrapper<>();
            wrapper.eq("class_name", name);
            StuClass stuClass = stuClassService.getOne(wrapper);
            if (stuClass != null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，该班级已经存在");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 年级的校验
     *
     * @param rowIndex 行数
     * @param grade     年级
     */
    private Boolean gradeValid(Integer rowIndex, String grade) {
        if (StringUtils.isBlank(grade)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，年级不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 所属专业校验
     *
     * @param rowIndex 行数
     * @param majorName  所属专业
     */
    private Boolean majorValid(int rowIndex, String majorName) {
        // 校验所属专业是否为空
        if (StringUtils.isBlank(majorName)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，所属专业不能为空");
            return Boolean.FALSE;
        } else {
            Map<String,Integer> map = majorService.getMajorList().stream().collect(Collectors.toMap(Major::getMajorName,Major::getMajorId));
            if (map.get(majorName) == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该专业");
                return Boolean.FALSE;
            }
            this.majorId = map.get(majorName);
        }
        return Boolean.TRUE;
    }

}
