/**
 * 简历 Word (.docx) 生成工具
 * 从 Optimize.vue / Templates.vue 调用
 */
import {
  Document,
  Paragraph,
  TextRun,
  HeadingLevel,
  Table,
  TableRow,
  TableCell,
  WidthType,
  AlignmentType,
  BorderStyle,
  ShadingType,
  Packer
} from 'docx'

// ── Optimize 用：从 improvedContent 生成 .docx blob ──
export async function buildOptimizedDocxBlob(c, jobTitle) {
  const accent = '4A90B3'
  const children = []

  // 姓名
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 80 },
    children: [new TextRun({ text: c.name || '你的姓名', bold: true, size: 48, color: accent, font: 'Microsoft YaHei' })],
  }))

  // AI 优化角标
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 120 },
    children: [new TextRun({ text: 'AI 优化版', color: '5D9B71', size: 20, font: 'Microsoft YaHei', italics: true })],
  }))

  // 求职意向
  if (c.title || jobTitle) {
    children.push(new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 200 },
      children: [new TextRun({ text: c.title || jobTitle || '', size: 24, font: 'Microsoft YaHei', color: '444444' })],
    }))
  }

  // 联系方式
  const contactParts = [c.phone, c.email, c.location].filter(Boolean)
  if (contactParts.length) {
    children.push(new Paragraph({
      border: { bottom: { style: BorderStyle.SINGLE, size: 1, color: 'CCCCCC', space: 1 } },
      spacing: { after: 240 },
      children: [new TextRun({ text: contactParts.join('    '), size: 20, font: 'Microsoft YaHei', color: '777777' })],
    }))
  }

  // section 标题工厂
  function secTitle(text) {
    return new Paragraph({
      heading: HeadingLevel.HEADING_3,
      spacing: { before: 280, after: 120 },
      border: { left: { style: BorderStyle.SINGLE, size: 12, color: accent, space: 4 } },
      indent: { left: 100 },
      children: [new TextRun({ text, bold: true, size: 24, color: accent, font: 'Microsoft YaHei' })],
    })
  }

  // 无边框 TableCell 配置（避免重复）
  const noBorder = {
    top: { style: BorderStyle.NONE },
    bottom: { style: BorderStyle.NONE },
    left: { style: BorderStyle.NONE },
    right: { style: BorderStyle.NONE }
  }

  // 个人简介
  if (c.summary) {
    children.push(secTitle('个人简介'))
    children.push(new Paragraph({
      spacing: { after: 160 },
      children: [new TextRun({ text: c.summary, size: 21, font: 'Microsoft YaHei', color: '444444' })],
    }))
  }

  // 教育背景
  const edus = (c.educationList || []).filter(function(e) { return e.school })
  if (edus.length) {
    children.push(secTitle('教育背景'))
    edus.forEach(function(e) {
      var row = new TableRow({
        children: [
          new TableCell({ width: { size: 7000, type: WidthType.DXA }, borders: noBorder, children: [
            new Paragraph({ children: [new TextRun({ text: e.school + (e.major ? ' · ' + e.major : ''), bold: true, size: 22, font: 'Microsoft YaHei' })] })
          ]}),
          new TableCell({ width: { size: 2000, type: WidthType.DXA }, borders: noBorder, children: [
            new Paragraph({ alignment: AlignmentType.RIGHT, children: [new TextRun({ text: e.period || '', size: 20, font: 'Microsoft YaHei', color: '888888' })] })
          ]}),
        ]
      })
      children.push(row)
      if (e.degree) {
        children.push(new Paragraph({ spacing: { after: 80 }, children: [new TextRun({ text: e.degree, size: 20, font: 'Microsoft YaHei', color: '888888' })] }))
      }
    })
  }

  // 工作经历
  const works = (c.workList || []).filter(function(w) { return w.company })
  if (works.length) {
    children.push(secTitle('工作经历'))
    works.forEach(function(w) {
      children.push(new Paragraph({
        spacing: { before: 160, after: 40 },
        children: [
          new TextRun({ text: w.company + (w.title ? ' · ' + w.title : ''), bold: true, size: 22, font: 'Microsoft YaHei' }),
          new TextRun({ text: w.duration || '', size: 20, font: 'Microsoft YaHei', color: '888888' }),
        ],
      }))
      if (w.description) {
        children.push(new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text: w.description, size: 21, font: 'Microsoft YaHei', color: '555555' })] }))
      }
    })
  }

  // 项目经历
  const projs = (c.projectList || []).filter(function(p) { return p.name })
  if (projs.length) {
    children.push(secTitle('项目经历'))
    projs.forEach(function(p) {
      children.push(new Paragraph({
        spacing: { before: 160, after: 40 },
        children: [new TextRun({ text: p.name, bold: true, size: 22, font: 'Microsoft YaHei' })],
      }))
      if (p.description) {
        children.push(new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text: p.description, size: 21, font: 'Microsoft YaHei', color: '555555' })] }))
      }
    })
  }

  // 技能标签（胶囊效果）
  const skills = c.skillList || []
  if (skills.length) {
    children.push(secTitle('专业技能'))
    var skillCells = skills.map(function(s) {
      return new TableCell({
        margins: { top: 60, bottom: 60, left: 140, right: 140 },
        shading: { type: ShadingType.SOLID, color: accent },
        borders: noBorder,
        children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: s, size: 18, font: 'Microsoft YaHei', color: 'FFFFFF' })] })]
      })
    })
    children.push(new Table({
      width: { size: 100, type: WidthType.PERCENTAGE },
      rows: [new TableRow({ children: skillCells })],
    }))
  }

  var doc = new Document({
    sections: [{
      properties: {
        page: { margin: { top: 1000, right: 1100, bottom: 1000, left: 1100 } }
      },
      children: children
    }],
  })

  return Packer.toBlob(doc)
}

