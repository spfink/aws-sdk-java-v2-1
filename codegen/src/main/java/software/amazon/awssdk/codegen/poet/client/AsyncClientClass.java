/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.codegen.poet.client;

import static com.squareup.javapoet.TypeSpec.Builder;
import static software.amazon.awssdk.codegen.poet.client.SyncClientClass.getProtocolSpecs;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import software.amazon.awssdk.annotation.SdkInternalApi;
import software.amazon.awssdk.client.AsyncClientHandler;
import software.amazon.awssdk.client.AwsAsyncClientParams;
import software.amazon.awssdk.client.ClientHandlerParams;
import software.amazon.awssdk.client.SdkAsyncClientHandler;
import software.amazon.awssdk.codegen.emitters.GeneratorTaskParams;
import software.amazon.awssdk.codegen.model.intermediate.OperationModel;
import software.amazon.awssdk.codegen.poet.PoetExtensions;
import software.amazon.awssdk.codegen.poet.PoetUtils;
import software.amazon.awssdk.codegen.poet.client.specs.ProtocolSpec;

public final class AsyncClientClass extends AsyncClientInterface {
    private final PoetExtensions poetExtensions;
    private final ClassName className;
    private final ProtocolSpec protocolSpec;
    private final String basePackage;

    public AsyncClientClass(GeneratorTaskParams dependencies) {
        super(dependencies.getModel());
        this.poetExtensions = dependencies.getPoetExtensions();
        this.className = poetExtensions.getClientClass(model.getMetadata().getAsyncClient());
        this.protocolSpec = getProtocolSpecs(poetExtensions, model.getMetadata().getProtocol());
        this.basePackage = dependencies.getModel().getMetadata().getFullClientPackageName();
    }

    @Override
    public TypeSpec poetSpec() {
        ClassName interfaceClass = poetExtensions.getClientClass(model.getMetadata().getAsyncInterface());
        Builder classBuilder = PoetUtils.createClassBuilder(className)
                                        .addAnnotation(SdkInternalApi.class)
                                        .addModifiers(Modifier.FINAL)
                                        .addField(AsyncClientHandler.class, "clientHandler", Modifier.PRIVATE, Modifier.FINAL)
                                        .addField(protocolSpec.protocolFactory(model))
                                        .addSuperinterface(interfaceClass)
                                        .addJavadoc("Internal implementation of {@link $1T}.\n\n@see $1T#builder()",
                                                    interfaceClass)
                                        .addMethod(constructor())
                                        .addMethods(operations())
                                        .addMethod(closeMethod())
                                        .addMethods(protocolSpec.additionalMethods())
                                        .addMethod(protocolSpec.initProtocolFactory(model));
        protocolSpec.createErrorResponseHandler().ifPresent(classBuilder::addMethod);

        if (model.getCustomizationConfig().getServiceSpecificClientConfigClass() != null) {
            classBuilder.addMethod(constructorWithAdvancedConfiguration());
        }

        return classBuilder.build();
    }

    private MethodSpec constructor() {
        if (model.getCustomizationConfig().getServiceSpecificClientConfigClass() != null) {
            return MethodSpec.constructorBuilder()
                             .addParameter(AwsAsyncClientParams.class, "asyncClientParams")
                             .addStatement("this($N, null)", "asyncClientParams")
                             .build();
        }
        return MethodSpec.constructorBuilder()
                         .addModifiers(Modifier.PROTECTED)
                         .addParameter(AwsAsyncClientParams.class, "clientParams")
                         .addStatement(
                                 "this.$N = new $T(new $T()\n" +
                                 ".withAsyncClientParams($N)\n" +
                                 ".withClientParams($N)\n" +
                                 ".withCalculateCrc32FromCompressedDataEnabled($L))",
                                 "clientHandler",
                                 // TODO this will likely differ for APIG clients
                                 SdkAsyncClientHandler.class,
                                 ClientHandlerParams.class,
                                 "clientParams",
                                 "clientParams",
                                 model.getCustomizationConfig().isCalculateCrc32FromCompressedData())
                         .addStatement("this.$N = init()", protocolSpec.protocolFactory(model).name)
                         .build();
    }

    private MethodSpec constructorWithAdvancedConfiguration() {
        ClassName advancedConfiguration = ClassName.get(basePackage,
                                                        model.getCustomizationConfig().getServiceSpecificClientConfigClass());
        return MethodSpec.constructorBuilder()
                         .addModifiers(Modifier.PROTECTED)
                         .addParameter(AwsAsyncClientParams.class, "clientParams")
                         .addParameter(advancedConfiguration, "advancedConfiguration")
                         .addStatement(
                                 "this.$N = new $T(new $T()\n" +
                                 ".withAsyncClientParams($N)\n" +
                                 ".withClientParams($N)\n" +
                                 ".withCalculateCrc32FromCompressedDataEnabled($L)" +
                                 ".withServiceAdvancedConfiguration(advancedConfiguration))",
                                 "clientHandler",
                                 // TODO this will likely differ for APIG clients
                                 SdkAsyncClientHandler.class,
                                 ClientHandlerParams.class,
                                 "clientParams",
                                 "clientParams",
                                 model.getCustomizationConfig().isCalculateCrc32FromCompressedData())
                         .addStatement("this.$N = init()", protocolSpec.protocolFactory(model).name)
                         .build();
    }

    private MethodSpec closeMethod() {
        return MethodSpec.methodBuilder("close")
                         .addAnnotation(Override.class)
                         .addException(Exception.class)
                         .addModifiers(Modifier.PUBLIC)
                         .addStatement("$N.close()", "clientHandler")
                         .build();
    }

    @Override
    protected MethodSpec.Builder operationBody(MethodSpec.Builder builder, OperationModel opModel) {
        return builder
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addCode(protocolSpec.asyncResponseHandler(opModel))
                .addCode(protocolSpec.errorResponseHandler(opModel))
                .addCode(protocolSpec.asyncExecutionHandler(opModel));
    }

    @Override
    public ClassName className() {
        return className;
    }
}