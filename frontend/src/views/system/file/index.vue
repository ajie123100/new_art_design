<template>
  <div class="file-page">
    <ElCard class="art-table-card">
      <template #header>
        <span>文件管理</span>
      </template>
      <ElUpload drag :auto-upload="false" :show-file-list="false" @change="handleUpload">
        <div class="upload-inner">
          <i class="ri-upload-cloud-2-line upload-icon"></i>
          <div>选择文件上传</div>
        </div>
      </ElUpload>
      <ElTable class="mt-4" :data="uploadedFiles" row-key="url">
        <ElTableColumn prop="name" label="文件名" min-width="180" />
        <ElTableColumn prop="url" label="访问地址" min-width="260" show-overflow-tooltip />
        <ElTableColumn label="操作" width="180" align="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="fetchDownloadFile(row.url)">下载</ElButton>
            <ElButton link type="danger" @click="deleteFile(row.url)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { fetchDeleteFile, fetchDownloadFile, fetchUploadFile } from '@/api/system-manage'
  import type { UploadFile } from 'element-plus'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'File' })

  const uploadedFiles = ref<Array<{ name: string; url: string }>>([])

  const handleUpload = async (uploadFile: UploadFile) => {
    if (!uploadFile.raw) return
    const result = await fetchUploadFile(uploadFile.raw)
    uploadedFiles.value.unshift({ name: uploadFile.name, url: result.url })
  }

  const deleteFile = async (url: string) => {
    await fetchDeleteFile(url)
    uploadedFiles.value = uploadedFiles.value.filter((item) => item.url !== url)
    ElMessage.success('删除成功')
  }
</script>

<style scoped>
  .file-page {
    height: 100%;
  }

  .upload-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 24px 0;
  }

  .upload-icon {
    font-size: 32px;
    color: var(--el-color-primary);
  }
</style>
