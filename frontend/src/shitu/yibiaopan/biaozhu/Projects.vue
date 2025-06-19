<template>
  <div class="projects-container">
    <el-card class="projects-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <h3>标注项目列表</h3>
          </div>
          <div class="right" v-if="isAdmin">
            <el-button type="primary" @click="$router.push('/dashboard/admin/create-project')">
              <el-icon><Plus /></el-icon>
              创建项目
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="projects-list">
        <el-empty v-if="projects.length === 0 && !loading" description="暂无项目" />
        
        <el-row :gutter="20" v-else>
          <el-col 
            v-for="project in projects" 
            :key="project.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="8" 
            :xl="6"
          >
            <el-card shadow="hover" class="project-item">
              <div class="project-info">
                <h4 class="project-name">{{ project.name }}</h4>
                <p class="project-desc">{{ project.description || '暂无描述' }}</p>
                <div class="project-stats">
                  <el-progress 
                    :percentage="calculateProgress(project)" 
                    :status="project.status === 1 ? 'success' : ''"
                  />
                  <div class="stats-text">
                    已标注: {{ project.annotatedImages }}/{{ project.totalImages }} 张图片
                  </div>
                </div>
                <div class="project-footer">
                  <el-tag :type="project.status === 0 ? 'warning' : 'success'">
                    {{ project.status === 0 ? '进行中' : '已完成' }}
                  </el-tag>
                  <span class="project-date">{{ formatDate(project.createTime) }}</span>
                </div>
              </div>
              <div class="project-actions">
                <el-button type="primary" @click="viewProject(project.id)">进入项目</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { get } from '../../../gongju/request'

interface ProjectItem {
  id: number
  name: string
  description: string
  status: number
  createTime: string
  totalImages: number
  annotatedImages: number
}

const router = useRouter()
const loading = ref(false)
const projects = ref<ProjectItem[]>([])

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
const calculateProgress = (project: ProjectItem): number => {
  if (project.totalImages === 0) return 0
  return Math.round((project.annotatedImages / project.totalImages) * 100)
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 查看项目
const viewProject = (id: number) => {
  router.push(`/dashboard/annotation/project/${id}`)
}

// 加载项目列表
const loadProjects = async () => {
  loading.value = true
  try {
    const res = await get<{code: number, data: ProjectItem[], success: boolean}>('/annotation/projects')
    
    if (res.success) {
      projects.value = res.data
    } else {
      ElMessage.error('加载项目列表失败')
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style lang="scss" scoped>
.projects-container {
  padding: 20px;
}

.projects-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .left {
      h3 {
        margin: 0;
        font-size: 18px;
      }
    }
  }
  
  .projects-list {
    margin-top: 20px;
  }
}

.project-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  
  .project-info {
    flex: 1;
    
    .project-name {
      margin-top: 0;
      margin-bottom: 10px;
      font-size: 16px;
      font-weight: bold;
    }
    
    .project-desc {
      margin: 0 0 15px 0;
      color: #606266;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
      height: 40px;
    }
    
    .project-stats {
      margin-bottom: 15px;
      
      .stats-text {
        margin-top: 5px;
        color: #909399;
        font-size: 13px;
      }
    }
    
    .project-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      .project-date {
        color: #909399;
        font-size: 13px;
      }
    }
  }
  
  .project-actions {
    display: flex;
    justify-content: flex-end;
  }
}
</style> 