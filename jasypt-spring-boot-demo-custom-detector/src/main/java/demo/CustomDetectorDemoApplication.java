package demo;


import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.environment.EncryptableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

/**
 * Sample Boot application that showcases easy integration of Jasypt encryption by
 * simply adding {@literal @EnableEncryptableProperties} to any Configuration class.
 * For decryption a password is required and is set through system properties in this example,
 * but it could be passed command line argument too like this: --jasypt.encryptor.password=password
 *
 * @author Ulises Bocchio
 */
@SpringBootApplication
@Import(TestConfig.class)
//Uncomment this if not using jasypt-spring-boot-starter (use jasypt-spring-boot) dependency in pom instead
public class CustomDetectorDemoApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CustomDetectorDemoApplication.class);

    @Autowired
    ApplicationContext appCtx;

    public static void main(String[] args) {
        //try commenting the following line out and run the app from the command line passing the password as
        //a command line argument: java -jar target/jasypt-spring-boot-demo-0.0.1-SNAPSHOT.jar --jasypt.encryptor.password=password
        //System.setProperty("jasypt.encryptor.password", "password");
        //Enable proxy mode for intercepting encrypted properties
        //System.setProperty("jasypt.encryptor.proxyPropertySources", "true");
        new SpringApplicationBuilder()
                .environment(new EncryptableEnvironment(new StandardEnvironment(), new MyEncryptablePropertyDetector()))
                .sources(CustomDetectorDemoApplication.class).run(args);
    }

    @Bean(name = "encryptablePropertyDetector")
    public EncryptablePropertyDetector encryptablePropertyDetector() {
        return new MyEncryptablePropertyDetector();
    }

    @Override
    public void run(String... args) throws Exception {
        MyService service = appCtx.getBean(MyService.class);
        Environment environment = appCtx.getBean(Environment.class);
        LOG.info("Environment's secret: {}", environment.getProperty("secret.property"));
        LOG.info("Environment's secret2: {}", environment.getProperty("secret2.property"));
        LOG.info("MyService's secret: {}", service.getSecret());
        LOG.info("MyService's secret2: {}", service.getSecret2());
        LOG.info("Done!");
    }

    private static class MyEncryptablePropertyDetector implements EncryptablePropertyDetector {
        @Override
        public boolean isEncrypted(String value) {
            if (value != null) {
                return value.startsWith("ENC@");
            }
            return false;
        }

        @Override
        public String unwrapEncryptedValue(String value) {
            return value.substring("ENC@".length());
        }
    }
}
