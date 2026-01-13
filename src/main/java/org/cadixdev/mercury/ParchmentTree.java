package org.cadixdev.mercury;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class ParchmentTree {

    private final Map<String, Class> tree;

    private ParchmentTree(Map<String, Class> tree) {
        this.tree = tree;
    }

    private static Optional<Javadoc> parseJavadoc(final JsonObject parent) {
        if (!parent.has("javadoc")) {
            return Optional.empty();
        }
        return Optional.of(new Javadoc(
                parent.get("javadoc").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()
        ));
    }

    private static Map<Integer, Parameter> parseParameters(final JsonArray array) {
        return array.asList().stream().map(JsonElement::getAsJsonObject).map(obj -> Map.entry(
                obj.get("index").getAsInt(),
                new Parameter(
                        obj.get("name").getAsString(),
                        parseJavadoc(obj)
                )
        )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<Pair, Method> parseMethods(final JsonArray array) {
        return array.asList().stream().map(JsonElement::getAsJsonObject).map((obj) -> Map.entry(
                new Pair(obj.get("name").getAsString(), obj.get("descriptor").getAsString()),
                new Method(
                        !obj.has("parameters") ? Map.of() : parseParameters(obj.get("parameters").getAsJsonArray()),
                        parseJavadoc(obj))
        )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<Pair, Field> parseFields(final JsonArray array) {
        return array.asList().stream().map(JsonElement::getAsJsonObject).map((obj) -> Map.entry(
                new Pair(obj.get("name").getAsString(), obj.get("descriptor").getAsString()),
                new Field(parseJavadoc(obj))
        )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Class parseClass(JsonObject obj) {
        return new Class(
                !obj.has("methods") ? Map.of() : parseMethods(obj.getAsJsonArray("methods")),
                !obj.has("fields") ? Map.of() : parseFields(obj.getAsJsonArray("fields"))
        );
    }

    private static Map<String, Class> parseClasses(JsonArray array) {

        return array.asList().stream().map(JsonElement::getAsJsonObject).map((obj) -> Map.entry(
                obj.get("name").getAsString(),
                parseClass(obj)
        )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static ParchmentTree loadFile(final Path path) throws IOException {
        var json = new JsonParser().parse(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8));
        return fromJson(json);
    }
    public static ParchmentTree fromJson(JsonElement node) {
        var classes = node.getAsJsonObject().getAsJsonArray("classes");
        if (classes == null) throw new RuntimeException("Not a valid parchment json");

        return new ParchmentTree(parseClasses(classes));
    }



    @Nullable
    public Method getMethod(String className, String methodName, String descriptor) {
        var clazz = tree.get(className);
        if (clazz == null) return null;

        return clazz.methods.get(new Pair(methodName, descriptor));
    }
    @Nullable
    public Field getField(String className, String fieldName, String descriptor) {
        var clazz = tree.get(className);
        if (clazz == null) return null;
        
        return clazz.fields.get(new Pair(fieldName, descriptor));
    }


    public record Pair(String name, String desc) {
    }

    public record Javadoc(List<String> data) {
    }

    public record Parameter(String name, Optional<Javadoc> javadoc) {
    }

    public record Method(Map<Integer, Parameter> parameters, Optional<Javadoc> javadoc) {
    }

    public record Field(Optional<Javadoc> javadoc) {
    }

    public record Class(Map<Pair, Method> methods, Map<Pair, Field> fields) {

    }

}
