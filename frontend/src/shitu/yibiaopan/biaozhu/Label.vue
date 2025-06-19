<template>
  <div class="annotation-container">
    <div class="header">
      <div class="back-btn">
        <el-button @click="goBack" :icon="ArrowLeft">返回项目</el-button>
      </div>
      <div class="info">
        <h3>{{ projectName }}</h3>
        <span class="image-name">{{ imageName }}</span>
      </div>
      <div class="actions">
        <el-button type="primary" @click="saveAnnotation" :loading="saving">
          保存标注
        </el-button>
      </div>
    </div>
    
    <div class="main-content" v-loading="loading">
      <div class="annotation-area" ref="annotationArea">
        <div class="canvas-container" ref="canvasContainer">
          <canvas
            ref="imageCanvas"
            @mousedown="handleMouseDown"
            @mousemove="handleMouseMove"
            @mouseup="handleMouseUp"
            @mouseleave="handleMouseLeave"
          ></canvas>
          <canvas
            ref="drawCanvas"
            class="draw-canvas"
            @mousedown="handleMouseDown"
            @mousemove="handleMouseMove"
            @mouseup="handleMouseUp"
            @mouseleave="handleMouseLeave"
          ></canvas>
        </div>
      </div>
      
      <div class="sidebar">
        <div class="tools-panel">
          <h4>工具</h4>
          <el-radio-group v-model="currentTool" size="large">
            <el-radio-button value="rectangle">
              <el-icon><PictureFilled /></el-icon>
              矩形框
            </el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="categories-panel">
          <h4>标注类别</h4>
          <el-empty v-if="categories.length === 0" description="暂无类别" />
          <el-radio-group v-else v-model="currentCategory" class="categories-list">
            <el-radio
              v-for="category in categories"
              :key="category.id"
              :value="category.id"
              :style="{ borderLeftColor: category.color }"
              class="category-item"
            >
              {{ category.name }}
            </el-radio>
          </el-radio-group>
          
          <div class="add-category" v-if="isAdmin">
            <el-button type="primary" plain @click="showAddCategoryDialog">
              添加类别
            </el-button>
          </div>
        </div>
        
        <div class="annotations-panel">
          <h4>标注列表</h4>
          <el-empty v-if="annotations.length === 0" description="暂无标注" />
          <div v-else class="annotations-list">
            <div
              v-for="(annotation, index) in annotations"
              :key="index"
              class="annotation-item"
              :class="{ active: currentAnnotationIndex === index }"
              @click="selectAnnotation(index)"
              :style="{ borderLeftColor: getCategoryColor(annotation.categoryId) }"
            >
              <div class="annotation-info">
                <span class="annotation-category">{{ getCategoryName(annotation.categoryId) }}</span>
                <span class="annotation-coords">
                  {{ Math.round(annotation.x * 100) / 100 }},
                  {{ Math.round(annotation.y * 100) / 100 }},
                  {{ Math.round(annotation.width * 100) / 100 }},
                  {{ Math.round(annotation.height * 100) / 100 }}
                </span>
              </div>
              <div class="annotation-actions">
                <el-button
                  type="danger"
                  size="small"
                  circle
                  @click.stop="removeAnnotation(index)"
                  :icon="Delete"
                ></el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加类别对话框 -->
    <el-dialog
      v-model="addCategoryDialogVisible"
      title="添加标注类别"
      width="30%"
    >
      <el-form :model="newCategory" label-width="80px">
        <el-form-item label="类别名称">
          <el-input v-model="newCategory.name" placeholder="请输入类别名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="newCategory.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addCategoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addCategory" :loading="addingCategory">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete, PictureFilled } from '@element-plus/icons-vue'
import { get, post, put } from '../../../gongju/request'

interface Category {
  id: number
  name: string
  color: string
}

interface Annotation {
  x: number
  y: number
  width: number
  height: number
  categoryId: number
}

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.projectId))
const imageId = computed(() => Number(route.params.imageId))

