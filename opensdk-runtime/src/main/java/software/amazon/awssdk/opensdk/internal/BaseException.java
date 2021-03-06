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

package software.amazon.awssdk.opensdk.internal;

import software.amazon.awssdk.annotation.SdkInternalApi;
import software.amazon.awssdk.opensdk.SdkErrorHttpMetadata;

/**
 * Interface to abstract the common methods that needs
 * to exist in all service exceptions.
 *
 * This is for internal use only.
 */
@SdkInternalApi
public interface BaseException {

    BaseException sdkHttpMetadata(SdkErrorHttpMetadata sdkHttpMetadata);

    SdkErrorHttpMetadata sdkHttpMetadata();

    String getMessage();

    void setMessage(String message);
}
