-- 补充酒店商家 facilities 和 tags 数据
-- facilities 值需与前端 el-option value 一致：免费WiFi,停车场,游泳池,健身房,餐厅,接送服务,行李寄存,24小时前台

UPDATE `merchant` SET
  `facilities` = '免费WiFi,停车场,24小时前台,行李寄存',
  `tags`       = '位置优越,交通便利,干净卫生'
WHERE `id` = 450;  -- 格林豪泰智选酒店(景德镇人民广场店)

UPDATE `merchant` SET
  `facilities` = '免费WiFi,停车场,24小时前台,行李寄存',
  `tags`       = '位置优越,干净卫生,服务周到'
WHERE `id` = 451;  -- 格林豪泰智选酒店(景德镇中国陶瓷博物馆店)

UPDATE `merchant` SET
  `facilities` = '免费WiFi,停车场,24小时前台',
  `tags`       = '位置优越,干净卫生'
WHERE `id` = 452;  -- 格林豪泰酒店(景德镇曙光路古玩市场店)

UPDATE `merchant` SET
  `facilities` = '免费WiFi,24小时前台,行李寄存',
  `tags`       = '位置优越,交通便利,干净卫生'
WHERE `id` = 453;  -- 城市便捷酒店(景德镇陶阳里御窑博物馆店)

UPDATE `merchant` SET
  `facilities` = '免费WiFi,停车场,24小时前台,餐厅',
  `tags`       = '位置优越,干净卫生,服务周到'
WHERE `id` = 454;  -- 宜必思尚品酒店(景德镇瓷都大道店)

UPDATE `merchant` SET
  `facilities` = '免费WiFi,24小时前台,行李寄存',
  `tags`       = '位置优越,干净卫生'
WHERE `id` = 455;  -- 7天酒店(景德镇中国陶瓷城店)

UPDATE `merchant` SET
  `facilities` = '免费WiFi,24小时前台',
  `tags`       = '干净卫生,位置优越'
WHERE `id` = 456;  -- 乐平阳光快捷酒店

UPDATE `merchant` SET
  `facilities` = '免费WiFi,停车场,24小时前台',
  `tags`       = '干净卫生,亲子友好'
WHERE `id` = 457;  -- 景德镇国喜酒店
