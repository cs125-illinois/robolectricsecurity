package edu.illinois.cs.cs125.robolectricsecurity

import java.io.File
import java.io.FilePermission
import java.nio.file.Path
import java.security.Permission
import java.util.*
import java.util.logging.LoggingPermission

@Suppress("unused", "SameParameterValue")
class RobolectricCompatibleSecurityManager : SecurityManager() {

    private val initialDirectory = File(".").absolutePath.replace('\\', '/').trimEnd('.', '/')
    private val untrustedPackage: String
            get() = System.getProperty("untrusted.package")
    private val trustedName: String
            get() = Trusted::class.java.name

    private val checking: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    private fun outermostClass(clazz: Class<*>): Class<*> {
        return clazz.classLoader.loadClass(clazz.name.split('$', limit = 2)[0])
    }

    private fun untrusted(clazz: Class<*>): Boolean {
        return clazz.name.startsWith(untrustedPackage) &&
                !outermostClass(clazz).annotations.any { it.annotationClass.java.name == trustedName }
    }

    private fun fullyTrustedContext(): Boolean {
        return classContext.find { untrusted(it) } == null
    }

    private fun beforeUntrustedContext(okPackage: String): Boolean {
        classContext.forEach {
            if (untrusted(it)) return false
            if (it.name.startsWith(okPackage)) return true
        }
        return true
    }

    private fun afterOneUntrustedContext(okPackage: String): Boolean {
        var hasUntrusted = false
        var inUntrusted = false
        classContext.forEach {
            if (untrusted(it)) {
                if (hasUntrusted && !inUntrusted) return false
                hasUntrusted = true
                inUntrusted = true
            } else {
                inUntrusted = false
            }
            if (it.name.startsWith(okPackage)) return true
        }
        return false
    }

    private fun calledByUntrusted(dangerousPackage: String): Boolean {
        var sawDangerous = false
        val loader = loaderForUntrusted()
        Thread.currentThread().stackTrace.forEach {
            val clazz = try { Class.forName(it.className, false, loader) } catch (e: ClassNotFoundException) { null }
            if (clazz != null && untrusted(clazz) && sawDangerous) return true
            sawDangerous = it.className.startsWith(dangerousPackage)
        }
        return false
    }

    private fun loaderForUntrusted(): ClassLoader {
        return classContext.first { untrusted(it) }.classLoader
    }

    override fun checkPermission(perm: Permission?) {
        if (checking.get()) return
        checking.set(true)
        try {
            checkPermissionInternal(perm!!)
        } finally {
            checking.set(false)
        }
    }

