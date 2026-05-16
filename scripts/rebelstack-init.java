///usr/bin/env jbang "$0" "$@" ; exit $?

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class rebelstack_init {
    private static final List<String> ALL_COMPONENTS = List.of("spring-web", "j2html", "j2css", "htmx", "deploy4j");

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            printUsage();
            return;
        }

        Path pomPath = Path.of("pom.xml");
        if (!Files.exists(pomPath)) {
            System.out.println("No pom.xml found in the current directory.");
            System.out.println("Run this command from the root of an existing Maven project.");
            System.exit(1);
        }

        Set<String> requested = parseRequestedComponents(args);
        if (requested == null) {
            printUsage();
            System.exit(1);
            return;
        }

        String pom = Files.readString(pomPath);
        if (!pom.contains("<project")) {
            System.out.println("The pom.xml file does not look like a valid Maven POM.");
            System.exit(1);
        }

        List<String> added = new ArrayList<>();
        boolean needsJitpack = requested.contains("j2css") || requested.contains("deploy4j");
        if (needsJitpack && !hasRepository(pom, "jitpack.io")) {
            pom = ensureRepositoriesSection(pom);
            pom = addRepository(pom,
                    "jitpack.io",
                    "https://jitpack.io");
            added.add("repository: jitpack.io");
        }

        if (requested.contains("spring-web")) {
            Dependency dep = new Dependency("org.springframework.boot", "spring-boot-starter-web", null);
            if (!hasDependency(pom, dep.groupId(), dep.artifactId())) {
                pom = ensureDependenciesSection(pom);
                pom = addDependency(pom, dep);
                added.add("dependency: org.springframework.boot:spring-boot-starter-web");
            }
        }

        if (requested.contains("j2html")) {
            Dependency dep = new Dependency("com.j2html", "j2html", "1.6.0");
            if (!hasDependency(pom, dep.groupId(), dep.artifactId())) {
                pom = ensureDependenciesSection(pom);
                pom = addDependency(pom, dep);
                added.add("dependency: com.j2html:j2html:1.6.0");
            }
        }

        if (requested.contains("j2css")) {
            Dependency dep = new Dependency("io.github.teggr", "j2css", "0.1.0-SNAPSHOT");
            if (!hasDependency(pom, dep.groupId(), dep.artifactId())) {
                pom = ensureDependenciesSection(pom);
                pom = addDependency(pom, dep);
                added.add("dependency: io.github.teggr:j2css:0.1.0-SNAPSHOT");
            }
        }

        if (requested.contains("htmx")) {
            Dependency dep = new Dependency("org.webjars.npm", "htmx.org", "2.0.4");
            if (!hasDependency(pom, dep.groupId(), dep.artifactId())) {
                pom = ensureDependenciesSection(pom);
                pom = addDependency(pom, dep);
                added.add("dependency: org.webjars.npm:htmx.org:2.0.4");
            }
        }

        if (requested.contains("deploy4j")) {
            Plugin plugin = new Plugin("com.github.teggr.deploy4j", "deploy4j-maven-plugin", "0.0.1");
            if (!hasPlugin(pom, plugin.groupId(), plugin.artifactId())) {
                pom = ensureBuildPluginsSection(pom);
                pom = addPlugin(pom, plugin);
                added.add("plugin: com.github.teggr.deploy4j:deploy4j-maven-plugin:0.0.1");
            }
        }

        if (added.isEmpty()) {
            System.out.println("No changes made. Requested components are already present in pom.xml.");
            return;
        }

        Files.writeString(pomPath, pom);

        System.out.println("Updated pom.xml with Rebelstack components:");
        for (String item : added) {
            System.out.println("  - " + item);
        }
        System.out.println("\nNext steps:");
        System.out.println("  mvn -q dependency:tree");
        System.out.println("  mvn test");
    }

    private static void printUsage() {
        System.out.println("Usage: jbang scripts/rebelstack-init.java [component ...]");
        System.out.println("\nRuns in the current Maven project and updates pom.xml in place.");
        System.out.println("If no components are provided, installs all Rebelstack components.");
        System.out.println("\nAvailable components:");
        System.out.println("  spring-web  -> org.springframework.boot:spring-boot-starter-web");
        System.out.println("  j2html      -> com.j2html:j2html:1.6.0");
        System.out.println("  j2css       -> io.github.teggr:j2css:0.1.0-SNAPSHOT");
        System.out.println("  htmx        -> org.webjars.npm:htmx.org:2.0.4");
        System.out.println("  deploy4j    -> com.github.teggr.deploy4j:deploy4j-maven-plugin:0.0.1");
        System.out.println("  all         -> install everything above");
    }

    private static Set<String> parseRequestedComponents(String[] args) {
        Set<String> requested = new LinkedHashSet<>();
        if (args.length == 0) {
            requested.addAll(ALL_COMPONENTS);
            return requested;
        }

        for (String arg : args) {
            String name = arg.toLowerCase(Locale.ROOT).trim();
            if ("all".equals(name)) {
                requested.clear();
                requested.addAll(ALL_COMPONENTS);
                continue;
            }
            if (!ALL_COMPONENTS.contains(name)) {
                System.out.println("Unknown component: " + arg);
                return null;
            }
            requested.add(name);
        }
        return requested;
    }

    private static boolean hasRepository(String pom, String repositoryId) {
        return pom.contains("<id>" + repositoryId + "</id>");
    }

    private static boolean hasDependency(String pom, String groupId, String artifactId) {
        return hasCoordinate(pom, groupId, artifactId);
    }

    private static boolean hasPlugin(String pom, String groupId, String artifactId) {
        return hasCoordinate(pom, groupId, artifactId);
    }

    private static boolean hasCoordinate(String xml, String groupId, String artifactId) {
        String groupTag = "<groupId>" + groupId + "</groupId>";
        String artifactTag = "<artifactId>" + artifactId + "</artifactId>";
        int artifactIndex = xml.indexOf(artifactTag);
        while (artifactIndex >= 0) {
            int start = Math.max(0, artifactIndex - 500);
            String window = xml.substring(start, artifactIndex + artifactTag.length());
            if (window.contains(groupTag)) {
                return true;
            }
            artifactIndex = xml.indexOf(artifactTag, artifactIndex + artifactTag.length());
        }
        return false;
    }

    private static String ensureRepositoriesSection(String pom) {
        if (pom.contains("<repositories>")) {
            return pom;
        }
        String section = """
                    <repositories>
                    </repositories>

                """;
        return insertBeforePreferred(pom, section, "<dependencies>", "<build>", "</project>");
    }

    private static String ensureDependenciesSection(String pom) {
        if (pom.contains("<dependencies>")) {
            return pom;
        }
        String section = """
                    <dependencies>
                    </dependencies>

                """;
        return insertBeforePreferred(pom, section, "<build>", "</project>");
    }

    private static String ensureBuildPluginsSection(String pom) {
        if (pom.contains("<plugins>")) {
            return pom;
        }

        if (pom.contains("<build>")) {
            String plugins = """
                            <plugins>
                            </plugins>
                """;
            return insertBefore(pom, "</build>", plugins);
        }

        String build = """
                    <build>
                        <plugins>
                        </plugins>
                    </build>

                """;
        return insertBeforePreferred(pom, build, "</project>");
    }

    private static String addRepository(String pom, String id, String url) {
        String repository = """
                        <repository>
                            <id>%s</id>
                            <url>%s</url>
                        </repository>
                """.formatted(id, url);
        return insertBefore(pom, "</repositories>", repository);
    }

    private static String addDependency(String pom, Dependency dep) {
        String versionLine = dep.version() == null ? "" : "\n            <version>" + dep.version() + "</version>";
        String dependency = """
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s</artifactId>%s
                        </dependency>
                """.formatted(dep.groupId(), dep.artifactId(), versionLine);
        return insertBefore(pom, "</dependencies>", dependency);
    }

    private static String addPlugin(String pom, Plugin plugin) {
        String versionLine = plugin.version() == null ? "" : "\n                    <version>" + plugin.version() + "</version>";
        String pluginXml = """
                                <plugin>
                                    <groupId>%s</groupId>
                                    <artifactId>%s</artifactId>%s
                                </plugin>
                """.formatted(plugin.groupId(), plugin.artifactId(), versionLine);
        return insertBefore(pom, "</plugins>", pluginXml);
    }

    private static String insertBeforePreferred(String xml, String content, String... markers) {
        for (String marker : markers) {
            int index = xml.indexOf(marker);
            if (index >= 0) {
                return xml.substring(0, index) + content + xml.substring(index);
            }
        }
        return xml + System.lineSeparator() + content;
    }

    private static String insertBefore(String xml, String marker, String content) {
        int index = xml.indexOf(marker);
        if (index < 0) {
            return xml;
        }
        return xml.substring(0, index) + content + xml.substring(index);
    }

    private record Dependency(String groupId, String artifactId, String version) {
    }

    private record Plugin(String groupId, String artifactId, String version) {
    }
}
