package com.reedelk.json.component;

import com.reedelk.json.internal.ObjectToJSONConverter;
import com.reedelk.json.internal.commons.Defaults;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Optional;

@ModuleComponent("Object to JSON")
@Description("Converts a Java Object into a JSON string. " +
        "A Java List is mapped to a JSON Array and a Java Map is mapped to a Java Map. " +
        "Any other Java object is mapped using getters. " +
        "If pretty print is set to true, the output JSON is pretty printed using the given indent factor which " +
        "adds a number of spaces to each level of indentation.")
@Component(service = ObjectToJSON.class, scope = ServiceScope.PROTOTYPE)
public class ObjectToJSON implements ProcessorSync {

    @Property("Pretty print")
    @Example("true")
    @InitValue("true")
    @DefaultValue("false")
    @Description("If true the output JSON is pretty printed using the given indent factor.")
    private Boolean prettyPrint;

    @Property("Indent factor")
    @Example("2")
    @DefaultValue("2")
    @Description("The number of spaces to add to each level of indentation. " +
            "If indent factor is 0 JSON object has only one key, " +
            " then the object will be output on a single line: <code>{ {\"key\": 1}}</code>")
    @When(propertyName = "prettyPrint", propertyValue = "true")
    private Integer indentFactor;

    @Reference
    ConverterService converterService;

    private int theIndentFactor;
    private boolean isPrettyPrint;
    private ObjectToJSONConverter converter;

    @Override
    public void initialize() {
        isPrettyPrint = Optional.ofNullable(prettyPrint).orElse(Defaults.PRETTY);
        theIndentFactor = Optional.ofNullable(indentFactor).orElse(Defaults.INDENT_FACTOR);
        converter = new ObjectToJSONConverter(converterService);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        Object payload = message.payload();
        if (payload == null) {
            // The payload was null, we return an empty message.
            return MessageBuilder.get(JSONToObject.class)
                    .empty()
                    .build();
        }

        Object result = converter.toJSON(payload);

        String json = null;

        if (result instanceof JSONObject) {
            JSONObject outObject = (JSONObject) result;
            json = isPrettyPrint ?
                    outObject.toString(theIndentFactor) :
                    outObject.toString();

        } else if (result instanceof JSONArray) {
            JSONArray outArray = (JSONArray) result;
            json = isPrettyPrint ?
                    outArray.toString(theIndentFactor) :
                    outArray.toString();
        }

        return MessageBuilder.get(ObjectToJSON.class)
                .withJson(json)
                .build();
    }

    public void setPrettyPrint(Boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void setIndentFactor(Integer indentFactor) {
        this.indentFactor = indentFactor;
    }
}