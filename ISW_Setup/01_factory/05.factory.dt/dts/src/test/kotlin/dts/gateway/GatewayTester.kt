package dts.gateway

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.mockito.Mockito.*
import dts.DataSourceTester
import dts.events.ErrorEvent
import dts.events.NewDataPointEvent
import dts.events.observer.IGatewayObserver
import org.mockito.Mockito

/**
 * Helper for testing a `Gateway` (`IDataSource`)
 */
abstract class GatewayTester<T: AbstractGateway> : DataSourceTester<T>() {
    /**
     * Observer of the data source.
     *
     * If the `setUp` and `tearDown` methods are overwritten, the `observer` must be initialized.
     * E.g. by calling the methods manually.
     * Otherwise, no helpers defined here will work.
     */
    open lateinit var observer: IGatewayObserver

    /**
     * Add a mock observer to the gateway
     */
    @BeforeEach
    open fun setUp() {
        observer = mock<IGatewayObserver>()
        dataSource.addObserver(observer)
    }

    /**
     * Remove the mock observer from the gateway.
     */
    @AfterEach
    open fun tearDown() {
        dataSource.removeObserver(observer)
    }

    /** Helper for Kotlin non nullable any */
    private fun <T> any(type: Class<T>): T = Mockito.any(type)

    /**
     * Assert that some `NewDataPointEvent`'s occurred.
     *
     * @param count The number of expected events
     */
    open fun assertDataPointEvents(count: Int) {
        verify(observer, times(count)).handleDatapointEvent(any(NewDataPointEvent::class.java))
    }

    /**
     * Assert that some `ErrorEvent`'s occurred.
     *
     * @param count The number of expected events
     */
    open fun assertErrorEvents(count: Int) {
        verify(observer, times(count)).handleError(any(ErrorEvent::class.java))
    }
}