package edu.illinois.cs.cs125.robolectricsecurity.test

import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import edu.illinois.cs.cs125.robolectricsecurity.Trusted
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.io.File

@Trusted
@RunWith(RobolectricTestRunner::class)
class FunctionalityTest {

    private lateinit var activity: MainActivity

    @Before
    fun before() {
        ensureSecurityManagerInstalled()
        activity = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
    }

    @Test
    fun testActivitySetup() {
        Assert.assertNotNull(activity.findViewById(R.id.mainLayout))
    }

    @Test
    fun testViews() {
        val layout = activity.findViewById<LinearLayout>(R.id.mainLayout)
        Assert.assertNull(layout.findViewWithTag("label"))
        activity.createUI()
        val label = layout.findViewWithTag<TextView>("label")
        Assert.assertEquals("Not clicked yet", label.text)
        val button = layout.findViewWithTag<Button>("button")
        button.performClick()
        Assert.assertEquals("Clicked", label.text)
    }

    @Test
    fun testImage() {
        activity.createImageView()
        val imageView = activity.findViewById<LinearLayout>(R.id.mainLayout).findViewWithTag<ImageView>("image")
        Assert.assertEquals(R.mipmap.ic_launcher, Shadows.shadowOf(imageView.drawable).createdFromResId)
    }

    @Test
    fun testTempFileIO() {
        val file = activity.createTempFile()
        Assert.assertNotNull(file)
        file.writeText("robolectricsecurity")
        Assert.assertEquals("robolectricsecurity", activity.readFileContents(file))
    }

    @Test
    fun testFileListingFromTrusted() {
        Assert.assertNotEquals(0, File("/").listFiles().size)
    }

    @Test
    fun testStreams() {
        Assert.assertEquals('c', activity.findFirstLowercase(arrayOf("A", "B", "c", "d", "E")))
    }

    @Test
    fun testTrustedReflection() {
        Assert.assertNotNull(activity.createTempFileReflective())
    }

}