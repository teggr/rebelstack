///usr/bin/env jbang "$0" "$@" ; exit $?

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class rebelstack_init {
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: jbang scripts/rebelstack-init.java <artifactId> [groupId]");
            System.exit(1);
        }

        String artifactId = args[0];
        String groupId = args.length > 1 ? args[1] : "com.example";
        String packageName = groupId + "." + artifactId.replace('-', '.');

        Path projectDir = Path.of(artifactId);
        Path packagePath = Path.of("src", "main", "java", packageName.replace('.', '/'));

        Files.createDirectories(projectDir.resolve(packagePath));
        Files.createDirectories(projectDir.resolve("src/main/resources/static"));

        String appClass = capitalize(artifactId.replaceAll("[^a-zA-Z0-9]", "")) + "Application";

        write(projectDir.resolve("pom.xml"), pom(groupId, artifactId));
        write(projectDir.resolve(packagePath).resolve(appClass + ".java"), appJava(packageName, appClass));
        write(projectDir.resolve("src/main/resources/static/index.html"), html());

        System.out.println("Created Rebelstack project in ./'" + artifactId + "'");
        System.out.println("Next steps:");
        System.out.println("  cd " + artifactId);
        System.out.println("  mvn spring-boot:run");
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    private static String pom(String groupId, String artifactId) {
        return """
                <project xmlns=\"http://maven.apache.org/POM/4.0.0\"
                         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
                         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.3.5</version>
                        <relativePath/>
                    </parent>

                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <name>%s</name>
                    <description>Rebelstack web application</description>

                    <properties>
                        <java.version>21</java.version>
                    </properties>

                    <repositories>
                        <repository>
                            <id>jitpack.io</id>
                            <url>https://jitpack.io</url>
                        </repository>
                    </repositories>

                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>com.j2html</groupId>
                            <artifactId>j2html</artifactId>
                            <version>1.6.0</version>
                        </dependency>
                        <dependency>
                            <groupId>io.github.teggr</groupId>
                            <artifactId>j2css</artifactId>
                            <version>0.1.0-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>org.webjars.npm</groupId>
                            <artifactId>htmx.org</artifactId>
                            <version>2.0.4</version>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                            <plugin>
                                <groupId>com.github.teggr.deploy4j</groupId>
                                <artifactId>deploy4j-maven-plugin</artifactId>
                                <version>0.0.1</version>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(groupId, artifactId, artifactId);
    }

    private static String appJava(String packageName, String appClass) {
        return """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class %s {

                    public static void main(String[] args) {
                        SpringApplication.run(%s.class, args);
                    }
                }
                """.formatted(packageName, appClass, appClass);
    }

    private static String html() {
        return """
                <!doctype html>
                <html lang=\"en\">
                <head>
                    <meta charset=\"utf-8\">
                    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
                    <title>Rebelstack App</title>
                </head>
                <body>
                    <main>
                        <h1>Welcome to Rebelstack</h1>
                        <p>Your project was created successfully.</p>
                    </main>
                </body>
                </html>
                """;
    }

    private static String capitalize(String value) {
        if (value.isEmpty()) {
            return "Rebelstack";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