const loading = ref(true)
const saving = ref(false)
const projectName = ref('')
const imageName = ref('')
const imageUrl = ref('')
const categories = ref<Category[]>([])
const annotations = ref<Annotation[]>([])

// 绘图相关
const annotationArea = ref<HTMLDivElement>()
const canvasContainer = ref<HTMLDivElement>()
const imageCanvas = ref<HTMLCanvasElement>()
const drawCanvas = ref<HTMLCanvasElement>()
const imageObj = ref<HTMLImageElement>()
const scale = ref(1)
const currentTool = ref('rectangle')
const currentCategory = ref<number>()
const currentAnnotationIndex = ref(-1)
const isDrawing = ref(false)
const startPoint = ref({ x: 0, y: 0 })
const endPoint = ref({ x: 0, y: 0 })

// 添加类别相关
const addCategoryDialogVisible = ref(false)
const newCategory = ref({ name: '', color: '#409EFF' })
const addingCategory = ref(false)

// 判断是否为管理员
const isAdmin = computed(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return false
  
  try {
    const user = JSON.parse(userStr)
    return user.roleType === 1
  } catch (e) {
    return false
  }
})

// 返回项目详情页
const goBack = () => {
  router.push(`/dashboard/annotation/project/${projectId.value}`)
}

// 加载项目和图片信息
const loadData = async () => {
  loading.value = true;
  try {
    // 加载项目信息
    const projectRes = await get<{code: number, data: any, success: boolean}>(
      `/annotation/project/${projectId.value}`
    );
    
    if (projectRes.success) {
      projectName.value = projectRes.data.name;
      categories.value = projectRes.data.categories || [];
      
      if (categories.value.length > 0) {
        currentCategory.value = categories.value[0].id;
      }
    } else {
      ElMessage.error('加载项目信息失败');
    }
    
    // 加载图片信息
    const imageRes = await get<{code: number, data: any, success: boolean}>(
      `/annotation/project/${projectId.value}/image/${imageId.value}`
    );
    
    if (imageRes.success) {
      imageName.value = imageRes.data.name;
      imageUrl.value = imageRes.data.url;
      
      console.log('图片信息:', imageRes.data);
      console.log('标注内容:', imageRes.data.annotationContent);
      console.log('标注内容类型:', typeof imageRes.data.annotationContent);
      
      // 重置标注数据
      annotations.value = [];
      
      if (imageRes.data.annotationContent) {
        try {
          // 确保标注内容是字符串
          let annotationContent = imageRes.data.annotationContent;
          if (typeof annotationContent !== 'string') {
            annotationContent = JSON.stringify(annotationContent);
            console.log('转换后的标注内容:', annotationContent);
          }
          
          // 解析标注数据
          const parsedData = JSON.parse(annotationContent);
          
          // 确保annotations是一个数组
          if (!Array.isArray(parsedData)) {
            console.error('标注数据不是数组格式:', parsedData);
            annotations.value = [];
          } else {
            console.log('成功解析标注数据，数量:', parsedData.length);
            
            // 验证标注数据的完整性并添加到annotations中
            parsedData.forEach((annotation, index) => {
              console.log(`标注 ${index}:`, annotation);
              if (annotation && 
                  annotation.x !== undefined && 
                  annotation.y !== undefined && 
                  annotation.width !== undefined && 
                  annotation.height !== undefined && 
                  annotation.categoryId !== undefined) {
                
                annotations.value.push({
                  x: Number(annotation.x),
                  y: Number(annotation.y),
                  width: Number(annotation.width),
                  height: Number(annotation.height),
                  categoryId: Number(annotation.categoryId)
                });
              } else {
                console.error(`标注 ${index} 数据不完整:`, annotation);
              }
            });
            
            console.log('处理后的标注数据:', annotations.value);
          }
        } catch (e) {
          console.error('解析标注数据失败:', e);
          annotations.value = [];
        }
      } else {
        console.log('没有标注数据');
        annotations.value = [];
      }
      
      // 加载图片
      loadImage();
    } else {
      ElMessage.error('加载图片信息失败');
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败，请重试');
  } finally {
    loading.value = false;
  }
}

// 加载图片
const loadImage = () => {
  console.log('开始加载图片:', imageUrl.value);
  
  if (!imageUrl.value) {
    console.error('图片URL为空');
    ElMessage.error('图片URL为空，无法加载');
    return;
  }
  
  imageObj.value = new Image();
  
  // 添加错误处理
  imageObj.value.onerror = (e) => {
    console.error('图片加载失败:', e);
    ElMessage.error('图片加载失败，请重试');
    loading.value = false;
  };
  
  imageObj.value.onload = () => {
    console.log('图片加载成功，尺寸:', imageObj.value?.width, 'x', imageObj.value?.height);
    
    // 使用nextTick确保DOM已经更新
    nextTick(() => {
      console.log('DOM更新后初始化画布');
      
      // 添加额外延迟，确保DOM元素完全渲染
      setTimeout(() => {
        console.log('DOM元素状态:', {
          imageCanvas: !!imageCanvas.value,
          drawCanvas: !!drawCanvas.value,
          canvasContainer: !!canvasContainer.value,
          imageObj: !!imageObj.value
        });
        
        if (!imageCanvas.value || !drawCanvas.value || !canvasContainer.value) {
          console.error('DOM元素未完全渲染，重试初始化');
          // 如果DOM元素未完全渲染，再次延迟尝试
          setTimeout(() => {
            initCanvas();
            
            // 再次使用nextTick确保画布初始化后再绘制标注
            nextTick(() => {
              console.log('准备绘制标注，数量:', annotations.value.length);
              drawAllAnnotations();
            });
          }, 300);
          return;
        }
        
        // 初始化画布
        initCanvas();
        
        // 再次使用nextTick确保画布初始化后再绘制标注
        nextTick(() => {
          console.log('准备绘制标注，数量:', annotations.value.length);
          drawAllAnnotations();
          
          // 额外的延迟，确保标注绘制完成
          setTimeout(() => {
            drawAllAnnotations();
          }, 200);
        });
      }, 300); // 增加延迟时间
    });
  };
  
  // 尝试使用代理API加载图片，避免跨域问题
  if (imageUrl.value.startsWith('http')) {
    try {
      // 检查是否是阿里云OSS链接
      const isOssUrl = imageUrl.value.includes('aliyuncs.com');
      
      if (isOssUrl) {
        console.log('使用代理API加载OSS图片');
        // 使用代理API加载图片
        imageObj.value.crossOrigin = 'anonymous';
        imageObj.value.src = `/api/image/proxy?url=${encodeURIComponent(imageUrl.value)}`;
      } else {
        // 直接加载图片
        imageObj.value.src = imageUrl.value;
      }
    } catch (error) {
      console.error('处理图片URL失败:', error);
      // 回退到直接加载
      imageObj.value.src = imageUrl.value;
    }
  } else {
    // 本地图片直接加载
    imageObj.value.src = imageUrl.value;
  }
}

// 初始化画布
const initCanvas = () => {
  if (!imageCanvas.value || !drawCanvas.value || !canvasContainer.value || !imageObj.value) {
    console.error('初始化画布失败: 缺少必要元素', {
      imageCanvas: !!imageCanvas.value,
      drawCanvas: !!drawCanvas.value,
      canvasContainer: !!canvasContainer.value,
      imageObj: !!imageObj.value
    });
    return;
  }
  
  // 计算合适的缩放比例，使图片适应容器
  const containerWidth = canvasContainer.value.clientWidth;
  const containerHeight = canvasContainer.value.clientHeight;
  const imgWidth = imageObj.value.width;
  const imgHeight = imageObj.value.height;
  
  console.log('容器尺寸:', containerWidth, 'x', containerHeight);
  console.log('图片原始尺寸:', imgWidth, 'x', imgHeight);
  
  const widthRatio = containerWidth / imgWidth;
  const heightRatio = containerHeight / imgHeight;
  
  // 取较小的比例，确保图片完全显示在容器内
  scale.value = Math.min(widthRatio, heightRatio, 1);
  
  console.log('计算的缩放比例:', scale.value);
  
  // 设置画布尺寸
  const scaledWidth = imgWidth * scale.value;
  const scaledHeight = imgHeight * scale.value;
  
  console.log('缩放后的尺寸:', scaledWidth, 'x', scaledHeight);
  
  imageCanvas.value.width = scaledWidth;
  imageCanvas.value.height = scaledHeight;
  drawCanvas.value.width = scaledWidth;
  drawCanvas.value.height = scaledHeight;
  
  // 绘制图片
  const ctx = imageCanvas.value.getContext('2d');
  if (ctx) {
    ctx.clearRect(0, 0, scaledWidth, scaledHeight);
    ctx.drawImage(imageObj.value, 0, 0, scaledWidth, scaledHeight);
    console.log('图片已绘制到画布');
  } else {
    console.error('获取画布上下文失败');
  }
}

// 清除绘图画布
const clearDrawCanvas = () => {
  if (!drawCanvas.value) {
    console.warn('清除画布失败: drawCanvas不存在');
    return;
  }
  
  const ctx = drawCanvas.value.getContext('2d');
  if (ctx) {
    ctx.clearRect(0, 0, drawCanvas.value.width, drawCanvas.value.height);
    console.log('绘图画布已清除');
  } else {
    console.error('获取绘图画布上下文失败');
  }
}

// 绘制所有标注
const drawAllAnnotations = () => {
  if (!drawCanvas.value) {
    console.warn('绘制标注失败: drawCanvas不存在');
    return;
  }
  
  clearDrawCanvas();
  
  const ctx = drawCanvas.value.getContext('2d');
  if (!ctx) {
    console.error('获取绘图上下文失败');
    return;
  }
  
  console.log('绘制所有标注，数量:', annotations.value.length);
  
  if (!annotations.value || annotations.value.length === 0) {
    console.log('没有标注需要绘制');
    return;
  }
  
  // 确保annotations是数组
  if (!Array.isArray(annotations.value)) {
    console.error('annotations不是数组:', annotations.value);
    return;
  }
  
  // 检查categories是否有数据
  if (!categories.value || categories.value.length === 0) {
    console.warn('没有类别数据，可能影响标注显示');
  }
  
  // 保存当前的绘图状态
  ctx.save();
  
  // 设置线条样式
  ctx.lineJoin = 'round';
  ctx.lineCap = 'round';
  
  annotations.value.forEach((annotation, index) => {
    try {
      if (!annotation || typeof annotation !== 'object') {
        console.error(`标注 ${index} 无效:`, annotation);
        return;
      }
      
      if (annotation.x === undefined || annotation.y === undefined || 
          annotation.width === undefined || annotation.height === undefined) {
        console.error(`标注 ${index} 数据不完整:`, annotation);
        return;
      }
      
      const categoryId = annotation.categoryId;
      if (categoryId === undefined) {
        console.error(`标注 ${index} 缺少类别ID:`, annotation);
        return;
      }
      
      const category = categories.value.find(c => c.id === categoryId);
      const color = category ? category.color : '#409EFF';
      
      const x = annotation.x * scale.value;
      const y = annotation.y * scale.value;
      const width = annotation.width * scale.value;
      const height = annotation.height * scale.value;
      
      console.log(`绘制标注框 ${index}:`, {
        x, y, width, height,
        originalX: annotation.x,
        originalY: annotation.y,
        originalWidth: annotation.width,
        originalHeight: annotation.height,
        categoryId,
        color,
        isSelected: index === currentAnnotationIndex.value
      });
      
      // 设置线条样式
      ctx.strokeStyle = color;
      ctx.lineWidth = index === currentAnnotationIndex.value ? 3 : 2;
      
      // 绘制矩形框
      ctx.beginPath();
      ctx.rect(x, y, width, height);
      ctx.stroke();
      
      // 绘制半透明填充
      ctx.fillStyle = `${color}33`; // 添加透明度
      ctx.fill();
      
      // 绘制类别名称
      if (category) {
        ctx.fillStyle = color;
        ctx.font = '12px Arial';
        ctx.fillText(category.name, x, y - 5);
      }
      
      // 如果是选中的标注，绘制控制点
      if (index === currentAnnotationIndex.value) {
        // 绘制控制点
        const controlPoints = [
          { x: x, y: y },                     // 左上
          { x: x + width / 2, y: y },         // 上中
          { x: x + width, y: y },             // 右上
          { x: x, y: y + height / 2 },        // 左中
          { x: x + width, y: y + height / 2 }, // 右中
          { x: x, y: y + height },            // 左下
          { x: x + width / 2, y: y + height }, // 下中
          { x: x + width, y: y + height }     // 右下
        ];
        
        // 绘制控制点
        ctx.fillStyle = '#ffffff';
        ctx.strokeStyle = color;
        ctx.lineWidth = 1;
        
        controlPoints.forEach(point => {
          ctx.beginPath();
          ctx.arc(point.x, point.y, 4, 0, Math.PI * 2);
          ctx.fill();
          ctx.stroke();
        });
      }
    } catch (error) {
      console.error(`绘制标注 ${index} 时出错:`, error);
    }
  });
  
  // 恢复绘图状态
  ctx.restore();
}

// 处理鼠标按下事件
const handleMouseDown = (e: MouseEvent) => {
  if (!drawCanvas.value || !currentCategory.value) return
  
  // 只有矩形工具支持绘制
  if (currentTool.value !== 'rectangle') return
  
  isDrawing.value = true
  
  const rect = drawCanvas.value.getBoundingClientRect()
  startPoint.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
  
  // 清除当前选中的标注
  currentAnnotationIndex.value = -1
}

// 处理鼠标移动事件
const handleMouseMove = (e: MouseEvent) => {
  if (!drawCanvas.value || !isDrawing.value) return
  
  const rect = drawCanvas.value.getBoundingClientRect()
  endPoint.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
  
  // 绘制临时矩形
  drawTempRectangle()
}

// 处理鼠标松开事件
const handleMouseUp = () => {
  if (!isDrawing.value || !currentCategory.value) return
  
  isDrawing.value = false
  
  // 添加新标注
  addAnnotation()
  
  // 重绘所有标注
  drawAllAnnotations()
}

// 处理鼠标离开事件
const handleMouseLeave = () => {
  if (isDrawing.value) {
    isDrawing.value = false
    drawAllAnnotations()
  }
}

// 绘制临时矩形
const drawTempRectangle = () => {
  if (!drawCanvas.value) return
  
  clearDrawCanvas()
  drawAllAnnotations()
  
  const ctx = drawCanvas.value.getContext('2d')
  if (!ctx) return
  
  const category = categories.value.find(c => c.id === currentCategory.value)
  const color = category ? category.color : '#409EFF'
  
  const x = Math.min(startPoint.value.x, endPoint.value.x)
  const y = Math.min(startPoint.value.y, endPoint.value.y)
  const width = Math.abs(endPoint.value.x - startPoint.value.x)
  const height = Math.abs(endPoint.value.y - startPoint.value.y)
  
  ctx.strokeStyle = color
  ctx.lineWidth = 2
  ctx.strokeRect(x, y, width, height)
}

// 添加新标注
const addAnnotation = () => {
  if (!drawCanvas.value || !currentCategory.value) return
  
  const x = Math.min(startPoint.value.x, endPoint.value.x)
  const y = Math.min(startPoint.value.y, endPoint.value.y)
  const width = Math.abs(endPoint.value.x - startPoint.value.x)
  const height = Math.abs(endPoint.value.y - startPoint.value.y)
  
  // 忽略太小的标注
  if (width < 5 || height < 5) return
  
  // 将坐标转换回原始图片坐标
  const originalX = x / scale.value
  const originalY = y / scale.value
  const originalWidth = width / scale.value
  const originalHeight = height / scale.value
  
  annotations.value.push({
    x: originalX,
    y: originalY,
    width: originalWidth,
    height: originalHeight,
    categoryId: currentCategory.value
  })
  
  // 选中新添加的标注
  currentAnnotationIndex.value = annotations.value.length - 1
}

// 选择标注
const selectAnnotation = (index: number) => {
  console.log('选中标注:', index, annotations.value[index]);
  currentAnnotationIndex.value = index;
  
  // 确保绘制画布已清除并重新绘制所有标注
  clearDrawCanvas();
  drawAllAnnotations();
  
  // 滚动到视图中间以确保选中的标注可见
  if (annotations.value[index] && drawCanvas.value) {
    const annotation = annotations.value[index];
    const x = annotation.x * scale.value;
    const y = annotation.y * scale.value;
    const width = annotation.width * scale.value;
    const height = annotation.height * scale.value;
    
    // 在控制台输出选中的标注信息
    console.log('选中标注框位置:', {
      x, y, width, height,
      scale: scale.value,
      original: annotation
    });
  }
}

// 删除标注
const removeAnnotation = (index: number) => {
  annotations.value.splice(index, 1)
  
  if (currentAnnotationIndex.value === index) {
    currentAnnotationIndex.value = -1
  } else if (currentAnnotationIndex.value > index) {
    currentAnnotationIndex.value--
  }
  
  drawAllAnnotations()
}

// 获取类别颜色
const getCategoryColor = (categoryId: number): string => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.color : '#409EFF'
}

