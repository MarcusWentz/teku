/*
 * Copyright 2022 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.infrastructure.restapi.openapi.request;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import tech.pegasys.teku.infrastructure.json.JsonUtil;
import tech.pegasys.teku.infrastructure.json.exceptions.MissingRequestBodyException;
import tech.pegasys.teku.infrastructure.json.types.DelegatingOpenApiTypeDefinition;
import tech.pegasys.teku.infrastructure.json.types.DeserializableTypeDefinition;
import tech.pegasys.teku.infrastructure.json.types.SerializableOneOfTypeDefinition;

public class OneOfJsonRequestContentTypeDefinition<T> extends DelegatingOpenApiTypeDefinition
    implements RequestContentTypeDefinition<T> {

  private final BodyTypeSelector<T> bodyTypeSelector;

  public OneOfJsonRequestContentTypeDefinition(
      final SerializableOneOfTypeDefinition<T> typeDefinition,
      final BodyTypeSelector<T> bodyTypeSelector) {
    super(typeDefinition);
    this.bodyTypeSelector = bodyTypeSelector;
  }

  @Override
  public T deserialize(final InputStream in) throws IOException {
    final String json = IOUtils.toString(in, StandardCharsets.UTF_8);
    final DeserializableTypeDefinition<? extends T> type = bodyTypeSelector.selectType(json);

    if (type == null) {
      throw new MissingRequestBodyException();
    }

    if (!openApiTypeDefinition.isEquivalentToDeserializableType(type)) {
      throw new IllegalStateException(
          "Schema determined for parsing request body is not listed in requestBodyTypes");
    }
    return JsonUtil.parse(json, type);
  }

  public interface BodyTypeSelector<T> {
    DeserializableTypeDefinition<? extends T> selectType(final String jsonContent);
  }
}
