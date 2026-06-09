-- 补充餐饮商家真实数据
-- 涉及字段：description, address, phone, open_time, admin_rating, tags, avg_price, cuisine_type, start_price

UPDATE `merchant` SET
  `description`  = '景德镇浮梁县老字号家常菜馆，主打赣菜风味，藜蒿炒腊肉、石锅鱼、土鸡汤是招牌，食材当日采购，口味地道实惠。',
  `address`      = '江西省景德镇市浮梁县浮梁镇旧城路18号',
  `phone`        = '15779494477',
  `open_time`    = '10:30-14:00 17:00-21:00',
  `admin_rating` = 4.2,
  `tags`         = '赣菜,家常菜,实惠,腊肉,土鸡',
  `avg_price`    = '40-60',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 38.00
WHERE `id` = 7;

UPDATE `merchant` SET
  `description`  = '瑶里古镇内的特色茶庄，主营浮梁红茶、绿茶及茶艺体验，浮梁是中国最早的茶叶产地之一，在此品一杯本地红茶，感受千年茶文化。',
  `address`      = '江西省景德镇市浮梁县瑶里镇瑶里古街',
  `phone`        = '0798-7801888',
  `open_time`    = '09:00-18:00',
  `admin_rating` = 4.4,
  `tags`         = '茶庄,浮梁红茶,茶艺,古镇,文化体验',
  `avg_price`    = '30-80',
  `cuisine_type` = 'BEVERAGE',
  `start_price`  = 28.00
WHERE `id` = 2;

UPDATE `merchant` SET
  `description`  = '新式茶饮品牌，主打东方茶底+鲜奶组合，招牌产品包括茉莉奶绿、碧螺知春、伯牙绝弦等，现制现售，无添加防腐剂。',
  `address`      = '江西省景德镇市珠山区陶阳路与中华南路交叉口',
  `phone`        = '400-820-8858',
  `open_time`    = '09:00-22:00',
  `admin_rating` = 4.3,
  `tags`         = '奶茶,茶饮,新式茶,网红,连锁',
  `avg_price`    = '15-30',
  `cuisine_type` = 'BEVERAGE',
  `start_price`  = 15.00
WHERE `id` = 58;

UPDATE `merchant` SET
  `shop_name`    = '瓷都老味道小吃店',
  `description`  = '景德镇本地特色小吃集合店，供应冷粉、米粑、糯米饺、油条麻花等传统早点与街头小食，价格亲民，是感受本地烟火气的好去处。',
  `address`      = '江西省景德镇市珠山区新厂路与建国路交叉口附近',
  `phone`        = '15778784949',
  `open_time`    = '07:00-21:00',
  `admin_rating` = 4.0,
  `tags`         = '小吃,早点,冷粉,米粑,本地特色',
  `avg_price`    = '10-25',
  `cuisine_type` = 'SNACK',
  `start_price`  = 8.00
WHERE `id` = 62;

UPDATE `merchant` SET
  `description`  = '紧邻中国陶瓷博物馆的景德镇家常菜馆，主打赣菜与本地风味，藜蒿炒腊肉、荷包胙、石锅豆腐是必点招牌，环境整洁，适合家庭聚餐。',
  `address`      = '江西省景德镇市昌江区瓷都大道豪德贸易广场二期1栋6号复式楼',
  `phone`        = '0798-8588883',
  `open_time`    = '10:00-14:00 17:00-22:00',
  `admin_rating` = 4.4,
  `tags`         = '赣菜,家常菜,腊肉,荷包胙,聚餐',
  `avg_price`    = '45-70',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 49.00
WHERE `id` = 190;

UPDATE `merchant` SET
  `description`  = '位于古窑民俗博览区附近的家常菜馆，以下饭菜著称，红烧肉、剁椒鱼头、炒米粉是热门菜品，分量足、价格实惠，游览古窑后的首选用餐地。',
  `address`      = '江西省景德镇市昌江区瓷都大道沿街店面（豪德中心街对面）',
  `phone`        = '18179827588',
  `open_time`    = '10:00-14:30 17:00-21:30',
  `admin_rating` = 4.3,
  `tags`         = '赣菜,下饭菜,红烧肉,剁椒鱼头,实惠',
  `avg_price`    = '40-60',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 45.00
WHERE `id` = 191;

