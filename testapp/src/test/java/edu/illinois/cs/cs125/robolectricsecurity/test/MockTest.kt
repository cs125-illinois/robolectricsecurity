package edu.illinois.cs.cs125.robolectricsecurity.test

import android.content.res.Resources
import edu.illinois.cs.cs125.robolectricsecurity.Trusted
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
class MockTest {

    private lateinit var activity: MainActivity

    @Rule
    @JvmField
    val mockRule = PowerMockRule()

    @Before
    fun before() {
        ensureSecurityManagerInstalled()
        activity = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
    }

    @Test
    fun testStaticMock() {
        PowerMockito.mockStatic(Adder::class.java)
        var called = false
        Mockito.`when`(Adder.add(Mockito.anyInt(), Mockito.anyInt())).then { i ->
            called = true
            return@then 125
        }
        Assert.assertEquals(125, activity.addToTwo(2))
        Assert.assertTrue(called)
    }

    @Test
    fun testMock() {
        val resources = Mockito.mock(Resources::class.java)
        Mockito.`when`(resources.getString(Mockito.anyInt())).thenReturn("CS 125")
        Assert.assertEquals("CS 125", activity.getAppName(resources))
    }

    @Test
    fun testSpy() {
        val resources = PowerMockito.spy(activity.resources)
        Mockito.doAnswer { "Zero" }.`when`(resources).getString(0)
        Assert.assertEquals("Zero", activity.getString(resources, 0))
        Assert.assertEquals("Test App", activity.getAppName(resources))
    }

}