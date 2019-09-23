package components;

import com.nirima.openapi.dsl.APIValidation;
import com.nirima.openapi.dsl.DSLExec;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import junit.framework.TestCase;

import java.io.File;
import java.net.MalformedURLException;

public class TestDataTypes extends TestCase {
    public void testDataType() throws MalformedURLException {
        DSLExec dsl = new DSLExec(getClass().getResource("/components/dataType.api"));

        OpenAPI spec = dsl.run();

        Yaml.prettyPrint(spec);

        APIValidation v = new APIValidation();
        v.validate(dsl.getContext(), spec);

        Schema f = (Schema) spec.getComponents().getSchemas().get("TimeSpan").getProperties().get("from");
        assertEquals(f.getPattern(),"^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$");
        
    }

    public void testPathExample() throws MalformedURLException {
        DSLExec dsl = new DSLExec(getClass().getResource("/components/pathExample.api"));

        OpenAPI spec = dsl.run();

        Yaml.prettyPrint(spec);

        APIValidation v = new APIValidation();
        v.validate(dsl.getContext(), spec);

        Parameter p =spec.getPaths().get("/thing/{id}").getPost().getParameters().get(0);

        Object example = p.getExample();
        assertEquals("33C33192-7B8D-4EE0-AD41-47AACF240A29", example);
    }

    public void testHeaders() throws MalformedURLException {
        DSLExec dsl = new DSLExec(getClass().getResource("/components/testHeaders.api"));

        OpenAPI spec = dsl.run();

        Yaml.prettyPrint(spec);

        APIValidation v = new APIValidation();
        v.validate(dsl.getContext(), spec);

        // TODO: Compare to the yaml
      
    }
}
