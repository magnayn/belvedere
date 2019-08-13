package com.nirima.openapi.dsl

import groovy.util.logging.Slf4j
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.apache.commons.beanutils.BeanUtils

import java.lang.reflect.Array


public class DSLContext {
    URL baseURL;

    public DSLContext(URL baseURL) {
        this.baseURL = baseURL;
    }
}


public class DSLItem<T> {

    DSLContext context;
    T self;

    public DSLItem(DSLContext context, T i) {
        this.context = context;
        this.self = i;
        assert context != null
    }

    public void evaluate(Closure closure1) {
        closure1.delegate = this;
        closure1.resolveStrategy = Closure.DELEGATE_FIRST;
        closure1()
    }

    public Object methodMissing(String name, Object args) {
        // here we extract the closure from arguments, etc
        // return "methodMissing called with name '" + name + "' and args = " + args;
        org.apache.commons.beanutils.BeanUtilsBean.getInstance().getProperty(self, name);

        BeanUtils.setProperty(self, name, args);
        return self;
    }

    def propertyMissing(String name, Object value) {

        println "     set ${name}=${value} on ${this}"

        try {

            // Throw exception if not there
            org.apache.commons.beanutils.BeanUtilsBean.getInstance().getProperty(self, name);

            BeanUtils.setProperty(self, name, value);

        }
        catch (MissingPropertyException e1) {
            println(
                    "Error setting state for resource ${getProxy()} property ${name} with value ${value}");
        }
        catch (Exception ex) {
            println(
                    "Error setting state for resource ${getProxy()} property ${name} with value ${value}");
            throw ex;
        }

        println "     done set ${name}=${value} on ${this}"

    }

    public void process(Closure closure) {
        closure.delegate = this;

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
    }

    public DSLSchema schema(Closure c) {

        DSLSchema child = new DSLSchema(context);
        child.process(c);
        self.setSchema(child.self);
        return child;
    }


}

@Slf4j
public class DSLOpenAPI extends DSLItem<OpenAPI> {


    OpenAPI theAPI;


    public DSLOpenAPI(DSLContext context, Closure c) {
        super(context, new OpenAPI());

        self.setPaths(new Paths());

        process(c);
        theAPI = self;
    }

    public void accept(Object context) {

        //  closure.delegate = this;
        //  closure.resolveStrategy = Closure.DELEGATE_FIRST

        //   closure()

    }

    public DSLInfo info(Closure c) {
        DSLInfo dslInfo = new DSLInfo(context);
        dslInfo.process(c);
        self.setInfo(dslInfo.self);
        return dslInfo;
    }

    DSLServer server(Closure c) {
        DSLServer s = new DSLServer(context);
        s.process(c);

        if (self.getServers() == null)
            self.setServers(new ArrayList<Server>());

        self.getServers().add(s.self);

        return s;
    }

    public DSLPathItem path(String name, Closure c) {
        DSLPathItem pi = new DSLPathItem(context);
        pi.process(c);
        self.getPaths().addPathItem(name, pi.self);
        return pi;
    }

    public DSLComponents components(Closure c) {
        DSLComponents components = new DSLComponents(context);
        components.process(c);
        self.setComponents(components.self);
        return components;
    }

    public DSLTagItem tag(String name, Closure c) {
        def item = new DSLTagItem(context, new Tag());
        item.name = name;

        self.addTagsItem(item.self);

        return item;
    }

}

@Slf4j
class DSLTagItem extends DSLItem<Tag> {

    DSLTagItem(DSLContext context, Tag i) {
        super(context, i)
    }
}

@Slf4j
class DSLPathItem extends DSLItem<PathItem> {

    DSLPathItem(DSLContext context) {
        super(context, new PathItem());
    }

    public DSLOperation post(Closure c) {
        DSLOperation pi = new DSLOperation(context);
        pi.process(c);
        self.setPost(pi.self);
        return pi;
    }

