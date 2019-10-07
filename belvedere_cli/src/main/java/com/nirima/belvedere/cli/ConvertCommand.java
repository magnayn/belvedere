package com.nirima.belvedere.cli;

import com.nirima.openapi.dsl.DSLExec;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class ConvertCommand extends Command {
    @Override
    public void execute() throws IOException {
        DSLExec dsl = new DSLExec(in.toURI().toURL());

        OpenAPI spec = dsl.run();

        if( out != null ) {

            FileOutputStream fos = new FileOutputStream(out);

            fos.write("# ---------------------------------------------------------------------------------------------- \n".getBytes());
            fos.write("# ** AUTO-GENERATED FILE ** CHANGES WILL BE OVERWRITTEN ** \n".getBytes());
            fos.write("# Converted by Belvedere \n".getBytes());
            fos.write("# http://github.com/magnayn/belvedere \n".getBytes());
            fos.write("# ---------------------------------------------------------------------------------------------- \n".getBytes());

            Yaml.pretty().writeValue(fos,spec);
        } else {
            Yaml.prettyPrint(spec);
        }


    }
}