// 获取类别名称
const getCategoryName = (categoryId: number): string => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '未知类别'
}

// 显示添加类别对话框
const showAddCategoryDialog = () => {
  addCategoryDialogVisible.value = true
  newCategory.value = { name: '', color: '#409EFF' }
}

// 添加类别
const addCategory = async () => {
  if (!newCategory.value.name) {
    ElMessage.warning('请输入类别名称')
    return
  }
  
  addingCategory.value = true
  try {
    const res = await post<{code: number, data: Category, success: boolean}>(
      `/annotation/project/${projectId.value}/category`,
      {
        name: newCategory.value.name,
        color: newCategory.value.color
      }
    )
    
    if (res.success) {
      categories.value.push(res.data)
      currentCategory.value = res.data.id
      addCategoryDialogVisible.value = false
      ElMessage.success('添加类别成功')
    } else {
      ElMessage.error('添加类别失败')
    }
  } catch (error) {
    console.error('添加类别失败:', error)
    ElMessage.error('添加类别失败，请重试')
  } finally {
    addingCategory.value = false
  }
}

// 保存标注
const saveAnnotation = async () => {
  saving.value = true;
  try {
    // 将标注数据转换为YOLO格式
    const yoloData = convertToYolo();
    
    // 打印调试信息
    console.log('保存标注数据:');
    console.log('- 标注数量:', annotations.value.length);
    console.log('- JSON数据:', JSON.stringify(annotations.value));
    console.log('- YOLO数据:', yoloData);
    
    if (annotations.value.length === 0) {
      ElMessage.warning('没有标注数据可保存');
      saving.value = false;
      return;
    }
    
    // 确保标注数据有效
    let hasInvalidData = false;
    annotations.value.forEach((annotation, index) => {
      if (!annotation || annotation.x === undefined || annotation.y === undefined || 
          annotation.width === undefined || annotation.height === undefined || 
          annotation.categoryId === undefined) {
        console.error(`标注 ${index} 数据不完整:`, annotation);
        hasInvalidData = true;
      }
    });
    
    if (hasInvalidData) {
      ElMessage.error('标注数据不完整，请重新标注');
      saving.value = false;
      return;
    }
    
    // 准备要保存的数据
    const annotationContent = JSON.stringify(annotations.value);
    
    console.log('发送到后端的数据:');
    console.log('- annotationContent:', annotationContent);
    console.log('- yoloContent:', yoloData);
    
    const res = await put<{code: number, success: boolean}>(
      `/annotation/project/${projectId.value}/image/${imageId.value}/annotation`,
      {
        annotationContent: annotationContent,
        yoloContent: yoloData
      }
    );
    
    if (res.success) {
      ElMessage.success('保存标注成功');
      // 重新加载数据以确保显示最新的标注
      await loadData();
    } else {
      ElMessage.error('保存标注失败');
    }
  } catch (error) {
    console.error('保存标注失败:', error);
    ElMessage.error('保存标注失败，请重试');
  } finally {
    saving.value = false;
  }
}

