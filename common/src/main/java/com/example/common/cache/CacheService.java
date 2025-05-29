//package com.example.common.cache;
//
//import com.example.common.config.cache.CommonCacheConfig;
//import com.example.common.dto.NutritionStatDTO;
//import com.example.common.dto.NutritionTrendDTO;
//import com.example.common.entity.UserNutritionGoal;
//import com.example.common.response.PageResult;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cache.CacheManager;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
///**
// * 统一的缓存服务类，提供缓存操作的通用方法
// * 封装了本地缓存和Redis缓存的操作，简化各模块的缓存使用
// */
//public class CacheService {
//    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
//
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final CacheManager cacheManager;
//    private final ObjectMapper objectMapper;
//
//    public CacheService(RedisTemplate<String, Object> redisTemplate,
//                        CacheManager cacheManager,
//                        ObjectMapper objectMapper) {
//        this.redisTemplate = redisTemplate;
//        this.cacheManager = cacheManager;
//        this.objectMapper = objectMapper;
//    }
//
//
//    /**
//     * 从缓存获取数据，先检查本地缓存，再检查Redis缓存
//     *
//     * @param cacheName 缓存名称
//     * @param key 缓存键
//     * @param <T> 返回数据类型
//     * @return 缓存的数据，如果不存在则返回null
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T get(String cacheName, Object key) {
//        // 先尝试从本地缓存获取
//        if (cacheManager.getCache(cacheName) != null) {
//            org.springframework.cache.Cache.ValueWrapper wrapper = cacheManager.getCache(cacheName).get(key);
//            if (wrapper != null) {
//                Object value = wrapper.get();
//                if (value != null) {
//                    return (T) value;
//                }
//            }
//        }
//
//        // 本地缓存未命中，尝试从Redis获取
//        String redisKey = CommonCacheConfig.buildRedisCacheKey(cacheName, key);
//        Object value = redisTemplate.opsForValue().get(redisKey);
//        if (value != null) {
//            // 处理从Redis获取的数据，确保类型正确
//            Object processedValue = processRedisValue(value);
//
//            // 将Redis中的数据同步到本地缓存，提高后续访问性能
//            if (cacheManager.getCache(cacheName) != null) {
//                cacheManager.getCache(cacheName).put(key, processedValue);
//            }
//
//            return (T) processedValue;
//        }
//
//        return null;
//    }
//
//    /**
//     * 处理从Redis获取的值，确保类型正确
//     * 主要处理LinkedHashMap转换为PageResult或UserNutritionGoal的情况，以及List类型转换
//     */
//    @SuppressWarnings("unchecked")
//    private Object processRedisValue(Object value) {
//        // 如果是List，可能需要转换元素类型
//        if (value instanceof List) {
//            List<?> list = (List<?>) value;
//            if (!list.isEmpty()) {
//                Object firstElement = list.get(0);
//                // 如果列表中包含Integer，但可能需要Long类型，进行转换
//                if (firstElement instanceof Integer) {
//                    List<Long> longList = new ArrayList<>();
//                    for (Object item : list) {
//                        if (item instanceof Integer) {
//                            longList.add(((Integer) item).longValue());
//                        } else if (item instanceof Number) {
//                            longList.add(((Number) item).longValue());
//                        } else {
//                            // 如果不是数字类型，保持原样
//                            return value;
//                        }
//                    }
//                    log.debug("将List<Integer>转换为List<Long>，元素数量: {}", longList.size());
//                    return longList;
//                }
//            }
//            return value;
//        }
//
//        // 如果是LinkedHashMap，可能需要转换为特定对象类型
//        if (value instanceof LinkedHashMap) {
//            Map<String, Object> map = (Map<String, Object>) value;
//
//            // 检查是否包含PageResult的关键字段
//            if (map.containsKey("total") && map.containsKey("records") &&
//                map.containsKey("current") && map.containsKey("size")) {
//                try {
//                    // 创建PageResult对象
//                    long total = ((Number) map.get("total")).longValue();
//                    int current = ((Number) map.get("current")).intValue();
//                    int size = ((Number) map.get("size")).intValue();
//                    List<?> records = (List<?>) map.get("records");
//
//                    log.debug("将LinkedHashMap转换为PageResult: total={}, current={}, size={}, records.size={}",
//                            total, current, size, records != null ? records.size() : 0);
//
//                    return new PageResult<>(total, records, current, size);
//                } catch (Exception e) {
//                    log.error("转换LinkedHashMap为PageResult失败", e);
//                }
//            }
//
//            // 检查是否包含UserNutritionGoal的关键字段
//            if (map.containsKey("userId") &&
//                (map.containsKey("calorieTarget") || map.containsKey("proteinTarget") ||
//                 map.containsKey("carbsTarget") || map.containsKey("fatTarget"))) {
//                try {
//                    // 创建UserNutritionGoal对象
//                    UserNutritionGoal goal = new UserNutritionGoal();
//
//                    // 设置基本字段
//                    if (map.containsKey("id")) {
//                        goal.setId(((Number) map.get("id")).longValue());
//                    }
//
//                    goal.setUserId(((Number) map.get("userId")).longValue());
//
//                    if (map.containsKey("calorieTarget")) {
//                        Object calorieTarget = map.get("calorieTarget");
//                        if (calorieTarget != null) {
//                            goal.setCalorieTarget(((Number) calorieTarget).intValue());
//                        }
//                    }
//
//                    if (map.containsKey("weightTarget")) {
//                        Object weightTarget = map.get("weightTarget");
//                        if (weightTarget != null) {
//                            if (weightTarget instanceof Number) {
//                                goal.setWeightTarget(BigDecimal.valueOf(((Number) weightTarget).doubleValue()));
//                            } else if (weightTarget instanceof Map) {
//                                // 处理BigDecimal序列化的情况
//                                Map<String, Object> bdMap = (Map<String, Object>) weightTarget;
//                                if (bdMap.containsKey("value")) {
//                                    goal.setWeightTarget(BigDecimal.valueOf(((Number) bdMap.get("value")).doubleValue()));
//                                }
//                            }
//                        }
//                    }
//
//                    if (map.containsKey("proteinTarget")) {
//                        Object proteinTarget = map.get("proteinTarget");
//                        if (proteinTarget != null) {
//                            goal.setProteinTarget(((Number) proteinTarget).intValue());
//                        }
//                    }
//
//                    if (map.containsKey("carbsTarget")) {
//                        Object carbsTarget = map.get("carbsTarget");
//                        if (carbsTarget != null) {
//                            goal.setCarbsTarget(((Number) carbsTarget).intValue());
//                        }
//                    }
//
//                    if (map.containsKey("fatTarget")) {
//                        Object fatTarget = map.get("fatTarget");
//                        if (fatTarget != null) {
//                            goal.setFatTarget(((Number) fatTarget).intValue());
//                        }
//                    }
//
//                    // 设置布尔值字段
//                    if (map.containsKey("isVegetarian")) {
//                        goal.setIsVegetarian((Boolean) map.get("isVegetarian"));
//                    }
//
//                    if (map.containsKey("isLowCarb")) {
//                        goal.setIsLowCarb((Boolean) map.get("isLowCarb"));
//                    }
//
//                    if (map.containsKey("isHighProtein")) {
//                        goal.setIsHighProtein((Boolean) map.get("isHighProtein"));
//                    }
//
//                    if (map.containsKey("isGlutenFree")) {
//                        goal.setIsGlutenFree((Boolean) map.get("isGlutenFree"));
//                    }
//
//                    if (map.containsKey("isLowSodium")) {
//                        goal.setIsLowSodium((Boolean) map.get("isLowSodium"));
//                    }
//
//                    // 设置日期字段
//                    if (map.containsKey("createdAt")) {
//                        Object createdAt = map.get("createdAt");
//                        if (createdAt instanceof Long) {
//                            goal.setCreatedAt(new Date((Long) createdAt));
//                        } else if (createdAt instanceof Number) {
//                            goal.setCreatedAt(new Date(((Number) createdAt).longValue()));
//                        } else if (createdAt instanceof String) {
//                            // 可能是ISO日期格式，需要解析
//                            try {
//                                goal.setCreatedAt(new Date(Long.parseLong((String) createdAt)));
//                            } catch (NumberFormatException e) {
//                                // 忽略解析错误
//                            }
//                        }
//                    }
//
//                    if (map.containsKey("updatedAt")) {
//                        Object updatedAt = map.get("updatedAt");
//                        if (updatedAt instanceof Long) {
//                            goal.setUpdatedAt(new Date((Long) updatedAt));
//                        } else if (updatedAt instanceof Number) {
//                            goal.setUpdatedAt(new Date(((Number) updatedAt).longValue()));
//                        } else if (updatedAt instanceof String) {
//                            // 可能是ISO日期格式，需要解析
//                            try {
//                                goal.setUpdatedAt(new Date(Long.parseLong((String) updatedAt)));
//                            } catch (NumberFormatException e) {
//                                // 忽略解析错误
//                            }
//                        }
//                    }
//
//                    log.debug("将LinkedHashMap转换为UserNutritionGoal: userId={}, calorieTarget={}",
//                            goal.getUserId(), goal.getCalorieTarget());
//
//                    return goal;
//                } catch (Exception e) {
//                    log.error("转换LinkedHashMap为UserNutritionGoal失败", e);
//                }
//            }
//
//            // 检查是否包含NutritionTrendDTO的关键字段
//            if (map.containsKey("dateList") &&
//                (map.containsKey("calorieList") || map.containsKey("proteinList") ||
//                 map.containsKey("carbsList") || map.containsKey("fatList"))) {
//                try {
//                    // 创建NutritionTrendDTO对象
//                    NutritionTrendDTO nutritionTrend = new NutritionTrendDTO();
//
//                    // 设置日期列表
//                    if (map.containsKey("dateList")) {
//                        Object dateList = map.get("dateList");
//                        if (dateList instanceof List) {
//                            nutritionTrend.setDateList((List<String>) dateList);
//                        }
//                    }
//
//                    // 设置热量列表
//                    if (map.containsKey("calorieList")) {
//                        Object calorieList = map.get("calorieList");
//                        if (calorieList instanceof List) {
//                            List<?> rawList = (List<?>) calorieList;
//                            List<Integer> integerList = new ArrayList<>();
//                            for (Object item : rawList) {
//                                if (item instanceof Number) {
//                                    integerList.add(((Number) item).intValue());
//                                }
//                            }
//                            nutritionTrend.setCalorieList(integerList);
//                        }
//                    }
//
//                    // 设置蛋白质列表
//                    if (map.containsKey("proteinList")) {
//                        Object proteinList = map.get("proteinList");
//                        if (proteinList instanceof List) {
//                            List<?> rawList = (List<?>) proteinList;
//                            List<Double> doubleList = new ArrayList<>();
//                            for (Object item : rawList) {
//                                if (item instanceof Number) {
//                                    doubleList.add(((Number) item).doubleValue());
//                                }
//                            }
//                            nutritionTrend.setProteinList(doubleList);
//                        }
//                    }
//
//                    // 设置碳水化合物列表
//                    if (map.containsKey("carbsList")) {
//                        Object carbsList = map.get("carbsList");
//                        if (carbsList instanceof List) {
//                            List<?> rawList = (List<?>) carbsList;
//                            List<Double> doubleList = new ArrayList<>();
//                            for (Object item : rawList) {
//                                if (item instanceof Number) {
//                                    doubleList.add(((Number) item).doubleValue());
//                                }
//                            }
//                            nutritionTrend.setCarbsList(doubleList);
//                        }
//                    }
//
//                    // 设置脂肪列表
//                    if (map.containsKey("fatList")) {
//                        Object fatList = map.get("fatList");
//                        if (fatList instanceof List) {
//                            List<?> rawList = (List<?>) fatList;
//                            List<Double> doubleList = new ArrayList<>();
//                            for (Object item : rawList) {
//                                if (item instanceof Number) {
//                                    doubleList.add(((Number) item).doubleValue());
//                                }
//                            }
//                            nutritionTrend.setFatList(doubleList);
//                        }
//                    }
//
//                    log.debug("将LinkedHashMap转换为NutritionTrendDTO: dateList.size={}, calorieList.size={}",
//                            nutritionTrend.getDateList() != null ? nutritionTrend.getDateList().size() : 0,
//                            nutritionTrend.getCalorieList() != null ? nutritionTrend.getCalorieList().size() : 0);
//
//                    return nutritionTrend;
//                } catch (Exception e) {
//                    log.error("转换LinkedHashMap为NutritionTrendDTO失败", e);
//                }
//            }
//
//            // 检查是否包含NutritionStatDTO的关键字段
//            if (map.containsKey("date") &&
//                (map.containsKey("calorie") || map.containsKey("protein") ||
//                 map.containsKey("carbs") || map.containsKey("fat"))) {
//                try {
//                    // 创建NutritionStatDTO对象
//                    NutritionStatDTO nutritionStat = new NutritionStatDTO();
//
//                    // 设置日期
//                    if (map.containsKey("date")) {
//                        nutritionStat.setDate((String) map.get("date"));
//                    }
//
//                    // 设置热量
//                    if (map.containsKey("calorie")) {
//                        Object calorie = map.get("calorie");
//                        if (calorie != null) {
//                            nutritionStat.setCalorie(((Number) calorie).intValue());
//                        }
//                    }
//
//                    // 设置蛋白质
//                    if (map.containsKey("protein")) {
//                        Object protein = map.get("protein");
//                        if (protein != null) {
//                            nutritionStat.setProtein(((Number) protein).doubleValue());
//                        }
//                    }
//
//                    // 设置碳水化合物
//                    if (map.containsKey("carbs")) {
//                        Object carbs = map.get("carbs");
//                        if (carbs != null) {
//                            nutritionStat.setCarbs(((Number) carbs).doubleValue());
//                        }
//                    }
//
//                    // 设置脂肪
//                    if (map.containsKey("fat")) {
//                        Object fat = map.get("fat");
//                        if (fat != null) {
//                            nutritionStat.setFat(((Number) fat).doubleValue());
//                        }
//                    }
//
//                    // 设置百分比字段
//                    if (map.containsKey("caloriePercentage")) {
//                        Object caloriePercentage = map.get("caloriePercentage");
//                        if (caloriePercentage != null) {
//                            nutritionStat.setCaloriePercentage(((Number) caloriePercentage).doubleValue());
//                        }
//                    }
//
//                    if (map.containsKey("proteinPercentage")) {
//                        Object proteinPercentage = map.get("proteinPercentage");
//                        if (proteinPercentage != null) {
//                            nutritionStat.setProteinPercentage(((Number) proteinPercentage).doubleValue());
//                        }
//                    }
//
//                    if (map.containsKey("carbsPercentage")) {
//                        Object carbsPercentage = map.get("carbsPercentage");
//                        if (carbsPercentage != null) {
//                            nutritionStat.setCarbsPercentage(((Number) carbsPercentage).doubleValue());
//                        }
//                    }
//
//                    if (map.containsKey("fatPercentage")) {
//                        Object fatPercentage = map.get("fatPercentage");
//                        if (fatPercentage != null) {
//                            nutritionStat.setFatPercentage(((Number) fatPercentage).doubleValue());
//                        }
//                    }
//
//                    log.debug("将LinkedHashMap转换为NutritionStatDTO: date={}, calorie={}",
//                            nutritionStat.getDate(), nutritionStat.getCalorie());
//
//                    return nutritionStat;
//                } catch (Exception e) {
//                    log.error("转换LinkedHashMap为NutritionStatDTO失败", e);
//                }
//            }
//        }
//
//        return value;
//    }
//
//    /**
//     * 将数据存入缓存（同时存入Redis和本地缓存）
//     *
//     * @param cacheName 缓存名称
//     * @param key 缓存键
//     * @param value 缓存值
//     * @param ttlMinutes 过期时间（分钟），如果为null则使用默认过期时间
//     */
//    public void put(String cacheName, Object key, Object value, Long ttlMinutes) {
//        if (value == null) {
//            return;
//        }
//
//        String redisKey = CommonCacheConfig.buildRedisCacheKey(cacheName, key);
//
//        // 存入Redis
//        if (ttlMinutes != null) {
//            redisTemplate.opsForValue().set(redisKey, value, ttlMinutes, TimeUnit.MINUTES);
//        } else {
//            redisTemplate.opsForValue().set(redisKey, value);
//        }
//
//        // 存入本地缓存
//        if (cacheManager.getCache(cacheName) != null) {
//            cacheManager.getCache(cacheName).put(key, value);
//        }
//    }
//
//    /**
//     * 使用默认过期时间将数据存入缓存
//     */
//    public void put(String cacheName, Object key, Object value) {
//        put(cacheName, key, value, null);
//    }
//
//    /**
//     * 异步将数据存入缓存
//     */
//    @Async
//    public CompletableFuture<Void> putAsync(String cacheName, Object key, Object value, Long ttlMinutes) {
//        try {
//            put(cacheName, key, value, ttlMinutes);
//        } catch (Exception e) {
//            log.error("异步写缓存出错 cacheName={}, key={}", cacheName, key, e);
//        }
//        return CompletableFuture.completedFuture(null);
//    }
//
//
//    /**
//     * 使用默认过期时间异步将数据存入缓存
//     */
//    @Async
//    public CompletableFuture<Void> putAsync(String cacheName, Object key, Object value) {
//        put(cacheName, key, value);
//        return CompletableFuture.completedFuture(null);
//    }
//
//    /**
//     * 从缓存中删除数据
//     *
//     * @param cacheName 缓存名称
//     * @param key 缓存键
//     */
//    public void evict(String cacheName, Object key) {
//        String redisKey = CommonCacheConfig.buildRedisCacheKey(cacheName, key);
//
//        // 从Redis删除
//        redisTemplate.delete(redisKey);
//
//        // 从本地缓存删除
//        if (cacheManager.getCache(cacheName) != null) {
//            cacheManager.getCache(cacheName).evict(key);
//        }
//    }
//
//    /**
//     * 清除指定缓存名称的所有数据
//     *
//     * @param cacheName 缓存名称
//     */
//    public void clear(String cacheName) {
//        // 清除Redis中的缓存
//        String keyPattern = CommonCacheConfig.getRedisCachePrefix(cacheName) + "*";
//        Set<String> keys = redisTemplate.keys(keyPattern);
//        if (keys != null && !keys.isEmpty()) {
//            redisTemplate.delete(keys);
//        }
//
//        // 清除本地缓存
//        if (cacheManager.getCache(cacheName) != null) {
//            cacheManager.getCache(cacheName).clear();
//        }
//    }
//
//    /**
//     * 异步从缓存中删除数据
//     */
//    @Async
//    public CompletableFuture<Void> evictAsync(String cacheName, Object key) {
//        evict(cacheName, key);
//        return CompletableFuture.completedFuture(null);
//    }
//
//    /**
//     * 根据模式删除缓存
//     *
//     * @param cacheName 缓存名称
//     * @param pattern 缓存键模式，支持通配符*
//     */
//    public void evictByPattern(String cacheName, String pattern) {
//        // 构建Redis键模式
//        String redisKeyPattern = CommonCacheConfig.getRedisCachePrefix(cacheName) + pattern;
//
//        // 从Redis删除匹配的键
//        Set<String> keys = redisTemplate.keys(redisKeyPattern);
//        if (keys != null && !keys.isEmpty()) {
//            redisTemplate.delete(keys);
//
//            // 对于本地缓存，如果模式中包含通配符，则清除整个缓存
//            // 这是为了确保本地缓存与Redis缓存保持一致
//            if (pattern.contains("*") && cacheManager.getCache(cacheName) != null) {
//                cacheManager.getCache(cacheName).clear();
//            }
//        }
//    }
//
//    /**
//     * 异步根据模式删除缓存
//     */
//    @Async
//    public CompletableFuture<Void> evictByPatternAsync(String cacheName, String pattern) {
//        evictByPattern(cacheName, pattern);
//        return CompletableFuture.completedFuture(null);
//    }
//
//    /**
//     * 异步清除指定缓存名称的所有数据
//     */
//    @Async
//    public CompletableFuture<Void> clearAsync(String cacheName) {
//        clear(cacheName);
//        return CompletableFuture.completedFuture(null);
//    }
//}
