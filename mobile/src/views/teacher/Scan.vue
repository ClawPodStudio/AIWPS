<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import QRCode from 'qrcode'

const router = useRouter()
const showTestQR = ref(false)
const testQRUrl = ref('')

// Generate a test QR code
const generateTestQR = async () => {
  const testData = {
    paperId: 'P20240325001',
    institutionId: 1,
    title: '高一数学期中考试'
  }
  try {
    testQRUrl.value = await QRCode.toDataURL(JSON.stringify(testData))
  } catch (err) {
    console.error(err)
  }
}

onMounted(() => {
  // Simulate camera scan
  // In production, use html5-qrcode or vue-qrcode-reader
})

const handleScanResult = (result) => {
  try {
    const data = JSON.parse(result)
    if (data.paperId) {
      router.push(`/teacher/paper/${data.paperId}`)
    }
  } catch (e) {
    // If plain text, try as paper ID
    router.push(`/teacher/paper/${result}`)
  }
}

// Simulated scan for demo
const simulateScan = () => {
  const mockResult = JSON.stringify({
    paperId: 'P20240325001',
    institutionId: 1,
    title: '高一数学期中考试'
  })
  handleScanResult(mockResult)
}
</script>

<template>
  <div class="scan-page">
    <div class="scan-header">
      <h2 class="title">扫描试卷二维码</h2>
      <p class="desc">扫描学生提供的试卷二维码，查看试卷详情</p>
    </div>

    <div class="scan-container">
      <div class="camera-frame">
        <div class="corner tl"></div>
        <div class="corner tr"></div>
        <div class="corner bl"></div>
        <div class="corner br"></div>
        
        <div class="scan-line"></div>
        
        <div class="camera-placeholder">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5">
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
            <circle cx="12" cy="13" r="4"/>
          </svg>
          <p>摄像头初始化中...</p>
        </div>
      </div>
      
      <p class="scan-tip">将二维码放入扫描框内</p>
    </div>

    <div class="action-buttons">
      <button class="action-btn primary" @click="simulateScan">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="23 4 23 10 17 10"/>
          <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
        </svg>
        手动模拟扫描
      </button>
      
      <button class="action-btn secondary" @click="showTestQR = true; generateTestQR()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <rect x="7" y="7" width="3" height="3"/>
          <rect x="14" y="7" width="3" height="3"/>
          <rect x="7" y="14" width="3" height="3"/>
          <rect x="14" y="14" width="3" height="3"/>
        </svg>
        生成测试二维码
      </button>
    </div>

    <div class="help-info">
      <h4>使用说明</h4>
      <ul>
        <li>点击"手动模拟扫描"可直接体验功能</li>
        <li>扫描学生试卷上的二维码查看试卷详情</li>
        <li>需要验证机构权限后才能查看</li>
      </ul>
    </div>

    <!-- Test QR Popup -->
    <van-popup v-model:show="showTestQR" position="bottom" round>
      <div class="qr-popup">
        <h3>测试二维码</h3>
        <p class="qr-desc">使用此二维码测试扫描功能</p>
        <div class="qr-image" v-if="testQRUrl">
          <img :src="testQRUrl" alt="Test QR Code" />
        </div>
        <div class="qr-info">
          <p><strong>试卷ID:</strong> P20240325001</p>
          <p><strong>试卷标题:</strong> 高一数学期中考试</p>
        </div>
        <button class="scan-btn" @click="simulateScan">扫码此二维码</button>
      </div>
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
.scan-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 16px;
}

.scan-header {
  text-align: center;
  margin-bottom: 30px;
  
  .title {
    font-size: 24px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8px;
  }
  
  .desc {
    font-size: 14px;
    color: rgba(255,255,255,0.8);
  }
}

.scan-container {
  margin-bottom: 30px;
}

.camera-frame {
  position: relative;
  width: 260px;
  height: 260px;
  margin: 0 auto;
  background: rgba(0,0,0,0.3);
  border-radius: 24px;
  overflow: hidden;
  
  .corner {
    position: absolute;
    width: 40px;
    height: 40px;
    border: 4px solid #fff;
    
    &.tl {
      top: 16px;
      left: 16px;
      border-right: none;
      border-bottom: none;
      border-radius: 8px 0 0 0;
    }
    
    &.tr {
      top: 16px;
      right: 16px;
      border-left: none;
      border-bottom: none;
      border-radius: 0 8px 0 0;
    }
    
    &.bl {
      bottom: 16px;
      left: 16px;
      border-right: none;
      border-top: none;
      border-radius: 0 0 0 8px;
    }
    
    &.br {
      bottom: 16px;
      right: 16px;
      border-left: none;
      border-top: none;
      border-radius: 0 0 8px 0;
    }
  }
  
  .scan-line {
    position: absolute;
    top: 0;
    left: 20px;
    right: 20px;
    height: 3px;
    background: linear-gradient(90deg, transparent, #34d399, transparent);
    animation: scan 2s linear infinite;
  }
  
  @keyframes scan {
    0% { top: 20px; opacity: 1; }
    50% { opacity: 0.5; }
    100% { top: 240px; opacity: 1; }
  }
  
  .camera-placeholder {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: rgba(255,255,255,0.6);
    
    p {
      margin-top: 12px;
      font-size: 14px;
    }
  }
}

.scan-tip {
  text-align: center;
  color: rgba(255,255,255,0.8);
  font-size: 14px;
  margin-top: 16px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 30px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 16px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &.primary {
    background: #fff;
    color: #667eea;
    border: none;
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  &.secondary {
    background: rgba(255,255,255,0.15);
    color: #fff;
    border: 1px solid rgba(255,255,255,0.3);
    
    &:active {
      background: rgba(255,255,255,0.25);
    }
  }
}

.help-info {
  background: rgba(255,255,255,0.1);
  border-radius: 16px;
  padding: 20px;
  
  h4 {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 12px;
  }
  
  ul {
    list-style: none;
    
    li {
      font-size: 13px;
      color: rgba(255,255,255,0.8);
      margin-bottom: 8px;
      padding-left: 16px;
      position: relative;
      
      &::before {
        content: '•';
        position: absolute;
        left: 0;
        color: #34d399;
      }
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.qr-popup {
  padding: 24px;
  text-align: center;
  
  h3 {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }
  
  .qr-desc {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 20px;
  }
  
  .qr-image {
    width: 200px;
    height: 200px;
    margin: 0 auto 20px;
    background: #fff;
    border-radius: 12px;
    padding: 12px;
    
    img {
      width: 100%;
      height: 100%;
    }
  }
  
  .qr-info {
    background: #f9fafb;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 20px;
    text-align: left;
    
    p {
      font-size: 14px;
      color: #4b5563;
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      strong {
        color: #1f2937;
      }
    }
  }
  
  .scan-btn {
    width: 100%;
    padding: 14px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border: none;
    border-radius: 25px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    
    &:active {
      opacity: 0.9;
    }
  }
}
</style>
