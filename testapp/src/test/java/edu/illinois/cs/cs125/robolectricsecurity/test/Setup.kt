package edu.illinois.cs.cs125.robolectricsecurity.test

import edu.illinois.cs.cs125.robolectricsecurity.RobolectricCompatibleSecurityManager

fun ensureSecurityManagerInstalled() {
    if (System.getSecurityManager() != null) return
    System.setProperty("rcsm.untrustedpackage", "edu.illinois.cs.cs125.robolectricsecurity.test")
    System.setProperty("rcsm.log", "true")
    System.setProperty("rcsm.permitted.reflect", "MainActivity#createTempFileReflective;MainActivity#tryPowerMockReflectiveSetOutFromTrustedReflection")
    System.setSecurityManager(RobolectricCompatibleSecurityManager())
}
