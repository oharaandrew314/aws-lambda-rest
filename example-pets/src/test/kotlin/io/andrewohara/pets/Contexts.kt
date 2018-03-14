package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger

object Contexts {

    val testContext = object: Context {
        override fun getAwsRequestId() = throw UnsupportedOperationException()
        override fun getLogGroupName() = throw UnsupportedOperationException()
        override fun getLogStreamName() = throw UnsupportedOperationException()
        override fun getClientContext() = throw UnsupportedOperationException()
        override fun getFunctionName() = throw UnsupportedOperationException()
        override fun getFunctionVersion() = throw UnsupportedOperationException()
        override fun getIdentity() = throw UnsupportedOperationException()
        override fun getInvokedFunctionArn() = throw UnsupportedOperationException()
        override fun getMemoryLimitInMB() = throw UnsupportedOperationException()
        override fun getRemainingTimeInMillis() = throw UnsupportedOperationException()
        override fun getLogger() = object : LambdaLogger {
            override fun log(message: String?) = println(message)
            override fun log(message: ByteArray?) = println(message)
        }
    }
}