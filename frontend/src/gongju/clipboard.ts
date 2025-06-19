/**
 * 通用剪贴板工具
 */

import { ElMessage } from 'element-plus'

/**
 * 复制文本到剪贴板
 * @param text 要复制的文本
 */
export const copyToClipboard = (text: string): void => {
  // 首先尝试使用现代Clipboard API
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text)
      .then(() => ElMessage.success('复制成功'))
      .catch(() => {
        // 如果API失败，回退到传统方法
        fallbackCopyToClipboard(text)
      })
  } else {
    // 不支持Clipboard API，使用传统方法
    fallbackCopyToClipboard(text)
  }
}

/**
 * 传统的剪贴板复制方法（作为备选）
 * @param text 要复制的文本
 */
const fallbackCopyToClipboard = (text: string): void => {
  try {
    // 1. 创建临时文本区域
    const textArea = document.createElement('textarea')
    textArea.value = text
    
    // 2. 设置样式使其不可见
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    textArea.style.zIndex = '-1000'
    textArea.style.opacity = '0'
    
    // 3. 添加到DOM
    document.body.appendChild(textArea)
    
    // 4. 选择并复制文本
    textArea.focus()
    textArea.select()
    
    const successful = document.execCommand('copy')
    
    // 5. 清理DOM
    document.body.removeChild(textArea)
    
    if (successful) {
      ElMessage.success('复制成功')
    } else {
      ElMessage.error('复制失败，请手动复制')
    }
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动复制')
  }
} 