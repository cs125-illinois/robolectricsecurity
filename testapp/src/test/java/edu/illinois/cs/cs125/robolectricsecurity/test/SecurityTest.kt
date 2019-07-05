package edu.illinois.cs.cs125.robolectricsecurity.test

import android.app.Activity
import edu.illinois.cs.cs125.robolectricsecurity.Trusted
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@Trusted
@RunWith(RobolectricTestRunner::class)
class SecurityTest {

    @Before
    fun installSM() {
        ensureSecurityManagerInstalled()
    }

    private fun setupActivity(): Activity {
        return Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
    }

    @Test
    fun testSecurityManagerInstalled() {
        Assert.assertEquals("RobolectricCompatibleSecurityManager", System.getSecurityManager()!!.javaClass.simpleName)
    }

    @Test
    fun testActivitySetup() {
        setupActivity()
    }

}