<template>
  <div class="create-project-container">
    <el-card class="create-project-card">
      <template #header>
        <div class="card-header">
          <h3>创建标注项目</h3>
        </div>
      </template>
      
      <el-form 
        ref="formRef" 
        :model="projectForm" 
        :rules="rules" 
        label-width="120px" 
        class="project-form"
        v-loading="loading"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        
        <el-form-item label="项目描述" prop="description">
          <el-input 
            v-model="projectForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入项目描述"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="标注类别" prop="categories">
          <div class="categories-container">
            <div 
              v-for="(category, index) in projectForm.categories" 
              :key="index" 
              class="category-item"
            >
              <el-input 
                v-model="projectForm.categories[index]" 
                placeholder="请输入类别名称"
              >
                <template #append>
                  <el-button @click="removeCategory(index)" :disabled="projectForm.categories.length <= 1">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
            <el-button type="primary" plain @click="addCategory" class="add-category-btn">
              <el-icon><Plus /></el-icon>
              添加类别
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="标注人员" prop="userIds">
          <el-transfer
            v-model="projectForm.userIds"
            :data="userOptions"
            :titles="['可选用户', '已选用户']"
            :button-texts="['移除', '添加']"
            :props="{
              key: 'id',
              label: 'username'
            }"
          ></el-transfer>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm(formRef)">创建项目</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { get, post } from '../../../gongju/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const userOptions = ref<{id: number, username: string, nickname: string}[]>([])

// 表单数据
const projectForm = reactive({
  name: '',
  description: '',
  categories: ['人', '车', '动物'],
  userIds: [] as number[]
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  categories: [
    { 
      type: 'array', 
      required: true, 
      message: '请至少添加一个标注类别', 
      trigger: 'change' 
    }
  ]
})

// 添加类别
const addCategory = () => {
  projectForm.categories.push('')
}

// 删除类别
const removeCategory = (index: number) => {
  projectForm.categories.splice(index, 1)
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    // 使用管理员专用接口获取用户列表
    const res = await get('/admin/users?page=1&size=100')
    
    if (res.success) {
      userOptions.value = res.data.list.map((user: any) => ({
        id: user.id,
        username: user.username,
        nickname: user.nickname || user.username
      }))
    } else {
      ElMessage.error('加载用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    // 如果接口不存在，则使用空用户列表
    userOptions.value = []
    ElMessage.warning('获取用户列表失败，请联系系统管理员')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
      // 过滤掉空的类别
      const filteredCategories = projectForm.categories.filter(cat => cat.trim() !== '')
      
      if (filteredCategories.length === 0) {
        ElMessage.warning('请至少添加一个有效的标注类别')
        return
      }
      
      loading.value = true
      try {
        const res = await post('/annotation/project', {
          name: projectForm.name,
          description: projectForm.description,
          categories: filteredCategories,
          userIds: projectForm.userIds
        })
        
        if (res.success) {
          ElMessage.success('创建项目成功')
          router.push('/dashboard/admin/projects')
        } else {
          ElMessage.error(res.message || '创建项目失败')
        }
      } catch (error) {
        console.error('创建项目失败:', error)
        ElMessage.error('创建项目失败，请重试')
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
    }
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.create-project-container {
  padding: 20px;
  
  .create-project-card {
    .card-header {
      h3 {
        margin: 0;
        font-size: 18px;
      }
    }
    
    .project-form {
      margin-top: 20px;
      
      .categories-container {
        .category-item {
          margin-bottom: 10px;
        }
        
        .add-category-btn {
          width: 100%;
        }
      }
    }
  }
}
</style> 