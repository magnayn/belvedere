package com.nirima.belvedere.cli;

import com.nirima.openapi.dsl.DSLExec;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class ConvertCommand extends Command {
    @Override
    public void execute() throws IOException {


        DSLExec dsl;

        if (in == null) {
            // Stdin             
            dsl = new DSLExec(System.in);
        } else {
            dsl = new DSLExec(in.toURI().toURL());
        }

        OpenAPI spec = dsl.run();

        if (out != null) {

            FileOutputStream fos = new FileOutputStream(out);

            fos.write(getHeader().getBytes());

            Yaml.pretty().writeValue(fos, spec);
        } else {
            System.out.println(getHeader());
            Yaml.prettyPrint(spec);
        }


    }

    String getHeader() {
        return "# ---------------------------------------------------------------------------------------------- \n" +
                "# ** AUTO-GENERATED FILE ** CHANGES WILL BE OVERWRITTEN ** \n" +
                "# Converted by Belvedere \n" + "# http://github.com/magnayn/belvedere \n" +
                "# ---------------------------------------------------------------------------------------------- \n";

    }
}
