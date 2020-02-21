package com.nirima.belvedere.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nirima.openapi.dsl.DSLExec;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

@RestController
public class ApiController {
    @RequestMapping("/apis")
    public List<ApiInfo> getAPIs() {
        var list = new ArrayList<ApiInfo>();

        list.add(new ApiInfo("1", "worker"));

        return list;
    }

    @RequestMapping("/apis/{id}")
    public ResponseEntity<String> getAPI(String id) throws MalformedURLException, JsonProcessingException {
        DSLExec dsl = new DSLExec(new File("/Users/magnayn/dev/allocate/Integration-APIs/api/allocate/worker.api").toURL());

        //dsl.setProfiles(profiles);

        OpenAPI spec = dsl.run();

        String ret = Json.pretty().writeValueAsString(spec);
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>(ret, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping("/apis/{id}/yaml")
    public ResponseEntity<String> getAPIYaml(String id) throws MalformedURLException, JsonProcessingException {
        String[] profiles;

        DSLExec dsl = new DSLExec(new File("/Users/magnayn/dev/allocate/Integration-APIs/api/allocate/worker.api").toURL());

        //dsl.setProfiles(profiles);

        OpenAPI spec = dsl.run();

        String ret = Yaml.pretty().writeValueAsString(spec);


        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.valueOf("application/x-yaml"));
        return new ResponseEntity<String>(ret, responseHeaders, HttpStatus.OK);
    }

}
