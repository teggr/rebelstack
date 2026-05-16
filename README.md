# rebelstack

<https://github.com/teggr/rebelstack>

Will be publishing to <https://rebelstack.sh>

Libraries and frameworks:

- Java 21+
- Spring Boot 4.x
- j2html - <https://github.com/tipsy/j2html>
  - j2html-extensions - <https://github.com/teggr/j2html-extensions>
- j2css - <https://github.com/teggr/j2css>
- deploy4j - <https://github.com/teggr/deploy4j>
- htmx - <https://htmx.org/>
  - spring boot htmx - <https://github.com/wimdeblauwe/htmx-spring-boot>

The rebelstack project is really the published vision of how all these frameworks can be brought together in order to build and publish Java applications with rapid speed assisted by ai.

The project is responsbile for providing tooling to tie them together in the published vision.

- Website source: [/docs](/docs)
- Homepage: [/docs/index.html](/docs/index.html)
- Documentation: [/docs/documentation.html](/docs/documentation.html)
- JBang project initializer: [/scripts/rebelstack-init.java](/scripts/rebelstack-init.java)
- JBang catalog: [/jbang-catalog.json](/jbang-catalog.json)

## Install the JBang Catalog

Add the Rebelstack catalog to your local JBang configuration:

```bash
jbang catalog add --name rebelstack https://raw.githubusercontent.com/teggr/rebelstack/main/jbang-catalog.json
```

You can verify it was added:

```bash
jbang catalog list
```

## Run the Project Init via JBang

After the catalog is installed, run the initializer alias:

```bash
jbang rebelstack-init@rebelstack
```

This executes [`scripts/rebelstack-init.java`](scripts/rebelstack-init.java) from the installed catalog.