// 将标注数据转换为YOLO格式
const convertToYolo = (): string => {
  if (!imageObj.value) return ''
  
  const imgWidth = imageObj.value.width
  const imgHeight = imageObj.value.height
  
  console.log('图片尺寸:', imgWidth, 'x', imgHeight);
  
  const yoloLines = annotations.value.map(annotation => {
    // YOLO格式：<class_id> <center_x> <center_y> <width> <height>
    // 所有值都是相对于图像宽高的比例
    
    // 找到类别索引
    const categoryIndex = categories.value.findIndex(c => c.id === annotation.categoryId)
    if (categoryIndex === -1) {
      console.warn('未找到类别:', annotation.categoryId);
      return '';
    }
    
    // 计算中心点坐标和宽高（相对值）
    const centerX = (annotation.x + annotation.width / 2) / imgWidth
    const centerY = (annotation.y + annotation.height / 2) / imgHeight
    const relativeWidth = annotation.width / imgWidth
    const relativeHeight = annotation.height / imgHeight
    
    console.log('标注框:', {
      category: categories.value[categoryIndex].name,
      centerX, centerY, relativeWidth, relativeHeight
    });
    
    return `${categoryIndex} ${centerX.toFixed(6)} ${centerY.toFixed(6)} ${relativeWidth.toFixed(6)} ${relativeHeight.toFixed(6)}`
  }).filter(line => line !== '');
  
  return yoloLines.join('\n')
}

