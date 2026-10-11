package com.kail.location.auth

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationAccessPolicyTest {
    @Test
    fun `simulation can start without an account or quota`() = runBlocking {
        assertTrue(UsageManager.canStartSimulation(null))
    }

    @Test
    fun `starting simulation does not consume a quota`() = runBlocking {
        assertTrue(UsageManager.consumeSimulation(null))
    }
}
