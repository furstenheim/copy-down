package io.github.furstenheim;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
        String markdown = new CopyDown().convert(testCase.input);
        assertThat(markdown, equalTo(testCase.output));

    }

    public static Stream<Arguments> testCases () throws IOException {
        String jsonFile = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/tests.json")));
        JsonElement commandsAsJson = JsonParser.parseString(jsonFile);
        Type listType = new TypeToken<List<TestCase>>() {}.getType();

        List<TestCase> testCases = new Gson().fromJson(commandsAsJson, listType);
        int i = 1;
        return testCases.subList(i, i + 1).stream().map(tc -> Arguments.of(tc.name, tc));
    }

}
