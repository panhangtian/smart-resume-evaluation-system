<template>
  <div class="page-wrapper">
    <!-- ===== 顶部 Hero 区 ===== -->
    <div class="hero">
      <div class="hero-text">
        <h1 class="hero-title">简历模板 <span class="hero-emoji">📄</span></h1>
        <p class="hero-sub">挑一个喜欢的风格，填写内容即可一键导出 HTML / PDF</p>
        <div class="hero-stats">
          <div class="hs-item">
            <div class="hs-num">{{ templates.length }}</div>
            <div class="hs-lbl">精选模板</div>
          </div>
          <div class="hs-divider"></div>
          <div class="hs-item">
            <div class="hs-num">3</div>
            <div class="hs-lbl">导出格式</div>
          </div>
          <div class="hs-divider"></div>
          <div class="hs-item">
            <div class="hs-num">∞</div>
            <div class="hs-lbl">免费使用</div>
          </div>
        </div>
      </div>
      <div class="hero-deco">
        <div class="deco-card d1"></div>
        <div class="deco-card d2"></div>
        <div class="deco-card d3"></div>
      </div>
    </div>

    <!-- ===== 分类筛选 ===== -->
    <div class="filter-bar no-print">
      <div class="filter-label">🎨 风格分类</div>
      <div class="filter-chips">
        <span v-for="c in categories" :key="c" :class="['fchip', { active: activeCat === c }]" @click="activeCat = c">{{ c }}</span>
      </div>
    </div>

    <!-- ===== 模板卡片网格 ===== -->
    <div class="tpl-grid no-print">
      <div v-for="t in filteredTemplates" :key="t.id"
        :class="['tpl-card', { active: form.tpl === t.id }]"
        :style="{ '--accent': t.accent, '--accent-soft': t.accentSoft }"
        @click="useTpl(t)">
        <!-- 迷你预览 -->
        <div :class="['tpl-mini', 'mini-' + t.id]">
          <div class="mini-head">
            <div class="mini-name"></div>
            <div class="mini-title"></div>
            <div class="mini-contact">
              <span></span><span></span><span></span>
            </div>
          </div>
          <div class="mini-sec">
            <div class="mini-h"></div>
            <div class="mini-line w90"></div>
            <div class="mini-line w70"></div>
          </div>
          <div class="mini-sec">
            <div class="mini-h"></div>
            <div class="mini-line w80"></div>
            <div class="mini-line w60"></div>
            <div class="mini-line w90"></div>
          </div>
          <div class="mini-sec">
            <div class="mini-h"></div>
            <div class="mini-skills">
              <span></span><span></span><span></span><span></span>
            </div>
          </div>
        </div>
        <!-- 信息区 -->
        <div class="tpl-info">
          <div class="tpl-info-head">
            <span class="tpl-swatch" :style="{ background: t.accent }"></span>
            <div class="tpl-name">{{ t.name }}</div>
            <span class="tpl-tag" v-for="tg in t.tags" :key="tg">{{ tg }}</span>
          </div>
          <div class="tpl-desc">{{ t.desc }}</div>
          <div class="tpl-info-foot">
            <el-button :type="form.tpl === t.id ? 'primary' : 'default'" size="small" :icon="form.tpl === t.id ? Check : ''">
              {{ form.tpl === t.id ? '正在使用' : '使用此模板' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 编辑区 ===== -->
    <div class="edit-head no-print">
      <h3 class="edit-title">📝 编辑简历</h3>
      <div class="edit-meta">
        当前模板：<b :style="{ color: currentTpl.accent }">{{ currentTpl.name }}</b>
        <span style="color:var(--color-text-muted);margin-left:8px">{{ currentTpl.desc }}</span>
      </div>
    </div>

    <el-row :gutter="20" class="edit-area">
      <!-- 左：编辑表单 -->
      <el-col :xs="24" :lg="10">
        <div class="section-card no-print">
          <h4 class="blk-title">✍️ 填写简历内容</h4>
          <el-form label-width="68px" label-position="left">
            <el-divider content-position="left">基本信息</el-divider>
            <el-form-item label="姓名"><el-input v-model="form.name" placeholder="如：张三" /></el-form-item>
            <el-form-item label="求职意向"><el-input v-model="form.title" placeholder="如：高级前端工程师" /></el-form-item>
            <el-form-item label="电话"><el-input v-model="form.phone" placeholder="138-0000-0000" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="name@example.com" /></el-form-item>
            <el-form-item label="所在地"><el-input v-model="form.location" placeholder="城市" /></el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="一句话概括你的核心优势" />
            </el-form-item>

            <el-divider content-position="left">教育背景</el-divider>
            <div v-for="(e, i) in form.education" :key="'e' + i" class="dyn-row">
              <el-input v-model="e.school" placeholder="学校" style="width:46%" />
              <el-input v-model="e.major" placeholder="专业" style="width:38%" />
              <el-button circle size="small" type="danger" plain @click="form.education.splice(i, 1)">−</el-button>
              <el-input v-model="e.degree" placeholder="学历" style="width:30%;margin-top:6px" />
              <el-input v-model="e.period" placeholder="时间 如 2015-2019" style="width:56%;margin-top:6px" />
            </div>
            <el-button text type="primary" size="small" @click="form.education.push({ school: '', major: '', degree: '', period: '' })">+ 添加教育</el-button>

            <el-divider content-position="left">工作经历</el-divider>
            <div v-for="(w, i) in form.work" :key="'w' + i" class="dyn-row">
              <el-input v-model="w.company" placeholder="公司" style="width:46%" />
              <el-input v-model="w.title" placeholder="职位" style="width:38%" />
              <el-button circle size="small" type="danger" plain @click="form.work.splice(i, 1)">−</el-button>
              <el-input v-model="w.period" placeholder="时间" style="width:88%;margin-top:6px" />
              <el-input v-model="w.desc" type="textarea" :rows="2" placeholder="工作内容与业绩" style="width:88%;margin-top:6px" />
            </div>
            <el-button text type="primary" size="small" @click="form.work.push({ company: '', title: '', period: '', desc: '' })">+ 添加工作</el-button>

            <el-divider content-position="left">项目经历</el-divider>
            <div v-for="(p, i) in form.projects" :key="'p' + i" class="dyn-row">
              <el-input v-model="p.name" placeholder="项目名称" style="width:70%" />
              <el-button circle size="small" type="danger" plain @click="form.projects.splice(i, 1)">−</el-button>
              <el-input v-model="p.desc" type="textarea" :rows="2" placeholder="项目描述与产出" style="width:88%;margin-top:6px" />
            </div>
            <el-button text type="primary" size="small" @click="form.projects.push({ name: '', desc: '' })">+ 添加项目</el-button>

            <el-divider content-position="left">专业技能</el-divider>
            <div class="skill-edit">
              <el-tag v-for="(s, i) in form.skills" :key="i" closable @close="form.skills.splice(i, 1)" style="margin:3px">{{ s }}</el-tag>
              <el-input v-model="skillInput" size="small" placeholder="输入后回车添加" style="width:150px;margin:3px"
                @keyup.enter="addSkill" />
            </div>
          </el-form>
        </div>
      </el-col>

      <!-- 右：实时预览 -->
      <el-col :xs="24" :lg="14">
        <div class="section-card preview-card">
          <div class="preview-toolbar no-print">
            <span class="pv-label">实时预览 · {{ currentTpl.name }}</span>
            <div class="toolbar-actions">
              <el-button size="small" @click="printResume"><el-icon><Printer /></el-icon> 打印 / 导出 PDF</el-button>
              <el-button size="small" type="success" @click="downloadDocx"><el-icon><Download /></el-icon> 下载 Word</el-button>
              <el-button size="small" type="primary" @click="downloadHtml"><el-icon><Download /></el-icon> 下载 HTML</el-button>
            </div>
          </div>
          <div class="pv-paper">
            <div class="resume-doc" :class="'tpl-' + form.tpl" :style="{ '--t-accent': currentTpl.accent }">
              <header class="rd-head">
                <div class="rd-name">{{ form.name || '你的姓名' }}</div>
                <div class="rd-title">{{ form.title || '求职意向' }}</div>
                <div class="rd-contact" v-if="form.phone || form.email || form.location">
                  {{ [form.phone, form.email, form.location].filter(Boolean).join(' · ') }}
                </div>
              </header>

              <section v-if="form.summary" class="rd-sec">
                <h3>个人简介</h3>
                <p class="rd-summary">{{ form.summary }}</p>
              </section>

              <section v-if="form.education.filter(e => e.school || e.major).length" class="rd-sec">
                <h3>教育背景</h3>
                <div v-for="(e, i) in form.education.filter(x => x.school || x.major)" :key="'pe' + i" class="rd-item">
                  <div class="rd-item-h"><span>{{ e.school }}<em v-if="e.major"> · {{ e.major }}</em></span><span class="rd-mut">{{ e.period }}</span></div>
                  <div class="rd-mut" v-if="e.degree">{{ e.degree }}</div>
                </div>
              </section>

              <section v-if="form.work.filter(w => w.company || w.title).length" class="rd-sec">
                <h3>工作经历</h3>
                <div v-for="(w, i) in form.work.filter(x => x.company || x.title)" :key="'pw' + i" class="rd-item">
                  <div class="rd-item-h"><span>{{ w.company }}<em v-if="w.title"> · {{ w.title }}</em></span><span class="rd-mut">{{ w.period }}</span></div>
                  <div class="rd-item-d" v-if="w.desc">{{ w.desc }}</div>
                </div>
              </section>

              <section v-if="form.projects.filter(p => p.name).length" class="rd-sec">
                <h3>项目经历</h3>
                <div v-for="(p, i) in form.projects.filter(x => x.name)" :key="'pp' + i" class="rd-item">
                  <div class="rd-item-h"><span>{{ p.name }}</span></div>
                  <div class="rd-item-d" v-if="p.desc">{{ p.desc }}</div>
                </div>
              </section>

              <section v-if="form.skills.length" class="rd-sec">
                <h3>专业技能</h3>
                <div class="rd-skills">
                  <span v-for="(s, i) in form.skills" :key="'ps' + i" class="rd-skill">{{ s }}</span>
                </div>
              </section>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Printer, Download, Check } from '@element-plus/icons-vue'
import {
  Document, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Packer, BorderStyle, ShadingType, Table, TableCell, TableRow, WidthType
} from 'docx'

const templates = [
  { id: 'classic',  name: '经典商务', desc: '稳重克制，适合传统行业 / 国企 / 银行', accent: '#243B4A', accentSoft: 'rgba(36,59,74,0.08)',  tags: ['通用', '商务'] },
  { id: 'modern',   name: '现代简约', desc: '大留白干净，适合互联网 / 外企',           accent: '#4A90B3', accentSoft: 'rgba(74,144,179,0.10)', tags: ['简约', '通用'] },
  { id: 'tech',     name: '技术风向', desc: '强调技能与项目，适合研发 / 工程师',         accent: '#2F6E4E', accentSoft: 'rgba(47,110,78,0.10)',  tags: ['技术', '通用'] },
  { id: 'creative', name: '创意活力', desc: '高亮笔点缀，适合设计 / 市场 / 运营',         accent: '#C75B39', accentSoft: 'rgba(199,91,57,0.12)',  tags: ['创意', '通用'] }
]

// 分类筛选
const categories = computed(() => {
  const set = new Set(['全部'])
  templates.forEach(t => t.tags.forEach(tg => set.add(tg)))
  return [...set]
})
const activeCat = ref('全部')
const filteredTemplates = computed(() => {
  if (activeCat.value === '全部') return templates
  return templates.filter(t => t.tags.includes(activeCat.value))
})

const currentTpl = computed(() => templates.find(t => t.id === form.tpl) || templates[0])

const skillInput = ref('')

const form = reactive({
  tpl: 'classic',
  name: '张三',
  title: '高级前端工程师',
  phone: '138-0000-0000',
  email: 'zhangsan@example.com',
  location: '上海',
  summary: '5 年前端开发经验，擅长 Vue / React 与前端工程化体系建设，主导过多个百万级用户产品的前端架构。',
  education: [{ school: '某某大学', major: '计算机科学与技术', degree: '本科', period: '2015 - 2019' }],
  work: [{ company: '某某科技有限公司', title: '前端工程师', period: '2019 - 至今', desc: '负责核心业务系统前端架构，推动微前端改造，首屏性能提升 40%。' }],
  projects: [{ name: 'XX 平台重构', desc: '主导前端微前端改造与组件库建设，研发效率提升 30%。' }],
  skills: ['JavaScript', 'TypeScript', 'Vue', 'React', 'Webpack', 'Node.js', 'Vite']
})

function useTpl(t) {
  form.tpl = t.id
  ElMessage.success('已切换到「' + t.name + '」模板')
}

function addSkill() {
  const v = skillInput.value.trim()
  if (v && !form.skills.includes(v)) form.skills.push(v)
  skillInput.value = ''
}

function printResume() { window.print() }

function esc(s) {
  return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function buildHtml() {
  const a = currentTpl.value.accent
  const edu = form.education.filter(e => e.school || e.major).map(e =>
    `<div class="it"><div class="ih"><span>${esc(e.school)}${e.major ? ' · ' + esc(e.major) : ''}</span><span class="mut">${esc(e.period)}</span></div>${e.degree ? `<div class="mut">${esc(e.degree)}</div>` : ''}</div>`).join('')
  const work = form.work.filter(w => w.company || w.title).map(w =>
    `<div class="it"><div class="ih"><span>${esc(w.company)}${w.title ? ' · ' + esc(w.title) : ''}</span><span class="mut">${esc(w.period)}</span></div>${w.desc ? `<div class="id">${esc(w.desc)}</div>` : ''}</div>`).join('')
  const proj = form.projects.filter(p => p.name).map(p =>
    `<div class="it"><div class="ih"><span>${esc(p.name)}</span></div>${p.desc ? `<div class="id">${esc(p.desc)}</div>` : ''}</div>`).join('')
  const skills = form.skills.map(s => `<span class="sk">${esc(s)}</span>`).join('')
  const contact = [form.phone, form.email, form.location].filter(Boolean).map(esc).join(' · ')

  const css = `
    *{box-sizing:border-box;margin:0;padding:0}
    body{font-family:-apple-system,'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;color:#243B4A;background:#fff;padding:40px;line-height:1.6}
    .rd{max-width:780px;margin:0 auto;--t-accent:${a}}
    .rd-head{border-bottom:3px solid var(--t-accent);padding-bottom:14px;margin-bottom:18px}
    .rd-name{font-size:30px;font-weight:800;color:var(--t-accent)}
    .rd-title{font-size:16px;color:#444;margin-top:4px;font-weight:600}
    .rd-contact{font-size:13px;color:#777;margin-top:6px}
    .rd-sec{margin-bottom:18px}
    .rd-sec h3{font-size:15px;color:var(--t-accent);border-left:4px solid var(--t-accent);padding-left:8px;margin-bottom:10px}
    .rd-summary{font-size:14px;color:#444}
    .it{margin-bottom:12px}
    .ih{display:flex;justify-content:space-between;font-weight:600;font-size:14px}
    .ih em{font-style:normal;font-weight:400;color:#555}
    .mut{color:#888;font-size:12px;font-weight:400}
    .id{font-size:13px;color:#555;margin-top:3px}
    .rd-skills{display:flex;flex-wrap:wrap;gap:8px}
    .sk{background:var(--t-accent);color:#fff;font-size:12px;padding:4px 12px;border-radius:14px}
    .tpl-modern .rd-head{border-bottom:1px solid #ccc}
    .tpl-tech .sk{background:#2F6E4E}
    .tpl-creative .rd-name{background:linear-gradient(transparent 60%, rgba(199,91,57,.25) 0)}
  `
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><title>${esc(form.name)}的简历</title><style>${css}</style></head><body>
  <div class="rd tpl-${form.tpl}">
    <header class="rd-head">
      <div class="rd-name">${esc(form.name) || '你的姓名'}</div>
      <div class="rd-title">${esc(form.title) || '求职意向'}</div>
      ${contact ? `<div class="rd-contact">${contact}</div>` : ''}
    </header>
    ${form.summary ? `<section class="rd-sec"><h3>个人简介</h3><p class="rd-summary">${esc(form.summary)}</p></section>` : ''}
    ${edu ? `<section class="rd-sec"><h3>教育背景</h3>${edu}</section>` : ''}
    ${work ? `<section class="rd-sec"><h3>工作经历</h3>${work}</section>` : ''}
    ${proj ? `<section class="rd-sec"><h3>项目经历</h3>${proj}</section>` : ''}
    ${skills ? `<section class="rd-sec"><h3>专业技能</h3><div class="rd-skills">${skills}</div></section>` : ''}
  </div></body></html>`
}

function downloadHtml() {
  const html = buildHtml()
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `简历-${form.name || 'template'}.html`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('已下载 HTML 简历，可用浏览器打开并打印为 PDF')
}

// 16 进制颜色去掉 #  →  docx 库用 6 位 hex
function hexNoHash(c) { return (c || '243B4A').replace('#', '') }

// 把 "FFFFFF" 转成 docx 的 shading fill 字符串
function shade(c) { return hexNoHash(c).toUpperCase() }

// 一行主标题
function heading(text) {
  return new Paragraph({
    spacing: { before: 260, after: 140 },
    border: { bottom: { color: hexNoHash(currentTpl.value.accent), space: 4, style: BorderStyle.SINGLE, size: 14 } },
    children: [
      new TextRun({ text: text || '', bold: true, size: 26, color: hexNoHash(currentTpl.value.accent), font: 'Microsoft YaHei' })
    ]
  })
}

function para(text, opts = {}) {
  return new Paragraph({
    spacing: { after: 80, line: 320 },
    children: [
      new TextRun({ text: text || '', size: opts.size || 21, color: opts.color || '243B4A', font: 'Microsoft YaHei' })
    ]
  })
}

function itemLine(left, right, isBold = true) {
  const children = [
    new TextRun({ text: left || '', bold: isBold, size: 21, color: '243B4A', font: 'Microsoft YaHei' })
  ]
  if (right) {
    children.push(new TextRun({ text: '    ' + right, size: 19, color: '888888', font: 'Microsoft YaHei' }))
  }
  return new Paragraph({ spacing: { after: 50, line: 300 }, children })
}

function skillRow(items) {
  // 用带底色的 cell 实现"胶囊"效果
  const accent = hexNoHash(currentTpl.value.accent)
  const cells = items.map(s =>
    new TableCell({
      width: { size: 14, type: WidthType.PERCENTAGE },
      margins: { top: 60, bottom: 60, left: 140, right: 140 },
      shading: { type: ShadingType.CLEAR, fill: accent },
      children: [new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: s, size: 19, color: 'FFFFFF', font: 'Microsoft YaHei' })]
      })]
    })
  )
  return new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    borders: {
      top: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
      bottom: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
      left: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
      right: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
      insideHorizontal: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
      insideVertical: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' }
    },
    rows: [new TableRow({ children: cells })]
  })
}

async function downloadDocx() {
  const accent = hexNoHash(currentTpl.value.accent)
  const children = []

  // 头部：姓名 + 求职意向 + 联系方式
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 60 },
    children: [new TextRun({
      text: form.name || '你的姓名',
      bold: true, size: 48, color: accent, font: 'Microsoft YaHei'
    })]
  }))
  if (form.title) {
    children.push(new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 40 },
      children: [new TextRun({ text: form.title, size: 24, color: '444444', font: 'Microsoft YaHei' })]
    }))
  }
  const contact = [form.phone, form.email, form.location].filter(Boolean).join(' · ')
  if (contact) {
    children.push(new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 240, before: 40 },
      border: { bottom: { color: accent, space: 8, style: BorderStyle.SINGLE, size: 16 } },
      children: [new TextRun({ text: contact, size: 20, color: '777777', font: 'Microsoft YaHei' })]
    }))
  } else {
    children.push(new Paragraph({ spacing: { after: 120 }, children: [] }))
  }

  // 个人简介
  if (form.summary) {
    children.push(heading('个人简介'))
    children.push(para(form.summary))
  }

  // 教育背景
  const edu = form.education.filter(e => e.school || e.major)
  if (edu.length) {
    children.push(heading('教育背景'))
    edu.forEach(e => {
      const left = (e.school || '') + (e.major ? ' · ' + e.major : '')
      children.push(itemLine(left, e.period))
      if (e.degree) children.push(para(e.degree, { size: 19, color: '888888' }))
    })
  }

  // 工作经历
  const work = form.work.filter(w => w.company || w.title)
  if (work.length) {
    children.push(heading('工作经历'))
    work.forEach(w => {
      const left = (w.company || '') + (w.title ? ' · ' + w.title : '')
      children.push(itemLine(left, w.period))
      if (w.desc) children.push(para(w.desc, { size: 20, color: '555555' }))
    })
  }

  // 项目经历
  const proj = form.projects.filter(p => p.name)
  if (proj.length) {
    children.push(heading('项目经历'))
    proj.forEach(p => {
      children.push(itemLine(p.name, ''))
      if (p.desc) children.push(para(p.desc, { size: 20, color: '555555' }))
    })
  }

  // 专业技能
  if (form.skills.length) {
    children.push(heading('专业技能'))
    // 每行最多 4 个技能
    const rows = []
    for (let i = 0; i < form.skills.length; i += 4) {
      rows.push(form.skills.slice(i, i + 4))
    }
    rows.forEach(r => children.push(skillRow(r)))
    children.push(new Paragraph({ spacing: { after: 60 }, children: [] }))
  }

  const doc = new Document({
    creator: '简历评估·推荐',
    title: (form.name || '简历') + ' 的简历',
    description: '通过简历评估·推荐平台生成',
    styles: {
      default: {
        document: { run: { font: 'Microsoft YaHei' } }
      }
    },
    sections: [{
      properties: {
        page: {
          margin: { top: 1000, right: 1100, bottom: 1000, left: 1100 }
        }
      },
      children
    }]
  })

  try {
    const blob = await Packer.toBlob(doc)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `简历-${form.name || 'template'}.docx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('已下载 Word (.docx) 简历，可用 Microsoft Word / WPS 打开')
  } catch (e) {
    ElMessage.error('生成 Word 失败：' + e.message)
  }
}
</script>

<style scoped>
.page-wrapper {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 24px 32px;
  box-sizing: border-box;
}

/* ===== Hero 区 ===== */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #FAF9F7 0%, #F4F1ED 100%);
  border: 1px solid #EDECE9;
  border-radius: 16px;
  padding: 32px 36px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}
