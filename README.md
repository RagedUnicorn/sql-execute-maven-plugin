# sql-execute-maven-plugin

> A maven plugin for executing single sql queries or sql files on a database

[![Maven Central](https://img.shields.io/maven-central/v/com.ragedunicorn.tools.maven/sql-execute-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.ragedunicorn.tools.maven%22%20AND%20a:%22sql-execute-maven-plugin%22)

# Usage

The below example is using MariaDB as an example. The plugin however supports also MySQL and PostgreSQL. See example module for more details.

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>com.ragedunicorn.tools.maven</groupId>
        <artifactId>sql-execute-maven-plugin</artifactId>
        <version>[version]</version>
        <dependencies>
          <!-- mariadbdriver -->
          <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
            <version>[version]</version>
          </dependency>
        </dependencies>
        <configuration>
          <driver>org.mariadb.jdbc</driver>
          <url>jdbc:mariadb://localhost:3306/</url>
          <server>[server]</server>
        </configuration>
        <executions>
          <execution>
            <id>execute-query</id>
            <phase>install</phase>
            <goals>
              <goal>sql-execute</goal>
            </goals>
            <configuration>
              <sqlQuery>DROP SCHEMA IF EXISTS world_test</sqlQuery>
            </configuration>
          </execution>
          <execution>
            <id>execute-file</id>
            <phase>install</phase>
            <goals>
              <goal>sql-execute</goal>
            </goals>
            <configuration>
              <sqlFiles>
                <sqlFiles>src/main/resources/[file].sql</sqlFiles>
              </sqlFiles>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

| Parameter | Required | Default Value | Description                                                                                                   |
|-----------|----------|---------------|---------------------------------------------------------------------------------------------------------------|
| driver    | true     | <>            | The database driver to use                                                                                    |
| url       | true     | <>            | Connection string for the database                                                                            |
| user      | false    | <>            | User to use to connect to the database - overwritten by server                                                |
| password  | false    | <>            | Password to use to connect to the database - overwritten by server                                            |
| server    | false    | <>            | References a server configuration in your .m2 settings.xml. This is the preferred way for storing credentials |
| sqlQuery  | false    | <>            | An sql query to execute                                                                                       |
| sqlFiles  | false    | <>            | A list of one or more sql files to execute                                                                    |


### Driver Dependencies

##### MariaDB

```xml
<dependency>
  <groupId>org.mariadb.jdbc</groupId>
  <artifactId>mariadb-java-client</artifactId>
  <version>[version]</version>
</dependency>
```

##### MySQL

```xml
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>[version]</version>
</dependency>
```

##### PostgreSQL

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <version>[version]</version>
</dependency>
```

##### Credentials

The plugin allows to either add database credentials directly into the plugin configuration or storing the in your maven `settings.xml`. The later should usually be preferred.

In `settings.xml` add a new server entry.

```xml
<server>
  <id>some-database</id>
  <username>app</username>
  <password>app</password>
</server>
```

Then in the plugin configuration reference the server.

```xml
<configuration>
  [...]
  <server>somedatabase</server>
  [...]
</configuration>
```

Or add the credentials directly into the plugin configuration.

```xml
<configuration>
  [...]
  <user>app</user>
  <password>app</password>
  [...]
</configuration>
```

## Development

##### IntelliJ Run Configurations

The project contains IntelliJ run configurations that can be used for most tasks. Create a folder `runConfigurations` inside the `.idea` folder and copy over all run configurations.

##### Build Project

sql-execute-maven-plugin

```
clean install
```

#### Create a Release

This project has GitHub action profiles for different Devops related work such as deployments to different places. See .github folder for details.
The project is deployed to three different places. Each deployment has its own Maven profile for configuration.

##### GitHub Release

`.github/workflows/github_release.yaml` - Creates a tag and release on GitHub

##### GitHub Package Release

`.github/workflows/github_package_release.yaml` - Releases a package on GitHub

##### OSSRH Package Release

`.github/workflows/ossrh_package_release.yaml` - Releases a package on OSSRH (Sonatype)

All steps are required to make a full release of the plugin but can be done independently of each other. The workflows have to be manually invoked on GitHub.

##### Docker

This project contains docker-compose files for testing and trying out for all supported databases. When using the `dev` variant of the docker-compose files you have to connect to the container and manually start the process by running the `docker-entrypoint.sh` script.

To start the containers either use the command line or the prepared docker run configurations if you are using intellij.

```
docker-compose -f [docker-compose-file] -d
```

**Note:** The MySQL and MariaDB container use the same ports and thus cannot be run at the same time.

## License

Copyright (c) 2020 Michael Wiesendanger

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
