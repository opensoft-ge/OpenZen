# Third-party notices

OpenZen's original application code is licensed under the [Apache License 2.0](LICENSE). Third-party components retain their own licences and notices.

## Maven Wrapper

The Apache Maven Wrapper 3.3.4 scripts retain their original headers. The [root Apache licence](LICENSE) and [upstream Wrapper notice](licenses/maven-wrapper-NOTICE.txt) accompany them. The notice comes from the Apache Maven Wrapper `maven-wrapper-3.3.4` release. Maven distribution details are pinned in `.mvn/wrapper/maven-wrapper.properties`.

## Dependencies and binary distributions

Runtime dependencies are declared in `pom.xml` and resolved from Maven repositories. Spring Boot packages dependency JARs with their own metadata and available notices. The project licence does not replace dependency licences. Distributors must preserve applicable notices and fulfil the terms of each included component. Container base images retain their separate licences.

The application packages its own licence and notices under `META-INF/openzen/`. This notice is not an exhaustive dependency licence audit.
