package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.entity.School;
import org.example.classpai.mapper.SchoolMapper;
import org.example.classpai.service.SchoolService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolServiceImpl implements SchoolService {

    private final SchoolMapper schoolMapper;

    public SchoolServiceImpl(SchoolMapper schoolMapper) {
        this.schoolMapper = schoolMapper;
    }

    @Override
    public List<School> list() {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(School::getSchoolName);
        return schoolMapper.selectList(wrapper);
    }
}
