package io.brc.projekte.weltderbuecherdataloader;

import io.brc.projekte.weltderbuecherdataloader.author.Author;
import io.brc.projekte.weltderbuecherdataloader.author.AuthorRepository;
import io.brc.projekte.weltderbuecherdataloader.buch.Buch;
import io.brc.projekte.weltderbuecherdataloader.buch.BuchRepository;
import io.brc.projekte.weltderbuecherdataloader.connection.DataStaxAstraProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class WeltderbuecherDataloaderApplication {

	@Autowired  AuthorRepository authorRepository;
	@Autowired  BuchRepository buchRepository;

	@Value("${datadump.location.author}")
	private String authorDumpLocation;

	@Value("${datadump.location.works}")
	private String worksDumpLocation;

	public static void main(String[] args) {
		SpringApplication.run(WeltderbuecherDataloaderApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void start() {
		initAuthors();
		initWorks();
	}

	private void initAuthors() {
		Path path = Paths.get(authorDumpLocation);

		try(Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				//Auslesen und Parsen des String
				String jsonStr = line.substring(line.indexOf("{"));
				//Erstellen daraus JSON Objekt
				try {
					JSONObject jsonObject = new JSONObject(jsonStr);
					//Konstruieren von Author Objekt
					Author author = new Author();
					author.setName(jsonObject.optString("name"));
					author.setPersonalName(jsonObject.optString("personal_name"));
					author.setId(jsonObject.optString("key").replace("/authors/", ""));

					//Uebergeben in die Datenbank mittels Repository
					authorRepository.save(author);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void initWorks() {
		Path path = Paths.get(worksDumpLocation);
		DateTimeFormatter dateFormat =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

		try(Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				//Auslesen und Parsen des String
				String jsonStr = line.substring(line.indexOf("{"));
				//Erstellen daraus JSON Objekt
				try {
					JSONObject jsonObject = new JSONObject(jsonStr);
					//Konstruieren von Buch Objekt
					Buch buch = new Buch();
					buch.setId(jsonObject.optString("key").replace("/works/", ""));
					buch.setName(jsonObject.optString("title"));

					JSONObject beschreibungObj = jsonObject.optJSONObject("description");
					if(beschreibungObj != null) {
						buch.setBeschreibung(beschreibungObj.optString("value"));
					}

					JSONArray bildIdsArray = jsonObject.optJSONArray("covers");
					if(bildIdsArray != null ) {
						List<String> buildIds = new ArrayList<>();
						for(int i=0; i<bildIdsArray.length(); i++) {
							buildIds.add(bildIdsArray.getString(i));
						}
						buch.setBildIds(buildIds);
					}

					JSONArray authorsArray = jsonObject.optJSONArray("authors");
					if(authorsArray != null) {
						List<String> authorIds = new ArrayList<>();
						for(int i=0; i<authorsArray.length(); i++) {
							authorIds.add(authorsArray.getJSONObject(i)
									.getJSONObject("author")
									.getString("key")
									.replace("/authors/", ""));
						}
						buch.setAuthorIds(authorIds);
						List<String> authorNamen = authorIds.stream()
								.map(id -> authorRepository.findById(id))
								.map(optionalAuthor -> {
									if(!optionalAuthor.isPresent()) return "Unbekannter Author";
									return optionalAuthor.get().getName();
								}).collect(Collectors.toList());
						buch.setAuthorNamen(authorNamen);
					}

					JSONObject datumObj = jsonObject.optJSONObject("created");
					if(datumObj != null) {
						buch.setDatum(LocalDate.parse(datumObj.optString("value"), dateFormat));
					}

					//Uebergeben in die Datenbank mittels Repository
					buchRepository.save(buch);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
