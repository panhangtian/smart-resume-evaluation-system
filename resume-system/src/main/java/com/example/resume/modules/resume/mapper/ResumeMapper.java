package com.example.resume.modules.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resume.modules.resume.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}
