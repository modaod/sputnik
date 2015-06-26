package pl.touk.sputnik.processor.sonar;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import pl.touk.sputnik.TestEnvironment;
import pl.touk.sputnik.configuration.Configuration;
import pl.touk.sputnik.configuration.ConfigurationSetup;
import pl.touk.sputnik.review.*;

public class SonarProcessorTest extends TestEnvironment {

    @Test
    public void shouldFilterResultFiles() {
        ReviewResult results = new ReviewResult();
        results.add(new Violation("src/t/f.cs", 0, "", Severity.ERROR));
        results.add(new Violation("src/t/f2.cs", 0, "", Severity.ERROR));
        results.add(new Violation("src/t/f3.cs", 0, "", Severity.ERROR));
        results.add(new Violation("src/t/f4.cs", 0, "", Severity.ERROR));

        ReviewFile r1 = new ReviewFile("src/t/f.cs");
        ReviewFile r2 = new ReviewFile("src/t/f2.cs");
        Review review = new Review(ImmutableList.of(r1, r2), new ReviewFormatter(config));

        ReviewResult filteredResults = new SonarProcessor(config).filterResults(results, review);
        assertThat(filteredResults.getViolations())
            .extracting("filenameOrJavaClassName")
            .containsExactly("src/t/f.cs", "src/t/f2.cs");
    }

    @Test
    public void shouldReportViolations() {
        SonarProcessor processor = new SonarProcessor(new SonarRunnerBuilder() {
            final Configuration configCopy = config;
            public SonarRunner prepareRunner(Review review, Configuration configuration) {
                return new SonarRunner(null, null, null) {
                    public File run() throws IOException {
                        return getResourceAsFile("json/sonar-result.json");
                    }
                };
            }
        }, config);
        ReviewResult result = processor.process(nonexistantReview("src/module2/dir/file2.cs"));
        assertThat(result.getViolations()).hasSize(3);
    }
}