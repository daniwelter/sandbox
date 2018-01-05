package uk.ac.ebi.spot.sandbox.patternanalyser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.spot.sandbox.patternanalyser.service.PatternAnalysisService;

@SpringBootApplication
public class PatternAnalyserApplication {

	@Autowired
	PatternAnalysisService analysisService;

	private String fileName = "/home/dwelter/Ontologies/HCA/ontology/hcao.owl";
	private String iri = "http://purl.obolibrary.org/obo/hcao.owl";


	private PatternAnalysisService getAnalysisService(){
		return analysisService;
	}

	public static void main(String[] args) {
		System.out.println("Starting pattern analysis application");
		ApplicationContext ctx = SpringApplication.run(PatternAnalyserApplication.class, args);
		System.out.println("Application executed successfully!");
		SpringApplication.exit(ctx);
	}

	@Bean
	CommandLineRunner run() {
		return strings -> {
			this.analysePatterns();
		};
	}

	private void analysePatterns() {
		getAnalysisService().analysePatterns(fileName, iri);
	}


}
