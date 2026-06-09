<template>
  <el-footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-info">
          <h3>{{ $t('footer.systemName') }}</h3>
          <p>{{ $t('footer.slogan') }}</p>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h4>{{ $t('footer.touristServices') }}</h4>
            <ul>
              <li><router-link to="/">{{ $t('footer.scenicBrowse') }}</router-link></li>
              <li><router-link to="/recommend">{{ $t('footer.recommend') }}</router-link></li>
              <li><router-link to="/plan">{{ $t('footer.plan') }}</router-link></li>
            </ul>
          </div>
          <div class="link-group">
            <h4>{{ $t('footer.merchantServices') }}</h4>
            <ul>
              <li><router-link to="/merchant">{{ $t('footer.merchantCenter') }}</router-link></li>
              <li><router-link to="/merchant/shop">{{ $t('footer.shopManage') }}</router-link></li>
              <li><router-link to="/merchant/orders">{{ $t('footer.orderManage') }}</router-link></li>
            </ul>
          </div>
          <div class="link-group">
            <h4>{{ $t('footer.aboutUs') }}</h4>
            <ul>
              <li><a href="#">{{ $t('footer.contactUs') }}</a></li>
              <li><a href="#">{{ $t('footer.helpCenter') }}</a></li>
              <li><a href="#">{{ $t('footer.feedback') }}</a></li>
            </ul>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 {{ $t('footer.systemName') }}. {{ $t('footer.copyright') }}</p>
      </div>
    </div>
  </el-footer>
</template>

<script setup>
// 无需特殊逻辑
</script>

<style scoped>
/* 深度覆盖 Element Plus el-footer 的默认样式 */
:deep(.el-footer) {
  padding: 0 !important;
  margin: 0 !important;
  height: auto !important;
}

.footer {
  background: linear-gradient(135deg, #141e30 0%, #243b55 100%);
  color: #ecf0f1;
  padding: 0;
  margin: 0;
  height: auto !important;
  width: 100%;
  position: relative;
  flex-shrink: 0; /* 防止被压缩 */
}

/* 顶部装饰彩条 */
.footer::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  z-index: 1;
}

.footer-container {
  width: 100%;
  margin: 0 auto;
  padding: 40px 20px 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-info h3 {
  margin: 0 0 10px 0;
  color: #ffffff;
  font-size: 22px;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.footer-info p {
  color: #bdc3c7;
  margin: 0;
  font-size: 14px;
}

.footer-links {
  display: flex;
  gap: 80px;
}

.link-group h4 {
  margin: 0 0 20px 0;
  color: #a0cfff;
  font-size: 16px;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.link-group h4::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -6px;
  width: 30px;
  height: 2px;
  background: #4facfe;
  border-radius: 2px;
}

.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.link-group li {
  margin-bottom: 12px;
}

.link-group a {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
  display: inline-block;
}

.link-group a:hover {
  color: #4facfe;
  transform: translateX(5px);
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
}

.footer-bottom p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 40px;
  }
  
  .footer-links {
    flex-wrap: wrap;
    gap: 40px;
  }
}
</style>