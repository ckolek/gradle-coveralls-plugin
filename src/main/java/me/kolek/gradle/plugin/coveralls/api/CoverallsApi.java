package me.kolek.gradle.plugin.coveralls.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

public class CoverallsApi {
    private Path tempDir;

    public void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }

    public CoverallsResponse createJob(Job job) throws Exception {
        Path jsonFile = Files.createTempFile(tempDir, "coveralls", "json");

        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(jsonFile.toFile(), job);

        HttpEntity entity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("json_file", jsonFile.toFile(), ContentType.APPLICATION_JSON, "coveralls.json").build();

        HttpPost post = new HttpPost("https://coveralls.io/api/v1/jobs");
        post.setEntity(entity);

        HttpResponse response = createClient().execute(post);

        CoverallsResponse cResponse = mapper.readValue(response.getEntity().getContent(), CoverallsResponse.class);
        cResponse.setCode(response.getStatusLine().getStatusCode());

        return cResponse;
    }

    public HttpClient createClient() throws Exception {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.build();
    }
}
