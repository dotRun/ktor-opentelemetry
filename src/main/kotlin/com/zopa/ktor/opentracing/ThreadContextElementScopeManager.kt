package com.zopa.ktor.opentracing

import io.opentracing.Scope
import io.opentracing.ScopeManager
import io.opentracing.Span
import java.util.Stack


internal val threadLocalSpanStack = ThreadLocal<Stack<Span>>()

class ThreadContextElementScopeManager: ScopeManager {
    override fun activate(span: Span?): Scope {
        var spanStack = threadLocalSpanStack.get()

        if (spanStack == null) {
            log.info { "Span stack is null, instantiating a new one." }
            spanStack = Stack<Span>()
            threadLocalSpanStack.set(spanStack)
        }

        spanStack.push(span)
        return CoroutineThreadLocalScope()
    }

    override fun activeSpan(): Span? {
        val spanStack = threadLocalSpanStack.get() ?: return null
        return if (spanStack.isNotEmpty()) spanStack.peek() else null
    }
}

internal class CoroutineThreadLocalScope: Scope {
    override fun close() {
        val spanStack = threadLocalSpanStack.get()
        if (spanStack == null) {
            log.error { "spanStack is null" }
            return
        }

        if (spanStack.isNotEmpty()) {
            spanStack.pop()
        }
    }
}


