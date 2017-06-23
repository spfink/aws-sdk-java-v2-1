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

package software.amazon.awssdk.opensdk;

import software.amazon.awssdk.annotation.SdkProtectedApi;

/**
 * Base type for all response types.
 */
public abstract class BaseResult {

    private SdkResponseMetadata sdkResponseMetadata;

    /**
     * @return Metadata from the successful response. Mostly used for debugging purposes.
     */
    public SdkResponseMetadata sdkResponseMetadata() {
        return sdkResponseMetadata;
    }

    @SdkProtectedApi
    public BaseResult sdkResponseMetadata(
            SdkResponseMetadata sdkResponseMetadata) {
        this.sdkResponseMetadata = sdkResponseMetadata;
        return this;
    }
}