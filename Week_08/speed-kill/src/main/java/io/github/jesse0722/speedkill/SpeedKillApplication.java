package io.github.jesse0722.speedkill;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@MapperScan(basePackages = "io.github.jesse0722.speedkill.dao")
public class SpeedKillApplication{
    public static void main(String[] args) {
        SpringApplication.run(SpeedKillApplication.class, args);
    }
}
