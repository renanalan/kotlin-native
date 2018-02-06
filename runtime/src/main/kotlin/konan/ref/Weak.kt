/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package konan.ref

/**
 * Class WeakReference encapsulates weak reference to an object, which could be used to either
 * retrieve a strong reference to an object, or return null, if object was already destoyed by
 * the memory manager.
 */
class WeakReference<T> {
    /**
     * Creates a weak reference object pointing to an object. Weak reference doesn't prevent
     * removing object, and is nullified once object is collected.
     */
    constructor(referred: T) {
        if (referred == null) throw Error("Weak reference to null?")
        pointer = getWeakReferenceCounter(referred)
    }

    /**
     * Backing store for the object pointer, inaccessible directly.
     */
    @PublishedApi
    internal var pointer: WeakReferenceCounter?

    /**
     * Clears reference to an object.
     */
    public fun clear() {
        pointer = null
    }
}

/**
 * Returns either reference to an object or null, if it was collected.
 */
@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
public inline fun <reified T> WeakReference<T>.get() = pointer?.get() as T?