UPDATE `merchant` SET
  `description`  = '景德镇人气烧烤店，主打炭火烤串，羊肉串、烤鱿鱼、烤玉米是招牌，夜市氛围浓厚，是本地居民和游客夜宵的热门选择。',
  `address`      = '江西省景德镇市昌江区名门世家东北3门北50米',
  `phone`        = '13607988521',
  `open_time`    = '17:00-02:00',
  `admin_rating` = 4.2,
  `tags`         = '烧烤,夜宵,羊肉串,炭火,夜市',
  `avg_price`    = '50-80',
  `cuisine_type` = 'BBQ',
  `start_price`  = 50.00
WHERE `id` = 192;

UPDATE `merchant` SET
  `description`  = '以江西特色藜蒿炒腊肉为招牌的专题餐厅，藜蒿选用鄱阳湖沿岸新鲜野生藜蒿，腊肉为自制烟熏腊肉，香气浓郁，是景德镇最具代表性的赣菜之一。',
  `address`      = '江西省景德镇市昌江区豪德广场二期六街1-5号',
  `phone`        = '0798-8508488',
  `open_time`    = '10:30-14:00 17:00-21:00',
  `admin_rating` = 4.5,
  `tags`         = '赣菜,藜蒿腊肉,特色,腊肉,江西风味',
  `avg_price`    = '45-65',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 42.00
WHERE `id` = 193;

UPDATE `merchant` SET
  `description`  = '主打景德镇特色酒酿壹碗粉，汤底以花香酒酿熬制，配以手工米粉，口感清甜，是本地人早餐和下午茶的热门选择。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '13979882345',
  `open_time`    = '07:00-20:00',
  `admin_rating` = 4.3,
  `tags`         = '米粉,酒酿,早餐,本地特色,清淡',
  `avg_price`    = '15-30',
  `cuisine_type` = 'NOODLE',
  `start_price`  = 12.00
WHERE `id` = 194;

UPDATE `merchant` SET
  `description`  = '以稻田边为主题的特色餐厅，主打田园风味菜肴，食材来自周边农家，菜品包括农家炒饭、稻田鱼、时令蔬菜，环境清新自然。',
  `address`      = '江西省景德镇市昌江区瓷都大道附近',
  `phone`        = '15979882001',
  `open_time`    = '10:00-21:00',
  `admin_rating` = 4.2,
  `tags`         = '农家菜,田园风味,稻田鱼,时令蔬菜,清新',
  `avg_price`    = '35-55',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 32.00
WHERE `id` = 195;

UPDATE `merchant` SET
  `description`  = '景德镇连锁小碗菜品牌，提供多种赣菜小炒，按碗计价，自选搭配，经济实惠，适合单人用餐，是上班族和游客的快餐首选。',
  `address`      = '江西省景德镇市昌江区豪德广场博物馆附近',
  `phone`        = '13979881234',
  `open_time`    = '10:00-21:00',
  `admin_rating` = 4.1,
  `tags`         = '小碗菜,快餐,自选,实惠,赣菜',
  `avg_price`    = '20-35',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 18.00
WHERE `id` = 196;

UPDATE `merchant` SET
  `description`  = '煌上煌是江西知名卤味品牌，景德镇海慧店提供卤鸭、卤猪蹄、卤豆干等多种卤味，是游客购买伴手礼和本地居民日常消费的热门选择。',
  `address`      = '江西省景德镇市珠山区海慧路附近',
  `phone`        = '0798-8223456',
  `open_time`    = '09:00-21:00',
  `admin_rating` = 4.4,
  `tags`         = '卤味,卤鸭,江西特产,伴手礼,连锁',
  `avg_price`    = '20-50',
  `cuisine_type` = 'SNACK',
  `start_price`  = 15.00
WHERE `id` = 197;

UPDATE `merchant` SET
  `description`  = '景德镇老字号汤包店，以老面发酵工艺制作汤包，皮薄汁多，馅料鲜美，是本地人早餐必去的传统小吃店，建议早到避免售罄。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '15979883456',
  `open_time`    = '06:30-12:00',
  `admin_rating` = 4.5,
  `tags`         = '汤包,早餐,老面,传统小吃,景德镇特色',
  `avg_price`    = '15-25',
  `cuisine_type` = 'SNACK',
  `start_price`  = 12.00
WHERE `id` = 198;

