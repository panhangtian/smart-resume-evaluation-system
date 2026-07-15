package com.example.resume.modules.resume.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resume.modules.resume.dto.ResumeEditDTO;
import com.example.resume.modules.resume.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService extends IService<Resume> {

    Long upload(Long userId, MultipartFile file);

    Resume detail(Long id);

    List<Resume> myResumes(Long userId);

    void reparse(Long id);

    /** 编辑简历解析后的数据 */
    void updateResume(Long id, Long userId, ResumeEditDTO dto);

    /** 给推荐引擎用：查询某用户最新已解析简历 */
    Resume latestParsed(Long userId);
}