.hero-text { flex: 1; z-index: 2; }
.hero-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  letter-spacing: 1px;
}
.hero-emoji { font-size: 26px; }
.hero-sub {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 18px;
}
.hero-stats {
  display: flex;
  align-items: center;
  gap: 18px;
}
.hs-item { display: flex; flex-direction: column; gap: 2px; }
.hs-num {
  font-size: 24px;
  font-weight: 800;
  color: var(--el-color-primary);
  line-height: 1;
}
.hs-lbl {
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 500;
}
.hs-divider {
  width: 1px;
  height: 24px;
  background: #D8D5CF;
}
.hero-deco {
  position: relative;
  width: 200px;
  height: 130px;
  flex-shrink: 0;
}
.deco-card {
  position: absolute;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(43, 43, 49, 0.10);
  border: 1px solid #E8E6E1;
}
.deco-card.d1 {
  width: 100px; height: 120px;
  top: 0; left: 0;
  transform: rotate(-6deg);
  background: linear-gradient(180deg, #243B4A 0 18px, #fff 18px);
}
.deco-card.d2 {
  width: 100px; height: 120px;
  top: 6px; left: 50px;
  transform: rotate(4deg);
  background: linear-gradient(180deg, #4A90B3 0 18px, #fff 18px);
}
.deco-card.d3 {
  width: 100px; height: 120px;
  top: 12px; left: 100px;
  transform: rotate(-2deg);
  background: linear-gradient(180deg, #C75B39 0 18px, #fff 18px);
}

/* ===== 分类筛选 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid #EDECE9;
  border-radius: 12px;
  padding: 10px 16px;
  margin-bottom: 16px;
}
.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}
.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.fchip {
  font-size: 12px;
  padding: 4px 14px;
  border-radius: 999px;
  background: #F5F4F2;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
  user-select: none;
}
.fchip:hover { background: #EBE9E5; }
.fchip.active {
  background: var(--el-color-primary);
  color: #fff;
  font-weight: 600;
}

/* ===== 模板卡片网格 ===== */
.tpl-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.tpl-card {
  background: #fff;
  border: 1px solid #EDECE9;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}
.tpl-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(43, 43, 49, 0.10);
  border-color: var(--accent);
}
.tpl-card.active {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-soft), 0 8px 24px rgba(43, 43, 49, 0.10);
}

/* 迷你预览 */
.tpl-mini {
  background: #F4F1ED;
  padding: 18px 18px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 200px;
  position: relative;
}
.tpl-mini::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, var(--accent-soft) 100%);
  opacity: 0.5;
  pointer-events: none;
}
.mini-head {
  background: #fff;
  border-radius: 4px;
  padding: 10px 12px;
  position: relative;
  z-index: 1;
}
.mini-name {
  width: 60%;
  height: 8px;
  border-radius: 2px;
  background: var(--accent);
  margin-bottom: 6px;
}
.mini-title {
  width: 40%;
  height: 5px;
  border-radius: 2px;
  background: #ccc;
  margin-bottom: 6px;
}
.mini-contact {
  display: flex;
  gap: 6px;
}
.mini-contact span {
  width: 50px;
  height: 4px;
  border-radius: 1px;
  background: #ddd;
}
.mini-sec {
  background: #fff;
  border-radius: 4px;
  padding: 8px 10px;
  position: relative;
  z-index: 1;
}
.mini-h {
  width: 30%;
  height: 5px;
  border-radius: 1px;
  background: var(--accent);
  margin-bottom: 6px;
}
.mini-line {
  height: 3px;
  border-radius: 1px;
  background: #E0DDD7;
  margin-bottom: 4px;
}
.mini-line.w90 { width: 90%; }
.mini-line.w80 { width: 80%; }
.mini-line.w70 { width: 70%; }
.mini-line.w60 { width: 60%; }
.mini-line.w50 { width: 50%; }
.mini-skills {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.mini-skills span {
  display: inline-block;
  height: 8px;
  width: 24px;
  border-radius: 4px;
  background: var(--accent);
  opacity: 0.7;
}
.mini-skills span:nth-child(2) { width: 32px; opacity: 0.6; }
.mini-skills span:nth-child(3) { width: 20px; opacity: 0.8; }
.mini-skills span:nth-child(4) { width: 28px; opacity: 0.5; }

/* 模板差异 - 迷你预览 */
.mini-classic .mini-name { background: #243B4A; }
.mini-modern .mini-head { border-bottom: 1px solid #ccc; }
.mini-tech .mini-skills span { background: #2F6E4E; }
.mini-creative .mini-name { background: linear-gradient(transparent 50%, rgba(199, 91, 57, 0.4) 50%); }

/* 信息区 */
.tpl-info {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.tpl-info-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.tpl-swatch {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  flex-shrink: 0;
}
.tpl-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  flex: 1;
}
.tpl-tag {
  font-size: 10px;
  padding: 1px 7px;
  border-radius: 4px;
  background: var(--accent-soft);
  color: var(--accent);
  font-weight: 500;
}
.tpl-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
  min-height: 36px;
}
.tpl-info-foot {
  display: flex;
  justify-content: flex-end;
  margin-top: 2px;
}

/* ===== 编辑区 ===== */
.edit-head {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-top: 8px;
  margin-bottom: 12px;
  padding: 0 4px;
}
.edit-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}
.edit-meta {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.edit-meta b { font-weight: 700; }

.blk-title { font-weight: 600; color: var(--color-text-primary); margin-bottom: 12px; }

.preview-card { padding: 0; overflow: hidden; }
.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #EDEDF0;
  background: var(--color-bg-card);
}
.pv-label { font-size: 13px; color: var(--color-text-secondary); font-weight: 600; }
.toolbar-actions { display: flex; gap: 6px; flex-wrap: wrap; }
.pv-paper {
  background: #F4F3EF;
  padding: 24px;
  display: flex;
  justify-content: center;
}
.resume-doc {
  width: 100%;
  max-width: 720px;
  background: #fff;
  padding: 36px 40px;
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(43, 43, 49, 0.1);
  --t-accent: #243B4A;
  color: #243B4A;
  font-size: 14px;
  line-height: 1.6;
}
.rd-head {
  border-bottom: 3px solid var(--t-accent);
  padding-bottom: 14px;
  margin-bottom: 18px;
}
.rd-name { font-size: 28px; font-weight: 800; color: var(--t-accent); }
.rd-title { font-size: 15px; color: #444; margin-top: 4px; font-weight: 600; }
.rd-contact { font-size: 12px; color: #777; margin-top: 6px; }
.rd-sec { margin-bottom: 18px; }
.rd-sec h3 {
  font-size: 14px;
  color: var(--t-accent);
  border-left: 4px solid var(--t-accent);
  padding-left: 8px;
  margin-bottom: 10px;
}
.rd-summary { color: #444; }
.rd-item { margin-bottom: 12px; }
.rd-item-h { display: flex; justify-content: space-between; font-weight: 600; }
.rd-item-h em { font-style: normal; font-weight: 400; color: #555; }
.rd-mut { color: #888; font-size: 12px; font-weight: 400; }
.rd-item-d { font-size: 13px; color: #555; margin-top: 3px; }
.rd-skills { display: flex; flex-wrap: wrap; gap: 8px; }
.rd-skill {
  background: var(--t-accent);
  color: #fff;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 14px;
}
.tpl-modern .rd-head { border-bottom: 1px solid #ccc; }
.tpl-tech .rd-skill { background: #2F6E4E; }
.tpl-creative .rd-name {
  background: linear-gradient(transparent 60%, rgba(199, 91, 57, 0.25) 0);
}
.dyn-row { margin-bottom: 8px; }
.dyn-row .el-input { margin-right: 4px; }
</style>

<style>
@media print {
  .app-aside, .app-header { display: none !important; }
  .el-main.app-main { padding: 0 !important; overflow: visible !important; }
  .no-print { display: none !important; }
  body { background: #fff !important; }
  .pv-paper { background: #fff !important; padding: 0 !important; }
  .resume-doc { box-shadow: none !important; max-width: 100% !important; }
}
</style>
