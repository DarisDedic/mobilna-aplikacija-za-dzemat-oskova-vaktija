package com.example

import com.example.updater.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun testVersionComparison() {
        assertTrue("v1.1 should be newer than 1.0", UpdateManager.isVersionNewer("v1.1", "1.0"))
        assertTrue("1.0.1 should be newer than 1.0", UpdateManager.isVersionNewer("1.0.1", "1.0"))
        assertTrue("2.0 should be newer than 1.9.9", UpdateManager.isVersionNewer("2.0", "1.9.9"))
        assertTrue("v1.2.0 should be newer than v1.1.9", UpdateManager.isVersionNewer("v1.2.0", "v1.1.9"))

        assertFalse("1.0 is not newer than 1.0", UpdateManager.isVersionNewer("1.0", "1.0"))
        assertFalse("v1.0 is equal to 1.0", UpdateManager.isVersionNewer("v1.0", "1.0"))
        assertFalse("0.9 is older than 1.0", UpdateManager.isVersionNewer("0.9", "1.0"))
    }
}