    private fun checkPermissionInternal(perm: Permission) {
        if (fullyTrustedContext()) return
        if (calledByUntrusted("java.lang.reflect.")) {
            super.checkPermission(perm)
            return
        }
        if (perm is PropertyPermission && perm.actions == "read") {
            if (perm.name == "line.separator") return
            if (perm.name.startsWith("android.")) return
            if (perm.name.startsWith("http.") && beforeUntrustedContext("android.net.")) return
            if (perm.name.endsWith(".version") && beforeUntrustedContext("sun.awt.")) return
            if (perm.name.startsWith("AWT.") && beforeUntrustedContext("sun.awt.")) return
            if (perm.name.startsWith("sun.") && beforeUntrustedContext("javax.imageio.")) return
            if (perm.name.startsWith("robolectric.") && beforeUntrustedContext("org.robolectric.")) return
            if (perm.name.contains(".xml.") && beforeUntrustedContext("javax.xml.")) return
            if (perm.name == "file.encoding" && beforeUntrustedContext("java.net.URLEncoder")) return
            if (beforeUntrustedContext("org.robolectric.res.builder.")) return
            if (beforeUntrustedContext("java.awt.Toolkit")) return
        }
        if (perm.name.startsWith("getenv.") && beforeUntrustedContext("java.awt.")) return
        if (perm is FilePermission) {
            if (perm.actions == "read") {
                val fwdSlashPath = perm.name.replace('\\', '/')
                if (perm.name.endsWith(".jar") || perm.name.endsWith(".class")) return
                if (fwdSlashPath.startsWith("$initialDirectory/build/")) return
                if (fwdSlashPath.contains("META-INF/services/javax.imageio.spi.") && beforeUntrustedContext("javax.imageio.spi.")) return
                if (fwdSlashPath.contains("META-INF/services/javax.xml.") && beforeUntrustedContext("javax.xml.")) return
                if (beforeUntrustedContext("org.robolectric.internal.bytecode.SandboxClassLoader")) return
                if (perm.name.contains("logging") && beforeUntrustedContext("org.robolectric.shadows.")) return
                if (beforeUntrustedContext("java.lang.ClassLoader") &&
                        (beforeUntrustedContext("sun.awt.AppContext") ||
                                beforeUntrustedContext("sun.java2d.") ||
                                beforeUntrustedContext("java.awt.image."))) return
                if (perm.name.endsWith("accessibility.properties") && beforeUntrustedContext("java.awt.Toolkit")) return
            }
            val runtimeEnvClass = Class.forName("org.robolectric.RuntimeEnvironment", false, loaderForUntrusted())
            val tempDir = runtimeEnvClass.getMethod("getTempDirectory").invoke(null)
            val tempRootPath = tempDir.javaClass.getMethod("createIfNotExists", String::class.java).invoke(tempDir, ".") as Path
            val tempRoot = tempRootPath.toFile().absolutePath.trimEnd('.')
            if (!tempRoot.contains("robolectric")) throw SecurityException("RuntimeEnvironment has been tampered with")
            if (perm.name.startsWith(tempRoot)) return
        }
        if (perm is LoggingPermission || perm.name.startsWith("accessClassInPackage.sun.util.logging.") ||
                perm.name == "accessClassInPackage.sun.awt.resources") {
            if (beforeUntrustedContext("org.robolectric.shadows.")) return
        }
        if (perm.name in setOf("getClassLoader", "accessDeclaredMembers", "suppressAccessChecks")) {
            if (beforeUntrustedContext("android.") ||
                    beforeUntrustedContext("androidx.appcompat.widget.") ||
                    beforeUntrustedContext("org.robolectric.internal.bytecode.") ||
                    beforeUntrustedContext("java.lang.Thread") ||
                    beforeUntrustedContext("org.powermock.api.mockito.internal.invocation.MockitoMethodInvocationControl") ||
                    beforeUntrustedContext("org.powermock.core.MockGateway\$MockInvocation") ||
                    beforeUntrustedContext("org.powermock.reflect.internal") ||
                    beforeUntrustedContext("org.powermock.api.mockito.repackaged.") ||
                    beforeUntrustedContext("java.util.EnumMap") ||
                    beforeUntrustedContext("java.lang.invoke.CallSite")) return
        }
        if (perm.name == "createClassLoader") {
            if (beforeUntrustedContext("android.content.res.")) return
            if (beforeUntrustedContext("android.view.")) return
            if (beforeUntrustedContext("android.graphics.")) return
            if (beforeUntrustedContext("org.robolectric.internal.bytecode.ShadowImpl")) return
        }
        if (perm.name == "accessClassInPackage.sun.misc") {
            if (beforeUntrustedContext("org.robolectric.internal.bytecode.SandboxClassLoader")) return
        }
        if (perm.name in setOf("modifyThreadGroup", "modifyThread", "setContextClassLoader")) {
            if (beforeUntrustedContext("sun.awt.AppContext") || beforeUntrustedContext("javax.imageio.stream.")) return
        }
        if (perm.name == "loadLibrary.awt") {
            if (beforeUntrustedContext("sun.awt.AppContext") ||
                    beforeUntrustedContext("javax.imageio.stream.") ||
                    beforeUntrustedContext("android.graphics.")) return
        }
        if (perm.name == "getProtectionDomain") {
            if (beforeUntrustedContext("org.powermock.core.classloader.")) return
        }
        if (perm.name == "accessDeclaredMembers") {
            if (afterOneUntrustedContext("org.objenesis.")) return
        }
        super.checkPermission(perm)
    }

}
