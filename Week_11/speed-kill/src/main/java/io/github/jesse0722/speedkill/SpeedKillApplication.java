package io.github.jesse0722.speedkill;


import io.github.jesse0722.speedkill.dao.SpeedKillMapper;
import io.github.jesse0722.speedkill.module.SpeedKill;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = "io.github.jesse0722.speedkill.dao")
@EnableScheduling
public class SpeedKillApplication implements ApplicationRunner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SpeedKillMapper speedKillMapper;

    public static void main(String[] args) {
        SpringApplication.run(SpeedKillApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        SpeedKill speedKill = speedKillMapper.get(1000);
        speedKillMapper.updateStock(1000L, 10);
        redisTemplate.opsForValue().set(speedKill.getName(), String.valueOf(speedKill.getNumber()));
    }
}
