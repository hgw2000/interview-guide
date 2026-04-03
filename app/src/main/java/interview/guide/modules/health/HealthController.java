package interview.guide.modules.health;

import interview.guide.common.result.Result;
import interview.guide.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供系统健康状态检查接口
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisService redisService;

    /**
     * 健康检查接口
     * 检查数据库和 Redis 连接状态
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        boolean allHealthy = true;

        // 检查数据库
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            healthInfo.put("database", "UP");
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            healthInfo.put("database", "DOWN");
            allHealthy = false;
        }

        // 检查 Redis
        try {
            redisService.getClient().getKeys().count();
            healthInfo.put("redis", "UP");
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            healthInfo.put("redis", "DOWN");
            allHealthy = false;
        }

        healthInfo.put("status", allHealthy ? "UP" : "DEGRADED");

        if (allHealthy) {
            return Result.success(healthInfo);
        } else {
            return Result.error(503, "Service degraded");
        }
    }
}