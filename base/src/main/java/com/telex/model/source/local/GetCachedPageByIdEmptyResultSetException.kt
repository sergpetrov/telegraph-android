package com.telex.model.source.local

/**
 * @author Sergey Petrov
 */
// TODO need to localize this error http://crashes.to/s/db652f60453
class GetCachedPageByIdEmptyResultSetException(methodName: String, cause: Throwable) : IllegalStateException("Error in call $methodName", cause)
