package com.nirima.belvedere.cli;

import com.nirima.openapi.dsl.DSLExec;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.net.MalformedURLException;

public class ConvertCommand extends Command {
    @Override
    public void execute() throws IOException {
        DSLExec dsl = new DSLExec(in.toURI().toURL());

        OpenAPI spec = dsl.run();

        if( out != null ) {
            Yaml.pretty().writeValue(out,spec);
        } else {
            Yaml.prettyPrint(spec);
        }


    }
}
