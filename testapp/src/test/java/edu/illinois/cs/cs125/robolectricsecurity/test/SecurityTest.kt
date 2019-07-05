package edu.illinois.cs.cs125.robolectricsecurity.test

import edu.illinois.cs.cs125.robolectricsecurity.Trusted
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@Trusted
@RunWith(RobolectricTestRunner::class)
class SecurityTest {

    private lateinit var activity: MainActivity

    @Before
    fun before() {
        ensureSecurityManagerInstalled()
        activity = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
    }

    @Test(expected = SecurityException::class)
    fun testFileListing() {
        activity.tryListFiles()
    }

    @Test(expected = SecurityException::class)
    fun testFileWrite() {
        activity.tryWriteFile()
    }

    @Test(expected = SecurityException::class)
    fun testSneakyFileWrite() {
        activity.trySneakyWriteFile()
    }

    @Test(expected = SecurityException::class)
    fun testNetwork() {
        activity.tryHttpRequest()
    }

    @Test(expected = SecurityException::class)
    fun testExit() {
        activity.tryExit()
    }

    @Test(expected = SecurityException::class)
    fun testRemoveSM() {
        activity.tryRemoveSecurityManager()
    }

}