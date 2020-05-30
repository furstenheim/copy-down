package io.github.furstenheim;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CopyDownTest {
    @ParameterizedTest
    @MethodSource("testCases")
    public void mainTest(String name, TestCase testCase) throws IOException {
        CopyDown copyDown;
        if (testCase.options.isJsonNull()) {
            copyDown = new CopyDown();
        } else {
            OptionsBuilder optionsBuilder = OptionsBuilder.anOptions();
            JsonObject options = testCase.options.getAsJsonObject();
            if (options.has("headingStyle") && options.get("headingStyle").getAsString().equals("atx")) {
                optionsBuilder.withHeadingStyle(HeadingStyle.ATX);
            }
            if (options.has("hr")) {
                optionsBuilder.withHr(options.get("hr").getAsString());
            }
            if (options.has("br")) {
                optionsBuilder.withBr(options.get("br").getAsString());
            }
            if (options.has("linkStyle") && options.get("linkStyle").getAsString().equals("referenced")) {
                optionsBuilder.withLinkStyle(LinkStyle.REFERENCED);
                if (options.has("linkReferenceStyle")) {
                    String linkReferenceStyle = options.get("linkReferenceStyle").getAsString();
                    if (linkReferenceStyle.equals("collapsed")) {
                        optionsBuilder.withLinkReferenceStyle(LinkReferenceStyle.COLLAPSED);
                    } else if (linkReferenceStyle.equals("shortcut")) {
                        optionsBuilder.withLinkReferenceStyle(LinkReferenceStyle.SHORTCUT);
                    }
                }
            }
            if (options.has("codeBlockStyle") && options.get("codeBlockStyle").getAsString().equals("fenced")) {
                optionsBuilder.withCodeBlockStyle(CodeBlockStyle.FENCED);
                if (options.has("fence")) {
                    optionsBuilder.withFence(options.get("fence").getAsString());
                }
            }
            if (options.has("bulletListMarker")) {
                optionsBuilder.withBulletListMaker(options.get("bulletListMarker").getAsString());
            }
            copyDown = new CopyDown(optionsBuilder.build());
        }
        String markdown = copyDown.convert(testCase.input);

        assertThat(markdown, equalTo(testCase.output));

    }

    public static Stream<Arguments> testCases () throws IOException {
        String jsonFile = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/tests.json")));
        JsonElement commandsAsJson = JsonParser.parseString(jsonFile);
        Type listType = new TypeToken<List<TestCase>>() {}.getType();

        List<TestCase> testCases = new Gson().fromJson(commandsAsJson, listType);
        return testCases.stream().map(tc -> Arguments.of(tc.name, tc));
    }
}
