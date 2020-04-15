package slf.xbb.stores;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {"slf.xbb.stores"})
@MapperScan("slf.xbb.stores.mapper")
@EnableAspectJAutoProxy(proxyTargetClass =  true)
public class StoresApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoresApplication.class, args);
    }

}
