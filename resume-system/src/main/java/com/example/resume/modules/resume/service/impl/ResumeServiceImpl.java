package com.example.resume.modules.resume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resume.common.BizException;
import com.example.resume.common.utils.TextExtractor;
import com.example.resume.common.utils.VectorUtils;
import com.example.resume.config.StorageService;
import com.example.resume.modules.resume.dto.ResumeEditDTO;
import com.example.resume.modules.resume.entity.Resume;
import com.example.resume.modules.resume.mapper.ResumeMapper;
import com.example.resume.modules.resume.service.ResumeService;
import com.example.resume.task.ResumeParseTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    private static final long MAX_SIZE = 10 * 1024 * 1024L;
    private static final List<String> ALLOWED = List.of(".pdf", ".doc", ".docx", ".txt");

    private final StorageService storageService;
    private final ResumeParseTask resumeParseTask;

    @Override
    public Long upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        String name = file.getOriginalFilename();
        if (name == null) throw new BizException("文件名缺失");
        String lower = name.toLowerCase();
        boolean ok = ALLOWED.stream().anyMatch(lower::endsWith);
        if (!ok) throw new BizException("仅支持 PDF / DOC / DOCX / TXT");
        if (file.getSize() > MAX_SIZE) throw new BizException("文件不能超过 10MB");

        try {
            String key = storageService.store(file);
            String text = TextExtractor.extract(file);

            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setFileName(name);
            resume.setStorageKey(key);
            resume.setRawText(text);
            resume.setStatus(0);
            save(resume);

            resumeParseTask.parse(resume.getId());
            return resume.getId();
        } catch (Exception e) {
            throw new BizException("简历上传失败：" + e.getMessage());
        }
    }

    @Override
    public Resume detail(Long id) {
        Resume r = getById(id);
        if (r == null) throw new BizException("简历不存在");
        return r;
    }

    @Override
    public List<Resume> myResumes(Long userId) {
        return lambdaQuery().eq(Resume::getUserId, userId)
                .orderByDesc(Resume::getCreateTime).list();
    }

    @Override
    public void reparse(Long id) {
        Resume r = getById(id);
        if (r == null) throw new BizException("简历不存在");
        r.setStatus(0);
        updateById(r);
        resumeParseTask.parse(id);
    }

    @Override
    public Resume latestParsed(Long userId) {
        return lambdaQuery().eq(Resume::getUserId, userId)
                .eq(Resume::getStatus, 2)
                .orderByDesc(Resume::getCreateTime)
                .last("limit 1").one();
    }

    @Override
    public void updateResume(Long id, Long userId, ResumeEditDTO dto) {
        Resume r = getById(id);
        if (r == null) throw new BizException("简历不存在");
        if (!r.getUserId().equals(userId)) throw new BizException("无权编辑该简历");
        if (dto.getParsedJson() != null) r.setParsedJson(dto.getParsedJson());
        if (dto.getAbilityProfile() != null) r.setAbilityProfile(dto.getAbilityProfile());
        if (dto.getSkills() != null) r.setSkills(dto.getSkills());
        if (dto.getYearsOfExperience() != null) r.setYearsOfExperience(dto.getYearsOfExperience());
        updateById(r);
    }
}
