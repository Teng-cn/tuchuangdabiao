<template>
  <div class="project-detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="left">
            <h3>{{ project.name }}</h3>
            <el-tag :type="project.status === 0 ? 'warning' : 'success'" class="status-tag">
              {{ project.status === 0 ? '进行中' : '已完成' }}
            </el-tag>
          </div>
          <div class="right">
            <el-button type="primary" v-if="isAdmin && project.status === 0" @click="completeProject">
              完成项目
            </el-button>
            <el-button type="success" v-if="isAdmin" @click="packageProject">
              <el-icon><Download /></el-icon>
              导出标注数据
            </el-button>
            <el-button v-if="isAdmin" @click="showAddImagesDialog">
              <el-icon><PictureFilled /></el-icon>
              添加图片
            </el-button>
            <el-button v-if="isAdmin" @click="showAddUsersDialog">
              <el-icon><UserFilled /></el-icon>
              添加用户
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="project-info" v-if="project.id">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="项目描述" :span="3">
            {{ project.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(project.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="图片数量">
            {{ project.totalImages }}
          </el-descriptions-item>
          <el-descriptions-item label="已标注">
            {{ project.annotatedImages }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="progress-bar">
          <h4>标注进度</h4>
          <el-progress 
            :percentage="calculateProgress()" 
            :status="project.status === 1 ? 'success' : ''"
          />
        </div>
        
        <div class="tabs-container">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="图片列表" name="images">
              <div class="images-list" v-loading="imagesLoading">
                <el-empty v-if="images.length === 0 && !imagesLoading" description="暂无图片" />
                
                <el-table v-else :data="images" style="width: 100%" border>
                  <el-table-column label="预览" width="120">
                    <template #default="scope">
                      <el-image 
                        style="width: 80px; height: 80px" 
                        :src="scope.row.url" 
                        :preview-src-list="[scope.row.url]"
                        fit="cover"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column prop="name" label="图片名称" />
                  <el-table-column label="标注状态" width="120">
                    <template #default="scope">
                      <el-tag :type="isImageAnnotated(scope.row) ? 'success' : 'info'">
                        {{ isImageAnnotated(scope.row) ? '已标注' : '未标注' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="180">
                    <template #default="scope">
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="goToLabel(scope.row.id)"
                      >
                        {{ isImageAnnotated(scope.row) ? '编辑标注' : '开始标注' }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                
                <div class="pagination-container">
                  <el-pagination
                    v-if="totalImages > 0"
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="totalImages"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="标注者" name="annotators">
              <div class="annotators-list">
                <el-empty v-if="project.annotators && project.annotators.length === 0" description="暂无标注者" />
                
                <el-table v-else :data="project.annotators" style="width: 100%" border>
                  <el-table-column label="头像" width="80">
                    <template #default="scope">
                      <el-avatar 
                        :size="40" 
                        :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                      />
                    </template>
                  </el-table-column>
                  <el-table-column prop="username" label="用户名" />
                  <el-table-column prop="nickname" label="昵称" />
                </el-table>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="标注类别" name="categories">
              <div class="categories-list">
                <el-empty v-if="project.categories && project.categories.length === 0" description="暂无标注类别" />
                
                <el-table v-else :data="project.categories" style="width: 100%" border>
                  <el-table-column label="颜色" width="80">
                    <template #default="scope">
                      <div class="color-block" :style="{ backgroundColor: scope.row.color }"></div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="name" label="类别名称" />
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-card>
    
    <!-- 添加图片对话框 -->
    <el-dialog
      v-model="addImagesDialogVisible"
      title="添加图片到项目"
      width="70%"
    >
      <div class="add-images-content" v-loading="allImagesLoading">
        <el-table
          :data="allImages"
          style="width: 100%"
          @selection-change="handleImagesSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="预览" width="120">
            <template #default="scope">
              <el-image 
                style="width: 80px; height: 80px" 
                :src="scope.row.url" 
                :preview-src-list="[scope.row.url]"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="图片名称" />
          <el-table-column prop="size" label="大小">
            <template #default="scope">
              {{ formatSize(scope.row.size) }}
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-if="allImagesTotal > 0"
            v-model:current-page="allImagesPage"
            v-model:page-size="allImagesPageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="allImagesTotal"
            @size-change="handleAllImagesSizeChange"
            @current-change="handleAllImagesPageChange"
          />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addImagesDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addImagesToProject" :loading="addingImages">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="addUsersDialogVisible"
      title="添加用户到项目"
      width="50%"
    >
      <div class="add-users-content" v-loading="allUsersLoading">
        <el-table
          :data="allUsers"
          style="width: 100%"
          @selection-change="handleUsersSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="头像" width="80">
            <template #default="scope">
              <el-avatar 
                :size="40" 
                :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
              />
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addUsersDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addUsersToProject" :loading="addingUsers">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, PictureFilled, UserFilled } from '@element-plus/icons-vue'
import { get, post, put } from '../../../gongju/request'
// JSZip已在index.html中通过CDN引入，在window对象上可用

interface ProjectDetail {
  id: number
  name: string
  description: string
  status: number
  createTime: string
  totalImages: number
  annotatedImages: number
  annotators: any[]
  categories: any[]
}

interface ImageItem {
  id: number
  name: string
  url: string
  size: number
  annotated: boolean
  annotationContent?: string
}

interface UserItem {
  id: number
  username: string
  nickname: string
  avatar: string
}

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.id))

const loading = ref(false)
const project = ref<ProjectDetail>({} as ProjectDetail)
const activeTab = ref('images')

// 图片列表相关
const imagesLoading = ref(false)
const images = ref<ImageItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalImages = ref(0)

// 添加图片相关
const addImagesDialogVisible = ref(false)
const allImagesLoading = ref(false)
const allImages = ref<any[]>([])
const allImagesPage = ref(1)
const allImagesPageSize = ref(10)
const allImagesTotal = ref(0)
const selectedImages = ref<any[]>([])
const addingImages = ref(false)

// 添加用户相关
const addUsersDialogVisible = ref(false)
const allUsersLoading = ref(false)
const allUsers = ref<UserItem[]>([])
const selectedUsers = ref<UserItem[]>([])
const addingUsers = ref(false)

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

// 计算标注进度
const calculateProgress = (): number => {
  if (project.value.totalImages === 0) return 0
  return Math.round((project.value.annotatedImages / project.value.totalImages) * 100)
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化文件大小
const formatSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 加载项目详情
const loadProjectDetail = async () => {
  loading.value = true
  try {
    const res = await get<{code: number, data: ProjectDetail, success: boolean}>(`/annotation/project/${projectId.value}`)
    
    if (res.success) {
      project.value = res.data
    } else {
      ElMessage.error('加载项目详情失败')
    }
  } catch (error) {
    console.error('加载项目详情失败:', error)
    ElMessage.error('加载项目详情失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载项目图片
const loadProjectImages = async () => {
  imagesLoading.value = true
  try {
    const res = await get<{code: number, data: any, success: boolean}>(
      `/annotation/project/${projectId.value}/images`, 
      { page: currentPage.value, size: pageSize.value }
    )
    
    if (res.success) {
      images.value = res.data.list
      totalImages.value = res.data.total
    } else {
      ElMessage.error('加载图片列表失败')
    }
  } catch (error) {
    console.error('加载图片列表失败:', error)
    ElMessage.error('加载图片列表失败，请重试')
  } finally {
    imagesLoading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadProjectImages()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadProjectImages()
}

// 去标注页面
const goToLabel = (imageId: number) => {
  console.log('跳转到标注页面:', projectId.value, imageId);
  router.push(`/dashboard/annotation/label/${projectId.value}/${imageId}`);
}

// 显示添加图片对话框
const showAddImagesDialog = () => {
  addImagesDialogVisible.value = true
  loadAllImages()
}

// 加载所有图片
const loadAllImages = async () => {
  allImagesLoading.value = true
  try {
    const res = await get<{code: number, data: any, success: boolean}>(
      '/image/list',
      { page: allImagesPage.value, size: allImagesPageSize.value }
    )
    
    if (res.success) {
      allImages.value = res.data.list
      allImagesTotal.value = res.data.total
    } else {
      ElMessage.error('加载图片失败')
    }
  } catch (error) {
    console.error('加载图片失败:', error)
    ElMessage.error('加载图片失败，请重试')
  } finally {
    allImagesLoading.value = false
  }
}

// 处理所有图片分页大小变化
const handleAllImagesSizeChange = (val: number) => {
  allImagesPageSize.value = val
  loadAllImages()
}

// 处理所有图片页码变化
const handleAllImagesPageChange = (val: number) => {
  allImagesPage.value = val
  loadAllImages()
}

// 处理图片选择变化
const handleImagesSelectionChange = (selection: any[]) => {
  selectedImages.value = selection
}

// 添加图片到项目
const addImagesToProject = async () => {
  if (selectedImages.value.length === 0) {
    ElMessage.warning('请至少选择一张图片')
    return
  }
  
  const imageIds = selectedImages.value.map(image => image.id)
  
  addingImages.value = true
  try {
    const res = await post<{code: number, success: boolean}>(
      `/annotation/project/${projectId.value}/images`,
      imageIds
    )
    
    if (res.success) {
      ElMessage.success('添加图片成功')
      addImagesDialogVisible.value = false
      // 重新加载项目详情和图片列表
      loadProjectDetail()
      loadProjectImages()
    } else {
      ElMessage.error('添加图片失败')
    }
  } catch (error) {
    console.error('添加图片失败:', error)
    ElMessage.error('添加图片失败，请重试')
  } finally {
    addingImages.value = false
  }
}

// 显示添加用户对话框
const showAddUsersDialog = () => {
  addUsersDialogVisible.value = true
  loadAllUsers()
}

// 加载所有用户
const loadAllUsers = async () => {
  allUsersLoading.value = true
  try {
    // 修改类型定义，正确处理分页数据结构
    const res = await get<{code: number, data: {list: UserItem[], total: number}, success: boolean}>('/admin/users', { page: 1, size: 1000 })
    
    if (res.success && res.data && res.data.list) {
      // 过滤掉已经在项目中的用户
      const existingUserIds = project.value.annotators.map(user => user.id)
      allUsers.value = res.data.list.filter(user => !existingUserIds.includes(user.id))
    } else {
      ElMessage.error('加载用户失败')
    }
  } catch (error) {
    console.error('加载用户失败:', error)
    ElMessage.error('加载用户失败，请重试')
  } finally {
    allUsersLoading.value = false
  }
}

// 处理用户选择变化
const handleUsersSelectionChange = (selection: UserItem[]) => {
  selectedUsers.value = selection
}

// 添加用户到项目
const addUsersToProject = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请至少选择一个用户')
    return
  }
  
  const userIds = selectedUsers.value.map(user => user.id)
  
  addingUsers.value = true
  try {
    const res = await post<{code: number, success: boolean}>(
      `/annotation/project/${projectId.value}/users`,
      userIds
    )
    
    if (res.success) {
      ElMessage.success('添加用户成功')
      addUsersDialogVisible.value = false
      // 重新加载项目详情
      loadProjectDetail()
    } else {
      ElMessage.error('添加用户失败')
    }
  } catch (error) {
    console.error('添加用户失败:', error)
    ElMessage.error('添加用户失败，请重试')
  } finally {
    addingUsers.value = false
  }
}

// 完成项目
const completeProject = async () => {
  ElMessageBox.confirm('确定要完成此项目吗？所有图片必须已标注。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await put<{code: number, success: boolean}>(
        `/annotation/project/${projectId.value}/complete`
      )
      
      if (res.success) {
        ElMessage.success('项目已完成')
        // 重新加载项目详情
        loadProjectDetail()
      } else {
        ElMessage.error('完成项目失败')
      }
    } catch (error: any) {
      console.error('完成项目失败:', error)
      if (error.response && error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('完成项目失败，请重试')
      }
    }
  }).catch(() => {
    // 取消操作
  })
}

// 导出项目数据
const packageProject = async () => {
  ElMessageBox.confirm('确定要导出此项目标注数据和图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      // 显示加载中提示
      const loadingInstance = ElMessage({
        type: 'info',
        message: '正在准备数据，请稍候...',
        duration: 0
      });
      
      // 获取项目所有图片的标注数据
      const res = await get<{code: number, data: any[], success: boolean}>(
        `/annotation/project/${projectId.value}/annotations`
      );
      
      console.log('获取到的标注数据:', res.data);
      
      if (!res.success || !res.data || res.data.length === 0) {
        loadingInstance.close();
        ElMessage.warning('没有找到标注数据');
        return;
      }
      
      // 更新加载提示
      loadingInstance.close();
      const progressInstance = ElMessage({
        type: 'info',
        message: `正在处理图片 (0/${res.data.length})...`,
        duration: 0
      }) as any; // 使用any类型避免TypeScript错误
      
      // 创建一个新的JSZip实例
      const zip = new window.JSZip();
      
      // 添加项目信息文件
      const projectInfo = `# 项目名称: ${project.value.name}\n` +
                          `# 项目描述: ${project.value.description || '无'}\n` +
                          `# 导出时间: ${new Date().toLocaleString()}\n\n` +
                          `# 标注类别:\n`;
      
      let categoryInfo = '';
      if (project.value.categories && project.value.categories.length > 0) {
        project.value.categories.forEach((category, index) => {
          categoryInfo += `# ${index}: ${category.name}\n`;
        });
      } else {
        categoryInfo = '# 无类别\n';
      }
      
      // 保存项目信息和类别信息
      zip.file("project_info.txt", projectInfo + categoryInfo);
      
      // 创建classes.txt文件（YOLO格式需要）
      let classesContent = '';
      if (project.value.categories && project.value.categories.length > 0) {
        project.value.categories.forEach(category => {
          classesContent += `${category.name}\n`;
        });
      }
      zip.file("classes.txt", classesContent);
      
      // 添加README文件
      const readmeContent = `# ${project.value.name} 标注数据导出
      
## 文件结构说明

- \`project_info.txt\`: 项目基本信息和类别列表
- \`classes.txt\`: YOLO格式训练所需的类别列表
- \`labels/\`: 包含YOLO格式的标注文件（每个图片对应一个.txt文件）
- \`annotations/\`: 包含详细的标注信息（每个图片对应一个_detailed.txt文件）
- \`images/\`: 包含通过代理API下载的图片

## 标注格式说明

### YOLO格式 (labels/*.txt)
每行一个标注框，格式为：
\`\`\`
<类别索引> <中心点X> <中心点Y> <宽度> <高度>
\`\`\`
所有值都是相对于图像宽高的比例（0-1之间的浮点数）。

### 详细格式 (annotations/*_detailed.txt)
每行一个标注框，格式为：
\`\`\`
<类别索引>, <类别名称>, <左上角X>, <左上角Y>, <宽度>, <高度>, <中心点X>, <中心点Y>
\`\`\`

## 图片下载说明

本导出包中的 \`images/\` 文件夹包含通过后端代理API下载的图片。

如果有图片下载失败，您可以在浏览器中访问 \`/api/image/proxy?url=图片URL\` 手动下载图片。

## 导出时间
${new Date().toLocaleString()}
`;
      
      zip.file("README.md", readmeContent);
      
      // 下载图片和创建标注数据
      const downloadPromises = [];
      let completedCount = 0;
      let successCount = 0;
      let errorCount = 0;
      
      console.log(`开始处理 ${res.data.length} 张图片的标注数据`);
      
      for (const item of res.data) {
        if (item.name && item.annotations && item.annotations.length > 0) {
          // 创建一个标记图片处理的Promise
          const imagePromise = new Promise<void>((resolve) => {
            console.log(`处理图片: ${item.name}, 标注数量: ${item.annotations.length}`);
            console.log(`图片URL: ${item.url}`);

            // 使用后端代理API下载图片
            console.log(`通过后端代理下载图片: ${item.name}`);
            
            // 处理标注数据的函数
            const processAnnotations = () => {
              // 为每个图片创建单独的YOLO格式标注文件
              let yoloContent = '';
              
              // 构建YOLO格式标注内容
              item.annotations.forEach((ann: any, index: number) => {
                console.log(`处理标注 #${index}: 类别=${ann.categoryIndex}, 坐标=(${ann.centerX}, ${ann.centerY}), 尺寸=(${ann.width}, ${ann.height})`);
                
                // YOLO格式: <class_id> <center_x> <center_y> <width> <height>
                yoloContent += `${ann.categoryIndex} ${ann.centerX} ${ann.centerY} ${ann.width} ${ann.height}\n`;
              });
              
              console.log(`为图片 ${item.name} 创建YOLO标注文件，内容长度: ${yoloContent.length}`);
              
              // 保存YOLO格式标注文件到labels文件夹（与图片同名，但扩展名为.txt）
              const fileNameWithoutExt = item.name.substring(0, item.name.lastIndexOf('.')) || item.name;
              zip.file(`labels/${fileNameWithoutExt}.txt`, yoloContent);
              
              // 同时创建一个详细的标注文件，包含更多信息
              let detailedContent = `# 图片名称: ${item.name}\n`;
              detailedContent += `# 图片URL: ${item.url}\n`;
              detailedContent += `# 标注时间: ${new Date().toLocaleString()}\n\n`;
              detailedContent += `# 标注数据:\n`;
              detailedContent += `# 格式: 类别ID, 类别名称, 左上角X, 左上角Y, 宽度, 高度, 中心点X, 中心点Y\n\n`;
              
              item.annotations.forEach((ann: any) => {
                // 使用后端返回的类别名称
                const categoryName = ann.categoryName || '未知类别';
                
                // 使用后端返回的原始坐标（左上角坐标）
                const x = ann.x !== undefined ? ann.x : (ann.centerX - ann.width / 2);
                const y = ann.y !== undefined ? ann.y : (ann.centerY - ann.height / 2);
                
                detailedContent += `${ann.categoryIndex}, ${categoryName}, ${x.toFixed(6)}, ${y.toFixed(6)}, ${ann.width.toFixed(6)}, ${ann.height.toFixed(6)}, ${ann.centerX.toFixed(6)}, ${ann.centerY.toFixed(6)}\n`;
              });
              
              console.log(`为图片 ${item.name} 创建详细标注文件，内容长度: ${detailedContent.length}`);
              
              // 保存详细标注文件
              zip.file(`annotations/${fileNameWithoutExt}_detailed.txt`, detailedContent);
              
              // 更新成功计数
              successCount++;
              
              // 更新进度
              completedCount++;
              progressInstance.message = `正在处理图片 (${completedCount}/${res.data.length})...`;
              
              // 完成此图片的处理
              resolve();
            };
            
            // 使用后端代理API获取图片
            fetch(`/api/image/proxy?url=${encodeURIComponent(item.url)}`)
              .then(response => {
                if (!response.ok) {
                  throw new Error(`图片下载失败: ${item.name}, 状态码: ${response.status}`);
                }
                return response.blob();
              })
              .then(blob => {
                console.log(`图片 ${item.name} 下载成功, 大小: ${blob.size} 字节`);
                
                // 添加图片到images文件夹
                zip.file(`images/${item.name}`, blob);
                
                // 继续处理标注数据
                processAnnotations();
              })
              .catch(error => {
                console.error(`下载图片 ${item.name} 失败:`, error);
                console.log('继续处理标注数据，但不包含图片');
                
                // 即使图片下载失败，也继续处理标注数据
                processAnnotations();
              });
          });
          
          downloadPromises.push(imagePromise);
        } else {
          console.warn(`跳过图片 ${item.name || '未命名'}: 没有标注数据`);
        }
      }
      
      // 等待所有图片下载完成
      await Promise.all(downloadPromises);
      
      console.log(`所有图片处理完成: 成功=${successCount}, 失败=${errorCount}`);
      
      // 创建一个空的文件夹标记文件，确保即使没有标注数据也能创建文件夹
      if (successCount === 0) {
        zip.file('labels/.keep', '');
        zip.file('annotations/.keep', '');
      }
      
      // 更新进度提示
      progressInstance.close();
      const zipInstance = ElMessage({
        type: 'info',
        message: '正在生成ZIP文件，请稍候...',
        duration: 0
      });
      
      // 生成ZIP文件
      const zipBlob = await zip.generateAsync({
        type: 'blob',
        compression: 'DEFLATE',
        compressionOptions: {
          level: 9 // 最高压缩级别
        }
      });
      
      console.log(`ZIP文件生成完成，大小: ${zipBlob.size} 字节`);
      
      // 关闭加载提示
      zipInstance.close();
      
      // 下载ZIP文件
      const url = URL.createObjectURL(zipBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${project.value.name}-data.zip`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      
      ElMessage.success(`标注数据已导出: ${successCount} 张图片的标注数据处理成功`);
    } catch (error: any) {
      console.error('导出数据失败:', error);
      
      // 尝试提取详细错误信息
      let errorMessage = '导出数据失败，请重试';
      if (error.response && error.response.data) {
        if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      ElMessage.error(errorMessage);
    }
  }).catch(() => {
    // 取消操作
  });
}

// 判断图片是否已标注
const isImageAnnotated = (image: ImageItem): boolean => {
  // 检查annotated属性
  if (image.annotated === true) {
    return true;
  }
  
  // 检查是否有标注内容
  if (image.annotationContent && image.annotationContent.length > 0) {
    return true;
  }
  
  return false;
}

onMounted(() => {
  loadProjectDetail()
  loadProjectImages()
})
</script>

<style lang="scss" scoped>
.project-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .left {
    display: flex;
    align-items: center;
    
    h3 {
      margin: 0;
      margin-right: 10px;
      font-size: 18px;
    }
  }
  
  .right {
    display: flex;
    gap: 10px;
  }
}

.project-info {
  margin-top: 20px;
}

.progress-bar {
  margin: 20px 0;
  
  h4 {
    margin-top: 0;
    margin-bottom: 10px;
  }
}

.tabs-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.color-block {
  width: 30px;
  height: 30px;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
