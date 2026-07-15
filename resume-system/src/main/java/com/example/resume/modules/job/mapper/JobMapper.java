package com.example.resume.modules.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resume.modules.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
