/**
 * 岗位外挂数据服务 - 提供多来源、多行业岗位数据
 * 独立于后端 jar 运行，前端可跨域调用来丰富岗位来源
 * 启动: node server.js  (端口 3001)
 */
const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 3001;

app.use(cors({ origin: '*' }));
app.use(express.json());

// ────── 岗位数据（多行业）──────
// 每条岗位含 industry 字段用于「行业」分类
const JOBS = [
  // ================= 互联网 / IT =================
  { title:'高级前端架构师', company:'字节跳动', location:'北京', salaryMin:40, salaryMax:70, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'5-10年', skills:['Vue.js','React','TypeScript','Webpack','微前端'], desc:'负责公司核心产品前端架构设计与性能优化，推动前端工程化建设。' },
  { title:'AI算法工程师', company:'阿里巴巴', location:'杭州', salaryMin:35, salaryMax:60, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'硕士', exp:'3-5年', skills:['Python','TensorFlow','PyTorch','NLP','推荐系统'], desc:'参与大模型训练与推理优化，落地 NLP/CV 相关业务场景。' },
  { title:'后端开发Leader', company:'腾讯', location:'深圳', salaryMin:50, salaryMax:80, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'8-15年', skills:['Go','Java','分布式','微服务','K8s','高并发'], desc:'带领 10 人后端团队，负责社交产品亿级用户架构升级。' },
  { title:'数据分析专家', company:'美团', location:'北京', salaryMin:30, salaryMax:55, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-8年', skills:['SQL','Python','Tableau','AB实验','用户增长'], desc:'深入业务数据分析，指导策略迭代与增长决策。' },
  { title:'运维开发SRE', company:'华为', location:'成都', salaryMin:25, salaryMax:45, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-5年', skills:['Linux','Docker','K8s','Prometheus','CI/CD'], desc:'负责云原生基础设施的自动化运维与稳定性保障。' },
  { title:'产品经理(增长方向)', company:'字节跳动', location:'上海', salaryMin:30, salaryMax:50, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-7年', skills:['产品设计','数据分析','A/B测试','用户增长','原型设计'], desc:'负责抖音增长方向产品规划，通过实验驱动用户规模提升。' },
  { title:'测试开发工程师', company:'快手', location:'北京', salaryMin:25, salaryMax:45, source:'猎聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-5年', skills:['Java','Python','Selenium','接口测试','性能测试'], desc:'建设自动化测试平台，提升研发交付质量效率。' },
  { title:'前端开发工程师', company:'京东', location:'北京', salaryMin:20, salaryMax:40, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'2-5年', skills:['Vue.js','React','JavaScript','CSS','Element Plus'], desc:'参与京东零售前台研发，负责核心页面开发与优化。' },
  { title:'Java高级开发', company:'拼多多', location:'上海', salaryMin:30, salaryMax:55, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-8年', skills:['Java','Spring Boot','MySQL','Redis','消息队列'], desc:'负责电商核心交易链路的后端研发与性能优化。' },
  { title:'客户端开发(iOS)', company:'网易', location:'杭州', salaryMin:25, salaryMax:45, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-5年', skills:['Swift','Objective-C','Flutter','iOS SDK','性能优化'], desc:'负责网易云音乐 iOS 端功能开发与体验优化。' },
  { title:'大数据开发工程师', company:'滴滴', location:'北京', salaryMin:28, salaryMax:50, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-6年', skills:['Hadoop','Spark','Flink','Hive','HBase','Kafka'], desc:'建设实时数仓与离线数仓，支撑数据驱动业务决策。' },
  { title:'后端工程师(Go)', company:'360', location:'北京', salaryMin:25, salaryMax:48, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'2-5年', skills:['Go','Gin','gRPC','MySQL','Redis'], desc:'参与安全产品后端服务开发，处理高并发实时请求。' },
  { title:'AI产品经理', company:'百度', location:'北京', salaryMin:30, salaryMax:55, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-7年', skills:['AI产品','需求分析','竞品分析','数据分析','原型设计'], desc:'负责百度智能云 AI 产品的需求分析与迭代规划。' },
  { title:'嵌入式软件开发', company:'华为', location:'武汉', salaryMin:20, salaryMax:40, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'2-5年', skills:['C','C++','Linux','RTOS','驱动开发'], desc:'参与鸿蒙生态设备端嵌入式软件开发。' },
  { title:'前端实习生', company:'小红书', location:'上海', salaryMin:4, salaryMax:8, source:'拉勾', industry:'互联网/IT', type:'实习', edu:'本科', exp:'应届', skills:['Vue.js','JavaScript','CSS','Git','HTTP'], desc:'参与小红书社区前端功能开发，mentor 一对一指导。' },
  { title:'后端开发实习生', company:'哔哩哔哩', location:'上海', salaryMin:4, salaryMax:7, source:'拉勾', industry:'互联网/IT', type:'实习', edu:'本科', exp:'应届', skills:['Java','Spring Boot','MySQL','Redis','Linux'], desc:'参与 B 站核心业务后端开发实习工作。' },
  { title:'产品助理(实习)', company:'知乎', location:'北京', salaryMin:3, salaryMax:6, source:'拉勾', industry:'互联网/IT', type:'实习', edu:'本科', exp:'应届', skills:['产品文档','需求分析','竞品调研','Axure','数据透视'], desc:'协助产品经理完成需求文档与数据分析工作。' },
  { title:'测试实习生', company:'京东', location:'北京', salaryMin:3, salaryMax:6, source:'拉勾', industry:'互联网/IT', type:'实习', edu:'本科', exp:'应届', skills:['测试用例','接口测试','Postman','Python','Linux'], desc:'参与京东物流系统功能测试与自动化。' },
  { title:'数据分析实习生', company:'美团', location:'北京', salaryMin:4, salaryMax:7, source:'拉勾', industry:'互联网/IT', type:'实习', edu:'本科', exp:'应届', skills:['SQL','Excel','Python','Tableau','统计学'], desc:'协助分析师进行业务数据提取与分析报告产出。' },
  { title:'全栈工程师(创业公司)', company:'一家科技', location:'深圳', salaryMin:18, salaryMax:30, source:'拉勾', industry:'互联网/IT', type:'全职', edu:'本科', exp:'2-4年', skills:['Vue.js','Node.js','MongoDB','AWS','Docker'], desc:'早期创业团队，技术栈前沿，期权丰厚。' },
  { title:'【内推】后端开发(校招)', company:'字节跳动', location:'北京', salaryMin:15, salaryMax:25, source:'牛客内推', industry:'互联网/IT', type:'全职', edu:'本科', exp:'应届', skills:['Java','Go','Python','数据库','计算机网络'], desc:'字节跳动 2026 校招内推通道，直达面试官。' },
  { title:'【内推】前端开发(校招)', company:'阿里巴巴', location:'杭州', salaryMin:15, salaryMax:25, source:'牛客内推', industry:'互联网/IT', type:'全职', edu:'本科', exp:'应届', skills:['JavaScript','CSS','Vue.js','React','数据结构'], desc:'阿里集团校招前端岗位，内推优先筛选简历。' },
  { title:'【内推】算法工程师(校招)', company:'腾讯', location:'深圳', salaryMin:18, salaryMax:30, source:'牛客内推', industry:'互联网/IT', type:'全职', edu:'硕士', exp:'应届', skills:['Python','机器学习','深度学习','数据结构','算法竞赛'], desc:'腾讯 AI Lab 校招内推，参与前沿 AI 研究。' },
  { title:'【内推】数据分析(校招)', company:'美团', location:'北京', salaryMin:12, salaryMax:20, source:'牛客内推', industry:'互联网/IT', type:'全职', edu:'本科', exp:'应届', skills:['SQL','Python','统计学','A/B实验','业务分析'], desc:'美团核心业务数据分析校招岗位。' },
  { title:'【内推】产品经理(校招)', company:'百度', location:'北京', salaryMin:14, salaryMax:22, source:'牛客内推', industry:'互联网/IT', type:'全职', edu:'本科', exp:'应届', skills:['产品思维','数据分析','逻辑表达','用户研究','竞品分析'], desc:'百度产品校招，参与搜索/AI产品方向。' },
  { title:'兼职前端开发', company:'灵工科技', location:'远程', salaryMin:8, salaryMax:15, source:'拉勾', industry:'互联网/IT', type:'兼职', edu:'本科', exp:'2-5年', skills:['Vue.js','uni-app','小程序','CSS','Git'], desc:'远程兼职，参与小程序/H5开发，按项目付费。' },
  { title:'新媒体运营', company:'得到', location:'北京', salaryMin:12, salaryMax:22, source:'Boss直聘', industry:'互联网/IT', type:'全职', edu:'本科', exp:'2-4年', skills:['内容运营','短视频','公众号','数据分析','社群'], desc:'负责得到 App 新媒体矩阵内容策划与增长。' },
  { title:'UI/UX设计师', company:'oppo', location:'成都', salaryMin:18, salaryMax:35, source:'51job', industry:'互联网/IT', type:'全职', edu:'本科', exp:'3-6年', skills:['Figma','Sketch','用户研究','交互设计','设计系统'], desc:'负责 ColorOS 系统级 UI 设计与规范制定。' },
  { title:'兼职UI设计师', company:'设计派', location:'远程', salaryMin:5, salaryMax:12, source:'牛客内推', industry:'互联网/IT', type:'兼职', edu:'大专', exp:'1-3年', skills:['Figma','PS','AI','UI设计','移动端规范'], desc:'远程兼职移动端 UI 设计任务。' },

  // ================= 服务业 =================
  { title:'餐厅服务员', company:'海底捞', location:'上海', salaryMin:5, salaryMax:9, source:'51job', industry:'服务业', type:'全职', edu:'高中', exp:'1-3年', skills:['客户服务','卫生标准','团队协作','沟通表达'], desc:'负责门店顾客接待、点餐与用餐服务，维护用餐体验。' },
  { title:'酒店前台接待', company:'如家酒店', location:'杭州', salaryMin:4, salaryMax:7, source:'51job', industry:'服务业', type:'全职', edu:'大专', exp:'1-2年', skills:['前台接待','PMS系统','客户沟通','普通话'], desc:'负责入住办理、退房结算与客人咨询，夜班轮值。' },
  { title:'家政服务员', company:'58到家', location:'北京', salaryMin:6, salaryMax:11, source:'Boss直聘', industry:'服务业', type:'全职', edu:'初中', exp:'1-3年', skills:['家庭保洁','做饭','育儿辅助','耐心细致'], desc:'上门提供家庭保洁、做饭、老人陪护等服务。' },
  { title:'商场导购', company:'优衣库', location:'上海', salaryMin:5, salaryMax:8, source:'51job', industry:'服务业', type:'全职', edu:'高中', exp:'1-2年', skills:['销售技巧','陈列','客户沟通','时尚敏感'], desc:'负责门店顾客接待、服装推荐与店铺陈列维护。' },
  { title:'咖啡师', company:'星巴克', location:'深圳', salaryMin:5, salaryMax:8, source:'Boss直聘', industry:'服务业', type:'全职', edu:'高中', exp:'1-2年', skills:['咖啡制作','拉花','顾客服务','吧台管理'], desc:'制作咖啡饮品，维护吧台卫生与顾客体验。' },
  { title:'美容美发师', company:'木北造型', location:'北京', salaryMin:6, salaryMax:14, source:'51job', industry:'服务业', type:'全职', edu:'中专', exp:'2-5年', skills:['剪发','烫染','造型设计','客户服务'], desc:'为顾客提供剪发、烫染、造型等美发服务。' },
  { title:'快递配送员', company:'顺丰速运', location:'广州', salaryMin:7, salaryMax:12, source:'Boss直聘', industry:'服务业', type:'全职', edu:'初中', exp:'1-3年', skills:['电动车驾驶','区域熟悉','准时送达','客户服务'], desc:'负责指定区域快件收派，确保准时安全送达。' },
  { title:'超市收银员', company:'永辉超市', location:'重庆', salaryMin:4, salaryMax:6, source:'51job', industry:'服务业', type:'全职', edu:'高中', exp:'1-2年', skills:['收银操作','点钞','耐心','基础电脑'], desc:'负责超市收银结算与会员服务。' },
  { title:'母婴护理(月嫂)', company:'好孕母婴', location:'北京', salaryMin:10, salaryMax:20, source:'Boss直聘', industry:'服务业', type:'全职', edu:'初中', exp:'3-5年', skills:['新生儿护理','产妇照护','营养餐','急救常识'], desc:'为产妇与新生儿提供专业月子期护理。' },
  { title:'物业保安', company:'万科物业', location:'武汉', salaryMin:4, salaryMax:6, source:'51job', industry:'服务业', type:'全职', edu:'初中', exp:'1-3年', skills:['巡逻','安防','消防常识','责任心'], desc:'负责小区门岗、巡逻与秩序维护。' },
  { title:'餐厅兼职服务员', company:'西贝莜面村', location:'北京', salaryMin:4, salaryMax:6, source:'拉勾', industry:'服务业', type:'兼职', edu:'高中', exp:'应届', skills:['餐饮服务','勤快','团队协作'], desc:'周末/节假日兼职，负责传菜与桌面清洁。' },

  // ================= 制造业 =================
  { title:'电气工程师', company:'比亚迪', location:'深圳', salaryMin:15, salaryMax:30, source:'智联', industry:'制造业', type:'全职', edu:'本科', exp:'3-5年', skills:['AutoCAD','PLC','电气设计','MATLAB','新能源'], desc:'参与新能源汽车电气系统设计与测试。' },
  { title:'机械工程师', company:'宁德时代', location:'宁德', salaryMin:15, salaryMax:28, source:'智联', industry:'制造业', type:'全职', edu:'本科', exp:'3-5年', skills:['SolidWorks','机械设计','有限元分析','CATIA','材料力学'], desc:'负责电池模组结构设计与工艺优化。' },
  { title:'工厂普工/操作工', company:'富士康', location:'郑州', salaryMin:5, salaryMax:8, source:'51job', industry:'制造业', type:'全职', edu:'初中', exp:'1-3年', skills:['流水线作业','设备操作','质检意识','吃苦耐劳'], desc:'电子产品的组装、测试与包装产线作业。' },
  { title:'焊工', company:'中车株机', location:'株洲', salaryMin:7, salaryMax:13, source:'智联', industry:'制造业', type:'全职', edu:'中专', exp:'2-5年', skills:['电弧焊','氩弧焊','识图','焊接证书'], desc:'负责轨道车辆结构件焊接，需持焊工证。' },
  { title:'质检员(QC)', company:'比亚迪', location:'西安', salaryMin:5, salaryMax:9, source:'51job', industry:'制造业', type:'全职', edu:'高中', exp:'1-3年', skills:['尺寸测量','巡检','SPC','细心'], desc:'负责产线产品质量检验与异常反馈。' },
  { title:'CNC数控技师', company:'格力电器', location:'珠海', salaryMin:8, salaryMax:14, source:'智联', industry:'制造业', type:'全职', edu:'中专', exp:'2-5年', skills:['CNC编程','Mastercam','机床操作','识图'], desc:'操作数控车床/加工中心进行精密零件加工。' },
  { title:'设备维修工', company:'海尔智家', location:'青岛', salaryMin:6, salaryMax:11, source:'51job', industry:'制造业', type:'全职', edu:'中专', exp:'2-5年', skills:['机电维修','PLC基础','故障排查','动手能力强'], desc:'负责生产线设备的日常维护与故障维修。' },
  { title:'自动驾驶感知工程师', company:'小鹏汽车', location:'广州', salaryMin:35, salaryMax:60, source:'Boss直聘', industry:'制造业', type:'全职', edu:'硕士', exp:'3-8年', skills:['C++','Python','CV','激光雷达','深度学习','Carla'], desc:'负责自动驾驶感知算法研发与车端部署。' },
  { title:'芯片验证工程师', company:'中芯国际', location:'上海', salaryMin:25, salaryMax:50, source:'智联', industry:'制造业', type:'全职', edu:'硕士', exp:'3-8年', skills:['Verilog','SystemVerilog','UVM','芯片验证','Perl'], desc:'负责 SoC 芯片验证环境搭建与用例开发。' },

  // ================= 金融 =================
  { title:'量化研究员', company:'华泰证券', location:'上海', salaryMin:40, salaryMax:80, source:'猎聘', industry:'金融', type:'全职', edu:'硕士', exp:'3-7年', skills:['Python','C++','金融工程','统计学','回测系统'], desc:'设计交易策略与因子模型，回测并实盘跟踪。' },
  { title:'风控建模专家', company:'蚂蚁集团', location:'杭州', salaryMin:35, salaryMax:60, source:'Boss直聘', industry:'金融', type:'全职', edu:'硕士', exp:'5-10年', skills:['Python','风控模型','信用评分','特征工程','图算法'], desc:'负责信贷风控模型研发与迭代优化。' },
  { title:'财务分析师', company:'招商银行', location:'上海', salaryMin:20, salaryMax:35, source:'智联', industry:'金融', type:'全职', edu:'本科', exp:'3-5年', skills:['财务分析','Excel','VBA','Power BI','CPA'], desc:'负责银行零售业务的财务数据分析与预算管理。' },
  { title:'银行柜员', company:'工商银行', location:'南京', salaryMin:6, salaryMax:12, source:'51job', industry:'金融', type:'全职', edu:'本科', exp:'1-3年', skills:['柜面业务','反欺诈','客户服务','细心'], desc:'负责个人客户存取款、理财认购等柜面业务。' },
  { title:'保险代理人', company:'平安人寿', location:'成都', salaryMin:6, salaryMax:18, source:'51job', industry:'金融', type:'全职', edu:'大专', exp:'1-3年', skills:['客户开发','保险知识','沟通','抗压'], desc:'为客户量身定制人寿/健康保险方案，提供续期服务。' },
  { title:'理财顾问', company:'招商银行', location:'深圳', salaryMin:12, salaryMax:30, source:'Boss直聘', industry:'金融', type:'全职', edu:'本科', exp:'2-5年', skills:['基金','保险','资产配置','客户维护','AFP'], desc:'为高净值客户提供资产配置与财富管理建议。' },
  { title:'证券客户经理', company:'中信证券', location:'武汉', salaryMin:8, salaryMax:20, source:'智联', industry:'金融', type:'全职', edu:'本科', exp:'1-3年', skills:['开户拓展','投资顾问','客户维护','证券从业'], desc:'负责证券账户开发、交易服务与投顾转化。' },
  { title:'信贷专员', company:'微众银行', location:'深圳', salaryMin:10, salaryMax:22, source:'Boss直聘', industry:'金融', type:'全职', edu:'本科', exp:'2-4年', skills:['信贷审批','风控意识','客户沟通','数据分析'], desc:'负责小微企业信贷审核与贷后管理。' },

  // ================= 教育 =================
  { title:'教师/教研', company:'新东方', location:'西安', salaryMin:10, salaryMax:20, source:'智联', industry:'教育', type:'全职', edu:'本科', exp:'1-3年', skills:['教学','课程设计','课件制作','学科知识','沟通能力'], desc:'负责大学生考研/留学考试课程研发与授课。' },
  { title:'在线辅导老师', company:'学而思', location:'远程', salaryMin:3, salaryMax:6, source:'51job', industry:'教育', type:'兼职', edu:'本科', exp:'1-3年', skills:['数学','物理','英语','线上授课','教案设计'], desc:'线上授课，时间灵活，按课时付费。' },
  { title:'幼儿园教师', company:'红黄蓝幼儿园', location:'北京', salaryMin:5, salaryMax:9, source:'51job', industry:'教育', type:'全职', edu:'大专', exp:'1-3年', skills:['幼儿教育','保育','儿歌舞蹈','耐心'], desc:'负责幼儿日常照料与游戏化教学。' },
  { title:'中小学学科老师', company:'学而思', location:'上海', salaryMin:8, salaryMax:18, source:'Boss直聘', industry:'教育', type:'全职', edu:'本科', exp:'1-3年', skills:['学科知识','中高考','备课','家校沟通'], desc:'负责 K12 数学/英语等学科授课与提分。' },
  { title:'职业培训师', company:'达内教育', location:'广州', salaryMin:10, salaryMax:20, source:'智联', industry:'教育', type:'全职', edu:'本科', exp:'2-5年', skills:['IT培训','课程开发','授课','项目实战'], desc:'负责 IT 职业技能培训课程设计与授课。' },
  { title:'留学顾问', company:'启德教育', location:'北京', salaryMin:8, salaryMax:18, source:'Boss直聘', industry:'教育', type:'全职', edu:'本科', exp:'2-4年', skills:['留学规划','文书指导','英语','客户沟通'], desc:'为学生提供留学国家/院校选择与申请全流程服务。' },
  { title:'教务管理', company:'新东方', location:'杭州', salaryMin:6, salaryMax:10, source:'51job', industry:'教育', type:'全职', edu:'本科', exp:'1-3年', skills:['排课','学员管理','系统操作','沟通协调'], desc:'负责校区排课、学员档案与教务运营。' },

  // ================= 医疗健康 =================
  { title:'护士', company:'北京协和医院', location:'北京', salaryMin:6, salaryMax:12, source:'智联', industry:'医疗健康', type:'全职', edu:'大专', exp:'1-3年', skills:['护理操作','护患沟通','无菌观念','执业护士证'], desc:'负责病区患者临床护理与医嘱执行。' },
  { title:'药剂师', company:'老百姓大药房', location:'长沙', salaryMin:5, salaryMax:10, source:'51job', industry:'医疗健康', type:'全职', edu:'大专', exp:'1-3年', skills:['药品调剂','药事管理','执业药师证','服务'], desc:'负责处方审核、药品调剂与用药指导。' },
  { title:'康复治疗师', company:'和睦康复医院', location:'上海', salaryMin:7, salaryMax:14, source:'Boss直聘', industry:'医疗健康', type:'全职', edu:'本科', exp:'2-4年', skills:['康复评定','PT/OT','理疗设备','耐心'], desc:'为患者制定并执行康复治疗计划。' },
  { title:'体检中心医生', company:'美年大健康', location:'深圳', salaryMin:10, salaryMax:20, source:'智联', industry:'医疗健康', type:'全职', edu:'本科', exp:'3-5年', skills:['全科/内科','体检报告','执业医师证'], desc:'负责健康体检问诊与报告解读。' },
  { title:'口腔助理医师', company:'瑞尔齿科', location:'成都', salaryMin:6, salaryMax:12, source:'51job', industry:'医疗健康', type:'全职', edu:'大专', exp:'1-3年', skills:['四手操作','口腔护理','助理医师证'], desc:'协助口腔主诊医生完成诊疗与器械准备。' },
  { title:'医药研究员', company:'恒瑞医药', location:'上海', salaryMin:20, salaryMax:40, source:'猎聘', industry:'医疗健康', type:'全职', edu:'硕士', exp:'3-8年', skills:['药物研发','临床研究','药理学','FDA申报','GMP'], desc:'负责创新药临床前研究与申报。' },
  { title:'生物信息工程师', company:'药明康德', location:'苏州', salaryMin:18, salaryMax:35, source:'猎聘', industry:'医疗健康', type:'全职', edu:'硕士', exp:'2-5年', skills:['Python','R','生物信息','基因组学','机器学习'], desc:'处理高通量测序数据，搭建分析流程。' },

  // ================= 物流供应链 =================
  { title:'仓库管理员', company:'顺丰', location:'武汉', salaryMin:6, salaryMax:10, source:'51job', industry:'物流供应链', type:'全职', edu:'高中', exp:'1-3年', skills:['仓储管理','WMS','库存盘点','物流调度','Excel'], desc:'负责仓储出入库管理与盘点。' },
  { title:'货运司机', company:'德邦物流', location:'广州', salaryMin:8, salaryMax:15, source:'Boss直聘', industry:'物流供应链', type:'全职', edu:'初中', exp:'2-5年', skills:['C1/B2驾照','熟悉路线','货物装卸','安全驾驶'], desc:'负责城市间或同城货物配送。' },
  { title:'分拣员', company:'菜鸟网络', location:'杭州', salaryMin:5, salaryMax:9, source:'51job', industry:'物流供应链', type:'全职', edu:'初中', exp:'1-3年', skills:['分拣扫码','体力好','准时','团队协作'], desc:'负责快递包裹分拣与扫描。' },
  { title:'仓储主管', company:'京东物流', location:'北京', salaryMin:10, salaryMax:18, source:'智联', industry:'物流供应链', type:'全职', edu:'大专', exp:'3-5年', skills:['仓储运营','WMS','团队管理','库存优化'], desc:'负责仓库日常运营与班组管理。' },
  { title:'供应链计划经理', company:'宝洁', location:'广州', salaryMin:25, salaryMax:45, source:'51job', industry:'物流供应链', type:'全职', edu:'本科', exp:'5-10年', skills:['供应链','SAP','需求预测','库存优化','精益管理'], desc:'负责大中华区供应链规划与需求预测。' },

  // ================= 房地产 / 建筑 =================
  { title:'建筑设计经理', company:'万科', location:'深圳', salaryMin:25, salaryMax:45, source:'智联', industry:'房地产/建筑', type:'全职', edu:'本科', exp:'5-10年', skills:['Revit','SketchUp','建筑设计','规范审查','BIM'], desc:'负责大型住宅与商业项目建筑方案设计管理。' },
  { title:'施工员', company:'中建三局', location:'武汉', salaryMin:7, salaryMax:13, source:'51job', industry:'房地产/建筑', type:'全职', edu:'大专', exp:'2-5年', skills:['现场管理','识图','施工规范','二级建造师'], desc:'负责施工现场进度、质量与安全管控。' },
  { title:'造价工程师', company:'中海地产', location:'深圳', salaryMin:10, salaryMax:20, source:'智联', industry:'房地产/建筑', type:'全职', edu:'本科', exp:'3-5年', skills:['广联达','算量','计价','造价咨询'], desc:'负责项目工程量计算与成本管控。' },
  { title:'物业管理员', company:'万科物业', location:'成都', salaryMin:5, salaryMax:9, source:'51job', industry:'房地产/建筑', type:'全职', edu:'高中', exp:'1-3年', skills:['业主服务','报修处理','巡检','沟通'], desc:'负责小区物业客服与现场服务协调。' },
  { title:'装修设计师', company:'业之峰装饰', location:'北京', salaryMin:8, salaryMax:18, source:'Boss直聘', industry:'房地产/建筑', type:'全职', edu:'大专', exp:'2-5年', skills:['CAD','3DMAX','量房','谈单'], desc:'负责家装方案设计与客户洽谈。' },

  // ================= 设计创意 =================
  { title:'平面设计师', company:'正邦设计', location:'上海', salaryMin:8, salaryMax:16, source:'Boss直聘', industry:'设计创意', type:'全职', edu:'大专', exp:'2-4年', skills:['PS','AI','品牌VI','排版','创意'], desc:'负责企业品牌视觉与物料设计。' },
  { title:'室内设计师', company:'东易日盛', location:'杭州', salaryMin:9, salaryMax:20, source:'智联', industry:'设计创意', type:'全职', edu:'大专', exp:'2-5年', skills:['CAD','3DMAX','软装','谈单','效果图'], desc:'负责住宅/商业空间室内方案设计。' },
  { title:'服装设计师', company:'江南布衣', location:'杭州', salaryMin:10, salaryMax:20, source:'猎聘', industry:'设计创意', type:'全职', edu:'本科', exp:'2-5年', skills:['服装制版','趋势调研','面料','PS/AI'], desc:'负责女装系列款式设计与企划。' },
  { title:'插画师', company:'自由职业', location:'远程', salaryMin:6, salaryMax:15, source:'牛客内推', industry:'设计创意', type:'兼职', edu:'大专', exp:'2-5年', skills:['Procreate','PS','板绘','风格化'], desc:'承接出版物、品牌插画约稿，远程协作。' },
  { title:'商业摄影师', company:'唯一视觉', location:'上海', salaryMin:8, salaryMax:18, source:'Boss直聘', industry:'设计创意', type:'全职', edu:'大专', exp:'2-5年', skills:['摄影','布光','后期','审美'], desc:'负责婚纱/商业产品拍摄与后期。' },

  // ================= 文化传媒 =================
  { title:'新媒体编辑', company:'十点读书', location:'厦门', salaryMin:7, salaryMax:14, source:'Boss直聘', industry:'文化传媒', type:'全职', edu:'本科', exp:'1-3年', skills:['文案','公众号','热点','排版'], desc:'负责公众号/小红书内容策划与撰写。' },
  { title:'视频剪辑', company:'无忧传媒', location:'杭州', salaryMin:8, salaryMax:16, source:'Boss直聘', industry:'文化传媒', type:'全职', edu:'大专', exp:'1-3年', skills:['PR','AE','剪映','节奏感'], desc:'负责短视频剪辑、包装与成片输出。' },
  { title:'编导/文案策划', company:'湖南卫视', location:'长沙', salaryMin:9, salaryMax:18, source:'智联', industry:'文化传媒', type:'全职', edu:'本科', exp:'2-4年', skills:['脚本','策划','镜头语言','创意'], desc:'负责节目/短视频脚本撰写与拍摄统筹。' },
  { title:'直播主播', company:'遥望网络', location:'杭州', salaryMin:8, salaryMax:30, source:'Boss直聘', industry:'文化传媒', type:'全职', edu:'大专', exp:'1-3年', skills:['直播控场','带货','镜头感','表达'], desc:'负责电商直播带货与粉丝互动。' },
  { title:'记者', company:'南方报业', location:'广州', salaryMin:6, salaryMax:13, source:'智联', industry:'文化传媒', type:'全职', edu:'本科', exp:'2-4年', skills:['采访','写作','新闻敏感','摄影'], desc:'负责时政/社会新闻采访与稿件撰写。' },

  // ================= 职能办公 =================
  { title:'人力资源总监', company:'中信集团', location:'北京', salaryMin:40, salaryMax:70, source:'智联', industry:'职能办公', type:'全职', edu:'硕士', exp:'10-15年', skills:['HR战略','组织发展','薪酬绩效','人才盘点','劳动法'], desc:'统筹集团人力资源体系升级与人才梯队建设。' },
  { title:'会计/出纳', company:'海尔', location:'青岛', salaryMin:6, salaryMax:12, source:'51job', industry:'职能办公', type:'全职', edu:'本科', exp:'2-4年', skills:['会计核算','金蝶','用友','税务申报','财务报表'], desc:'负责公司日常账务处理与税务申报。' },
  { title:'行政助理', company:'格力', location:'珠海', salaryMin:5, salaryMax:9, source:'51job', industry:'职能办公', type:'全职', edu:'大专', exp:'1-2年', skills:['办公软件','文书撰写','会议管理','档案管理','沟通协调'], desc:'负责部门日常行政事务与会议安排。' },
  { title:'客服主管', company:'京东', location:'宿迁', salaryMin:8, salaryMax:15, source:'51job', industry:'职能办公', type:'全职', edu:'大专', exp:'2-5年', skills:['客服管理','团队建设','投诉处理','CRM','数据分析'], desc:'管理 30 人客服团队，提升服务满意度。' },
  { title:'法务专员', company:'金杜律所', location:'北京', salaryMin:10, salaryMax:22, source:'智联', industry:'职能办公', type:'全职', edu:'本科', exp:'2-5年', skills:['合同审查','法律风险','法律检索','司考A证'], desc:'负责公司合同审查与法律风险防控。' },
  { title:'招聘专员(HR)', company:'BOSS直聘', location:'北京', salaryMin:7, salaryMax:13, source:'Boss直聘', industry:'职能办公', type:'全职', edu:'本科', exp:'1-3年', skills:['招聘渠道','面试安排','人才mapping','沟通'], desc:'负责公司社招/校招渠道运营与面试协调。' },

  // ================= 农林牧渔 =================
  { title:'养殖技术员', company:'牧原股份', location:'南阳', salaryMin:6, salaryMax:12, source:'51job', industry:'农林牧渔', type:'全职', edu:'大专', exp:'1-3年', skills:['畜禽养殖','疫病防控','饲养管理','现场'], desc:'负责规模养殖场日常饲养与技术管理。' },
  { title:'农艺师', company:'中化农业', location:'济南', salaryMin:7, salaryMax:14, source:'智联', industry:'农林牧渔', type:'全职', edu:'本科', exp:'2-5年', skills:['作物栽培','植保','土壤','农技推广'], desc:'负责种植基地作物管理与农技指导。' },
  { title:'园艺师', company:'东方园林', location:'成都', salaryMin:6, salaryMax:12, source:'Boss直聘', industry:'农林牧渔', type:'全职', edu:'大专', exp:'2-4年', skills:['园林养护','苗木','造景','修剪'], desc:'负责园林景观植物养护与造型。' },
  { title:'畜牧兽医', company:'温氏股份', location:'云浮', salaryMin:7, salaryMax:14, source:'51job', industry:'农林牧渔', type:'全职', edu:'大专', exp:'2-5年', skills:['兽医','疫病诊断','疫苗','执业兽医证'], desc:'负责畜禽疾病诊断、免疫与健康管理。' },

  // ================= 销售 =================
  { title:'销售经理', company:'华为', location:'南京', salaryMin:15, salaryMax:30, source:'51job', industry:'销售', type:'全职', edu:'大专', exp:'3-8年', skills:['销售管理','渠道开拓','大客户维护','谈判','ERP'], desc:'负责华东区企业业务客户拓展与销售团队管理。' },
  { title:'房产置业顾问', company:'链家', location:'上海', salaryMin:8, salaryMax:25, source:'Boss直聘', industry:'销售', type:'全职', edu:'高中', exp:'1-3年', skills:['房产销售','客户带看','谈判','抗压'], desc:'负责二手房/新房带看、撮合与成交。' },
  { title:'汽车销售顾问', company:'宝马4S店', location:'深圳', salaryMin:8, salaryMax:22, source:'51job', industry:'销售', type:'全职', edu:'中专', exp:'1-3年', skills:['汽车知识','试驾','客户跟进','驾照'], desc:'负责门店汽车展示、试驾与销售转化。' },
  { title:'大客户销售(B2B)', company:'用友网络', location:'北京', salaryMin:12, salaryMax:28, source:'Boss直聘', industry:'销售', type:'全职', edu:'本科', exp:'2-5年', skills:['B2B销售','方案讲解','招投标','客户关系'], desc:'负责企业软件解决方案的客户拓展。' },

  // ================= 游戏 =================
  { title:'游戏客户端主程', company:'米哈游', location:'上海', salaryMin:40, salaryMax:70, source:'Boss直聘', industry:'游戏', type:'全职', edu:'本科', exp:'5-10年', skills:['Unity','C#','图形学','性能优化','Lua'], desc:'负责开放世界游戏客户端核心系统开发。' },
  { title:'游戏策划', company:'莉莉丝', location:'上海', salaryMin:20, salaryMax:40, source:'拉勾', industry:'游戏', type:'全职', edu:'本科', exp:'3-5年', skills:['游戏策划','数值设计','关卡设计','用户研究','Excel'], desc:'负责 SLG 游戏数值体系与关卡设计。' },

  // ================= 公务员 / 事业单位 =================
  { title:'社区工作者', company:'街道办事处', location:'北京', salaryMin:5, salaryMax:8, source:'51job', industry:'公务员/事业单位', type:'全职', edu:'大专', exp:'1-3年', skills:['群众工作','公文写作','组织协调','耐心'], desc:'负责社区网格管理、政策宣传与居民服务。' },
  { title:'网格员', company:'社区服务中心', location:'杭州', salaryMin:4, salaryMax:7, source:'51job', industry:'公务员/事业单位', type:'全职', edu:'高中', exp:'1-2年', skills:['信息采集','巡查','沟通','责任心'], desc:'负责责任网格内信息采集与安全巡查。' },
  { title:'事业单位招考(综合岗)', company:'某事业单位', location:'武汉', salaryMin:6, salaryMax:12, source:'智联', industry:'公务员/事业单位', type:'全职', edu:'本科', exp:'应届', skills:['笔试','面试','公文','综合素质'], desc:'各地事业单位公开招聘，综合管理与服务岗位。' },
];

// ────── 把原始数组扩展为完整对象 ──────
const JOB_LIST = JOBS.map((j, i) => {
  const tags = ['来源:' + j.source, '学历:' + j.edu, '经验:' + j.exp, '类型:' + j.type, ...j.skills].join(',');
  return {
    id: 'ext-' + (i + 1),
    title: j.title,
    company: j.company,
    location: j.location,
    salaryMin: j.salaryMin,
    salaryMax: j.salaryMax,
    industry: j.industry,
    description: j.desc,
    requirementTags: tags,
    // 单独保留 source 字段方便前端直接使用
    source: j.source,
  };
});

// ────── 动态合成辅助 ──────
const FRESH_TITLES = ['高级前端工程师','Java资深开发','Python算法工程师','产品总监','UI/UX主管','技术VP','数据分析师','测试经理','架构师','全栈开发','运维负责人','增长黑客','项目经理','AI产品经理','DevOps工程师'];
const FRESH_COMPANIES = ['星火科技','云端智能','光年互动','蓝鲸数据','智领未来','脉动科技','蜂窝创新','拓荒者','零重力','元象科技','深声科技','裂变科技','触角科技','灵境AR','智造未来'];
const FRESH_LOCATIONS = ['北京','上海','深圳','杭州','广州','成都'];
const FRESH_INDUSTRIES = ['互联网/IT','金融','教育','医疗健康','服务业','游戏','设计创意'];
const FRESH_SOURCES = ['猎聘','Boss直聘','拉勾','智联','51job','牛客内推'];
const FRESH_SKILL_POOL = [['Vue.js','React','TypeScript'],['Java','Spring Boot','MySQL'],['Python','TensorFlow','PyTorch'],['Figma','Sketch','用户研究'],['Go','K8s','分布式'],['Swift','Flutter','iOS']];

function shuffleArray(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

function generateFreshJobs(count, now) {
  const fresh = [];
  for (let i = 0; i < count; i++) {
    const ts = now.toString(36);
    const rand = Math.random().toString(36).slice(2, 7);
    const id = 'ext-fresh-' + ts + '-' + rand;
    const comp = FRESH_COMPANIES[Math.floor(Math.random() * FRESH_COMPANIES.length)];
    const title = FRESH_TITLES[Math.floor(Math.random() * FRESH_TITLES.length)];
    const loc = FRESH_LOCATIONS[Math.floor(Math.random() * FRESH_LOCATIONS.length)];
    const ind = FRESH_INDUSTRIES[Math.floor(Math.random() * FRESH_INDUSTRIES.length)];
    const src = FRESH_SOURCES[Math.floor(Math.random() * FRESH_SOURCES.length)];
    const skillSet = FRESH_SKILL_POOL[Math.floor(Math.random() * FRESH_SKILL_POOL.length)];
    const salaryLo = 8 + Math.floor(Math.random() * 30);
    const salaryHi = salaryLo + 10 + Math.floor(Math.random() * 40);
    const expOptions = ['1-3年','3-5年','5-10年'];
    const eduOptions = ['大专','本科','硕士'];
    const typeOptions = ['全职','全职','全职','实习']; // 3/4 全职
    const tags = ['来源:' + src, '学历:' + eduOptions[Math.floor(Math.random() * eduOptions.length)], '经验:' + expOptions[Math.floor(Math.random() * expOptions.length)], '类型:' + typeOptions[Math.floor(Math.random() * typeOptions.length)], ...skillSet].join(',');
    fresh.push({
      id,
      title,
      company: comp,
      location: loc,
      salaryMin: salaryLo,
      salaryMax: salaryHi,
      industry: ind,
      description: '【新发布】' + comp + '诚聘' + title + '，薪资优厚，团队氛围好，欢迎投递简历加入我们！',
      requirementTags: tags,
      source: src,
      postedAgo: '刚刚',
      isFresh: true,
    });
  }
  return fresh;
}

function formatPostedAgo(now, minutes) {
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return minutes + '分钟前';
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return hours + '小时前';
  const days = Math.floor(hours / 24);
  return days + '天前';
}

// ────── 辅助解析 tags ──────
function parseTags(tags) {
  const out = { source: null, edu: null, exp: null, type: null, skills: [] };
  if (!tags) return out;
  tags.split(',').map(t => t.trim()).filter(Boolean).forEach(t => {
    if (t.startsWith('来源:')) out.source = t.slice(3);
    else if (t.startsWith('学历:')) out.edu = t.slice(3);
    else if (t.startsWith('经验:')) out.exp = t.slice(3);
    else if (t.startsWith('类型:')) out.type = t.slice(3);
    else out.skills.push(t);
  });
  return out;
}

// ────── GET /api/jobs ──────
app.get('/api/jobs', (req, res) => {
  let list = [...JOB_LIST];

  const { keyword, city, source, type, edu, industry, salaryMin, salaryMax } = req.query;

  if (keyword) {
    const kw = keyword.toLowerCase();
    list = list.filter(j =>
      j.title.toLowerCase().includes(kw) ||
      j.company.toLowerCase().includes(kw) ||
      j.description.toLowerCase().includes(kw)
    );
  }
  if (city) list = list.filter(j => j.location === city);
  if (source) list = list.filter(j => j.source === source);
  if (type) list = list.filter(j => parseTags(j.requirementTags).type === type);
  if (edu) list = list.filter(j => parseTags(j.requirementTags).edu === edu);
  if (industry) list = list.filter(j => j.industry === industry);
  if (salaryMin) list = list.filter(j => (j.salaryMax || 0) >= Number(salaryMin));
  if (salaryMax) list = list.filter(j => (j.salaryMin || 0) <= Number(salaryMax));

  // ── 动态化：打乱顺序 + 注入新鲜度 ──
  shuffleArray(list);
  const now = Date.now();
  const enriched = list.map((job, i) => ({
    ...job,
    postedAgo: formatPostedAgo(now, Math.floor(Math.random() * 14400) + 1), // 随机1~14400分钟 (10天)
    isFresh: false,
  }));

  // 合成新岗位（3~5 条，仅当无关键词/筛选时）
  const noFilter = !keyword && !city && !source && !type && !edu && !industry && !salaryMin && !salaryMax;
  let freshJobs = [];
  if (noFilter) {
    const freshCount = 3 + Math.floor(Math.random() * 3); // 3-5
    freshJobs = generateFreshJobs(freshCount, now);
  }

  // 新岗位排前面
  const allJobs = [...freshJobs, ...enriched];

  // 筛选唯一值（基于 JOB_LIST 保持稳定）
  const allSources = [...new Set(JOB_LIST.map(j => j.source))].sort();
  const allCities = [...new Set(JOB_LIST.map(j => j.location))].sort();
  const allTypes = [...new Set(JOB_LIST.map(j => parseTags(j.requirementTags).type))].sort();
  const allEdus = [...new Set(JOB_LIST.map(j => parseTags(j.requirementTags).edu))].sort();
  const allIndustries = [...new Set(JOB_LIST.map(j => j.industry).filter(Boolean))].sort();

  res.json({
    total: allJobs.length,
    jobs: allJobs,
    meta: {
      cities: allCities,
      sources: allSources,
      types: allTypes,
      edus: allEdus,
      industries: allIndustries,
      serverTime: new Date().toISOString(),
      freshCount: freshJobs.length,
    }
  });
});

// ────── GET /api/jobs/stats ──────
app.get('/api/jobs/stats', (req, res) => {
  const sourceCount = {};
  const cityCount = {};
  const industryCount = {};
  JOB_LIST.forEach(j => {
    sourceCount[j.source] = (sourceCount[j.source] || 0) + 1;
    cityCount[j.location] = (cityCount[j.location] || 0) + 1;
    if (j.industry) industryCount[j.industry] = (industryCount[j.industry] || 0) + 1;
  });
  res.json({ total: JOB_LIST.length, sourceCount, cityCount, industryCount });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`📡 Job Service running at http://localhost:${PORT}`);
  console.log(`   Total jobs: ${JOB_LIST.length}`);
  console.log(`   Industries: ${[...new Set(JOB_LIST.map(j => j.industry))].length}`);
  console.log(`   GET /api/jobs        - 岗位列表(含筛选)`);
  console.log(`   GET /api/jobs/stats  - 统计数据`);
});
