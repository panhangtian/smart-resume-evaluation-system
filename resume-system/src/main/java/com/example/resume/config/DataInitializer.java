package com.example.resume.config;

import com.example.resume.ai.EmbeddingService;
import com.example.resume.common.utils.VectorUtils;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.service.JobService;
import com.example.resume.modules.user.entity.User;
import com.example.resume.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 演示数据初始化器：首次启动时创建默认用户和岗位数据。
 * 如果数据库中已有数据则跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final JobService jobService;
    private final PasswordEncoder passwordEncoder;
    private final EmbeddingService embeddingService;

    @Override
    public void run(String... args) {
        // 如果已有用户则跳过
        if (userService.count() > 0) {
            log.info("数据库已有数据，跳过演示数据初始化");
            return;
        }

        log.info("========== 开始初始化演示数据 ==========");

        // 1. 创建默认用户
        createUsers();

        // 2. 创建演示岗位
        createDemoJobs();

        log.info("========== 演示数据初始化完成 ==========");
    }

    private void createUsers() {
        // 管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin.setNickname("系统管理员");
        admin.setEmail("admin@resume.com");
        admin.setStatus(1);
        userService.save(admin);

        // HR
        User hr = new User();
        hr.setUsername("hr");
        hr.setPassword(passwordEncoder.encode("hr123"));
        hr.setRole("HR");
        hr.setNickname("HR张三");
        hr.setEmail("hr@resume.com");
        hr.setPhone("13800000001");
        hr.setStatus(1);
        userService.save(hr);

        // 求职者
        User seeker = new User();
        seeker.setUsername("seeker");
        seeker.setPassword(passwordEncoder.encode("seeker123"));
        seeker.setRole("JOBSEEKER");
        seeker.setNickname("求职者李四");
        seeker.setEmail("seeker@resume.com");
        seeker.setPhone("13800000002");
        seeker.setStatus(1);
        userService.save(seeker);

        log.info("默认账号创建完成：admin/admin123, hr/hr123, seeker/seeker123");
    }

    private void createDemoJobs() {
        // 用 HR id 1 作为发布者
        long hrId = 1L;

        String[][] jobData = {
                {"Java后端开发工程师", "腾讯科技", "深圳",
                        "负责高并发后端服务的设计与开发，参与微服务架构落地，优化系统性能。",
                        "Java,Spring Boot,MyBatis,MySQL,Redis,RocketMQ,Docker,Kubernetes,微服务,分布式",
                        "25", "50"},
                {"前端开发工程师", "阿里巴巴", "杭州",
                        "负责电商平台前端架构设计与开发，参与中后台系统建设，提升用户体验。",
                        "Vue.js,React,TypeScript,JavaScript,Webpack,HTML,CSS,Node.js,前端工程化",
                        "20", "40"},
                {"Python数据分析师", "字节跳动", "北京",
                        "负责海量数据的清洗、分析和建模，为业务决策提供数据支持。",
                        "Python,Pandas,NumPy,SQL,Spark,机器学习,数据可视化,TensorFlow",
                        "18", "38"},
                {"全栈开发工程师", "美团", "北京/上海",
                        "负责业务系统的前后端全栈开发，从需求理解到上线交付全流程。",
                        "Java,Spring Boot,Vue.js,JavaScript,MySQL,Redis,Docker,Linux,Git",
                        "22", "45"},
                {"算法工程师（NLP方向）", "百度", "北京",
                        "负责自然语言处理相关算法的研发与应用，推动大模型技术落地。",
                        "Python,PyTorch,TensorFlow,深度学习,NLP,Transformer,BERT,Spark",
                        "30", "60"},
                {"DevOps运维开发工程师", "华为", "东莞/深圳",
                        "负责CI/CD流水线建设，容器云平台运维，保障服务高可用。",
                        "Docker,Kubernetes,Linux,CI/CD,Prometheus,Git,Go,Python,自动化运维",
                        "20", "42"},
                {"移动端开发工程师（Android）", "小米", "北京",
                        "负责小米商城App的架构设计和功能开发，优化启动速度和页面渲染。",
                        "Android,Kotlin,Java,React Native,Flutter,Git,性能优化",
                        "18", "36"},
                {"产品经理（技术方向）", "京东", "北京",
                        "负责B端产品的需求分析和规划，协调研发团队推进产品落地。",
                        "Axure,SQL,数据分析,项目管理,需求分析,用户研究",
                        "15", "35"},
                {"测试开发工程师", "网易", "广州",
                        "负责自动化测试框架搭建，编写测试用例，保障产品质量。",
                        "Java,Python,Selenium,Jenkins,Linux,MySQL,自动化测试,性能测试",
                        "15", "30"},
                {"数据仓库工程师", "拼多多", "上海",
                        "负责数据仓库建模与ETL开发，建设稳定的数据基础设施。",
                        "SQL,Spark,Hadoop,Flink,Kafka,ClickHouse,数据建模,数据治理",
                        "20", "45"}
        };

        for (String[] data : jobData) {
            Job job = new Job();
            job.setHrId(hrId);
            job.setTitle(data[0]);
            job.setCompany(data[1]);
            job.setLocation(data[2]);
            job.setDescription(data[3]);
            job.setRequirementTags(data[4]);
            job.setSalaryMin(Integer.parseInt(data[5]));
            job.setSalaryMax(Integer.parseInt(data[6]));
            job.setStatus(1); // 已上架
            jobService.save(job);

            // 生成 embedding
            String text = job.getTitle() + " " + job.getDescription() + " " + job.getRequirementTags();
            float[] vec = embeddingService.embed(text);
            job.setEmbedding(VectorUtils.toJson(vec));
            jobService.updateById(job);
        }

        log.info("已创建 " + jobData.length + " 个演示岗位");
    }
}
