package org.asciidoctor.asciidoclet;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AsciidoctorOptionsFactoryTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();
    private StubReporter reporter = new StubReporter();

    private AsciidoctorOptionsFactory optionsFactory;

    @BeforeEach
    void setup() {
        optionsFactory = new AsciidoctorOptionsFactory(asciidoctor, reporter);
    }

    @Test
    void createsDefaultOptions() {
        DocletOptions docletOptions = new DocletOptions(reporter);

        Options options = optionsFactory.create(docletOptions, null);

        Map<String, Object> optionsMap = options.map();
        assertContainsDefaultOptions(optionsMap, 3);

        Map<String, Object> attributes = (Map<String, Object>) optionsMap.get("attributes");
        assertContainsDefaultAttributes(attributes, 11);
    }

    @Test
    void withCustomAttributes() {
        DocletOptions docletOptions = Mockito.mock(DocletOptions.class);
        Mockito.when(docletOptions.attributes()).thenReturn(List.of("my-attribute=my-value", "another=42"));

        Options options = optionsFactory.create(docletOptions, null);

        Map<String, Object> optionsMap = options.map();
        assertContainsDefaultOptions(optionsMap, 3);
        assertThat(optionsMap).hasSize(3);

        Map<String, Object> attributes = (Map<String, Object>) optionsMap.get("attributes");
        assertContainsDefaultAttributes(attributes, 13);
        assertThat(attributes)
                .containsEntry("my-attribute", "my-value")
                .containsEntry("another", "42");
    }

    @Test
    void withCustomBaseDir() {
        DocletOptions docletOptions = Mockito.mock(DocletOptions.class);
        File baseDir = new File("some/path");
        Mockito.when(docletOptions.baseDir()).thenReturn(Optional.of(baseDir));

        Options options = optionsFactory.create(docletOptions, null);

        Map<String, Object> optionsMap = options.map();
        assertContainsDefaultOptions(optionsMap, 4);
        assertThat(optionsMap)
                .containsEntry("base_dir", baseDir.getAbsolutePath());

        Map<String, Object> attributes = (Map<String, Object>) optionsMap.get("attributes");
        assertContainsDefaultAttributes(attributes, 11);
    }

    @Test
    void withTemplateDir() {
        DocletOptions docletOptions = new DocletOptions(reporter);
        OutputTemplates outputTemplates = OutputTemplates.create(reporter);
        assertThat(outputTemplates.templateDir()).isNotEmptyDirectory();

        Options options = optionsFactory.create(docletOptions, outputTemplates);

        Map<String, Object> optionsMap = options.map();
        assertContainsDefaultOptions(optionsMap, 4);
        assertThat(optionsMap)
                .containsEntry("template_dirs", List.of(outputTemplates.templateDir().toString()));

        Map<String, Object> attributes = (Map<String, Object>) optionsMap.get("attributes");
        assertContainsDefaultAttributes(attributes, 11);
    }

    @Test
    void withRequires() {
        DocletOptions mock = Mockito.mock(DocletOptions.class);
        // Use gems available in the classpath to avoid errors
        Mockito.when(mock.requires()).thenReturn(List.of("asciidoctor", "coderay"));

        Options options = optionsFactory.create(mock, null);

        Map<String, Object> optionsMap = options.map();
        assertContainsDefaultOptions(optionsMap, 3);
        assertThat(optionsMap).hasSize(3);

        Map<String, Object> attributes = (Map<String, Object>) optionsMap.get("attributes");
        assertContainsDefaultAttributes(attributes, 11);
    }

    private static void assertContainsDefaultOptions(Map<String, Object> options, int size) {
        assertThat(options)
                .containsEntry("backend", "html5")
                .containsEntry("safe", SafeMode.SAFE.getLevel())
                .containsKey("attributes");
        assertThat(options).hasSize(size);
    }

    private static void assertContainsDefaultAttributes(Map<String, Object> attributes, int size) {
        assertThat(attributes)
                .containsEntry("at", "&#64;")
                .containsEntry("slash", "/")
                .containsEntry("icons", null)
                .containsEntry("idprefix", "")
                .containsEntry("idseparator", "-")
                .containsEntry("javadoc", "")
                .containsEntry("showtitle", true)
                .containsEntry("source-highlighter", "coderay")
                .containsEntry("coderay-css", "class")
                .containsEntry("env-asciidoclet", "")
                .containsEntry("env", "asciidoclet");
        assertThat(attributes).hasSize(size);
    }

}
