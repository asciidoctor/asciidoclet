import java.nio.file.Files
import java.nio.file.Path


def javadocExpectedPath = Path.of((String) basedir).resolve('target/site/apidocs')
def expectedJavadoc = javadocExpectedPath.resolve('example/StringUtils.html')

if (Files.list(javadocExpectedPath).count() == 0) {
    throw new Exception("${javadocExpectedPath.toFile().getAbsolutePath()} path cannot me empty")
}

def javadocContent = Files.readString(expectedJavadoc)

def expectClassDescription = Html.div(Html.p('Class comment for "Integration Test: generate Javadoc for class and method".'), 'block')
def expectMethodDescription = Html.div(Html.p('This is a method comment.'), 'block')

assertStringContains(javadocContent, expectClassDescription)
assertStringContains(javadocContent, expectMethodDescription)

def expectMethodArgument1 = Html.dd(Html.code('haystack') + " - the haystack")
def expectMethodArgument2 = Html.dd(Html.code('needle') + " - it stings")

assertStringContains(javadocContent, expectMethodArgument1)
assertStringContains(javadocContent, expectMethodArgument2)

def expectMethodReturn = Html.dt("Returns:") + "\n" + Html.dd('true if lucky')

assertStringContains(javadocContent, expectMethodReturn)


void assertStringContains(String value, String expected) {
    if (!value.contains(expected)) {
        throw new Exception("'$expected' expected to be present")
    }
}

class Html {

    static String div(String text, String classname) {
        return "<div class=\"${classname}\">${text}</div>"
    }

    static String span(String text, String classname) {
        return "<span class=\"${classname}\">${text}</span>"
    }

    static String p(String text) {
        return "<p>${text}</p>"
    }

    static String code(String text) {
        return "<code>${text}</code>"
    }

    static String dd(String text) {
        return "<dd>${text}</dd>"
    }

    static String dt(String text) {
        return "<dt>${text}</dt>"
    }
}

return true
