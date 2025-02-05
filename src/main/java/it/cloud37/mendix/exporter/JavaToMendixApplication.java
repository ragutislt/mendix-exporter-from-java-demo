package it.cloud37.mendix.exporter;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import it.cloud37.mendix.exporter.export.MendixExporter;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class JavaToMendixApplication {
	public static void main(String[] args) throws IOException {
		SpringApplication.run(JavaToMendixApplication.class, args);

		// todo present several options on which parts to export at a time
		MendixExporter.exportEntitiesTo(args[0]);
		MendixExporter.exportRESTLayerTo(args[1]);
	}
}