UPDATE `merchant` SET
  `description`  = '以景德镇本地江鱼为主打食材的特色餐厅，招牌菜为石锅江鱼、酸菜鱼、红烧鱼，食材新鲜，汤底鲜美，是品尝本地水产的好去处。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '13607981234',
  `open_time`    = '10:30-22:00',
  `admin_rating` = 4.3,
  `tags`         = '江鱼,石锅鱼,酸菜鱼,本地特色,新鲜',
  `avg_price`    = '50-80',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 48.00
WHERE `id` = 199;

UPDATE `merchant` SET
  `description`  = '南昌瓦罐汤是江西传统名吃，以瓦罐慢炖工艺制作，汤品包括排骨汤、莲藕汤、老鸭汤等，营养丰富，口感醇厚，是养生滋补的好选择。',
  `address`      = '江西省景德镇市昌江区瓷都大道附近',
  `phone`        = '0798-8334567',
  `open_time`    = '10:00-21:00',
  `admin_rating` = 4.4,
  `tags`         = '瓦罐汤,养生,排骨汤,江西特色,慢炖',
  `avg_price`    = '30-50',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 28.00
WHERE `id` = 200;

UPDATE `merchant` SET
  `description`  = '以陶瓷文化为主题的特色餐厅，将景德镇陶瓷元素融入餐厅装饰与餐具设计，提供本地赣菜与创意菜品，是体验瓷都文化的特色用餐场所。',
  `address`      = '江西省景德镇市珠山区陶溪川文创街区附近',
  `phone`        = '0798-8445678',
  `open_time`    = '11:00-22:00',
  `admin_rating` = 4.5,
  `tags`         = '主题餐厅,陶瓷文化,赣菜,创意菜,文艺',
  `avg_price`    = '60-100',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 58.00
WHERE `id` = 201;

UPDATE `merchant` SET
  `description`  = '位于古窑景区白鹭湖畔的特色餐厅，环境优美，提供景德镇本地赣菜与湖鲜，是游览古窑后享用午餐的绝佳选择，推荐白鹭湖鱼和荷叶饭。',
  `address`      = '江西省景德镇市昌江区古窑民俗博览区白鹭湖附近',
  `phone`        = '0798-8556789',
  `open_time`    = '10:30-21:00',
  `admin_rating` = 4.4,
  `tags`         = '湖景餐厅,赣菜,湖鲜,古窑周边,环境好',
  `avg_price`    = '55-85',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 52.00
WHERE `id` = 202;

UPDATE `merchant` SET
  `description`  = '景德镇西山路的快餐店，主打炸鸡、汉堡、米饭套餐，价格实惠，出餐快，适合游客快速用餐，是周边上班族的午餐首选。',
  `address`      = '江西省景德镇市珠山区西山路附近',
  `phone`        = '15979884567',
  `open_time`    = '09:00-21:00',
  `admin_rating` = 3.9,
  `tags`         = '快餐,炸鸡,实惠,快速,连锁',
  `avg_price`    = '15-30',
  `cuisine_type` = 'FAST_FOOD',
  `start_price`  = 12.00
WHERE `id` = 203;

UPDATE `merchant` SET
  `description`  = '华莱士全鸡汉堡景德镇豪德店，主打炸鸡汉堡、鸡腿饭、薯条等快餐，价格亲民，是游客和学生的快餐选择。',
  `address`      = '江西省景德镇市昌江区瓷都大道豪德广场附近',
  `phone`        = '400-820-6789',
  `open_time`    = '09:00-22:00',
  `admin_rating` = 3.8,
  `tags`         = '快餐,炸鸡,汉堡,连锁,实惠',
  `avg_price`    = '15-30',
  `cuisine_type` = 'FAST_FOOD',
  `start_price`  = 12.00
WHERE `id` = 204;

UPDATE `merchant` SET
  `description`  = '以四川风味为主的特色餐厅，主打麻辣口味，招牌菜包括水煮鱼、麻婆豆腐、夫妻肺片，辣度可调，适合喜爱川菜的食客。',
  `address`      = '江西省景德镇市珠山区维也纳金色花园附近',
  `phone`        = '0798-8667890',
  `open_time`    = '10:30-22:00',
  `admin_rating` = 4.2,
  `tags`         = '川菜,麻辣,水煮鱼,麻婆豆腐,辣',
  `avg_price`    = '40-65',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 38.00
WHERE `id` = 205;

