/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.gradle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.openrewrite.Recipe
import org.openrewrite.properties.PropertiesRecipeTest
import org.openrewrite.yaml.YamlRecipeTest
import java.nio.file.Path

class UpdateImageTest : YamlRecipeTest {
    override val recipe: Recipe
        get() = UpdateImage("circleci/openjdk:jdk")

    @Test
    fun circleImage(@TempDir tempDir: Path) = assertChanged(
        before = tempDir.resolve(".circleci/config.yml").toFile().apply {
            parentFile.mkdirs()
            writeText(
                """
                    version: 2
                    jobs:
                      build:
                        machine:
                          image: ubuntu-1604:202007-01
                        branches:
                          ignore:
                            - gh-pages
                """.trimIndent()
            )
        },
        relativeTo = tempDir,
        after = """
            version: 2
            jobs:
              build:
                machine:
                  image: circleci/openjdk:jdk
                branches:
                  ignore:
                    - gh-pages
        """
    )
}
