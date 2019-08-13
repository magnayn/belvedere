package com.nirima.belvedere.cli;

import com.nirima.openapi.dsl.DSLExec;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Cli {

    @Option(name="-f", aliases="--file", usage="Fully qualified path and name of file.", required=true)
    private File in;

    @Option(name="-o",usage="output to this file",metaVar="OUTPUT")
    private File out = new File(".");

    public static void main(String[] args) throws IOException {
        new Cli().doMain(args);
    }

    @Argument
    private List<String> arguments = new ArrayList<String>();

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
           // if( arguments.isEmpty() )
            //    throw new CmdLineException(parser,"No argument is given");

            // Convert it
            //DSLExec dsl = new DSLExec(getClass().getResource("test.api"));
            DSLExec dsl = new DSLExec(in.toURI().toURL());

            OpenAPI spec = dsl.run();

            Yaml.prettyPrint(spec);



        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java belvedere [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java belvedere"+parser.printExample(ALL));

            return;
        }

    }
}