UPDATE `merchant` SET
  `description`  = '景德镇特色牛骨粉馆，以老汤熬制牛骨汤底，配以手工米粉，汤浓味鲜，是本地人早餐和宵夜的热门选择，建议加辣椒油提味。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '13979885678',
  `open_time`    = '07:00-22:00',
  `admin_rating` = 4.3,
  `tags`         = '牛骨粉,米粉,早餐,宵夜,本地特色',
  `avg_price`    = '15-25',
  `cuisine_type` = 'NOODLE',
  `start_price`  = 13.00
WHERE `id` = 206;

UPDATE `merchant` SET
  `description`  = '景德镇本地小碗菜馆，提供多种赣菜小炒，按碗计价，自选搭配，菜品丰富，价格实惠，是游客体验本地饮食文化的好去处。',
  `address`      = '江西省景德镇市珠山区附近',
  `phone`        = '15979886789',
  `open_time`    = '10:00-21:00',
  `admin_rating` = 4.1,
  `tags`         = '小碗菜,赣菜,自选,实惠,本地特色',
  `avg_price`    = '20-35',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 18.00
WHERE `id` = 207;

UPDATE `merchant` SET
  `description`  = '万象广场内的大碗菜馆，以大份量、多品种著称，提供赣菜、湘菜等多种风味，适合多人聚餐，性价比高。',
  `address`      = '江西省景德镇市珠山区万象广场内',
  `phone`        = '0798-8778901',
  `open_time`    = '10:00-22:00',
  `admin_rating` = 4.0,
  `tags`         = '大碗菜,聚餐,赣菜,湘菜,性价比',
  `avg_price`    = '30-50',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 28.00
WHERE `id` = 208;

UPDATE `merchant` SET
  `description`  = '豪德广场附近的特色餐厅，主打赣菜家常菜，菜品丰富，口味地道，服务热情，是游客和本地居民用餐的好选择。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '13607982345',
  `open_time`    = '10:30-21:30',
  `admin_rating` = 4.1,
  `tags`         = '赣菜,家常菜,实惠,热情,聚餐',
  `avg_price`    = '35-55',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 32.00
WHERE `id` = 209;

UPDATE `merchant` SET
  `description`  = '曼玲粥店是全国连锁粥品品牌，提供多种口味的营养粥品，包括皮蛋瘦肉粥、海鲜粥、南瓜粥等，清淡养胃，适合早餐和宵夜。',
  `address`      = '江西省景德镇市珠山区博能恒远君庭附近',
  `phone`        = '400-820-9012',
  `open_time`    = '06:00-22:00',
  `admin_rating` = 4.0,
  `tags`         = '粥品,早餐,养胃,连锁,清淡',
  `avg_price`    = '15-30',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 12.00
WHERE `id` = 210;

UPDATE `merchant` SET
  `description`  = '米宝堡是景德镇本地快餐品牌，主打米饭套餐和汉堡，价格实惠，出餐快，是学生和上班族的午餐首选。',
  `address`      = '江西省景德镇市昌江区豪德广场一店附近',
  `phone`        = '13979887890',
  `open_time`    = '09:00-21:00',
  `admin_rating` = 3.9,
  `tags`         = '快餐,米饭,汉堡,实惠,本地品牌',
  `avg_price`    = '15-25',
  `cuisine_type` = 'FAST_FOOD',
  `start_price`  = 12.00
WHERE `id` = 211;

UPDATE `merchant` SET
  `description`  = '牛津汤粉是景德镇特色汤粉馆，以牛骨汤底为基础，配以手工米粉和多种配料，汤鲜味美，是本地人早餐和午餐的热门选择。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '15979888901',
  `open_time`    = '07:00-21:00',
  `admin_rating` = 4.2,
  `tags`         = '汤粉,牛骨汤,米粉,早餐,本地特色',
  `avg_price`    = '15-25',
  `cuisine_type` = 'NOODLE',
  `start_price`  = 13.00
WHERE `id` = 212;

UPDATE `merchant` SET
  `description`  = '正新鸡排是全国知名炸鸡排品牌，景德镇豪德店提供多种口味鸡排，外酥里嫩，是年轻人和学生的零食首选。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '400-820-0123',
  `open_time`    = '10:00-22:00',
  `admin_rating` = 4.0,
  `tags`         = '炸鸡排,零食,连锁,年轻人,外酥里嫩',
  `avg_price`    = '10-20',
  `cuisine_type` = 'SNACK',
  `start_price`  = 8.00
WHERE `id` = 213;