// 窗口大小变化时重新绘制
window.addEventListener('resize', () => {
  nextTick(() => {
    initCanvas()
    drawAllAnnotations()
  })
})

// 监听currentCategory变化
watch(currentCategory, () => {
  drawAllAnnotations()
})

// 监听annotations变化
watch(annotations, () => {
  console.log('标注数据已更新，重新绘制', annotations.value);
  nextTick(() => {
    drawAllAnnotations();
  });
}, { deep: true });

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.annotation-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid #eee;
  
  .info {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    h3 {
      margin: 0;
      font-size: 18px;
    }
    
    .image-name {
      font-size: 14px;
      color: #666;
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.annotation-area {
  flex: 1;
  padding: 20px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f8f8;
}

.canvas-container {
  position: relative;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

canvas {
  display: block;
}

.draw-canvas {
  position: absolute;
  top: 0;
  left: 0;
  cursor: crosshair;
}

.sidebar {
  width: 300px;
  border-left: 1px solid #eee;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  
  h4 {
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 16px;
  }
}

.tools-panel {
  .el-radio-group {
    display: flex;
    flex-wrap: wrap;
  }
}

.categories-panel {
  .categories-list {
    display: flex;
    flex-direction: column;
    gap: 5px;
    
    .category-item {
      display: block;
      padding: 8px 10px;
      border-left: 4px solid;
      background-color: #f9f9f9;
      border-radius: 0 4px 4px 0;
    }
  }
  
  .add-category {
    margin-top: 10px;
  }
}

.annotations-panel {
  flex: 1;
  overflow-y: auto;
  
  .annotations-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .annotation-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 10px;
      border-left: 4px solid;
      background-color: #f9f9f9;
      border-radius: 0 4px 4px 0;
      cursor: pointer;
      
      &.active {
        background-color: #ecf5ff;
      }
      
      .annotation-info {
        display: flex;
        flex-direction: column;
        
        .annotation-category {
          font-weight: bold;
        }
        
        .annotation-coords {
          font-size: 12px;
          color: #666;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
 