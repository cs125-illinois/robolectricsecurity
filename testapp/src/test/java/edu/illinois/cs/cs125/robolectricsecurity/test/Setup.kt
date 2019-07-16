package edu.illinois.cs.cs125.robolectricsecurity.test

import edu.illinois.cs.cs125.robolectricsecurity.RobolectricCompatibleSecurityManager

fun ensureSecurityManagerInstalled() {
    if (System.getSecurityManager() != null) return
    System.setProperty("untrusted.package", "edu.illinois.cs.cs125.robolectricsecurity.test")
    System.setProperty("log.denials", "true")
    System.setSecurityManager(RobolectricCompatibleSecurityManager())
}