UPDATE `merchant` SET
  `description`  = '欧记大排档是景德镇凤凰城附近的特色大排档，提供多种赣菜和烧烤，夜市氛围浓厚，是本地居民夜宵聚会的热门场所。',
  `address`      = '江西省景德镇市珠山区凤凰城附近',
  `phone`        = '13607983456',
  `open_time`    = '17:00-02:00',
  `admin_rating` = 4.1,
  `tags`         = '大排档,夜宵,烧烤,赣菜,夜市',
  `avg_price`    = '40-70',
  `cuisine_type` = 'BBQ',
  `start_price`  = 38.00
WHERE `id` = 214;

UPDATE `merchant` SET
  `description`  = '香他她煲仔饭是景德镇豪德广场的特色煲仔饭馆，以砂锅慢煮工艺制作，米饭香糯，配料丰富，包括腊肠煲仔饭、排骨煲仔饭等多种口味。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '15979889012',
  `open_time`    = '10:30-21:30',
  `admin_rating` = 4.3,
  `tags`         = '煲仔饭,砂锅,腊肠,排骨,香糯',
  `avg_price`    = '25-40',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 22.00
WHERE `id` = 215;

UPDATE `merchant` SET
  `description`  = '柴火灶老渔村是景德镇特色农家菜馆，以柴火灶烹饪为特色，主打本地河鲜和农家菜，食材新鲜，口味纯正，环境质朴，深受游客喜爱。',
  `address`      = '江西省景德镇市昌江区附近',
  `phone`        = '13979880123',
  `open_time`    = '10:30-21:00',
  `admin_rating` = 4.4,
  `tags`         = '农家菜,柴火灶,河鲜,质朴,本地特色',
  `avg_price`    = '45-70',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 42.00
WHERE `id` = 216;

UPDATE `merchant` SET
  `description`  = '泽友忆家北京烤鸭景德镇豪德店，以传统北京烤鸭工艺制作，鸭皮酥脆，肉质鲜嫩，配以薄饼、葱丝、甜面酱，是景德镇少见的北京风味餐厅。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '0798-8891234',
  `open_time`    = '10:30-22:00',
  `admin_rating` = 4.3,
  `tags`         = '北京烤鸭,烤鸭,薄饼,特色,北京风味',
  `avg_price`    = '60-100',
  `cuisine_type` = 'CHINESE',
  `start_price`  = 58.00
WHERE `id` = 217;

UPDATE `merchant` SET
  `description`  = '喜姐炸串是全国知名炸串品牌，景德镇豪德店提供多种食材炸串，包括豆腐、香肠、蔬菜等，现炸现卖，是年轻人的零食首选。',
  `address`      = '江西省景德镇市昌江区豪德广场附近',
  `phone`        = '400-820-1234',
  `open_time`    = '10:00-22:00',
  `admin_rating` = 4.1,
  `tags`         = '炸串,零食,连锁,年轻人,现炸',
  `avg_price`    = '15-30',
  `cuisine_type` = 'SNACK',
  `start_price`  = 10.00
WHERE `id` = 218;

UPDATE `merchant` SET
  `description`  = '黄蜀郎鸡公煲是景德镇特色鸡公煲馆，以秘制酱料慢炖土鸡，配以多种蔬菜和豆腐，麻辣鲜香，是聚餐和家庭用餐的热门选择。',
  `address`      = '江西省景德镇市昌江区瓷都大道附近',
  `phone`        = '13607984567',
  `open_time`    = '10:30-22:00',
  `admin_rating` = 4.3,
  `tags`         = '鸡公煲,麻辣,土鸡,聚餐,特色',
  `avg_price`    = '50-80',
  `cuisine_type` = 'HOTPOT',
  `start_price`  = 48.00
WHERE `id` = 219;

UPDATE `merchant` SET
  `description`  = '东北烧烤铜锅涮肉是景德镇少见的东北风味餐厅，提供东北烧烤和铜锅涮肉，食材新鲜，口味豪爽，是喜爱东北菜的食客的好去处。',
  `address`      = '江西省景德镇市珠山区康家花园附近',
  `phone`        = '15979882345',
  `open_time`    = '17:00-02:00',
  `admin_rating` = 4.2,
  `tags`         = '东北菜,烧烤,涮肉,铜锅,豪爽',
  `avg_price`    = '60-100',
  `cuisine_type` = 'HOTPOT',
  `start_price`  = 55.00
WHERE `id` = 220;
