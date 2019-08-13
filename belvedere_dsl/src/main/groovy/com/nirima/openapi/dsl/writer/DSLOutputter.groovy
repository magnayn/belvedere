package com.nirima.openapi.dsl.writer


import groovy.util.logging.Slf4j
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Schema
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.beanutils.PropertyUtils
import org.apache.commons.lang3.ClassUtils

class DW {


    String write(Object o, String name) {
        def value = BeanUtils.getProperty(o, name);
        if( value == null )
            return '';
        return "  ${name} \"${value}\"\n";
    }
}

@Slf4j
public class BaseWriter<T> {
    String name;
    T item;

    BaseWriter(String name, T item) {
        this.name = name;
        this.item = item;
    }

    String toDSL() {
        if( item == null )
            return;

        String s = writeHeader();

        def itemMap = PropertyUtils.describe(item);

        itemMap.each() { k,v ->
            s += write(item,k);
        }


        s += "}\n";

        return s;
    }

    String writeHeader() {
        "${name} {\n"
    }


    String write(Object o, String name) {
        def value = PropertyUtils.getProperty(o, name);
        if (value == null)
            return '';

        writeValue(o, name, value);
    }

    String writeValue(Object o, String name, Object value) {

        if( name == "class")
            return '';

        if( ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value.getClass() == String.class )
            return "  ${name} \"${value}\"\n";

        if( name == "extensions" ) {

            String ret = "";

            value.each() { k,v ->

                ret += "  extension('${k}') { \n"

                v.each() { k2,v2 ->

                    ret += """        ${k2} "${v2}"\n """;

                }


                ret += "}\n"


            }
            return ret;
        }

        if( value instanceof List ) {
            String ret = "";
            value.each() { v ->
                try {
                    ret += generate(v);
                } catch(Exception ex) {}
            }
            return ret;
        }

        if( value instanceof HashMap ) {
            String ret = "";
            value.each() { k, v ->
                ret += "   " + createFor(v).toDSL();
            }
            return ret;
        }

        if( value instanceof Class ) {
            return value.toString();
        }

        return createFor(value).toDSL();
        //return "  ${name} \"${value}\"\n";
    }


    static BaseWriter createFor(Object item) {

        log.info("Create for ${item}");

        assert item != null;

        String pkg = item.getClass().getPackage().getName().toString();
        assert pkg.startsWith("io.swagger.v3.oas.models");


        if( item instanceof OpenAPI ) {
            return new OpenAPIWriter(item);
        }

        if( item instanceof HashMap ) {
            return new MappedWriter(item.getClass().getSimpleName().toLowerCase(), item);
        }

        if( item instanceof Schema) {
            return new SchemaWriter("schema", item);
        }

        if( item instanceof Components) {
            return new ComponentsWriter(item);
        }


        if( item instanceof PathItem) {
            return new PathItemWriter(item);
        }

        if( item instanceof Header ) {
            return new BaseWriter("header", item);
        }

        return new BaseWriter(item.getClass().getSimpleName().toLowerCase(), item);
    }


}

class PathItemWriter extends BaseWriter<PathItem> {
    PathItemWriter(PathItem item) {
        super("", item);
    }

    String toDSL() {

        String ret = "";

        if( item.get != null )
            ret += new OperationWriter("get", item.get).toDSL();

        if( item.put != null )
            ret += new OperationWriter("put", item.put).toDSL();

        if( item.post != null )
            ret += new OperationWriter("post", item.post).toDSL();

        
        return ret;
    }

}

class OperationWriter extends BaseWriter<Operation> {

    OperationWriter(String name, Operation item) {
        super(name, item)
    }

    String writeHeader() {
        "operation(OperationType.${name.toUpperCase()}, '${item.operationId}'}( \n"
    }
}

class ComponentsWriter extends BaseWriter {

    ComponentsWriter(Components item) {
        super("components", item)
    }

    String writeValue(Object o, String name, Object value) {
        if( name == "schemas" )
        {
            def ret = "";

            value.each() {
                ret += "    schema('${it.key}') {\n";
                ret += createFor(it.value).toDSL();
                ret += "\n    }\n";
            }

            return ret;
        }



        return super.writeValue(o, name, value);
    }

}

class OpenAPIWriter extends BaseWriter {

    OpenAPIWriter(OpenAPI item) {
        super("api", item);
    }


    String writeValue(Object o, String name, Object value) {
        if( name == "paths" )
        {
            def ret = "";

            value.each() {
                ret += "    path('${it.key}') {\n";
                ret += createFor(it.value).toDSL();
                ret += "\n    }\n";
            }

            return ret;
        }



        return super.writeValue(o, name, value);
    }
}

class MappedWriter extends BaseWriter {
    MappedWriter(String name, Object item) {
        super(name, item);
    }
}


class SchemaWriter extends BaseWriter {
    SchemaWriter(String name, Object item) {
        super(name, item);
    }
}


public class DSLWriter {

    OpenAPI api;
    DW writer = new DW();

    public DSLWriter(OpenAPI api) {
        this.api = api;
    }

    String generate(OpenAPI api) {

        OpenAPIWriter wx = new OpenAPIWriter(api);
        return wx.toDSL();

    }

   /*
    static String generate(ExternalDocumentation docs) {
        if( docs == null )
            return '';

        return """
    externalDocs {
        url '${docs.url}'
        description '${docs.description}'
        ${generate(docs.extensions)}
    }
"""
    }

    static String generate(Map<String, Object> extensions) {
        if(extensions == null )
            return '';

        String ret = "";

        extensions.each() { k,v ->

            ret += "  extension('${k}') { \n"

            v.each() { k2,v2 ->

                ret += """        ${k2} ${v2}""";

            }


            ret += "}\n"


        }
    }  */
}
