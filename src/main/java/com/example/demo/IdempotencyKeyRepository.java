# 应用设置
spring.application.name=vortexnet-ecommerce
server.port=8080

# 禁用数据源自动配置（因为没有 MySQL）
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=60000ms
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8
spring.redis.jedis.pool.min-idle=0
