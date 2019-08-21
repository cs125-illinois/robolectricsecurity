package edu.illinois.cs.cs125.robolectricsecurity.test

import edu.illinois.cs.cs125.robolectricsecurity.Trusted
import edu.illinois.cs.cs125.robolectricsecurity.secureMockMethodCache
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@PowerMockIgnore("org.mockito.*", "org.powermock.*", "org.robolectric.*", "android.*", "androidx.*")
@PrepareForTest(Adder::class)
@Trusted
class MockSecurityTest {

    private lateinit var activity: MainActivity

    @Rule
    @JvmField
    val mockRule = PowerMockRule()

    @Before
    fun before() {
        ensureSecurityManagerInstalled()
        secureMockMethodCache()
        activity = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
    }

    @Test
    fun testMockedDangerousMethod() {
        PowerMockito.mockStatic(Adder::class.java)
        Mockito.`when`(Adder.increment(Mockito.anyInt())).thenAnswer { 127 }
        Assert.assertEquals(127, activity.addOne(1))
    }

    @Test(expected = SecurityException::class)
    fun testUnmockedDangerousMethod() {
        activity.addOne(2)
    }

    @Test(expected = SecurityException::class)
    fun testIndirectReflectionExploit() {
        activity.tryPowerMockReflectiveSetOut()
    }

    @Test(expected = SecurityException::class)
    fun testIndirectReflectionExploitFromTrustedReflection() {
        activity.tryPowerMockReflectiveSetOutFromTrustedReflection()
    }

    @Test
    fun testIndirectReflectionExploitAfterTrusted() {
        Assert.assertNotNull(activity.createTempFileReflective())
        try {
            activity.tryPowerMockReflectiveSetOut()
            Assert.fail("tryPowerMockReflectiveSetOut completed")
        } catch (e: SecurityException) {
            // Expected
        }
    }

}