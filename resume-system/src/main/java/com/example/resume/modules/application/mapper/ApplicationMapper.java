package com.example.resume.modules.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resume.modules.application.entity.Application;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
}