    public DSLOperation operation(OperationType method, String operationId, Closure closure) {
        DSLOperation pi = new DSLOperation(context);
        pi.process(closure);

        switch (method) {
            case OperationType.GET:
                self.setGet(pi.self);
                break;
            case OperationType.PUT:
                self.setPut(pi.self);
                break;
            case OperationType.POST:
                self.setPost(pi.self);
                break;

            case OperationType.DELETE:
                self.setDelete(pi.self);
                break;


        }


        return pi;
    }
}


@Slf4j
class DSLServer extends DSLItem<Server> {

    DSLServer(DSLContext context) {
        super(context, new Server());
    }
}

@Slf4j
class DSLApiResponse extends DSLItem<ApiResponse> {

    DSLApiResponse(DSLContext context) {
        super(context, new ApiResponse());
    }

    public DSLMediaType content(String type, Closure c) {
        if (self.getContent() == null)
            self.setContent(new Content());

        DSLMediaType pi = new DSLMediaType(context);
        pi.process(c);
        self.getContent().addMediaType(type, pi.self);
        return pi;

    }
}

@Slf4j
class DSLComponents extends DSLItem<Components> {

    DSLComponents(DSLContext context) {
        super(context, new Components());
    }

    void include(String filename) {


        DSLContext childContext = new DSLContext(new URL(context.baseURL, filename));


        DSL d = new DSL();
        d.parseScript(childContext);


        d.runScript();

        if (self.getSchemas() == null)
            self.setSchemas(new HashMap<>());

        d.globe.self.getComponents().getSchemas().each { k, v -> self.getSchemas().put(k, v); }

    }


    DSLSchema schema(String name, Closure c) {
        DSLSchema s = new DSLSchema(context);
        s.process(c);

        if (self.getSchemas() == null)
            self.setSchemas(new HashMap<>());

        self.getSchemas().put(name, s.self);
        return s;
    }

    DSLSchema schema(LinkedHashMap items, Closure c) {


        if (self.getSchemas() == null)
            self.setSchemas(new HashMap<>());


        Class cls = items.values().first();

        DSLSchema s = new DSLSchema();
        s.self = new io.swagger.v3.oas.models.media.StringSchema();

        s.process(c);

        self.getSchemas().put(items.keySet().first(), s.self);

        return s;
    }


}


@Slf4j
class DSLOperation extends DSLItem<Operation> {

    DSLOperation(DSLContext context) {
        super(context, new Operation());
    }

    public DSLRequestBody requestBody(Closure c) {
        DSLRequestBody pi = new DSLRequestBody(context);
        pi.process(c);
        self.setRequestBody(pi.self);
        return pi;
    }

    public DSLParameter parameter(LinkedHashMap m, Closure c) {
        DSLParameter param = new DSLParameter(context);
        param.process(c);

        // parameter map name:Type

        def e = m.entrySet().first();
        String name = e.key;
        String type = e.value;

        String inType = m.get('in');
        param.self.setIn(inType); // TODO make into type so can do in:Path

        param.self.name = name;
        param.self.schema = new StringSchema(); // TODO from type

        self.addParametersItem(param.self);

        return param;
    }

    public DSLApiResponse response(String type, Closure c) {
        DSLApiResponse pi = new DSLApiResponse(context);
        pi.process(c);

        if (self.getResponses() == null)
            self.setResponses(new ApiResponses());

        self.getResponses().put(type, pi.self);
        return pi;
    }

    public DSLOperation tags(String tags) {
        self.addTagsItem(tags);
        return this;
    }



    public DSLOperation tags(Object[] tags) {
        tags.each() {
            self.addTagsItem(it)
        }
        return this;
    }
}

@Slf4j
class DSLParameter extends DSLItem<Parameter> {
    DSLParameter(DSLContext context) {
        super(context, new Parameter());
    }
}


@Slf4j
class DSLRequestBody
        extends DSLItem<RequestBody> {

    DSLRequestBody(DSLContext context) {
        super(context, new RequestBody());
    }

    public DSLMediaType content(String type, Closure c) {
        if (self.getContent() == null)
            self.setContent(new Content());

        DSLMediaType pi = new DSLMediaType(context);
        pi.process(c);
        self.getContent().addMediaType(type, pi.self);
        return pi;

    }
}

