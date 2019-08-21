@file:JvmName("PowerMockSecurity")

package edu.illinois.cs.cs125.robolectricsecurity

import java.lang.Exception
import java.lang.reflect.ReflectPermission
import java.security.Permission
import java.util.concurrent.ConcurrentMap

class ReadCheckedConcurrentMap<K, V>(
        private val wrap: ConcurrentMap<K, V>,
        private val securityManager: SecurityManager,
        private val permission: Permission) : ConcurrentMap<K, V> {

    private fun check() {
        securityManager.checkPermission(permission)
    }

    override fun clear() {
        wrap.clear()
    }

    override fun containsKey(key: K): Boolean {
        return wrap.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return wrap.containsValue(value)
    }

    override fun putAll(from: Map<out K, V>) {
        wrap.putAll(from)
    }

    override fun putIfAbsent(p0: K, p1: V): V? {
        check()
        return wrap.putIfAbsent(p0, p1)
    }

    override fun replace(p0: K, p1: V, p2: V): Boolean {
        return wrap.replace(p0, p1, p2)
    }

    override fun replace(p0: K, p1: V): V? {
        check()
        return wrap.replace(p0, p1)
    }

    override fun get(key: K): V? {
        check()
        return wrap.get(key)
    }

    override fun put(key: K, value: V): V? {
        check()
        return wrap.put(key, value)
    }

    override fun isEmpty(): Boolean {
        return wrap.isEmpty()
    }

    override fun remove(key: K, value: V): Boolean {
        return wrap.remove(key, value)
    }

    override val size: Int
        get() = wrap.size

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            check()
            return wrap.entries
        }

    override val keys: MutableSet<K>
        get() {
            check()
            return wrap.keys
        }

    override val values: MutableCollection<V>
        get() {
            check()
            return wrap.values
        }

    override fun remove(key: K): V? {
        check()
        return wrap.remove(key)
    }

}

@Suppress("unused")
fun secureMockMethodCache() {
    try {
        val clazz = Class.forName("org.powermock.reflect.internal.WhiteboxImpl")
        val field = clazz.getDeclaredField("allClassMethodsCache")
        field.isAccessible = true
        val originalMap = field.get(null) as? ConcurrentMap<*, *> ?: return
        if (originalMap.javaClass.name == ReadCheckedConcurrentMap::class.java.name) return
        val newMap = ReadCheckedConcurrentMap(originalMap, System.getSecurityManager(), ReflectPermission("accessDeclaredMembers"))
        field.set(null, newMap)
    } catch (e: Exception) {
        System.err.println("Couldn't secure PowerMock method cache:")
        e.printStackTrace()
    }
}
