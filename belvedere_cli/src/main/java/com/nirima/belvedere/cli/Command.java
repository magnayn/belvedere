package com.nirima.belvedere.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public abstract class Command {

    @Option(name="-f", aliases="--file", usage="Fully qualified path and name of file.", required=true)
    protected File in;

    @Option(name="-o",usage="output to this file",metaVar="OUTPUT")
    protected File out = new File(".");

    public abstract void execute() throws IOException;
}