@Slf4j
class DSLSchema
        extends DSLItem<Schema<?>> {

    DSLSchema(DSLContext context) {
        super(context, new ObjectSchema());
    }

    public DSLSchema(DSLContext context, Closure c) {
        super(context, new ObjectSchema());

        process(c);
    }

    public DSLSchema(DSLContext context, Schema s, Closure c) {
        super(context, s);

        process(c);
    }

    DSLSchema ref(LinkedHashMap refspec) {
        // format schema:item
        self.set$ref(refspec.values().first());
        return this;
    }

    DSLSchema required(DSLSchema schema) {

        self.addRequiredItem(schema.self.name);
        return schema;

    }

    DSLSchema schema(String name, Closure c) {
        DSLSchema s = new DSLSchema(context);
        s.process(c);

        if (self.getProperties() == null)
            self.setProperties(new HashMap<>());

        self.getProperties().put(name, s.self);
        return s;
    }

    DSLSchema schema(LinkedHashMap items) {

        schema(items, {});
    }

    DSLSchema schema(LinkedHashMap items, Closure c) {

        def cls = items.values().first();
        String name = items.keySet().first();

        DSLSchema s = new DSLSchema(context);

        if( cls instanceof Class )
            s.self = makeSchemaFromClass(cls, items);
        else if( cls instanceof String) {
            s.self = new Schema();
            s.self.$ref(cls);
        }
        else {
            // Assume closure?
            DSLSchema resultContainer = new DSLSchema(context, new Schema(), cls);

            s.self = resultContainer.self;
            /*def op = cls()

            def dat = op.item;

            s.item = op.item;
            s.item.name = name;

             */
            //return s;
        }

        s.self.name = name;

        s.process(c);



        self.addProperties(name, s.self);

        return s;
    }

    static Schema makeSchemaFromClass(Class cls, LinkedHashMap data) {
        if (cls == String.class)
            return new StringSchema();
        else if (cls == Date.class)
            return new DateTimeSchema();
        else if (cls == BigDecimal.class || cls == Double.class)
            return new NumberSchema();
        else if (cls == Boolean.class || cls == boolean.class)
            return new BooleanSchema();
        else if (cls.isEnum()) {
            def ss = new StringSchema();
            cls.getEnumConstants().each() { ss.addEnumItem(it.toString()) }
            return ss;
        } else if (cls == Collection.class) {
            def t = cls.getTypeParameters()[0];
            println t;
        } else if (cls.isArray()) {
            Class clsx = cls.getComponentType()
            def ars = new ArraySchema();

            ars.items = makeSchemaFromClass(clsx, data);

            return ars;
        } else if (cls == Array) {

            def ars = new ArraySchema();
            ars.items = new Schema();
            ars.items.set$ref(data['arrayType']);

            return ars;



        } else {
            log.error("Unknown parameter type ${cls}");
            throw new RuntimeException("Unknown parameter type ${cls}");
        }
    }

}


@Slf4j
class DSLMediaType
        extends DSLItem<MediaType> {

    DSLMediaType(DSLContext context) {
        super(context, new MediaType());
    }


}

@Slf4j
public class DSLInfo extends DSLItem<Info> {

    public DSLInfo(DSLContext context) {
        super(context, new Info());

    }

    public void contact(Closure c) {
        DSLContact child = new DSLContact(context);
        child.process(c);
        self.setContact(child.self);
    }

    public void extension(String name, Closure c) {
        DSLExtension child = new DSLExtension();
        child.process(c);

        if (this.self.getExtensions() == null)
            this.self.setExtensions(new HashMap<String, Object>());

        this.self.getExtensions().put('x-' + name, child)

    }


}

public class DSLExtension extends LinkedHashMap {

    public void process(Closure closure) {
        closure.delegate = this;

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
    }


    def propertyMissing(String name, Object value) {

        this.put(name, value);

    }

    public Object methodMissing(String name, Object args) {

        this.put(name, args);

        return item;
    }

}


@Slf4j
class DSLContact extends DSLItem<Contact> {
    public DSLContact(DSLContext context) {
        super(context, new Contact());
    }
}


