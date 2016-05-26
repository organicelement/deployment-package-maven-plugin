# OSGi Deployment packaging plugin for Maven

This plugin aids in creating .dp package files

## Usage
Simply create a pom with the dependencies you want included in the dp file.

## Example

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.redhat.iot</groupId>
  <artifactId>com.redhat.iot.demo</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>deployment-package</packaging>

  <dependencies>
    <dependency>
      <groupId>org.eclipse.kura</groupId>
      <artifactId>org.eclipse.kura.example.ble.tisensortag</artifactId>
      <version>1.0.2-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.kura</groupId>
      <artifactId>org.eclipse.kura.example.camel.quickstart</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.organicelement</groupId>
        <artifactId>deployment-package-maven-plugin</artifactId>
        <extensions>true</extensions>
        <version>1.0.0-SNAPSHOT</version>
        <configuration>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>
```

Licensing
=========
Licensed under the Apache License, Version 2.0. See
[LICENSE](https://github.com/organicelement/deployment-package-maven-plugin/blob/master/LICENSE) for the full
license text.

