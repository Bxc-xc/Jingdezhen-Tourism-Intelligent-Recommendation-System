<template>
  <div class="system-manage">
    <div class="page-header">
      <div class="page-title">
        <el-button link type="primary" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>系统管理</h2>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 系统概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon system">
              <el-icon><Location /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ systemInfo.totalScenics }}</div>
              <div class="overview-label">景点总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ systemInfo.totalUsers }}</div>
              <div class="overview-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon merchants">
              <el-icon><Shop /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ systemInfo.totalMerchants }}</div>
              <div class="overview-label">商家总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon routes">
              <el-icon><MapLocation /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ systemInfo.totalRoutes }}</div>
              <div class="overview-label">路线总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 管理标签页 -->
    <el-card class="manage-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 景点管理 -->
        <el-tab-pane label="景点管理" name="scenic">
          <div class="tab-header">
            <el-input
              v-model="scenicSearchKeyword"
              placeholder="搜索景点..."
              style="width: 300px; margin-right: 12px;"
              @input="handleScenicSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddScenicDialog">
              <el-icon><Plus /></el-icon>
              新增景点
            </el-button>
            <el-button type="danger" :disabled="selectedScenics.length === 0" @click="batchDeleteScenics">
              批量删除
            </el-button>
          </div>
      <el-table
        :data="paginatedScenics"
            v-loading="scenicLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleScenicSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="景点名称" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            <el-tag :type="getCategoryTagType(scope.row.category)">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            <span v-if="scope.row.price > 0">¥{{ scope.row.price }}</span>
            <span v-else class="free-text">免费</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="scope">
            <el-rate 
              :model-value="getRatingValue(scope.row.rating)" 
              disabled 
              size="small"
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column label="关联账号" width="150">
          <template #default="scope">
            <el-tag v-if="scope.row.user" type="info">{{ scope.row.user.username }}</el-tag>
            <el-tag v-else type="warning">暂未关联</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
                <el-button size="small" type="primary" @click="editScenic(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteScenic(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">
              <span class="info-text">
                <span class="pill">Total <strong>{{ scenicTotal }}</strong></span>
                <span class="pill">Page <strong>{{ scenicCurrentPage }}</strong>/<strong>{{ scenicTotalPages }}</strong></span>
                <span v-if="scenicRange" class="pill">Showing <strong>{{ scenicRange.from }}</strong>-<strong>{{ scenicRange.to }}</strong></span>
              </span>
            </div>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="scenicCurrentPage"
                v-model:page-size="scenicPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="Number(scenicTotal) || 0"
                layout="sizes, prev, pager, next, jumper"
                @size-change="handleScenicSizeChange"
                @current-change="handleScenicCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 新增餐饮商家对话框 -->
        <el-dialog v-model="showAddFoodDialog" title="新增餐饮商家" width="680px">
          <el-form label-width="110px">
            <el-form-item label="登录用户名" required>
              <el-input v-model="addFoodForm.username" placeholder="请输入用户名（唯一）" />
            </el-form-item>
            <el-form-item label="登录密码" required>
              <el-input v-model="addFoodForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item label="店铺名称" required>
              <el-input v-model="addFoodForm.shopName" placeholder="请输入店铺名称" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="addFoodForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="addFoodForm.address" placeholder="请输入地址" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="addFoodForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="营业时间">
              <el-input v-model="addFoodForm.openTime" placeholder="如 10:00-22:00" />
            </el-form-item>
            <el-form-item label="人均/起步价">
              <el-input v-model="addFoodForm.startPrice" placeholder="如 45 或 45.00" />
            </el-form-item>
            <el-form-item label="上传封面图">
              <el-upload
                :action="uploadAction"
                name="file"
                :limit="1"
                :show-file-list="false"
                :headers="uploadHeaders"
                :on-success="handleAvatarUploadSuccess"
                accept=".jpg,.jpeg,.png,.gif,.webp"
              >
                <el-button type="primary">上传头像</el-button>
                <span v-if="addFoodForm.avatar" style="margin-left: 8px; color: #909399;">已上传</span>
              </el-upload>
            </el-form-item>
            <el-form-item label="上传详情图页">
              <el-upload
                :action="uploadAction"
                name="file"
                list-type="picture-card"
                multiple
                :headers="uploadHeaders"
                :on-success="handleImagesUploadSuccess"
                :on-remove="handleImagesRemove"
                accept=".jpg,.jpeg,.png,.gif,.webp"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div style="color:#909399; font-size:12px; margin-top:6px;">可按住 Ctrl/Shift 多选一次性上传多张</div>
              <div v-if="Array.isArray(addFoodForm.images) && addFoodForm.images.length" style="margin-top: 8px; color: #909399;">
                已上传 {{ addFoodForm.images.length }} 张
              </div>
            </el-form-item>
            <el-form-item label="餐饮类型">
              <el-select v-model="addFoodForm.cuisineType" placeholder="请选择餐饮类型" style="width: 100%;">
                <el-option label="SNACK（小吃）" value="SNACK" />
                <el-option label="BEVERAGE（奶茶饮品）" value="BEVERAGE" />
                <el-option label="DESSERT（甜品）" value="DESSERT" />
                <el-option label="FAST_FOOD（快餐）" value="FAST_FOOD" />
                <el-option label="HOTPOT（火锅）" value="HOTPOT" />
                <el-option label="BBQ（烧烤）" value="BBQ" />
                <el-option label="NOODLE（面食）" value="NOODLE" />
                <el-option label="BAKERY（烘焙）" value="BAKERY" />
                <el-option label="COFFEE（咖啡）" value="COFFEE" />
                <el-option label="LIGHT_MEAL（轻食）" value="LIGHT_MEAL" />
                <el-option label="LOCAL（地方特色）" value="LOCAL" />
                <el-option label="OTHER（其他）" value="OTHER" />
              </el-select>
            </el-form-item>
            <el-form-item label="人均区间">
              <el-input v-model="addFoodForm.avgPrice" placeholder="如 50-80" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="showAddFoodDialog = false">取消</el-button>
              <el-button type="primary" @click="submitAddFood">确定</el-button>
            </div>
          </template>
        </el-dialog>

        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="user">
          <div class="tab-header">
            <el-input
              v-model="userSearchKeyword"
              placeholder="搜索用户..."
              style="width: 300px; margin-right: 12px;"
              @input="handleUserSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="danger" :disabled="selectedUsers.length === 0" @click="batchDeleteUsers">
              批量删除
            </el-button>
          </div>
          <el-table
            :data="paginatedUsers"
            v-loading="userLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleUserSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="scope">
                <el-tag :type="getRoleTagType(scope.row.role)">
                  {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
            <el-table-column label="商家类型" width="150">
          <template #default="scope">
                <el-tag v-if="scope.row.merchantCategory" type="success">
                  {{ getMerchantCategoryText(scope.row.merchantCategory) }}
                </el-tag>
                <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
                <span v-if="scope.row.createTime">{{ formatDate(scope.row.createTime) }}</span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
                <el-button size="small" type="warning" @click="openResetPassword(scope.row)">重置密码</el-button>
                <el-button size="small" type="danger" @click="deleteUser(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">
              <span class="info-text">
                共 <strong>{{ filteredUsers.length }}</strong> 条记录，
                第 <strong>{{ userCurrentPage }}</strong> / <strong>{{ Math.ceil(filteredUsers.length / userPageSize) || 1 }}</strong> 页
                <template v-if="filteredUsers.length > 0">
                  （显示第 <strong>{{ (userCurrentPage - 1) * userPageSize + 1 }}</strong> - 
                  <strong>{{ Math.min(userCurrentPage * userPageSize, filteredUsers.length) }}</strong> 条）
                </template>
              </span>
            </div>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="userCurrentPage"
                v-model:page-size="userPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="filteredUsers.length"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleUserSizeChange"
                @current-change="handleUserCurrentChange"
              />
            </div>
          </div>

          <!-- 重置密码弹窗 -->
          <el-dialog v-model="resetPasswordVisible" title="重置用户密码" width="400px" :close-on-click-modal="false">
            <div style="margin-bottom:12px;color:#606266;font-size:14px">
              正在重置用户 <strong>{{ resetPasswordTarget?.username }}</strong> 的密码
            </div>
            <el-form :model="resetPasswordForm" label-width="80px">
              <el-form-item label="新密码">
                <el-input
                  v-model="resetPasswordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入新密码（至少6位）"
                  @keyup.enter="confirmResetPassword"
                />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input
                  v-model="resetPasswordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="再次输入新密码"
                  @keyup.enter="confirmResetPassword"
                />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="resetPasswordVisible = false">取消</el-button>
              <el-button type="primary" :loading="resetPasswordLoading" @click="confirmResetPassword">确认重置</el-button>
            </template>
          </el-dialog>
        </el-tab-pane>

        <!-- 餐饮商家管理 -->
        <el-tab-pane label="餐饮商家" name="food">
          <div class="tab-header">
            <el-input
              v-model="foodSearchKeyword"
              placeholder="搜索餐饮商家..."
              style="width: 300px; margin-right: 12px;"
              @input="handleFoodSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="openAddFoodDialog" style="margin-right: 12px;">
              <el-icon><Plus /></el-icon>
              新增餐饮商家
            </el-button>
            <el-button type="danger" :disabled="selectedFoods.length === 0" @click="batchDeleteFoods">
              批量删除
            </el-button>
          </div>
          <el-table
            :data="paginatedFoods"
            v-loading="foodLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleFoodSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="商家名称" />
            <el-table-column prop="category" label="分类" width="120">
              <template #default="scope">
                <el-tag>{{ scope.row.category || '餐饮商家' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="120">
              <template #default="scope">
                <el-rate 
                  :model-value="getRatingValue(scope.row.rating)" 
                  disabled 
                  size="small"
                  show-score
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" />
            <el-table-column label="关联账号" width="150">
              <template #default="scope">
                <el-tag v-if="scope.row.user" type="info">{{ scope.row.user.username }}</el-tag>
                <el-tag v-else type="warning">无关联账号</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editFood(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteFood(scope.row.id, scope.row.merchantId)">删除</el-button>
              </template>
            </el-table-column>
      </el-table>
          <div class="pagination">
            <div class="pagination-info">
              <span class="info-text">
                共 <strong>{{ filteredFoods.length }}</strong> 条记录，
                第 <strong>{{ foodCurrentPage }}</strong> / <strong>{{ Math.ceil(filteredFoods.length / foodPageSize) || 1 }}</strong> 页
                <template v-if="filteredFoods.length > 0">
                  （显示第 <strong>{{ (foodCurrentPage - 1) * foodPageSize + 1 }}</strong> - 
                  <strong>{{ Math.min(foodCurrentPage * foodPageSize, filteredFoods.length) }}</strong> 条）
                </template>
              </span>
            </div>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="foodCurrentPage"
                v-model:page-size="foodPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="filteredFoods.length"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleFoodSizeChange"
                @current-change="handleFoodCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 陶瓷工坊管理 -->
        <el-tab-pane label="陶瓷工坊" name="ceramic">
          <div class="tab-header">
            <el-input
              v-model="ceramicSearchKeyword"
              placeholder="搜索陶瓷工坊..."
              style="width: 300px; margin-right: 12px;"
              @input="handleCeramicSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="danger" :disabled="selectedCeramics.length === 0" @click="batchDeleteCeramics">
              批量删除
            </el-button>
          </div>
          <el-table
            :data="paginatedCeramics"
            v-loading="ceramicLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleCeramicSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="shopName" label="工坊名称" />
            <el-table-column prop="category" label="分类" width="120">
              <template #default="scope">
                <el-tag type="success">陶瓷工坊</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="address" label="地址" />
            <el-table-column label="关联账号" width="150">
              <template #default="scope">
                <el-tag v-if="scope.row.user" type="info">{{ scope.row.user.username }}</el-tag>
                <el-tag v-else type="warning">无关联账号</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editCeramic(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteCeramic(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
      <div class="pagination">
        <div class="pagination-info">
          <span class="info-text">
            共 <strong>{{ filteredCeramics.length }}</strong> 条记录，
            第 <strong>{{ ceramicCurrentPage }}</strong> / <strong>{{ Math.ceil(filteredCeramics.length / ceramicPageSize) || 1 }}</strong> 页
            <template v-if="filteredCeramics.length > 0">
              （显示第 <strong>{{ (ceramicCurrentPage - 1) * ceramicPageSize + 1 }}</strong> - 
              <strong>{{ Math.min(ceramicCurrentPage * ceramicPageSize, filteredCeramics.length) }}</strong> 条）
            </template>
          </span>
        </div>
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="ceramicCurrentPage"
            v-model:page-size="ceramicPageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="filteredCeramics.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleCeramicSizeChange"
            @current-change="handleCeramicCurrentChange"
          />
        </div>
      </div>
        </el-tab-pane>

        <!-- 酒店商家管理 -->
        <el-tab-pane label="酒店商家" name="hotel">
          <div class="tab-header">
            <el-input
              v-model="hotelSearchKeyword"
              placeholder="搜索酒店..."
              style="width: 300px; margin-right: 12px;"
              @input="handleHotelSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="danger" :disabled="selectedHotels.length === 0" @click="batchDeleteHotels">
              批量删除
            </el-button>
          </div>
          <el-table
            :data="paginatedHotels"
            v-loading="hotelLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleHotelSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="shopName" label="酒店名称" />
            <el-table-column prop="rating" label="评分" width="120">
              <template #default="scope">
                <el-rate :model-value="getRatingValue(scope.row.rating || scope.row.adminRating)" disabled show-score allow-half />
              </template>
            </el-table-column>
            <el-table-column prop="tags" label="标签" min-width="180" show-overflow-tooltip>
              <template #default="scope">
                <div v-if="Array.isArray(scope.row.tags) && scope.row.tags.length">
                  <el-tag v-for="(tag, idx) in scope.row.tags" :key="idx" type="info" style="margin-right:6px;margin-bottom:4px;">{{ tag }}</el-tag>
                </div>
                <span v-else>{{ scope.row.tags || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.enabled !== undefined" :type="scope.row.enabled ? 'success' : 'info'">
                  {{ scope.row.enabled ? '启用' : '停用' }}
                </el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="120">
              <template #default="scope">
                <el-tag type="success">酒店</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="address" label="地址" />
            <el-table-column label="关联账号" width="150">
              <template #default="scope">
                <el-tag v-if="scope.row.user" type="info">{{ scope.row.user.username }}</el-tag>
                <el-tag v-else type="warning">无关联账号</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editHotel(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteHotel(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">
              <span class="info-text">
                共 <strong>{{ filteredHotels.length }}</strong> 条记录，
                第 <strong>{{ hotelCurrentPage }}</strong> / <strong>{{ Math.ceil(filteredHotels.length / hotelPageSize) || 1 }}</strong> 页
                <template v-if="filteredHotels.length > 0">
                  （显示第 <strong>{{ (hotelCurrentPage - 1) * hotelPageSize + 1 }}</strong> - 
                  <strong>{{ Math.min(hotelCurrentPage * hotelPageSize, filteredHotels.length) }}</strong> 条）
                </template>
              </span>
            </div>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="hotelCurrentPage"
                v-model:page-size="hotelPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="filteredHotels.length"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleHotelSizeChange"
                @current-change="handleHotelCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 路线管理 -->
        <el-tab-pane label="路线管理" name="route">
          <div class="tab-header">
            <el-input
              v-model="routeSearchKeyword"
              placeholder="搜索路线..."
              style="width: 300px; margin-right: 12px;"
              @input="handleRouteSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddRouteDialog">
              <el-icon><Plus /></el-icon>
              新增路线
            </el-button>
            <el-button type="danger" :disabled="selectedRoutes.length === 0" @click="batchDeleteRoutes">
              批量删除
            </el-button>
          </div>
          <el-table
            :data="paginatedRoutes"
            v-loading="routeLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleRouteSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="路线名称" />
            <el-table-column prop="days" label="天数" width="80" />
            <el-table-column prop="difficulty" label="难度" width="100">
              <template #default="scope">
                <el-tag>{{ scope.row.difficulty }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="总价" width="100">
              <template #default="scope">
                <span v-if="scope.row.totalPrice > 0">¥{{ scope.row.totalPrice }}</span>
                <span v-else class="free-text">免费</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editRoute(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteRoute(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">
              <span class="info-text">
                共 <strong>{{ filteredRoutes.length }}</strong> 条记录，
                第 <strong>{{ routeCurrentPage }}</strong> / <strong>{{ Math.ceil(filteredRoutes.length / routePageSize) || 1 }}</strong> 页
                <template v-if="filteredRoutes.length > 0">
                  （显示第 <strong>{{ (routeCurrentPage - 1) * routePageSize + 1 }}</strong> - 
                  <strong>{{ Math.min(routeCurrentPage * routePageSize, filteredRoutes.length) }}</strong> 条）
                </template>
              </span>
            </div>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="routeCurrentPage"
                v-model:page-size="routePageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="filteredRoutes.length"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleRouteSizeChange"
                @current-change="handleRouteCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 市集管理 -->
        <el-tab-pane label="市集管理" name="marketplace">
          <div class="tab-header">
            <el-input
              v-model="marketplaceSearchKeyword"
              placeholder="搜索市集名称/地址/开放时间..."
              style="width: 320px; margin-right: 12px;"
              @input="handleMarketplaceSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddMarketplaceDialog">
              <el-icon><Plus /></el-icon>
              新增市集
            </el-button>
            <el-button type="danger" :disabled="selectedMarketplaces.length === 0" @click="batchDeleteMarketplaces">
              批量删除
            </el-button>
          </div>
          <el-table
            :data="paginatedMarketplaces"
            v-loading="marketplaceLoading"
            style="width: 100%; margin-top: 16px;"
            @selection-change="handleMarketplaceSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="市集名称" min-width="160" />
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.enabled ? 'success' : 'info'">
                  {{ scope.row.enabled ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="openTime" label="时间" width="140" show-overflow-tooltip />
            <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editMarketplace(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteMarketplaceRow(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">
              <span class="info-text">
                共 <strong>{{ filteredMarketplaces.length }}</strong> 条记录，
                第 <strong>{{ marketplaceCurrentPage }}</strong> / <strong>{{ Math.ceil(filteredMarketplaces.length / marketplacePageSize) || 1 }}</strong> 页
                <template v-if="filteredMarketplaces.length > 0">
                  （显示第 <strong>{{ (marketplaceCurrentPage - 1) * marketplacePageSize + 1 }}</strong> - 
                  <strong>{{ Math.min(marketplaceCurrentPage * marketplacePageSize, filteredMarketplaces.length) }}</strong> 条）
                </template>
              </span>
            </div>
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="marketplaceCurrentPage"
                v-model:page-size="marketplacePageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="filteredMarketplaces.length"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleMarketplaceSizeChange"
                @current-change="handleMarketplaceCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 系统设置 -->
        <el-tab-pane label="系统设置" name="settings">
          <el-card>
            <template #header>
              <span>系统参数配置</span>
            </template>
            <el-form :model="systemSettings" label-width="150px">
              <el-form-item label="系统名称">
                <el-input v-model="systemSettings.systemName" placeholder="请输入系统名称" />
        </el-form-item>
              <el-form-item label="系统公告">
          <el-input
                  v-model="systemSettings.announcement"
            type="textarea"
            :rows="4"
                  placeholder="请输入系统公告"
          />
        </el-form-item>
              <el-form-item label="推荐算法">
                <el-select v-model="systemSettings.recommendAlgorithm" placeholder="请选择推荐算法">
                  <el-option label="协同过滤" value="collaborative" />
                  <el-option label="内容推荐" value="content" />
                  <el-option label="混合推荐" value="hybrid" />
                </el-select>
        </el-form-item>
              <el-form-item label="数据备份">
                <el-button type="primary" @click="backupData">立即备份</el-button>
                <el-button @click="restoreData">恢复数据</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveSettings">保存设置</el-button>
                <el-button @click="resetSettings">重置</el-button>
        </el-form-item>
      </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增景点对话框 -->
    <el-dialog v-model="showScenicDialog" :title="scenicDialogTitle" width="700px">
      <el-form :model="scenicForm" :rules="scenicRules" ref="scenicFormRef" label-width="100px">
        <el-form-item label="景点名称" prop="name">
          <el-input v-model="scenicForm.name" placeholder="请输入景点名称" />
        </el-form-item>
          <el-form-item label="角标标签">
            <el-input v-model="scenicForm.tag" placeholder="如：热门、亲子优选" />
          </el-form-item>
        <el-form-item label="景点分类" prop="category">
          <el-select v-model="scenicForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="博物馆" value="博物馆" />
            <el-option label="陶瓷工坊" value="陶瓷工坊" />
            <el-option label="景区" value="景区" />
            <el-option label="文化街区" value="文化街区" />
            <el-option label="古镇" value="古镇" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
        <el-form-item label="门票价格" prop="price">
              <el-input-number v-model="scenicForm.price" :min="0" :precision="2" placeholder="请输入价格" style="width: 100%;" />
        </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开放时间">
          <el-input v-model="scenicForm.openTime" placeholder="如：9:00-18:00" />
        </el-form-item>
          </el-col>
        </el-row>
          <el-form-item label="开放时间说明">
            <el-input v-model="scenicForm.openingHoursDetail" placeholder="如：全年 08:30-22:00开放（21:30停止入园）" />
          </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="scenicForm.rating" :max="5" :allow-half="true" show-score />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            <span v-if="editingScenicId">管理员可手动设置评分（0-5分），如不设置则自动从用户评论计算平均评分</span>
            <span v-else>设置初始评分（0-5分），如不设置则自动从用户评论计算平均评分</span>
          </span>
        </el-form-item>
          <!-- 点评数量由后端实时统计，移除手动输入 -->
        <el-form-item label="景点地址">
          <el-input v-model="scenicForm.address" placeholder="请输入景点地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="scenicForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
          <el-form-item label="官方电话">
            <el-input v-model="scenicForm.officialPhone" placeholder="如与联系电话不同可填写" />
          </el-form-item>
          <el-form-item label="公告">
            <el-input v-model="scenicForm.notice" placeholder="简短公告（可为空）" />
          </el-form-item>
          <el-form-item label="公告详情">
            <el-input v-model="scenicForm.noticeDetail" type="textarea" :rows="3" placeholder="详细公告/须知（可为空）" />
          </el-form-item>
          <el-form-item label="优待政策">
            <el-input v-model="scenicForm.preferentialPolicy" type="textarea" :rows="3" placeholder="如：学生半价、老人免票、军人免票等（可为空）" />
          </el-form-item>
          <el-form-item label="服务设施">
            <el-input v-model="scenicForm.facilitiesDetail" type="textarea" :rows="3" placeholder="如：停车场、卫生间、景区地图等（可为空）" />
          </el-form-item>
          <el-form-item label="门票信息">
            <el-input v-model="scenicForm.ticketInfo" placeholder="如：2026年中国100家核心卡点（可为空）" />
          </el-form-item>
          <el-form-item label="景点特色">
            <el-input v-model="scenicForm.features" type="textarea" :rows="2" placeholder="多个特色用逗号分隔，如：陶瓷文化,非遗体验,亲子游（可为空）" />
          </el-form-item>
          <el-form-item label="亮点推荐">
            <el-input v-model="scenicForm.highlights" type="textarea" :rows="2" placeholder="多个亮点用逗号分隔，如：千年窑火,匠心传承（可为空）" />
          </el-form-item>
          <el-form-item label="游玩提示">
            <el-input v-model="scenicForm.tips" type="textarea" :rows="2" placeholder="如：建议游览时间2-3小时，旺季请提前购票（可为空）" />
          </el-form-item>
        <el-form-item label="景点描述">
          <el-input
            v-model="scenicForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入景点描述"
          />
        </el-form-item>
        <el-form-item label="景点标签">
          <el-input v-model="scenicForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="景点图片">
          <div class="scenic-images-upload">
            <div class="upload-hint">第一张图片将作为封面图，所有图片会在详情页以轮播图形式展示</div>
            
            <!-- 图片列表展示区域（支持拖拽排序） -->
            <div class="scenic-image-list-container">
              <div 
                class="scenic-image-list"
                v-if="scenicForm.images && scenicForm.images.length > 0"
              >
                <div
                  v-for="(img, index) in scenicForm.images"
                  :key="index"
                  class="scenic-image-item"
                  :class="{ 'is-cover': index === 0 }"
                  draggable="true"
                  @dragstart="handleScenicDragStart(index, $event)"
                  @dragover.prevent="handleScenicDragOver(index, $event)"
                  @drop="handleScenicDrop(index, $event)"
                  @dragend="handleScenicDragEnd"
                >
                  <div class="scenic-image-thumbnail" @click.stop="previewScenicImage(img)">
                    <img :src="getScenicImageUrl(img)" :alt="`景点图片 ${index + 1}`" />
                    <div class="scenic-image-overlay">
                      <div class="scenic-image-badges">
                        <el-tag v-if="index === 0" type="success" size="small">封面</el-tag>
                      </div>
                      <div class="scenic-image-actions">
                        <el-button
                          v-if="index !== 0"
                          type="primary"
                          size="small"
                          circle
                          @click.stop="setScenicAsCover(index)"
                          title="设为封面"
                        >
                          <el-icon><Star /></el-icon>
                        </el-button>
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click.stop="removeScenicImageByIndex(index)"
                          title="删除"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div class="scenic-image-index">{{ index + 1 }}</div>
                </div>
              </div>
              
              <!-- 上传按钮 -->
              <el-upload
                class="scenic-image-upload-btn"
                :http-request="customScenicImageUpload"
                :show-file-list="false"
                :before-upload="beforeScenicImageUpload"
                accept="image/*"
                multiple
              >
                <div class="scenic-upload-placeholder">
                  <el-icon class="scenic-upload-icon"><Plus /></el-icon>
                  <div class="scenic-upload-text">添加图片</div>
                </div>
              </el-upload>
            </div>
            
            <div class="scenic-image-info">
              <p class="scenic-image-hint">
                <el-icon><InfoFilled /></el-icon>
                图片说明：
              </p>
              <ul class="scenic-image-usage-list">
                <li>可以拖拽图片调整顺序，第一张自动成为封面</li>
                <li>建议尺寸：800x600px 或更高，支持 JPG、PNG 格式</li>
              </ul>
            </div>
            
            <!-- 图片预览对话框 -->
            <el-dialog v-model="showImagePreview" title="图片预览" width="800px">
              <img :src="previewImageUrl" style="width: 100%;" alt="预览图片" />
            </el-dialog>
          </div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model="scenicForm.longitude" type="number" placeholder="经度（如：117.2074）" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model="scenicForm.latitude" type="number" placeholder="纬度（如：29.2926）" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showScenicDialog = false">取消</el-button>
        <el-button type="primary" @click="saveScenic">保存</el-button>
      </template>
    </el-dialog>

    <!-- 市集编辑对话框 -->
    <el-dialog
      v-model="showMarketplaceDialog"
      :title="editingMarketplaceId ? '编辑市集' : '新增市集'"
      width="760px"
    >
      <el-form :model="marketplaceForm" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="市集名称" required>
              <el-input v-model="marketplaceForm.name" placeholder="请输入市集名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否启用">
              <el-switch v-model="marketplaceForm.enabled" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开放时间">
              <el-input v-model="marketplaceForm.openTime" placeholder="如：9:00-18:00 / 全天开放" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="marketplaceForm.sortOrder" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址">
          <el-input v-model="marketplaceForm.address" placeholder="请输入地址" />
        </el-form-item>

        <el-form-item label="详细介绍">
          <el-input
            v-model="marketplaceForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入详细介绍（用于陶瓷体验页市集详情）"
          />
        </el-form-item>

        <el-form-item label="轮播图图片">
          <div class="marketplace-image-section">
            <div class="marketplace-image-grid" v-if="marketplaceForm.images.length">
              <div
                v-for="(img, index) in marketplaceForm.images"
                :key="img.url + '_' + index"
                class="marketplace-image-item"
              >
                <img
                  :src="img.url"
                  class="marketplace-image"
                  alt="市集图片"
                  @click="previewMarketplaceImage(img.url)"
                />
                <div class="marketplace-image-actions">
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click.stop="removeMarketplaceImageByIndex(index)"
                    title="删除"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <div class="marketplace-image-index">{{ index + 1 }}</div>
              </div>
            </div>

            <el-upload
              class="marketplace-image-upload-btn"
              :http-request="customMarketplaceImageUpload"
              :show-file-list="false"
              :before-upload="beforeMarketplaceImageUpload"
              accept="image/*"
              multiple
            >
              <div class="marketplace-upload-placeholder">
                <el-icon class="marketplace-upload-icon"><Plus /></el-icon>
                <div class="marketplace-upload-text">添加轮播图</div>
              </div>
            </el-upload>

            <div class="marketplace-image-info">
              <p class="marketplace-image-hint">
                <el-icon><InfoFilled /></el-icon>
                说明：第一张图片将作为市集封面，游客端详情页会轮播展示全部图片。
              </p>
            </div>

            <el-dialog v-model="showMarketplaceImagePreview" title="图片预览" width="800px">
              <img :src="previewMarketplaceImageUrl" style="width: 100%;" alt="预览图片" />
            </el-dialog>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showMarketplaceDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMarketplace">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑餐饮商家对话框 -->
    <el-dialog v-model="showFoodDialog" title="编辑餐饮商家" width="700px">
      <el-form :model="foodForm" label-width="100px">
        <el-form-item label="商家名称">
          <el-input v-model="foodForm.shopName" placeholder="请输入商家名称" />
        </el-form-item>
        <el-form-item label="商家简介">
          <el-input
            v-model="foodForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商家简介"
          />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="foodForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="foodForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="foodForm.openTime" placeholder="如：9:00-22:00" />
        </el-form-item>
        <el-form-item label="管理员评分">
          <el-rate v-model="foodForm.adminRating" :max="5" :allow-half="true" show-score />
          <div style="font-size: 12px; color: #909399; margin-left: 10px;">设置后将优先展示此评分</div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="foodForm.tags" placeholder="请输入标签，多个标签用逗号分隔，如：赣菜,辣,特色" />
          <div style="font-size: 12px; color: #909399;">用于智能推荐筛选，多个标签请用逗号分隔</div>
        </el-form-item>
        <!-- 封面图（单独展示与更换） -->
        <el-form-item label="封面图">
          <div class="ceramic-images-upload">
            <div class="upload-hint">封面图用于卡片与列表展示</div>
            <div class="ceramic-image-list-container">
              <div class="ceramic-image-list" v-if="foodForm.images && foodForm.images.length > 0">
                <div class="ceramic-image-item is-cover">
                  <div class="ceramic-image-thumbnail" @click.stop="previewCeramicImage(foodForm.images[0])">
                    <img :src="getCeramicImageUrl(foodForm.images[0])" alt="封面图" />
                    <div class="ceramic-image-overlay">
                      <div class="ceramic-image-badges">
                        <el-tag type="success" size="small">封面</el-tag>
                      </div>
                      <div class="ceramic-image-actions">
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click.stop="removeFoodImageByIndex(0)"
                          title="删除"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <el-upload
                class="ceramic-image-upload-btn"
                :http-request="uploadFoodCover"
                :show-file-list="false"
                :before-upload="beforeFoodImageUpload"
                accept="image/*"
              >
                <div class="ceramic-upload-placeholder">
                  <el-icon class="ceramic-upload-icon"><Plus /></el-icon>
                  <div class="ceramic-upload-text">更换封面</div>
                </div>
              </el-upload>
            </div>
          </div>
        </el-form-item>

        <!-- 详情图（与封面分开展示） -->
        <el-form-item label="详情图">
          <div class="ceramic-images-upload">
            <div class="upload-hint">详情图会在详情页轮播展示，不包含封面图</div>
            <div class="ceramic-image-list-container">
              <div class="ceramic-image-list" v-if="foodForm.images && foodForm.images.length > 1">
                <div
                  v-for="(img, index) in foodForm.images.slice(1)"
                  :key="index"
                  class="ceramic-image-item"
                >
                  <div class="ceramic-image-thumbnail" @click.stop="previewCeramicImage(img)">
                    <img :src="getCeramicImageUrl(img)" :alt="`详情图 ${index + 1}`" />
                    <div class="ceramic-image-overlay">
                      <div class="ceramic-image-actions">
                        <el-button
                          type="primary"
                          size="small"
                          circle
                          @click.stop="setFoodAsCover(index + 1)"
                          title="设为封面"
                        >
                          <el-icon><Star /></el-icon>
                        </el-button>
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click.stop="removeFoodImageByIndex(index + 1)"
                          title="删除"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div class="ceramic-image-index">{{ index + 1 }}</div>
                </div>
              </div>
              <el-upload
                class="ceramic-image-upload-btn"
                :http-request="customFoodImageUpload"
                :show-file-list="false"
                :before-upload="beforeFoodImageUpload"
                accept="image/*"
                multiple
              >
                <div class="ceramic-upload-placeholder">
                  <el-icon class="ceramic-upload-icon"><Plus /></el-icon>
                  <div class="ceramic-upload-text">添加详情图</div>
                </div>
              </el-upload>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFoodDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFood">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑陶瓷工坊对话框 -->
    <el-dialog v-model="showCeramicDialog" title="编辑陶瓷工坊" width="700px">
      <el-form :model="ceramicForm" label-width="100px">
        <el-form-item label="工坊名称">
          <el-input v-model="ceramicForm.shopName" placeholder="请输入工坊名称" />
        </el-form-item>
        <el-form-item label="工坊简介">
          <el-input
            v-model="ceramicForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入工坊简介"
          />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="ceramicForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="ceramicForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="ceramicForm.openTime" placeholder="如：9:00-18:00" />
        </el-form-item>
        <el-form-item label="管理员评分">
          <el-rate v-model="ceramicForm.adminRating" :max="5" :allow-half="true" show-score />
          <div style="font-size: 12px; color: #909399; margin-left: 10px;">设置后将优先展示此评分</div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="ceramicForm.tags" placeholder="请输入标签，多个标签用逗号分隔，如：陶艺体验,亲子,DIY" />
          <div style="font-size: 12px; color: #909399;">用于智能推荐筛选，多个标签请用逗号分隔</div>
        </el-form-item>
        <el-form-item label="工坊图片">
          <div class="ceramic-images-upload">
            <div class="upload-hint">第一张图片将作为封面图，所有图片会在详情页以轮播图形式展示</div>
            
            <!-- 图片列表展示区域（支持拖拽排序） -->
            <div class="ceramic-image-list-container">
              <div 
                class="ceramic-image-list"
                v-if="ceramicForm.images && ceramicForm.images.length > 0"
              >
                <div
                  v-for="(img, index) in ceramicForm.images"
                  :key="index"
                  class="ceramic-image-item"
                  :class="{ 'is-cover': index === 0 }"
                  draggable="true"
                  @dragstart="handleCeramicDragStart(index, $event)"
                  @dragover.prevent="handleCeramicDragOver(index, $event)"
                  @drop="handleCeramicDrop(index, $event)"
                  @dragend="handleCeramicDragEnd"
                >
                  <div class="ceramic-image-thumbnail" @click.stop="previewCeramicImage(img)">
                    <img :src="getCeramicImageUrl(img)" :alt="`工坊图片 ${index + 1}`" />
                    <div class="ceramic-image-overlay">
                      <div class="ceramic-image-badges">
                        <el-tag v-if="index === 0" type="success" size="small">封面</el-tag>
                      </div>
                      <div class="ceramic-image-actions">
                        <el-button
                          v-if="index !== 0"
                          type="primary"
                          size="small"
                          circle
                          @click.stop="setCeramicAsCover(index)"
                          title="设为封面"
                        >
                          <el-icon><Star /></el-icon>
                        </el-button>
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click.stop="removeCeramicImageByIndex(index)"
                          title="删除"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div class="ceramic-image-index">{{ index + 1 }}</div>
                </div>
              </div>
              
              <!-- 上传按钮 -->
              <el-upload
                class="ceramic-image-upload-btn"
                :http-request="customCeramicImageUpload"
                :show-file-list="false"
                :before-upload="beforeCeramicImageUpload"
                accept="image/*"
                multiple
              >
                <div class="ceramic-upload-placeholder">
                  <el-icon class="ceramic-upload-icon"><Plus /></el-icon>
                  <div class="ceramic-upload-text">添加图片</div>
                </div>
              </el-upload>
            </div>
            
            <div class="ceramic-image-info">
              <p class="ceramic-image-hint">
                <el-icon><InfoFilled /></el-icon>
                图片说明：
              </p>
              <ul class="ceramic-image-usage-list">
                <li>可以拖拽图片调整顺序，第一张自动成为封面</li>
                <li>建议尺寸：800x600px 或更高，支持 JPG、PNG 格式</li>
              </ul>
            </div>
            
            <!-- 图片预览对话框 -->
            <el-dialog v-model="showCeramicImagePreview" title="图片预览" width="800px">
              <img :src="previewCeramicImageUrl" style="width: 100%;" alt="预览图片" />
            </el-dialog>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCeramicDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCeramic">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑酒店对话框 -->
    <el-dialog v-model="showHotelDialog" title="编辑酒店" width="700px">
      <el-form :model="hotelForm" label-width="100px">
        <el-form-item label="酒店名称">
          <el-input v-model="hotelForm.shopName" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="酒店简介">
          <el-input
            v-model="hotelForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入酒店简介"
          />
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="hotelForm.adminRating" :max="5" :allow-half="true" show-score />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="hotelForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="hotelForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="hotelForm.openTime" placeholder="如：24小时营业" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="hotelForm.enabled"
            active-text="启用"
            inactive-text="停用"
            active-color="#67c23a"
            inactive-color="#909399"
          />
        </el-form-item>
        <el-form-item label="酒店设施">
          <el-select
            v-model="hotelForm.facilities"
            multiple
            collapse-tags
            placeholder="请选择酒店设施"
            style="width: 100%"
          >
            <el-option label="免费WiFi" value="免费WiFi" />
            <el-option label="停车场" value="停车场" />
            <el-option label="游泳池" value="游泳池" />
            <el-option label="健身房" value="健身房" />
            <el-option label="餐厅" value="餐厅" />
            <el-option label="接送服务" value="接送服务" />
            <el-option label="行李寄存" value="行李寄存" />
            <el-option label="24小时前台" value="24小时前台" />
          </el-select>
        </el-form-item>
        <el-form-item label="酒店标签">
          <el-select
            v-model="hotelForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入或选择标签，如：亲子友好、交通便利"
            style="width: 100%"
          >
            <el-option label="位置优越" value="位置优越" />
            <el-option label="亲子友好" value="亲子友好" />
            <el-option label="干净卫生" value="干净卫生" />
            <el-option label="服务周到" value="服务周到" />
            <el-option label="海景房" value="海景房" />
          </el-select>
        </el-form-item>
        <el-form-item label="酒店图片">
          <div class="hotel-images-upload">
            <div class="upload-hint">第一张图片将作为封面图，所有图片会在详情页以轮播图形式展示</div>
            
            <!-- 图片列表展示区域（支持拖拽排序） -->
            <div class="hotel-image-list-container">
              <div 
                class="hotel-image-list"
                v-if="hotelForm.images && hotelForm.images.length > 0"
              >
                <div
                  v-for="(img, index) in hotelForm.images"
                  :key="index"
                  class="hotel-image-item"
                  :class="{ 'is-cover': index === 0 }"
                  draggable="true"
                  @dragstart="handleHotelDragStart(index, $event)"
                  @dragover.prevent="handleHotelDragOver(index, $event)"
                  @drop="handleHotelDrop(index, $event)"
                  @dragend="handleHotelDragEnd"
                >
                  <div class="hotel-image-thumbnail" @click.stop="previewHotelImage(img)">
                    <img :src="getHotelImageUrl(img)" :alt="`酒店图片 ${index + 1}`" />
                    <div class="hotel-image-overlay">
                      <div class="hotel-image-badges">
                        <el-tag v-if="index === 0" type="success" size="small">封面</el-tag>
                      </div>
                      <div class="hotel-image-actions">
                        <el-button
                          v-if="index !== 0"
                          type="primary"
                          size="small"
                          circle
                          @click.stop="setHotelAsCover(index)"
                          title="设为封面"
                        >
                          <el-icon><Star /></el-icon>
                        </el-button>
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click.stop="removeHotelImageByIndex(index)"
                          title="删除"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div class="hotel-image-index">{{ index + 1 }}</div>
                </div>
              </div>
              
              <!-- 上传按钮 -->
              <el-upload
                class="hotel-image-upload-btn"
                :http-request="customHotelImageUpload"
                :show-file-list="false"
                :before-upload="beforeHotelImageUpload"
                accept="image/*"
                multiple
              >
                <div class="hotel-upload-placeholder">
                  <el-icon class="hotel-upload-icon"><Plus /></el-icon>
                  <div class="hotel-upload-text">添加图片</div>
                </div>
              </el-upload>
            </div>
            
            <div class="hotel-image-info">
              <p class="hotel-image-hint">
                <el-icon><InfoFilled /></el-icon>
                图片说明：
              </p>
              <ul class="hotel-image-usage-list">
                <li>可以拖拽图片调整顺序，第一张自动成为封面</li>
                <li>建议尺寸：800x600px 或更高，支持 JPG、PNG 格式</li>
              </ul>
            </div>
            
            <!-- 图片预览对话框 -->
            <el-dialog v-model="showHotelImagePreview" title="图片预览" width="800px">
              <img :src="previewHotelImageUrl" style="width: 100%;" alt="预览图片" />
            </el-dialog>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHotelDialog = false">取消</el-button>
        <el-button type="primary" @click="saveHotel">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑路线对话框 -->
    <el-dialog v-model="showRouteDialog" :title="routeDialogTitle" width="800px">
      <el-form :model="routeForm" :rules="routeRules" ref="routeFormRef" label-width="100px">
        <el-form-item label="路线名称" prop="name">
          <el-input v-model="routeForm.name" placeholder="请输入路线名称" />
        </el-form-item>
        <el-form-item label="路线描述">
          <el-input
            v-model="routeForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入路线描述"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="天数" prop="days">
              <el-input-number v-model="routeForm.days" :min="1" :max="30" placeholder="天数" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="routeForm.difficulty" placeholder="请选择难度" style="width: 100%;">
                <el-option label="轻松" value="轻松" />
                <el-option label="中等" value="中等" />
                <el-option label="困难" value="困难" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总价">
              <el-input-number v-model="routeForm.totalPrice" :min="0" :precision="2" placeholder="总价" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="路线标签">
          <el-input v-model="routeForm.tags" placeholder="多个标签用逗号分隔，如：经典,一日游,必游" />
        </el-form-item>
        <el-form-item label="路线图片">
          <el-upload
            class="route-image-uploader"
            :http-request="customRouteImageUpload"
            :show-file-list="false"
            :before-upload="beforeRouteImageUpload"
            accept="image/*"
          >
            <img v-if="routeForm.image" :src="routeForm.image" class="route-uploaded-image" />
            <div v-else class="route-upload-placeholder">
              <el-icon class="route-upload-icon"><Plus /></el-icon>
              <div class="route-upload-text">上传图片</div>
            </div>
          </el-upload>
          <div v-if="routeForm.image" class="route-image-actions">
            <el-button size="small" type="danger" @click="removeRouteImage">删除图片</el-button>
          </div>
        </el-form-item>
        <el-form-item label="包含景点">
          <div class="route-scenic-spots">
            <div class="scenic-spots-hint">景点信息将以JSON格式存储，格式：[{id: 1, name: "景点名称", order: 1, duration: "2小时"}]</div>
            <el-input
              v-model="routeForm.scenicSpots"
              type="textarea"
              :rows="6"
              placeholder='请输入JSON格式的景点列表，例如：[{"id": 1, "name": "古窑民俗博览区", "order": 1, "duration": "2小时"}]'
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRouteDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoute">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, Plus, Search, Location, User, Shop, MapLocation, Setting, Star, Delete, InfoFilled, UserFilled } from '@element-plus/icons-vue'
import { useRealtimeData } from '../../composables/useRealtimeData'
import { getScenicList, getScenicDetail, deleteScenic as deleteScenicAPI, createScenic as createScenicAPI, updateScenic } from '../../api/scenic'
import {
  getAllMerchants,
  createMerchantDeleteJob as createMerchantDeleteJobAPI,
  getMerchantDeleteJobStatus as getMerchantDeleteJobStatusAPI,
  updateMerchantBasicInfo,
  createFoodMerchantAdmin
} from '../../api/merchant'
import { useUserStore } from '../../stores/user'
import { getRecommendFood, deleteFood as deleteFoodAPI } from '../../api/food'
import { getRecommendRoutes, getAllRoutes as getAllRoutesAPI, deleteRoute as deleteRouteAPI, getRouteDetail, createRoute, updateRoute } from '../../api/route'
import { getAllUsers, deleteUser as deleteUserAPI, adminResetPassword as adminResetPasswordAPI } from '../../api/auth'
import { getSystemSettings, saveSystemSettings, backupData as backupDataAPI, restoreData as restoreDataAPI } from '../../api/systemConfig'
import { getAllMarketplaces, createMarketplace, updateMarketplace, deleteMarketplace } from '../../api/marketplace'
import request from '../../utils/request'

const router = useRouter()

// 实时数据
const { data: realtimeScenics } = useRealtimeData('scenic_spot', [])
const { data: realtimeUsers } = useRealtimeData('user', [])
const { data: realtimeMerchants } = useRealtimeData('merchant', [])
const { data: realtimeRoutes } = useRealtimeData('travel_route', [])

// 当前标签页
const activeTab = ref('scenic')

// 系统信息
const systemInfo = ref({
  totalScenics: 0,
  totalUsers: 0,
  totalMerchants: 0,
  totalRoutes: 0
})

// 景点管理
const scenicLoading = ref(false)
const scenics = ref([])
const scenicTotal = ref(0)
const scenicSearchKeyword = ref('')
const selectedScenics = ref([])
const scenicCurrentPage = ref(1)
const scenicPageSize = ref(10)

// 用户管理
const userLoading = ref(false)
const users = ref([])
const userSearchKeyword = ref('')
const selectedUsers = ref([])
const userCurrentPage = ref(1)
const userPageSize = ref(10)

// 美食商家管理
const foodLoading = ref(false)
const foods = ref([])
const foodSearchKeyword = ref('')
const selectedFoods = ref([])
const foodCurrentPage = ref(1)
const foodPageSize = ref(10)
// 新增餐饮商家对话框
const showAddFoodDialog = ref(false)
const addFoodForm = reactive({
  username: '',
  password: '',
  shopName: '',
  description: '',
  address: '',
  phone: '',
  openTime: '',
  startPrice: '',
  avatar: '',
  images: [],
  cuisineType: '',
  avgPrice: ''
})

const uploadAction = '/api/upload'
const handleAvatarUploadSuccess = (res) => {
  if (res && res.success && res.url) {
    addFoodForm.avatar = res.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res?.message || '头像上传失败')
  }
}
const handleImagesUploadSuccess = (res, file, fileList) => {
  if (res && res.success && res.url) {
    // 与 picture-card 组合：以显示用的 fileList 控制，表单里仅保留 URL 数组
    if (!addFoodForm.images.includes(res.url)) {
      addFoodForm.images.push(res.url)
    }
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res?.message || '图片上传失败')
  }
}
const handleImagesRemove = (file, fileList) => {
  const url = (file && (file.response?.url || file.url)) || ''
  if (url) {
    addFoodForm.images = addFoodForm.images.filter(u => u !== url)
  }
}

const resetAddFoodForm = () => {
  addFoodForm.username = ''
  addFoodForm.password = ''
  addFoodForm.shopName = ''
  addFoodForm.description = ''
  addFoodForm.address = ''
  addFoodForm.phone = ''
  addFoodForm.openTime = ''
  addFoodForm.startPrice = ''
  addFoodForm.avatar = ''
  addFoodForm.images = []
  addFoodForm.cuisineType = ''
  addFoodForm.avgPrice = ''
}

const openAddFoodDialog = () => {
  resetAddFoodForm()
  showAddFoodDialog.value = true
}

const submitAddFood = async () => {
  if (!addFoodForm.username || !addFoodForm.password || !addFoodForm.shopName) {
    ElMessage.warning('请填写用户名、密码和店铺名称')
    return
  }
  try {
    const payload = {
      username: addFoodForm.username,
      password: addFoodForm.password,
      shopName: addFoodForm.shopName,
      description: addFoodForm.description,
      address: addFoodForm.address,
      phone: addFoodForm.phone,
      openTime: addFoodForm.openTime,
      startPrice: addFoodForm.startPrice,
      avatar: addFoodForm.avatar,
      images: Array.isArray(addFoodForm.images) ? addFoodForm.images : [],
      cuisineType: addFoodForm.cuisineType,
      avgPrice: addFoodForm.avgPrice
    }
    const res = await createFoodMerchantAdmin(payload)
    if (res && res.success) {
      ElMessage.success('创建成功')
      showAddFoodDialog.value = false
      // 清空表单
      addFoodForm.username = ''
      addFoodForm.password = ''
      addFoodForm.shopName = ''
      addFoodForm.description = ''
      addFoodForm.address = ''
      addFoodForm.phone = ''
      addFoodForm.openTime = ''
      addFoodForm.startPrice = ''
      addFoodForm.avatar = ''
      addFoodForm.images = []
      addFoodForm.cuisineType = ''
      addFoodForm.avgPrice = ''
      // 重新加载数据
      await loadAllData()
    } else {
      const msg = res?.message || '创建失败'
      ElMessage.error(msg)
    }
  } catch (e) {
    console.error('创建餐饮商家失败:', e)
    ElMessage.error(e?.response?.data?.message || e.message || '创建失败')
  }
}

// 陶瓷商家管理
const ceramicLoading = ref(false)
const ceramics = ref([])
const ceramicSearchKeyword = ref('')
const selectedCeramics = ref([])
const ceramicCurrentPage = ref(1)
const ceramicPageSize = ref(10)

// 酒店商家管理
const hotelLoading = ref(false)
const hotels = ref([])
const hotelSearchKeyword = ref('')
const selectedHotels = ref([])
const hotelCurrentPage = ref(1)
const hotelPageSize = ref(10)

// 酒店编辑对话框
const showHotelDialog = ref(false)
const editingHotelId = ref(null)
const showHotelImagePreview = ref(false)
const previewHotelImageUrl = ref('')
const hotelForm = reactive({
  shopName: '',
  description: '',
  address: '',
  phone: '',
  openTime: '',
  adminRating: 0, // 新增管理员评分字段
  images: [], // 多张图片数组
  tags: [],
  facilities: [],
  enabled: true
})

// 餐饮商家编辑对话框
const showFoodDialog = ref(false)
const editingFoodId = ref(null)
const showFoodImagePreview = ref(false)
const previewFoodImageUrl = ref('')
const foodForm = reactive({
  shopName: '',
  description: '',
  address: '',
  phone: '',
  openTime: '',
  adminRating: 0,
  tags: '',
  images: []
})
const editingCeramicId = ref(null)
const showCeramicDialog = ref(false)
const showCeramicImagePreview = ref(false)
const previewCeramicImageUrl = ref('')
const ceramicForm = reactive({
  shopName: '',
  description: '',
  address: '',
  phone: '',
  openTime: '',
  adminRating: 0, // 新增管理员评分字段
  tags: '', // 新增标签字段
  images: [] // 多张图片数组
})

// 路线管理
const routeLoading = ref(false)
const routes = ref([])
const routeSearchKeyword = ref('')
const selectedRoutes = ref([])
const routeCurrentPage = ref(1)
const routePageSize = ref(10)

// 市集管理
const marketplaceLoading = ref(false)
const marketplaces = ref([])
const marketplaceSearchKeyword = ref('')
const selectedMarketplaces = ref([])
const marketplaceCurrentPage = ref(1)
const marketplacePageSize = ref(10)

// 市集编辑对话框
const showMarketplaceDialog = ref(false)
const editingMarketplaceId = ref(null)
const showMarketplaceImagePreview = ref(false)
const previewMarketplaceImageUrl = ref('')
const marketplaceForm = reactive({
  name: '',
  openTime: '',
  address: '',
  description: '',
  enabled: true,
  sortOrder: 0,
  images: [] // 轮播图图片数组
})

// 编辑路线对话框
const showRouteDialog = ref(false)
const routeFormRef = ref()
const editingRouteId = ref(null) // 编辑中的路线ID，null表示新增
const routeForm = reactive({
  name: '',
  description: '',
  days: 1,
  difficulty: '',
  totalPrice: 0,
  tags: '',
  image: '',
  scenicSpots: '' // JSON字符串格式
})

const userStore = useUserStore()
const uploadHeaders = computed(() => {
  return userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {}
})

const routeRules = {
  name: [
    { required: true, message: '请输入路线名称', trigger: 'blur' }
  ],
  days: [
    { required: true, message: '请输入天数', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择难度', trigger: 'change' }
  ]
}

const routeDialogTitle = computed(() => {
  return editingRouteId.value ? '编辑路线' : '新增路线'
})

// 新增/编辑景点对话框
const showScenicDialog = ref(false)
const scenicFormRef = ref()
const editingScenicId = ref(null) // 编辑中的景点ID，null表示新增
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const scenicForm = reactive({
  name: '',
  category: '',
  price: 0,
  rating: 0,
  openTime: '',
  openingHoursDetail: '',
  address: '',
  phone: '',
  officialPhone: '',
  notice: '',
  noticeDetail: '',
  preferentialPolicy: '',
  facilitiesDetail: '',
  ticketInfo: '',
  features: '',
  highlights: '',
  tips: '',
  description: '',
  tags: '',
  tag: '',
  images: [], // 多张图片数组
  latitude: null,
  longitude: null
})

const scenicRules = {
  name: [
    { required: true, message: '请输入景点名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择景点分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入门票价格', trigger: 'blur' }
  ]
}

const scenicDialogTitle = computed(() => {
  return editingScenicId.value ? '编辑景点' : '新增景点'
})

// 系统设置
const systemSettings = reactive({
  systemName: '景德镇旅游智能推荐系统',
  announcement: '',
  recommendAlgorithm: 'hybrid'
})

// 计算属性
// 景点列表改为“后端分页 + 后端搜索”，这里不再做前端 filtered/slice
const scenicTotalPages = computed(() => {
  const total = Number(scenicTotal.value) || 0
  const size = Number(scenicPageSize.value) || 10
  return Math.max(1, Math.ceil(total / size))
})

const scenicRange = computed(() => {
  const total = Number(scenicTotal.value) || 0
  const page = Number(scenicCurrentPage.value) || 1
  const size = Number(scenicPageSize.value) || 10
  if (total <= 0) return null
  const from = (page - 1) * size + 1
  const to = Math.min(page * size, total)
  return { from, to }
})

// 获取评分值（确保返回数字类型）
const getRatingValue = (rating) => {
  if (rating === null || rating === undefined) {
    return 0
  }
  // 如果是字符串，转换为数字
  if (typeof rating === 'string') {
    const num = parseFloat(rating)
    return isNaN(num) ? 0 : num
  }
  // 如果是数字，直接返回
  if (typeof rating === 'number') {
    return rating
  }
  // 如果是对象（BigDecimal），尝试获取数值
  if (typeof rating === 'object' && rating !== null) {
    if (rating.value !== undefined) {
      return Number(rating.value) || 0
    }
    if (rating.toString) {
      const num = parseFloat(rating.toString())
      return isNaN(num) ? 0 : num
    }
  }
  // 其他情况返回0
  return 0
}

const paginatedScenics = computed(() => scenics.value)

const loadScenicsPage = async () => {
  try {
    scenicLoading.value = true
    const params = {
      page: scenicCurrentPage.value,
      size: scenicPageSize.value
    }
    if (scenicSearchKeyword.value && scenicSearchKeyword.value.trim()) {
      params.keyword = scenicSearchKeyword.value.trim()
    }
    const response = await getScenicList(params)
    if (response?.success) {
      scenics.value = Array.isArray(response.data) ? response.data : []
      scenicTotal.value = Number(response.total) || scenics.value.length || 0
      // 如果后端返回了 pages，则优先使用它纠正页码范围
      const pages = Number(response.pages)
      if (Number.isFinite(pages) && pages > 0 && scenicCurrentPage.value > pages) {
        scenicCurrentPage.value = pages
      }
    } else if (Array.isArray(response)) {
      // 兜底：旧接口/异常返回数组
      scenics.value = response
      scenicTotal.value = response.length
    } else {
      scenics.value = []
      scenicTotal.value = 0
    }
  } catch (error) {
    console.error('加载景点分页数据失败:', error)
    scenics.value = []
    scenicTotal.value = 0
  } finally {
    scenicLoading.value = false
  }
}

const filteredUsers = computed(() => {
  if (!userSearchKeyword.value) return users.value
  return users.value.filter(item =>
    item.username?.includes(userSearchKeyword.value) ||
    item.email?.includes(userSearchKeyword.value)
  )
})

const paginatedUsers = computed(() => {
  const start = (userCurrentPage.value - 1) * userPageSize.value
  const end = start + userPageSize.value
  return filteredUsers.value.slice(start, end)
})

const filteredFoods = computed(() => {
  if (!foodSearchKeyword.value) return foods.value
  return foods.value.filter(item =>
    item.name?.includes(foodSearchKeyword.value) ||
    item.category?.includes(foodSearchKeyword.value)
  )
})

const paginatedFoods = computed(() => {
  const start = (foodCurrentPage.value - 1) * foodPageSize.value
  const end = start + foodPageSize.value
  return filteredFoods.value.slice(start, end)
})

const filteredCeramics = computed(() => {
  if (!ceramicSearchKeyword.value) return ceramics.value
  return ceramics.value.filter(item =>
    item.shopName?.includes(ceramicSearchKeyword.value) ||
    item.description?.includes(ceramicSearchKeyword.value)
  )
})

const paginatedCeramics = computed(() => {
  const start = (ceramicCurrentPage.value - 1) * ceramicPageSize.value
  const end = start + ceramicPageSize.value
  return filteredCeramics.value.slice(start, end)
})

const filteredHotels = computed(() => {
  if (!hotelSearchKeyword.value) return hotels.value
  return hotels.value.filter(item =>
    item.shopName?.includes(hotelSearchKeyword.value) ||
    item.description?.includes(hotelSearchKeyword.value)
  )
})

const paginatedHotels = computed(() => {
  const start = (hotelCurrentPage.value - 1) * hotelPageSize.value
  const end = start + hotelPageSize.value
  return filteredHotels.value.slice(start, end)
})

const filteredRoutes = computed(() => {
  if (!routeSearchKeyword.value) return routes.value
  return routes.value.filter(item =>
    item.name?.includes(routeSearchKeyword.value) ||
    item.description?.includes(routeSearchKeyword.value)
  )
})

const paginatedRoutes = computed(() => {
  const start = (routeCurrentPage.value - 1) * routePageSize.value
  const end = start + routePageSize.value
  return filteredRoutes.value.slice(start, end)
})

const filteredMarketplaces = computed(() => {
  if (!marketplaceSearchKeyword.value) return marketplaces.value
  return marketplaces.value.filter(item =>
    item.name?.includes(marketplaceSearchKeyword.value) ||
    item.address?.includes(marketplaceSearchKeyword.value) ||
    item.openTime?.includes(marketplaceSearchKeyword.value)
  )
})

const paginatedMarketplaces = computed(() => {
  const start = (marketplaceCurrentPage.value - 1) * marketplacePageSize.value
  const end = start + marketplacePageSize.value
  return filteredMarketplaces.value.slice(start, end)
})

// 方法
const handleBack = () => {
  router.back()
}

const refreshData = async () => {
  await loadAllData()
    ElMessage.success('数据刷新成功')
}

const loadAllData = async () => {
  // 所有请求并行发出，getAllMerchants 只调一次
  const [scenicResult, usersResponse, merchantsResponse, foodResponse, routesResponse] =
    await Promise.allSettled([
      loadScenicsPage(),
      getAllUsers(),
      getAllMerchants(),
      getRecommendFood({ includeMerchants: true }),
      getAllRoutesAPI()
    ])

  // 处理用户数据
  userLoading.value = true
  try {
    let userList = []
    if (usersResponse.status === 'fulfilled') {
      const response = usersResponse.value
      if (response?.success && Array.isArray(response.data)) userList = response.data
      else if (Array.isArray(response)) userList = response
      else if (response?.data && Array.isArray(response.data)) userList = response.data
    }

    // 商家数据（复用已拿到的结果）
    let merchantList = []
    if (merchantsResponse.status === 'fulfilled') {
      const r = merchantsResponse.value
      if (r?.success && Array.isArray(r.data)) merchantList = r.data
      else if (Array.isArray(r)) merchantList = r
    }

    users.value = userList.map(user => {
      const merchant = merchantList.find(m => {
        if (m.user) {
          return (typeof m.user === 'object' && m.user.id === user.id) ||
                 (typeof m.user === 'number' && m.user === user.id) ||
                 (typeof m.user === 'string' && parseInt(m.user) === user.id)
        }
        return m.userId === user.id || m.user_id === user.id
      })
      const createTime = user.createdAt || user.created_at || user.createTime || user.create_time
      return { ...user, merchantCategory: merchant?.category || null, createTime: createTime || null }
    })
  } catch (error) {
    console.error('处理用户数据失败:', error)
  } finally {
    userLoading.value = false
  }

  // 处理商家数据（陶瓷 + 酒店，复用同一份结果）
  ceramicLoading.value = true
  hotelLoading.value = true
  try {
    let merchantList = []
    if (merchantsResponse.status === 'fulfilled') {
      const r = merchantsResponse.value
      if (r?.success && Array.isArray(r.data)) merchantList = r.data
      else if (Array.isArray(r)) merchantList = r
    }
    ceramics.value = merchantList.filter(item => item.category === 'CERAMIC' || item.category === '陶瓷')
    hotels.value = merchantList.filter(item => item.category === 'HOTEL' || item.category === '酒店')
  } catch (error) {
    console.error('处理商家数据失败:', error)
  } finally {
    ceramicLoading.value = false
    hotelLoading.value = false
  }

  // 处理美食数据
  foodLoading.value = true
  try {
    if (foodResponse.status === 'fulfilled') {
      const response = foodResponse.value
      const list = (response?.success && Array.isArray(response.data)) ? response.data
                 : Array.isArray(response) ? response : []
      foods.value = list.filter(item =>
        item.category === '美食商家' || item.merchantId || item.category?.includes('美食')
      )
    }
  } catch (error) {
    console.error('处理美食数据失败:', error)
  } finally {
    foodLoading.value = false
  }

  // 处理路线数据
  routeLoading.value = true
  try {
    if (routesResponse.status === 'fulfilled') {
      const response = routesResponse.value
      if (response?.success && Array.isArray(response.data)) routes.value = response.data
      else if (Array.isArray(response)) routes.value = response
    } else {
      // 降级
      try {
        const fallback = await getRecommendRoutes()
        if (fallback?.success && Array.isArray(fallback.data)) routes.value = fallback.data
      } catch (e) {
        console.error('降级加载路线也失败:', e)
      }
    }
  } catch (error) {
    console.error('处理路线数据失败:', error)
  } finally {
    routeLoading.value = false
  }

  // 加载市集数据（独立接口，并行触发后等待）
  await loadMarketplaces()

  // 更新统计信息
  updateSystemInfo()
}

const loadMarketplaces = async () => {
  try {
    marketplaceLoading.value = true
    const res = await getAllMarketplaces()
    if (res && res.success && Array.isArray(res.data)) {
      marketplaces.value = res.data
    } else if (Array.isArray(res)) {
      marketplaces.value = res
    } else {
      marketplaces.value = []
    }
  } catch (error) {
    console.error('加载市集数据失败:', error)
  } finally {
    marketplaceLoading.value = false
  }
}

const updateSystemInfo = () => {
  systemInfo.value = {
    totalScenics: Number(scenicTotal.value) || scenics.value.length,
    totalUsers: users.value.length,
    totalMerchants: ceramics.value.length + foods.value.length + hotels.value.length,
    totalRoutes: routes.value.length
  }
}

// 监听实时数据
watch(realtimeScenics, (val) => {
  if (Array.isArray(val) && val.length > 0) {
    scenics.value = val
    updateSystemInfo()
  }
}, { deep: true })

watch(realtimeUsers, (val) => {
  if (Array.isArray(val) && val.length > 0) {
    users.value = val
    updateSystemInfo()
  }
}, { deep: true })

watch(realtimeMerchants, (val) => {
  if (Array.isArray(val) && val.length > 0) {
    const ceramicMerchants = val.filter(item => 
      item.category === 'CERAMIC' || item.category === '陶瓷'
    )
    ceramics.value = ceramicMerchants
    
    const hotelMerchants = val.filter(item => 
      item.category === 'HOTEL' || item.category === '酒店'
    )
    hotels.value = hotelMerchants
    
    updateSystemInfo()
  }
}, { deep: true })

watch(realtimeRoutes, (val) => {
  if (Array.isArray(val) && val.length > 0) {
    routes.value = val
    updateSystemInfo()
  }
}, { deep: true })

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'scenic' && scenics.value.length === 0) {
    loadAllData()
  } else if (tabName === 'user' && users.value.length === 0) {
    loadAllData()
  } else if (tabName === 'food' && foods.value.length === 0) {
    loadAllData()
  } else if (tabName === 'ceramic' && ceramics.value.length === 0) {
    loadAllData()
  } else if (tabName === 'hotel' && hotels.value.length === 0) {
    loadAllData()
  } else if (tabName === 'route' && routes.value.length === 0) {
    loadAllData()
  } else if (tabName === 'marketplace' && marketplaces.value.length === 0) {
    loadMarketplaces()
  } else if (tabName === 'settings') {
    // 切换到系统设置时加载设置
    loadSystemSettings()
  }
}

// 景点管理方法
const handleScenicSearch = () => {
  scenicCurrentPage.value = 1
  loadScenicsPage()
}

const handleScenicSelectionChange = (selection) => {
  selectedScenics.value = selection
}

const handleScenicSizeChange = (val) => {
  scenicPageSize.value = val
  scenicCurrentPage.value = 1
  loadScenicsPage()
}

const handleScenicCurrentChange = (val) => {
  scenicCurrentPage.value = val
  loadScenicsPage()
}

const showAddScenicDialog = () => {
  editingScenicId.value = null
  // 重置表单
  Object.assign(scenicForm, {
    name: '',
    category: '',
    price: 0,
    rating: 0,
    openTime: '',
    openingHoursDetail: '',
    address: '',
    phone: '',
    officialPhone: '',
    notice: '',
    noticeDetail: '',
    preferentialPolicy: '',
    facilitiesDetail: '',
    ticketInfo: '',
    features: '',
    highlights: '',
    tips: '',
    description: '',
    tags: '',
    tag: '',
    images: [],
    latitude: null,
    longitude: null
  })
  // 等待DOM更新后再显示对话框
  showScenicDialog.value = true
}

// 编辑景点
const editScenic = async (scenic) => {
  // 确保有有效的景点ID
  if (!scenic || !scenic.id) {
    ElMessage.error('景点信息无效，无法编辑')
    return
  }
  
  // 设置编辑ID，确保不会创建新景点
  editingScenicId.value = scenic.id
  console.log('开始编辑景点，ID:', editingScenicId.value)

  // 直接用列表数据填表单（列表走 scenic_spot 表，字段完整）
  const s = scenic
  Object.assign(scenicForm, {
    name: s.name || '',
    category: s.category || '',
    price: s.price || 0,
    rating: s.rating || 0,
    openTime: s.openTime || '',
    openingHoursDetail: s.openingHoursDetail || s.opening_hours_detail || '',
    address: s.address || '',
    phone: s.phone || '',
    officialPhone: s.officialPhone || s.official_phone || '',
    notice: s.notice || '',
    noticeDetail: s.noticeDetail || s.noticeContent || s.publicNotice || s.ticketInfo || '',
    preferentialPolicy: s.preferentialPolicy || s.preferential_policy || '',
    facilitiesDetail: s.facilitiesDetail || s.facilities_detail || '',
    ticketInfo: s.ticketInfo || s.ticket_info || '',
    features: Array.isArray(s.features) ? s.features.join(',') : (s.features || ''),
    highlights: Array.isArray(s.highlights) ? s.highlights.join(',') : (s.highlights || ''),
    tips: s.tips || '',
    description: s.description || '',
    tags: s.tags || '',
    tag: s.tag || '',
    latitude: s.latitude != null ? s.latitude : null,
    longitude: s.longitude != null ? s.longitude : null
  })
  
  // 处理图片：将字符串转换为数组
  scenicForm.images = []
  if (s.scenicImages) {
    const imageUrls = s.scenicImages.split(',').map(url => url.trim()).filter(url => url)
    scenicForm.images = imageUrls.map(url => ({
      url: url,
      name: url.split('/').pop() || 'image.jpg'
    }))
  } else if (s.images) {
    if (Array.isArray(s.images)) {
      scenicForm.images = s.images.map(url => ({
        url: url,
        name: url.split('/').pop() || 'image.jpg'
      }))
    } else if (typeof s.images === 'string') {
      const imageUrls = s.images.split(',').map(url => url.trim()).filter(url => url)
      scenicForm.images = imageUrls.map(url => ({
        url: url,
        name: url.split('/').pop() || 'image.jpg'
      }))
    }
  } else if (s.image) {
    scenicForm.images = [{
      url: s.image,
      name: s.image.split('/').pop() || 'image.jpg'
    }]
  }
  
  showScenicDialog.value = true
}

const saveScenic = async () => {
  if (!scenicFormRef.value) return

  try {
    await scenicFormRef.value.validate()

    // 确保编辑模式下有有效的ID
    if (editingScenicId.value) {
      // 强制ID为字符串格式并清理空白
      const safeId = String(editingScenicId.value).trim()
      console.log('Save Scenic Debug: editingScenicId =', editingScenicId.value, 'SafeId =', safeId)
      
      if (!safeId || safeId === '0' || safeId === 'undefined' || safeId === 'null') {
        ElMessage.error('景点ID无效，无法更新: ' + editingScenicId.value)
        return
      }
      
      // 更新为清理后的ID
      editingScenicId.value = safeId
      console.log('编辑模式：景点ID =', editingScenicId.value)
    } else {
      console.log('新增模式：创建新景点')
    }

    // 提取图片URL数组
    const imageUrls = scenicForm.images.map(img => {
      if (typeof img === 'string') return img
      return img.url || img.response?.url || img.name
    }).filter(url => url && !url.startsWith('data:'))

    // 构建景点数据，确保所有字段都被发送（包括空值）
    const scenicData = {
      name: scenicForm.name || '',
      category: scenicForm.category || '',
      price: scenicForm.price || 0,
      tag: scenicForm.tag || '',
      openTime: scenicForm.openTime || '',
      openingHoursDetail: scenicForm.openingHoursDetail || '',
      address: scenicForm.address || '',
      phone: scenicForm.phone || '',
      officialPhone: scenicForm.officialPhone || '',
      notice: scenicForm.notice || '',
      noticeDetail: scenicForm.noticeDetail || '',
      preferentialPolicy: scenicForm.preferentialPolicy || '',
      facilitiesDetail: scenicForm.facilitiesDetail || '',
      ticketInfo: scenicForm.ticketInfo || '',
      features: scenicForm.features || '',
      highlights: scenicForm.highlights || '',
      tips: scenicForm.tips || '',
      description: scenicForm.description || '',
      tags: scenicForm.tags || '',
      images: imageUrls, // 传递图片数组
      latitude: scenicForm.latitude !== '' && scenicForm.latitude != null ? Number(scenicForm.latitude) : null,
      longitude: scenicForm.longitude !== '' && scenicForm.longitude != null ? Number(scenicForm.longitude) : null
    }
    
    // 处理评分：如果设置了评分则发送，否则不发送（让系统自动计算）
    if (scenicForm.rating && scenicForm.rating > 0) {
      scenicData.rating = scenicForm.rating
    }
    
    console.log('准备保存的景点数据:', scenicData)
    console.log('编辑模式状态:', editingScenicId.value ? '编辑 (ID: ' + editingScenicId.value + ')' : '新增')

    let response
    if (editingScenicId.value) {
      // 编辑模式：使用PUT方法更新现有景点
      console.log('执行更新操作，景点ID:', editingScenicId.value)
      try {
        response = await updateScenic(editingScenicId.value, scenicData)
        if (response && response.success) {
          ElMessage.success('景点更新成功')
        } else {
          ElMessage.error(response?.message || '更新失败')
          return
        }
      } catch (error) {
        console.error('更新景点失败:', error)
        if (error.response) {
          console.error('错误响应状态:', error.response.status)
          console.error('错误响应数据:', error.response.data)
        }
        ElMessage.error(error.response?.data?.message || error.message || '更新失败')
        return
      }
    } else {
      // 新增模式：使用POST方法创建新景点
      console.log('执行创建操作')
      try {
        response = await createScenicAPI(scenicData)
        if (response && response.success) {
          ElMessage.success('景点创建成功')
        } else {
          ElMessage.error(response?.message || '创建失败')
          return
        }
      } catch (error) {
        console.error('创建景点失败:', error)
        ElMessage.error(error.response?.data?.message || error.message || '创建失败')
        return
      }
    }

    if (response && response.success) {
      showScenicDialog.value = false
      editingScenicId.value = null
      // 重新加载景点列表
      await loadAllData()
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('保存景点失败:', error)
      ElMessage.error('保存失败: ' + (error.message || '未知错误'))
    }
  }
}

// 图片上传相关方法
const beforeScenicImageUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  // 检查文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  return true
}

const customScenicImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    // 使用 request 工具，确保携带 token
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 处理响应
    const url = response.url || response.data?.url
    if (response.success && url) {
      // 上传成功，添加到图片列表
      const newImage = {
        url: url,
        name: file.name || url.split('/').pop() || 'image.jpg',
        response: response
      }
      scenicForm.images.push(newImage)
      ElMessage.success('图片上传成功')
      if (onSuccess) {
        onSuccess(response, file)
      }
    } else {
      ElMessage.error(response.message || '图片上传失败')
      if (onError) {
        onError(new Error(response.message || '图片上传失败'))
      }
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
    if (onError) {
      onError(error)
    }
  }
}

// 获取图片URL（兼容不同格式）
const getScenicImageUrl = (img) => {
  if (typeof img === 'string') return img
  return img.url || img.response?.url || img.name || ''
}

// 删除图片（通过索引）
const removeScenicImageByIndex = (index) => {
  if (!Array.isArray(scenicForm.images) || scenicForm.images.length === 0) {
    return
  }
  
  scenicForm.images.splice(index, 1)
  ElMessage.success('图片已删除')
}

// 设置某张图片为封面（移动到第一位）
const setScenicAsCover = (index) => {
  if (!Array.isArray(scenicForm.images) || index === 0) {
    return
  }
  
  const images = [...scenicForm.images]
  const targetImage = images[index]
  images.splice(index, 1)
  images.unshift(targetImage)
  
  scenicForm.images = images
  ElMessage.success('已设置为封面图')
}

// 拖拽排序相关
const scenicDraggedIndex = ref(null)

const handleScenicDragStart = (index, event) => {
  scenicDraggedIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/html', event.target.outerHTML)
  event.target.style.opacity = '0.5'
}

const handleScenicDragOver = (index, event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
  
  // 添加视觉反馈
  const target = event.currentTarget
  if (scenicDraggedIndex.value !== null && scenicDraggedIndex.value !== index) {
    target.classList.add('drag-over')
  }
}

const handleScenicDrop = (index, event) => {
  event.preventDefault()
  
  // 移除视觉反馈
  document.querySelectorAll('.scenic-image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  
  if (scenicDraggedIndex.value === null || scenicDraggedIndex.value === index) {
    return
  }
  
  // 执行数组元素移动
  const images = [...scenicForm.images]
  const draggedItem = images[scenicDraggedIndex.value]
  images.splice(scenicDraggedIndex.value, 1)
  images.splice(index, 0, draggedItem)
  
  scenicForm.images = images
  
  scenicDraggedIndex.value = null
}

const handleScenicDragEnd = (event) => {
  event.target.style.opacity = '1'
  document.querySelectorAll('.scenic-image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  scenicDraggedIndex.value = null
}

const previewScenicImage = (img) => {
  const url = getScenicImageUrl(img)
  if (url) {
    previewImageUrl.value = url
    showImagePreview.value = true
  }
}

const deleteScenic = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除这个景点吗？\n删除后将同时删除关联的景点商家记录。\n删除后无法恢复！', 
      '删除景点商家', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }
    )

    const response = await deleteScenicAPI(id)
    if (response.success) {
      scenics.value = scenics.value.filter(s => s.id !== id)
      updateSystemInfo()
      ElMessage.success('景点及关联商家删除成功')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除景点失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteScenics = async () => {
  if (selectedScenics.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确认要删除选中的 ${selectedScenics.value.length} 个景点吗？\n删除后将同时删除关联的景点商家记录。\n删除后无法恢复！`, 
      '批量删除景点商家', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const deletePromises = selectedScenics.value.map(item => deleteScenicAPI(item.id))
    await Promise.all(deletePromises)
    
    const ids = selectedScenics.value.map(item => item.id)
    scenics.value = scenics.value.filter(s => !ids.includes(s.id))
    selectedScenics.value = []
    updateSystemInfo()
    ElMessage.success('批量删除景点及关联商家成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 用户管理方法
const handleUserSearch = () => {
  userCurrentPage.value = 1
}

const handleUserSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleUserSizeChange = (val) => {
  userPageSize.value = val
  userCurrentPage.value = 1
}

const handleUserCurrentChange = (val) => {
  userCurrentPage.value = val
}

// 重置密码
const resetPasswordVisible = ref(false)
const resetPasswordLoading = ref(false)
const resetPasswordTarget = ref(null)
const resetPasswordForm = ref({ newPassword: '', confirmPassword: '' })

const openResetPassword = (user) => {
  resetPasswordTarget.value = user
  resetPasswordForm.value = { newPassword: '', confirmPassword: '' }
  resetPasswordVisible.value = true
}

const confirmResetPassword = async () => {
  const { newPassword, confirmPassword } = resetPasswordForm.value
  if (!newPassword || newPassword.length < 6) {
    ElMessage.warning('密码不能少于6位')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  resetPasswordLoading.value = true
  try {
    await adminResetPasswordAPI(resetPasswordTarget.value.id, newPassword)
    ElMessage.success(`用户 ${resetPasswordTarget.value.username} 密码重置成功`)
    resetPasswordVisible.value = false
  } catch (e) {
    ElMessage.error('重置失败：' + (e.message || '未知错误'))
  } finally {
    resetPasswordLoading.value = false
  }
}

const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除这个用户吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteUserAPI(id)
    if (response.success) {
      users.value = users.value.filter(u => u.id !== id)
      updateSystemInfo()
    ElMessage.success('删除成功')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteUsers = async () => {
  if (selectedUsers.value.length === 0) return

  try {
    await ElMessageBox.confirm(`确认要删除选中的 ${selectedUsers.value.length} 个用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const deletePromises = selectedUsers.value.map(item => deleteUserAPI(item.id))
    await Promise.all(deletePromises)
    
    const ids = selectedUsers.value.map(item => item.id)
    users.value = users.value.filter(u => !ids.includes(u.id))
    selectedUsers.value = []
    updateSystemInfo()
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 美食商家管理方法
const handleFoodSearch = () => {
  foodCurrentPage.value = 1
}

const handleFoodSelectionChange = (selection) => {
  selectedFoods.value = selection
}

const handleFoodSizeChange = (val) => {
  foodPageSize.value = val
  foodCurrentPage.value = 1
}

const handleFoodCurrentChange = (val) => {
  foodCurrentPage.value = val
}

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const executeMerchantDeleteTask = async (merchantId, options = {}) => {
  const { timeoutMs = 120000, intervalMs = 1500 } = options
  const createRes = await createMerchantDeleteJobAPI(merchantId)
  const jobId = createRes?.data?.jobId
  if (!jobId) {
    throw new Error('创建删除任务失败')
  }

  const startedAt = Date.now()
  while (Date.now() - startedAt < timeoutMs) {
    const statusRes = await getMerchantDeleteJobStatusAPI(jobId)
    const status = statusRes?.data?.status
    if (status === 'SUCCESS') {
      return statusRes.data
    }
    if (status === 'FAILED') {
      throw new Error(statusRes?.data?.errorMessage || '删除任务执行失败')
    }
    await sleep(intervalMs)
  }

  throw new Error('删除任务超时，请稍后刷新页面确认结果')
}

const deleteFood = async (id, merchantId) => {
  try {
    await ElMessageBox.confirm('确认要删除这个美食商家吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 如果是商家数据，删除商家；否则删除美食
    if (merchantId) {
      await executeMerchantDeleteTask(merchantId)
      foods.value = foods.value.filter(f => f.id !== id && f.merchantId !== merchantId)
      updateSystemInfo()
      ElMessage.success('删除成功')
    } else {
      const response = await deleteFoodAPI(id)
      if (response.success) {
        foods.value = foods.value.filter(f => f.id !== id)
        updateSystemInfo()
        ElMessage.success('删除成功')
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除美食商家失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteFoods = async () => {
  if (selectedFoods.value.length === 0) return

  try {
    await ElMessageBox.confirm(`确认要删除选中的 ${selectedFoods.value.length} 个美食商家吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    for (const item of selectedFoods.value) {
      if (item.merchantId) {
        await executeMerchantDeleteTask(item.merchantId)
      } else {
        await deleteFoodAPI(item.id)
      }
    }
    
    const ids = selectedFoods.value.map(item => item.id)
    foods.value = foods.value.filter(f => !ids.includes(f.id))
    selectedFoods.value = []
    updateSystemInfo()
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 陶瓷商家管理方法
const handleCeramicSearch = () => {
  ceramicCurrentPage.value = 1
}

const handleCeramicSelectionChange = (selection) => {
  selectedCeramics.value = selection
}

const handleCeramicSizeChange = (val) => {
  ceramicPageSize.value = val
  ceramicCurrentPage.value = 1
}

const handleCeramicCurrentChange = (val) => {
  ceramicCurrentPage.value = val
}

const editCeramic = (ceramic) => {
  editingCeramicId.value = ceramic.id
  Object.assign(ceramicForm, {
    shopName: ceramic.shopName || '',
    description: ceramic.description || '',
    address: ceramic.address || '',
    phone: ceramic.phone || '',
    openTime: ceramic.openTime || '',
    adminRating: ceramic.adminRating || 0
  })
  
  // 处理图片：将字符串转换为数组
  ceramicForm.images = []
  if (ceramic.shopImages) {
    // 如果有shopImages字段（逗号分隔的字符串）
    const imageUrls = ceramic.shopImages.split(',').map(url => url.trim()).filter(url => url)
    ceramicForm.images = imageUrls.map(url => ({
      url: url,
      name: url.split('/').pop() || 'image.jpg'
    }))
  } else if (ceramic.avatar) {
    // 兼容单张图片的情况
    ceramicForm.images = [{
      url: ceramic.avatar,
      name: ceramic.avatar.split('/').pop() || 'image.jpg'
    }]
  }
  
  showCeramicDialog.value = true
}

const saveCeramic = async () => {
  if (!editingCeramicId.value) {
    ElMessage.error('缺少工坊ID')
    return
  }
  try {
    const imageUrls = Array.isArray(ceramicForm.images)
      ? ceramicForm.images
          .map(img => getCeramicImageUrl(img))
          .filter(url => typeof url === 'string' && url.trim() !== '')
      : []
    const original = ceramics.value.find(c => c.id === editingCeramicId.value)
    const avatarForServer = imageUrls[0] || original?.avatar || null

    const res = await updateMerchantBasicInfo(editingCeramicId.value, {
      shopName: ceramicForm.shopName,
      description: ceramicForm.description,
      address: ceramicForm.address,
      phone: ceramicForm.phone,
      openTime: ceramicForm.openTime,
      adminRating: ceramicForm.adminRating || 0,
      avatar: avatarForServer,
      images: imageUrls
    })
    if (res.success) {
      ElMessage.success('保存成功')
      showCeramicDialog.value = false
      // 更新本地列表
      const idx = ceramics.value.findIndex(c => c.id === editingCeramicId.value)
      if (idx !== -1) {
        ceramics.value[idx] = { ...ceramics.value[idx], ...res.data }
      }
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error('保存陶瓷工坊失败:', e)
    ElMessage.error('保存失败')
  }
}

const deleteCeramic = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除这个陶瓷商家吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await executeMerchantDeleteTask(id)
    ceramics.value = ceramics.value.filter(c => c.id !== id)
    updateSystemInfo()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除陶瓷商家失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteCeramics = async () => {
  if (selectedCeramics.value.length === 0) return

  try {
    await ElMessageBox.confirm(`确认要删除选中的 ${selectedCeramics.value.length} 个陶瓷商家吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    for (const item of selectedCeramics.value) {
      await executeMerchantDeleteTask(item.id)
    }
    
    const ids = selectedCeramics.value.map(item => item.id)
    ceramics.value = ceramics.value.filter(c => !ids.includes(c.id))
    selectedCeramics.value = []
    updateSystemInfo()
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const editFood = (row) => {
  // 如果是商家数据（有merchantId或本身就是商家对象）
  const id = row.merchantId || row.id
  editingFoodId.value = id
  
  foodForm.shopName = row.name || row.shopName
  foodForm.description = row.description
  foodForm.address = row.address
  foodForm.phone = row.phone
  foodForm.openTime = row.openTime
  foodForm.adminRating = row.adminRating || 0
  foodForm.tags = row.tags || ''
  
  // 处理图片
  foodForm.images = []
  if (row.images && Array.isArray(row.images)) {
    foodForm.images = row.images.map(url => ({
      url: url,
      name: url.split('/').pop() || 'image.jpg'
    }))
  } else if (row.shopImages) {
    const imageUrls = row.shopImages.split(',').map(url => url.trim()).filter(url => url)
    foodForm.images = imageUrls.map(url => ({
      url: url,
      name: url.split('/').pop() || 'image.jpg'
    }))
  } else if (row.image || row.avatar) {
    const url = row.image || row.avatar
    foodForm.images = [{
      url: url,
      name: url.split('/').pop() || 'image.jpg'
    }]
  }
  
  showFoodDialog.value = true
}

const saveFood = async () => {
  if (!editingFoodId.value) {
    ElMessage.error('缺少商家ID')
    return
  }
  try {
    const imageUrls = Array.isArray(foodForm.images)
      ? foodForm.images
          .map(img => getCeramicImageUrl(img)) // 复用获取URL的方法
          .filter(url => typeof url === 'string' && url.trim() !== '')
      : []
    
    // 获取当前商家对象以保留封面图逻辑
    const original = foods.value.find(f => (f.merchantId === editingFoodId.value) || (f.id === editingFoodId.value))
    const avatarForServer = imageUrls[0] || original?.image || original?.avatar || null

    const res = await updateMerchantBasicInfo(editingFoodId.value, {
      shopName: foodForm.shopName,
      description: foodForm.description,
      address: foodForm.address,
      phone: foodForm.phone,
      openTime: foodForm.openTime,
      adminRating: foodForm.adminRating || 0,
      tags: Array.isArray(foodForm.tags)
        ? foodForm.tags
        : (foodForm.tags ? String(foodForm.tags).split(',').map(s => s && s.trim()).filter(Boolean) : []),
      avatar: avatarForServer,
      // 后端 basic-info 更新只处理 images（数组/字符串），由后端落库到 shopImages
      images: imageUrls
    })
    
    if (res.success) {
      ElMessage.success('保存成功')
      showFoodDialog.value = false
      loadAllData() // 重新加载以刷新列表
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error('保存餐饮商家失败:', e)
    ElMessage.error('保存失败')
  }
}

// 陶瓷工坊图片上传相关方法
const beforeCeramicImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  return true
}

// 餐饮图片上传相关方法（复用部分逻辑）
const beforeFoodImageUpload = beforeCeramicImageUpload
const customFoodImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    const response = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const url = response.url || response.data?.url
    if (response.success && url) {
      foodForm.images.push({
        url: url,
        name: file.name || url.split('/').pop() || 'image.jpg',
        response: response
      })
      ElMessage.success('图片上传成功')
      if (onSuccess) onSuccess(response, file)
    } else {
      ElMessage.error(response.message || '图片上传失败')
      if (onError) onError(new Error(response.message))
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
    if (onError) onError(error)
  }
}

// 替换/设置封面图
const uploadFoodCover = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    const response = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const url = response.url || response.data?.url
    if (response.success && url) {
      const newCover = {
        url: url,
        name: file.name || url.split('/').pop() || 'image.jpg',
        response
      }
      if (Array.isArray(foodForm.images) && foodForm.images.length > 0) {
        foodForm.images.splice(0, 1, newCover)
      } else {
        foodForm.images = [newCover]
      }
      ElMessage.success('封面更新成功')
      if (onSuccess) onSuccess(response, file)
    } else {
      ElMessage.error(response.message || '封面更新失败')
      if (onError) onError(new Error(response.message))
    }
  } catch (error) {
    console.error('封面更新失败:', error)
    ElMessage.error('封面更新失败')
    if (onError) onError(error)
  }
}

const removeFoodImageByIndex = (index) => {
  if (Array.isArray(foodForm.images)) {
    foodForm.images.splice(index, 1)
  }
}

const setFoodAsCover = (index) => {
  if (Array.isArray(foodForm.images) && index > 0) {
    const item = foodForm.images[index]
    foodForm.images.splice(index, 1)
    foodForm.images.unshift(item)
    ElMessage.success('已设置为封面图')
  }
}

// 陶瓷工坊图片上传相关方法
const customCeramicImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const url = response.url || response.data?.url
    if (response.success && url) {
      const newImage = {
        url: url,
        name: file.name || url.split('/').pop() || 'image.jpg',
        response: response
      }
      ceramicForm.images.push(newImage)
      ElMessage.success('图片上传成功')
      if (onSuccess) {
        onSuccess(response, file)
      }
    } else {
      ElMessage.error(response.message || '图片上传失败')
      if (onError) {
        onError(new Error(response.message || '图片上传失败'))
      }
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
    if (onError) {
      onError(error)
    }
  }
}

// 获取图片URL（兼容不同格式）
const getCeramicImageUrl = (img) => {
  if (typeof img === 'string') return img
  return img.url || img.response?.url || img.name || ''
}

// 删除图片（通过索引）
const removeCeramicImageByIndex = (index) => {
  if (!Array.isArray(ceramicForm.images) || ceramicForm.images.length === 0) {
    return
  }
  
  ceramicForm.images.splice(index, 1)
  ElMessage.success('图片已删除')
}

// 设置某张图片为封面（移动到第一位）
const setCeramicAsCover = (index) => {
  if (!Array.isArray(ceramicForm.images) || index === 0) {
    return
  }
  
  const images = [...ceramicForm.images]
  const targetImage = images[index]
  images.splice(index, 1)
  images.unshift(targetImage)
  
  ceramicForm.images = images
  ElMessage.success('已设置为封面图')
}

// 拖拽排序相关
const ceramicDraggedIndex = ref(null)

const handleCeramicDragStart = (index, event) => {
  ceramicDraggedIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/html', event.target.outerHTML)
  event.target.style.opacity = '0.5'
}

const handleCeramicDragOver = (index, event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
  
  const target = event.currentTarget
  if (ceramicDraggedIndex.value !== null && ceramicDraggedIndex.value !== index) {
    target.classList.add('drag-over')
  }
}

const handleCeramicDrop = (index, event) => {
  event.preventDefault()
  
  document.querySelectorAll('.ceramic-image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  
  if (ceramicDraggedIndex.value === null || ceramicDraggedIndex.value === index) {
    return
  }
  
  const images = [...ceramicForm.images]
  const draggedItem = images[ceramicDraggedIndex.value]
  images.splice(ceramicDraggedIndex.value, 1)
  images.splice(index, 0, draggedItem)
  
  ceramicForm.images = images
  
  ceramicDraggedIndex.value = null
}

const handleCeramicDragEnd = (event) => {
  event.target.style.opacity = '1'
  document.querySelectorAll('.ceramic-image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  ceramicDraggedIndex.value = null
}

const previewCeramicImage = (img) => {
  const url = getCeramicImageUrl(img)
  if (url) {
    previewCeramicImageUrl.value = url
    showCeramicImagePreview.value = true
  }
}

// 酒店商家管理方法
const handleHotelSearch = () => {
  hotelCurrentPage.value = 1
}

const handleHotelSelectionChange = (selection) => {
  selectedHotels.value = selection
}

const handleHotelSizeChange = (val) => {
  hotelPageSize.value = val
  hotelCurrentPage.value = 1
}

const handleHotelCurrentChange = (val) => {
  hotelCurrentPage.value = val
}

const editHotel = (hotel) => {
  editingHotelId.value = hotel.id
  
  // 处理图片：将字符串转换为数组（必须在 Object.assign 之前处理）
  let processedImages = []
  if (hotel.shopImages) {
    // 如果有shopImages字段（逗号分隔的字符串）
    const imageUrls = hotel.shopImages.split(',').map(url => url.trim()).filter(url => url)
    processedImages = imageUrls.map(url => ({
      url: url,
      name: url.split('/').pop() || 'image.jpg'
    }))
  } else if (hotel.avatar) {
    // 兼容单张图片的情况
    processedImages = [{
      url: hotel.avatar,
      name: hotel.avatar.split('/').pop() || 'image.jpg'
    }]
  }
  
  // 一次性赋值所有字段（包括图片）
  Object.assign(hotelForm, {
    shopName: hotel.shopName || '',
    description: hotel.description || '',
    address: hotel.address || '',
    phone: hotel.phone || '',
    openTime: hotel.openTime || '',
    adminRating: Number(hotel.rating || hotel.adminRating) || 0,
    tags: Array.isArray(hotel.tags) ? hotel.tags : (hotel.tags ? String(hotel.tags).split(',').map(s => s && s.trim()).filter(Boolean) : []),
    facilities: Array.isArray(hotel.facilities) ? hotel.facilities : [],
    enabled: hotel.enabled !== undefined ? hotel.enabled : true,
    images: processedImages  // 关键：在 Object.assign 中包含图片数组
  })
  
  showHotelDialog.value = true
}

const saveHotel = async () => {
  if (!editingHotelId.value) {
    ElMessage.error('缺少酒店ID')
    return
  }
  try {
    const imageUrls = Array.isArray(hotelForm.images)
      ? hotelForm.images
          .map(img => getHotelImageUrl(img))
          .filter(url => typeof url === 'string' && url.trim() !== '')
      : []
    const original = hotels.value.find(c => c.id === editingHotelId.value)
    const avatarForServer = imageUrls[0] || original?.avatar || null
    
    // 兼容本地展示：保留 shopImages 字符串版本
    const shopImagesStr = imageUrls.join(',')

    const res = await updateMerchantBasicInfo(editingHotelId.value, {
      shopName: hotelForm.shopName,
      description: hotelForm.description,
      address: hotelForm.address,
      phone: hotelForm.phone,
      openTime: hotelForm.openTime,
      adminRating: hotelForm.adminRating || 0,
      tags: Array.isArray(hotelForm.tags) ? hotelForm.tags : (hotelForm.tags ? String(hotelForm.tags).split(',').map(s => s && s.trim()).filter(Boolean) : []),
      facilities: Array.isArray(hotelForm.facilities) ? hotelForm.facilities : [],
      enabled: hotelForm.enabled,
      avatar: avatarForServer,
      // 后端 basic-info 更新只处理 images（数组/字符串），由后端落库到 shopImages
      images: imageUrls
    })
    if (res.success) {
      ElMessage.success('保存成功')
      showHotelDialog.value = false
      // 更新本地列表（确保图片字段也被正确更新）
      const idx = hotels.value.findIndex(c => c.id === editingHotelId.value)
      if (idx !== -1) {
        hotels.value[idx] = { 
          ...hotels.value[idx], 
          shopName: hotelForm.shopName,
          description: hotelForm.description,
          address: hotelForm.address,
          phone: hotelForm.phone,
          openTime: hotelForm.openTime,
          rating: hotelForm.adminRating,
          adminRating: hotelForm.adminRating,
          tags: hotelForm.tags,
          facilities: hotelForm.facilities,
          enabled: hotelForm.enabled,
          // 关键：确保图片字段被正确更新到本地数据
          avatar: avatarForServer,
          shopImages: shopImagesStr,
          images: imageUrls
        }
      }
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error('保存酒店失败:', e)
    ElMessage.error('保存失败')
  }
}

const deleteHotel = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除这个酒店商家吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await executeMerchantDeleteTask(id)
    hotels.value = hotels.value.filter(c => c.id !== id)
    updateSystemInfo()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除酒店商家失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteHotels = async () => {
  if (selectedHotels.value.length === 0) return

  try {
    await ElMessageBox.confirm(`确认要删除选中的 ${selectedHotels.value.length} 个酒店商家吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    for (const item of selectedHotels.value) {
      await executeMerchantDeleteTask(item.id)
    }
    
    const ids = selectedHotels.value.map(item => item.id)
    hotels.value = hotels.value.filter(c => !ids.includes(c.id))
    selectedHotels.value = []
    updateSystemInfo()
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 酒店图片上传相关方法
const beforeHotelImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  return true
}

const customHotelImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const url = response.url || response.data?.url
    if (response.success && url) {
      const newImage = {
        url: url,
        name: file.name || url.split('/').pop() || 'image.jpg',
        response: response
      }
      hotelForm.images.push(newImage)
      ElMessage.success('图片上传成功')
      if (onSuccess) {
        onSuccess(response, file)
      }
    } else {
      ElMessage.error(response.message || '图片上传失败')
      if (onError) {
        onError(new Error(response.message || '图片上传失败'))
      }
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
    if (onError) {
      onError(error)
    }
  }
}

// 获取图片URL（兼容不同格式）
const getHotelImageUrl = (img) => {
  if (typeof img === 'string') return img
  return img.url || img.response?.url || img.name || ''
}

// 删除图片（通过索引）
const removeHotelImageByIndex = (index) => {
  if (!Array.isArray(hotelForm.images) || hotelForm.images.length === 0) {
    return
  }
  
  hotelForm.images.splice(index, 1)
  ElMessage.success('图片已删除')
}

// 设置某张图片为封面（移动到第一位）
const setHotelAsCover = (index) => {
  if (!Array.isArray(hotelForm.images) || index === 0) {
    return
  }
  
  const images = [...hotelForm.images]
  const targetImage = images[index]
  images.splice(index, 1)
  images.unshift(targetImage)
  
  hotelForm.images = images
  ElMessage.success('已设置为封面图')
}

// 拖拽排序相关
const hotelDraggedIndex = ref(null)

const handleHotelDragStart = (index, event) => {
  hotelDraggedIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/html', event.target.outerHTML)
  event.target.style.opacity = '0.5'
}

const handleHotelDragOver = (index, event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
  
  const target = event.currentTarget
  if (hotelDraggedIndex.value !== null && hotelDraggedIndex.value !== index) {
    target.classList.add('drag-over')
  }
}

const handleHotelDrop = (index, event) => {
  event.preventDefault()
  
  document.querySelectorAll('.hotel-image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  
  if (hotelDraggedIndex.value === null || hotelDraggedIndex.value === index) {
    return
  }
  
  const images = [...hotelForm.images]
  const draggedItem = images[hotelDraggedIndex.value]
  images.splice(hotelDraggedIndex.value, 1)
  images.splice(index, 0, draggedItem)
  
  hotelForm.images = images
  
  hotelDraggedIndex.value = null
}

const handleHotelDragEnd = (event) => {
  event.target.style.opacity = '1'
  document.querySelectorAll('.hotel-image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  hotelDraggedIndex.value = null
}

const previewHotelImage = (img) => {
  const url = getHotelImageUrl(img)
  if (url) {
    previewHotelImageUrl.value = url
    showHotelImagePreview.value = true
  }
}

// 路线管理方法
const handleRouteSearch = () => {
  routeCurrentPage.value = 1
}

const handleRouteSelectionChange = (selection) => {
  selectedRoutes.value = selection
}

const handleRouteSizeChange = (val) => {
  routePageSize.value = val
  routeCurrentPage.value = 1
}

const handleRouteCurrentChange = (val) => {
  routeCurrentPage.value = val
}

const showAddRouteDialog = () => {
  editingRouteId.value = null
  // 重置表单
  Object.assign(routeForm, {
    name: '',
    description: '',
    days: 1,
    difficulty: '',
    totalPrice: 0,
    tags: '',
    image: '',
    scenicSpots: ''
  })
  showRouteDialog.value = true
}

// 编辑路线
const editRoute = async (route) => {
  editingRouteId.value = route.id
  
  // 先获取完整的路线详情
  try {
    const response = await getRouteDetail(route.id)
    const routeData = response.data || response.data?.data || route
    
    // 填充表单数据
    Object.assign(routeForm, {
      name: routeData.name || '',
      description: routeData.description || '',
      days: routeData.days || 1,
      difficulty: routeData.difficulty || '',
      totalPrice: routeData.totalPrice || 0,
      tags: routeData.tags || '',
      image: routeData.image || ''
    })
    
    // 处理景点列表：如果是JSON字符串，直接使用；如果是对象，转换为JSON字符串
    if (routeData.scenicSpots) {
      if (typeof routeData.scenicSpots === 'string') {
        routeForm.scenicSpots = routeData.scenicSpots
      } else {
        routeForm.scenicSpots = JSON.stringify(routeData.scenicSpots, null, 2)
      }
    } else {
      routeForm.scenicSpots = ''
    }
    
    showRouteDialog.value = true
  } catch (error) {
    console.error('加载路线详情失败:', error)
    ElMessage.error('加载路线详情失败')
  }
}

// 保存路线
const saveRoute = async () => {
  if (!routeFormRef.value) return

  try {
    await routeFormRef.value.validate()

    // 验证景点列表JSON格式
    let scenicSpotsStr = routeForm.scenicSpots
    if (scenicSpotsStr && scenicSpotsStr.trim()) {
      try {
        // 验证JSON格式
        JSON.parse(scenicSpotsStr)
      } catch (e) {
        ElMessage.error('景点列表JSON格式错误，请检查格式')
        return
      }
    }

    const routeData = {
      name: routeForm.name,
      description: routeForm.description || null,
      days: routeForm.days || 1,
      difficulty: routeForm.difficulty || null,
      totalPrice: routeForm.totalPrice || 0,
      tags: routeForm.tags || null,
      image: routeForm.image || null,
      scenicSpots: scenicSpotsStr || null
    }

    let response
    if (editingRouteId.value) {
      // 编辑模式
      response = await updateRoute(editingRouteId.value, routeData)
      if (response.success) {
        ElMessage.success('路线更新成功')
      }
    } else {
      // 新增模式
      response = await createRoute(routeData)
      if (response.success) {
        ElMessage.success('路线创建成功')
      }
    }

    if (response.success) {
      showRouteDialog.value = false
      editingRouteId.value = null
      // 重新加载路线列表
      await loadAllData()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('保存路线失败:', error)
      // 显示更详细的错误信息
      let errorMessage = '保存失败'
      if (error.response) {
        // 有响应，说明请求已发送到服务器
        const status = error.response.status
        const data = error.response.data
        if (data && data.message) {
          errorMessage = data.message
        } else if (status === 400) {
          errorMessage = '请求参数错误，请检查填写的信息'
        } else if (status === 500) {
          errorMessage = '服务器内部错误，请稍后重试'
        } else {
          errorMessage = `请求失败 (${status})`
        }
      } else if (error.request) {
        // 请求已发送但没有收到响应
        errorMessage = '网络错误，请检查网络连接或后端服务是否启动'
      } else {
        // 请求配置错误
        errorMessage = error.message || '请求配置错误'
      }
      ElMessage.error(errorMessage)
    }
  }
}

// 路线图片上传相关方法
const beforeRouteImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  return true
}

const customRouteImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const url = response.url || response.data?.url
    if (response.success && url) {
      routeForm.image = url
      ElMessage.success('图片上传成功')
      if (onSuccess) {
        onSuccess(response)
      }
    } else {
      ElMessage.error(response.message || '图片上传失败')
      if (onError) {
        onError(new Error(response.message || '图片上传失败'))
      }
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
    if (onError) {
      onError(error)
    }
  }
}

const removeRouteImage = () => {
  routeForm.image = ''
  ElMessage.info('已删除图片')
}

const deleteRoute = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除这条路线吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteRouteAPI(id)
    if (response.success) {
      routes.value = routes.value.filter(r => r.id !== id)
      updateSystemInfo()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除路线失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteRoutes = async () => {
  if (selectedRoutes.value.length === 0) return

  try {
    await ElMessageBox.confirm(`确认要删除选中的 ${selectedRoutes.value.length} 条路线吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const successIds = []
    const failedCount = { value: 0 }
    await Promise.all(
      selectedRoutes.value.map(async (item) => {
        try {
          await deleteRouteAPI(item.id)
          successIds.push(item.id)
        } catch (e) {
          failedCount.value++
        }
      })
    )

    routes.value = routes.value.filter(r => !successIds.includes(r.id))
    selectedRoutes.value = []
    updateSystemInfo()

    if (failedCount.value === 0) {
      ElMessage.success('批量删除成功')
    } else {
      ElMessage.warning(`成功删除 ${successIds.length} 条，${failedCount.value} 条删除失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 市集管理方法
const handleMarketplaceSearch = () => {
  marketplaceCurrentPage.value = 1
}

const handleMarketplaceSelectionChange = (selection) => {
  selectedMarketplaces.value = selection
}

const handleMarketplaceSizeChange = (val) => {
  marketplacePageSize.value = val
  marketplaceCurrentPage.value = 1
}

const handleMarketplaceCurrentChange = (val) => {
  marketplaceCurrentPage.value = val
}

const showAddMarketplaceDialog = () => {
  editingMarketplaceId.value = null
  Object.assign(marketplaceForm, {
    name: '',
    openTime: '',
    address: '',
    description: '',
    enabled: true,
    sortOrder: 0,
    images: []
  })
  showMarketplaceDialog.value = true
}

const editMarketplace = (item) => {
  if (!item || !item.id) {
    ElMessage.error('市集信息无效，无法编辑')
    return
  }
  editingMarketplaceId.value = item.id

  // 把后端逗号分隔的 carouselImages 转为 images 数组
  const imgs = (item.carouselImages || '')
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
    .map((url) => ({ url, name: url.split('/').pop() || 'image.jpg' }))

  Object.assign(marketplaceForm, {
    name: item.name || '',
    openTime: item.openTime || '',
    address: item.address || '',
    description: item.description || '',
    enabled: item.enabled !== false,
    sortOrder: item.sortOrder ?? 0,
    images: imgs
  })

  showMarketplaceDialog.value = true
}

const beforeMarketplaceImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const customMarketplaceImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    const response = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    const url = response.url || response.data?.url
    if (response.success && url) {
      marketplaceForm.images.push({
        url,
        name: file.name || url.split('/').pop() || 'image.jpg',
        response
      })
      ElMessage.success('图片上传成功')
      onSuccess && onSuccess(response, file)
    } else {
      ElMessage.error(response.message || '图片上传失败')
      onError && onError(new Error(response.message || '图片上传失败'))
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
    onError && onError(error)
  }
}

const previewMarketplaceImage = (url) => {
  previewMarketplaceImageUrl.value = url
  showMarketplaceImagePreview.value = true
}

const removeMarketplaceImageByIndex = (index) => {
  if (index < 0 || index >= marketplaceForm.images.length) return
  marketplaceForm.images.splice(index, 1)
}

const saveMarketplace = async () => {
  try {
    if (!marketplaceForm.name?.trim()) {
      ElMessage.error('请输入市集名称')
      return
    }

    const carouselImages = (marketplaceForm.images || [])
      .map(img => img?.url)
      .filter(Boolean)
      .join(',')

    const payload = {
      name: marketplaceForm.name.trim(),
      openTime: marketplaceForm.openTime,
      address: marketplaceForm.address,
      description: marketplaceForm.description,
      enabled: marketplaceForm.enabled,
      sortOrder: marketplaceForm.sortOrder,
      carouselImages,
      coverImage: marketplaceForm.images?.[0]?.url || ''
    }

    if (editingMarketplaceId.value) {
      await updateMarketplace(editingMarketplaceId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createMarketplace(payload)
      ElMessage.success('创建成功')
    }

    showMarketplaceDialog.value = false
    editingMarketplaceId.value = null
    await loadMarketplaces()
  } catch (error) {
    console.error('保存市集失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  }
}

const deleteMarketplaceRow = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除该市集吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMarketplace(id)
    marketplaces.value = marketplaces.value.filter(m => m.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除市集失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const batchDeleteMarketplaces = async () => {
  if (selectedMarketplaces.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认要删除选中的 ${selectedMarketplaces.value.length} 个市集吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await Promise.all(selectedMarketplaces.value.map(item => deleteMarketplace(item.id)))
    const ids = selectedMarketplaces.value.map(item => item.id)
    marketplaces.value = marketplaces.value.filter(m => !ids.includes(m.id))
    selectedMarketplaces.value = []
    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除市集失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 系统设置方法
const saveSettings = async () => {
  try {
    const response = await saveSystemSettings({
      systemName: systemSettings.systemName,
      announcement: systemSettings.announcement,
      recommendAlgorithm: systemSettings.recommendAlgorithm
    })
    if (response.success) {
      ElMessage.success('设置保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存系统设置失败:', error)
    ElMessage.error(error.message || '保存失败')
  }
}

const resetSettings = async () => {
  try {
    // 从服务器重新加载设置
    await loadSystemSettings()
    ElMessage.info('设置已重置为服务器保存的值')
  } catch (error) {
    // 如果加载失败，使用默认值
    systemSettings.systemName = '景德镇旅游智能推荐系统'
    systemSettings.announcement = ''
    systemSettings.recommendAlgorithm = 'hybrid'
    ElMessage.info('设置已重置为默认值')
  }
}

// 加载系统设置
const loadSystemSettings = async () => {
  try {
    const response = await getSystemSettings()
    if (response.success && response.data) {
      systemSettings.systemName = response.data.systemName || '景德镇旅游智能推荐系统'
      systemSettings.announcement = response.data.announcement || ''
      systemSettings.recommendAlgorithm = response.data.recommendAlgorithm || 'hybrid'
    }
  } catch (error) {
    console.error('加载系统设置失败:', error)
    // 使用默认值
    systemSettings.systemName = '景德镇旅游智能推荐系统'
    systemSettings.announcement = ''
    systemSettings.recommendAlgorithm = 'hybrid'
  }
}

const backupData = async () => {
  try {
    ElMessage.info('正在备份数据，请稍候...')
    const response = await backupDataAPI()
    
    if (response.success && response.data) {
      // 将备份数据转换为JSON字符串
      const jsonData = JSON.stringify(response.data, null, 2)
      
      // 创建Blob对象
      const blob = new Blob([jsonData], { type: 'application/json' })
      
      // 创建下载链接
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // 生成文件名（包含时间戳）
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5)
      link.download = `tourism-backup-${timestamp}.json`
      
      // 触发下载
      document.body.appendChild(link)
      link.click()
      
      // 清理
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      ElMessage.success('数据备份成功，文件已下载')
    } else {
      ElMessage.error(response.message || '数据备份失败')
    }
  } catch (error) {
    console.error('数据备份失败:', error)
    ElMessage.error('数据备份失败: ' + (error.message || '未知错误'))
  }
}

const restoreData = async () => {
  try {
    await ElMessageBox.confirm(
      '警告：数据恢复将覆盖现有数据，此操作不可逆！请确保已备份当前数据。',
      '确认恢复数据',
      {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }
    )
    
    // 创建文件输入元素
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = '.json'
    input.style.display = 'none'
    
    input.onchange = async (event) => {
      const file = event.target.files[0]
      if (!file) {
        document.body.removeChild(input)
        return
      }
      
      try {
        ElMessage.info('正在恢复数据，请稍候...')
        
        // 读取文件内容
        const reader = new FileReader()
        reader.onload = async (e) => {
          try {
            const backupData = JSON.parse(e.target.result)
            
            // 调用恢复API
            const response = await restoreDataAPI(backupData)
            
            if (response.success) {
              ElMessage.success('数据恢复成功')
              // 刷新页面数据
              await loadAllData()
              await loadSystemSettings()
            } else {
              ElMessage.error(response.message || '数据恢复失败')
            }
          } catch (parseError) {
            console.error('解析备份文件失败:', parseError)
            ElMessage.error('备份文件格式错误，请选择有效的JSON文件')
          } finally {
            document.body.removeChild(input)
          }
        }
        
        reader.onerror = () => {
          ElMessage.error('读取文件失败')
          document.body.removeChild(input)
        }
        
        reader.readAsText(file)
      } catch (error) {
        console.error('数据恢复失败:', error)
        ElMessage.error('数据恢复失败: ' + (error.message || '未知错误'))
        document.body.removeChild(input)
      }
    }
    
    // 触发文件选择
    document.body.appendChild(input)
    input.click()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('数据恢复操作失败:', error)
    }
  }
}

// 工具方法
const getCategoryTagType = (category) => {
  const types = {
    '博物馆': 'primary',
    '陶瓷工坊': 'success',
    '景区': 'warning',
    '文化街区': 'info',
    '古镇': 'danger'
  }
  return types[category] || 'info'
}

const getRoleTagType = (role) => {
  const types = {
    'ADMIN': 'danger',
    'MERCHANT': 'warning',
    'TOURIST': 'success'
  }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = {
    'ADMIN': '管理员',
    'MERCHANT': '商家',
    'TOURIST': '游客'
  }
  return texts[role] || role
}

const getMerchantCategoryText = (category) => {
  const texts = {
    'FOOD': '餐饮商家',
    'CERAMIC': '陶瓷工坊',
    'HOTEL': '酒店',
    'SCENIC': '景点',
    'OTHER': '其他商家'
  }
  return texts[category] || category || '商家'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadAllData()
  loadSystemSettings()
})
</script>

<style scoped>
.system-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  height: 100px;
}

.overview-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.overview-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 16px;
}

.overview-icon.system {
  background: #409eff;
}

.overview-icon.users {
  background: #67c23a;
}

.overview-icon.merchants {
  background: #e6a23c;
}

.overview-icon.routes {
  background: #f56c6c;
}

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.overview-label {
  color: #909399;
  font-size: 14px;
}

.manage-card {
  margin-top: 20px;
}

.tab-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 12px;
}

.pagination-info {
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-info .info-text {
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  flex: 1;
  min-width: 0;
}

/* 优化分页器样式 */
:deep(.el-pagination) {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.el-pagination .el-pagination__total) {
  margin-right: 16px;
  color: #606266;
  font-weight: 500;
}

:deep(.el-pagination .el-pagination__sizes) {
  margin-right: 16px;
}

:deep(.el-pagination .el-select) {
  width: 110px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  padding: 0 8px;
  min-width: 32px;
  height: 32px;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover) {
  background-color: #f5f7fa;
  color: #409eff;
}

:deep(.el-pagination .el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 4px;
  transition: all 0.3s;
  margin: 0 2px;
}

:deep(.el-pagination .el-pager li:hover) {
  background-color: #f5f7fa;
  color: #409eff;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: #409eff;
  color: white;
  font-weight: 600;
}

:deep(.el-pagination .el-pagination__jump) {
  margin-left: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-pagination .el-pagination__jump .el-input) {
  width: 50px;
}

:deep(.el-pagination .el-pagination__jump .el-input__inner) {
  text-align: center;
  padding: 0 8px;
  height: 32px;
  line-height: 32px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .pagination {
    flex-direction: column;
    align-items: stretch;
    padding: 12px 0;
  }

  .pagination-info {
    justify-content: center;
    margin-bottom: 8px;
    font-size: 13px;
  }

  .pagination-wrapper {
    width: 100%;
    overflow-x: auto;
    padding: 8px 0;
  }

  :deep(.el-pagination) {
    justify-content: flex-start;
    min-width: max-content;
  }

  :deep(.el-pagination .el-pagination__total) {
    display: none;
  }

  :deep(.el-pagination .el-pagination__sizes) {
    display: none;
  }

  :deep(.el-pagination .el-pagination__jump) {
    display: none;
  }
}

@media (max-width: 480px) {
  .pagination-info {
    font-size: 12px;
  }

  :deep(.el-pagination .el-pager li) {
    min-width: 28px;
    height: 28px;
    line-height: 28px;
    font-size: 13px;
    margin: 0 1px;
  }

  :deep(.el-pagination .btn-prev),
  :deep(.el-pagination .btn-next) {
    min-width: 28px;
    height: 28px;
    padding: 0 6px;
  }
}

.free-text {
  color: #67c23a;
  font-weight: 600;
}

.scenic-images-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.scenic-image-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.scenic-image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.scenic-image-item {
  position: relative;
  width: 120px;
  cursor: move;
  transition: all 0.3s;
}

.scenic-image-item:hover {
  transform: translateY(-2px);
}

.scenic-image-item.is-cover {
  border: 2px solid #67c23a;
  border-radius: 6px;
}

.scenic-image-item.drag-over {
  border: 2px dashed #409eff;
  background-color: #ecf5ff;
}

.scenic-image-thumbnail {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.scenic-image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.scenic-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.scenic-image-item:hover .scenic-image-overlay {
  opacity: 1;
}

.scenic-image-badges {
  display: flex;
  justify-content: flex-start;
}

.scenic-image-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.scenic-image-index {
  text-align: center;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.scenic-image-upload-btn {
  width: 120px;
  height: 120px;
}

.scenic-image-upload-btn :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.scenic-image-upload-btn :deep(.el-upload):hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.scenic-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
}

.scenic-upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.scenic-upload-text {
  font-size: 14px;
  color: #606266;
}

.scenic-image-info {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.scenic-image-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.scenic-image-usage-list {
  margin: 0;
  padding-left: 20px;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}

.scenic-image-usage-list li {
  margin-bottom: 4px;
}

/* 路线图片上传样式 */
.route-image-uploader {
  width: 100%;
}

.route-image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.route-image-uploader :deep(.el-upload):hover {
  border-color: #409eff;
}

.route-uploaded-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
  border-radius: 6px;
}

.route-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
}

.route-upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.route-upload-text {
  font-size: 14px;
  color: #606266;
}

.route-image-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.route-scenic-spots {
  width: 100%;
}

.scenic-spots-hint {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  line-height: 1.6;
}

/* 陶瓷工坊图片上传样式（复用景点图片样式） */
.ceramic-images-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ceramic-image-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.ceramic-image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.ceramic-image-item {
  position: relative;
  width: 120px;
  cursor: move;
  transition: all 0.3s;
}

.ceramic-image-item:hover {
  transform: translateY(-2px);
}

.ceramic-image-item.is-cover {
  border: 2px solid #67c23a;
  border-radius: 6px;
}

.ceramic-image-item.drag-over {
  border: 2px dashed #409eff;
  background-color: #ecf5ff;
}

.ceramic-image-thumbnail {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.ceramic-image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.ceramic-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.ceramic-image-item:hover .ceramic-image-overlay {
  opacity: 1;
}

.ceramic-image-badges {
  display: flex;
  justify-content: flex-start;
}

.ceramic-image-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.ceramic-image-index {
  text-align: center;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.ceramic-image-upload-btn {
  width: 120px;
  height: 120px;
}

.ceramic-image-upload-btn :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.ceramic-image-upload-btn :deep(.el-upload):hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.ceramic-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
}

.ceramic-upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.ceramic-upload-text {
  font-size: 14px;
  color: #606266;
}

.ceramic-image-info {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.ceramic-image-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.ceramic-image-usage-list {
  margin: 0;
  padding-left: 20px;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}

.ceramic-image-usage-list li {
  margin-bottom: 4px;
}

/* 酒店商家图片上传样式（复用景点图片样式） */
.hotel-images-upload {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hotel-image-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.hotel-image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hotel-image-item {
  position: relative;
  width: 120px;
  cursor: move;
  transition: all 0.3s;
}

.hotel-image-item:hover {
  transform: translateY(-2px);
}

.hotel-image-item.is-cover {
  border: 2px solid #67c23a;
  border-radius: 6px;
}

.hotel-image-item.drag-over {
  border: 2px dashed #409eff;
  background-color: #ecf5ff;
}

.hotel-image-thumbnail {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.hotel-image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hotel-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.hotel-image-item:hover .hotel-image-overlay {
  opacity: 1;
}

.hotel-image-badges {
  display: flex;
  justify-content: flex-start;
}

.hotel-image-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.hotel-image-index {
  text-align: center;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.hotel-image-upload-btn {
  width: 120px;
  height: 120px;
}

.hotel-image-upload-btn :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.hotel-image-upload-btn :deep(.el-upload):hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.hotel-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
}

.hotel-upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.hotel-upload-text {
  font-size: 14px;
  color: #606266;
}

.hotel-image-info {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.hotel-image-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 市集图片上传样式（复用酒店/景点的交互风格） */
.marketplace-image-section {
  width: 100%;
}

.marketplace-image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.marketplace-image-item {
  width: 120px;
}

.marketplace-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  cursor: pointer;
}

.marketplace-image-actions {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

.marketplace-image-index {
  text-align: center;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.marketplace-image-upload-btn {
  width: 120px;
  height: 120px;
}

.marketplace-image-upload-btn :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.marketplace-image-upload-btn :deep(.el-upload):hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.marketplace-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
}

.marketplace-upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.marketplace-upload-text {
  font-size: 14px;
  color: #606266;
}

.marketplace-image-info {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.marketplace-image-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 13px;
  color: #606266;
}

.hotel-image-usage-list {
  margin: 0;
  padding-left: 20px;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}

.hotel-image-usage-list li {
  margin-bottom: 4px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .overview-row .el-col {
    margin-bottom: 16px;
  }
  
  .tab-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>
