package fraud_scoring_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FraudScoringEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudScoringEngineApplication.class, args);
	}

}
