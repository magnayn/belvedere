package com.nirima.openapi.dsl

import io.swagger.v3.oas.models.OpenAPI
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.tools.shell.IO

class DSLExec {

    DSLOpenAPI dslOpenAPI;

    public DSLExec(URL inputStream) {
        init(inputStream);
    }

    void init(URL fis) {

        DSL dsl = new DSL();

        dsl.parseScript(new DSLContext(fis));
        dslOpenAPI = dsl.runScript();


    }

    public OpenAPI run() {
        def ctx = new Object();
        dslOpenAPI.accept(ctx);
        return dslOpenAPI.getTheAPI();
    }

}

public class DSL {

    IoCapture c = new IoCapture();
    Script dslScript;

    DSLOpenAPI globe;
    DSLSchema schema;

    void parseScript(DSLContext context) {
        Binding binding = new Binding();

        binding.setProperty("out", c.io.out);
        binding.setProperty("print", new MethodClosure(c, "print"));
        binding.setProperty("println", new MethodClosure(c, "println"));


        GroovyShell shell = new GroovyShell(binding);



        dslScript = shell.parse(context.baseURL.text);


        dslScript.metaClass = createEMC(dslScript.class,
                {
                    ExpandoMetaClass emc ->

                        emc.api = {
                            cl ->  globe = new DSLOpenAPI(context, cl);
                        }


                })



    }

    DSLOpenAPI runScript() {
        dslScript.run();
        return globe;
    }

    static ExpandoMetaClass createEMC(Class scriptClass, Closure cl) {
        ExpandoMetaClass emc = new ExpandoMetaClass(scriptClass, false);
        cl(emc)
        emc.initialize()
        return emc
    }

}

public class IoCapture implements Closeable {
    public final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    public final IO io;
    public final PrintStream ps;

    private final InputStream emptyInputStream;

    public IoCapture() {
        emptyInputStream = new ByteArrayInputStream(new byte[0]);

        io = new IO(emptyInputStream, byteArrayOutputStream, byteArrayOutputStream);

        ps = new PrintStream(byteArrayOutputStream);
    }

    public void print(String message) {
        ps.print(message);
    }

    public void println(Object message) {
        ps.println(message);
    }

    public String toString() {
        return new String(byteArrayOutputStream.toByteArray());
    }

    @Override
    public void close() throws IOException {
        emptyInputStream.close();
        io.close();
        ps.close();
    }
}