// ── Templates 用：从表单数据生成 .docx blob ──
export async function buildTemplateDocxBlob(form, tpl) {
  var accent = (tpl && tpl.accent) || '4A90B3'
  var children = []

  // 姓名
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 80 },
    children: [new TextRun({ text: form.name || '姓名', bold: true, size: 48, color: accent, font: 'Microsoft YaHei' })]
  }))

  // 求职意向
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: form.targetJob || form.title || '', size: 24, font: 'Microsoft YaHei', color: '444444' })]
  }))

  // 联系方式
  var contact = [form.phone, form.email, form.location].filter(Boolean).join('    ')
  if (contact) {
    children.push(new Paragraph({
      border: { bottom: { style: BorderStyle.SINGLE, size: 1, color: 'CCCCCC', space: 1 } },
      spacing: { after: 240 },
      children: [new TextRun({ text: contact, size: 20, font: 'Microsoft YaHei', color: '777777' })]
    }))
  }

  function h3(text) {
    return new Paragraph({
      heading: HeadingLevel.HEADING_3,
      spacing: { before: 280, after: 120 },
      border: { left: { style: BorderStyle.SINGLE, size: 12, color: accent, space: 4 } },
      indent: { left: 100 },
      children: [new TextRun({ text: text, bold: true, size: 24, color: accent, font: 'Microsoft YaHei' })]
    })
  }

  var nb = {
    top: { style: BorderStyle.NONE },
    bottom: { style: BorderStyle.NONE },
    left: { style: BorderStyle.NONE },
    right: { style: BorderStyle.NONE }
  }

  if (form.summary) {
    children.push(h3('个人简介'))
    children.push(new Paragraph({
      spacing: { after: 160 },
      children: [new TextRun({ text: form.summary, size: 21, font: 'Microsoft YaHei', color: '444444' })]
    }))
  }

  if (form.eduSchool) {
    children.push(h3('教育背景'))
    children.push(new Paragraph({
      spacing: { before: 120, after: 40 },
      children: [new TextRun({ text: form.eduSchool + (form.eduMajor ? ' · ' + form.eduMajor : ''), bold: true, size: 22, font: 'Microsoft YaHei' })]
    }))
    var eduRow = new TableRow({
      children: [
        new TableCell({ width: { size: 6000, type: WidthType.DXA }, borders: nb, children: [
          new Paragraph({ children: [new TextRun({ text: form.eduDegree || '', size: 20, font: 'Microsoft YaHei', color: '888888' })] })
        ]}),
        new TableCell({ width: { size: 3000, type: WidthType.DXA }, borders: nb, children: [
          new Paragraph({ alignment: AlignmentType.RIGHT, children: [new TextRun({ text: form.eduPeriod || '', size: 20, font: 'Microsoft YaHei', color: '888888' })] })
        ]})
      ]
    })
    children.push(eduRow)
  }

  if (form.workCompany) {
    children.push(h3('工作经历'))
    children.push(new Paragraph({
      spacing: { before: 120, after: 40 },
      children: [new TextRun({ text: form.workCompany + (form.workTitle ? ' · ' + form.workTitle : ''), bold: true, size: 22, font: 'Microsoft YaHei' })]
    }))
    if (form.workDuration) {
      children.push(new Paragraph({ spacing: { after: 40 }, children: [new TextRun({ text: form.workDuration, size: 20, font: 'Microsoft YaHei', color: '888888' })] }))
    }
    if (form.workDesc) {
      children.push(new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text: form.workDesc, size: 21, font: 'Microsoft YaHei', color: '555555' })] }))
    }
  }

  if (form.projName) {
    children.push(h3('项目经历'))
    children.push(new Paragraph({
      spacing: { before: 120, after: 40 },
      children: [new TextRun({ text: form.projName, bold: true, size: 22, font: 'Microsoft YaHei' })]
    }))
    if (form.projDesc) {
      children.push(new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text: form.projDesc, size: 21, font: 'Microsoft YaHei', color: '555555' })] }))
    }
  }

  if (form.skills) {
    var skArr = typeof form.skills === 'string' ? form.skills.split(',').map(function(s) { return s.trim() }).filter(Boolean) : form.skills
    if (skArr.length) {
      children.push(h3('专业技能'))
      children.push(new Table({
        width: { size: 100, type: WidthType.PERCENTAGE },
        rows: [new TableRow({
          children: skArr.map(function(s) {
            return new TableCell({
              margins: { top: 60, bottom: 60, left: 140, right: 140 },
              shading: { type: ShadingType.SOLID, color: accent },
              borders: nb,
              children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: s, size: 18, font: 'Microsoft YaHei', color: 'FFFFFF' })] })]
            })
          })
        })]
      }))
    }
  }

  var doc2 = new Document({
    sections: [{
      properties: { page: { margin: { top: 1000, right: 1100, bottom: 1000, left: 1100 } } },
      children: children
    }]
  })

  return Packer.toBlob(doc2)
}
