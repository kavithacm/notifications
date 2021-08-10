package org.opensearch.notifications.index

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.opensearch.action.ActionFuture
import org.opensearch.action.admin.indices.create.CreateIndexResponse
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.action.support.master.AcknowledgedResponse
import org.opensearch.client.AdminClient
import org.opensearch.client.Client
import org.opensearch.client.IndicesAdminClient
import org.opensearch.cluster.ClusterState
import org.opensearch.cluster.routing.RoutingTable
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.collect.MapBuilder
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings
import org.opensearch.common.unit.ByteSizeValue
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.common.util.concurrent.ThreadContext.StoredContext
//import org.opensearch.common.util.concurrent.ThreadContext.ThreadContextStruct
import org.opensearch.commons.notifications.model.*
import org.opensearch.http.HttpTransportSettings
import org.opensearch.notifications.model.DocInfo
import org.opensearch.notifications.model.DocMetadata
import org.opensearch.notifications.model.NotificationEventDoc
import org.opensearch.notifications.model.NotificationEventDocInfo
import org.opensearch.notifications.util.SecureIndexClient
import org.opensearch.threadpool.ThreadPool
import java.time.Instant
import java.util.stream.Collector


internal class NotificationEventIndexTest{


    private lateinit var client: Client

    //@Mock
    private val INDEX_NAME = ".opensearch-notifications-event"


    private lateinit var clusterService: ClusterService

    @BeforeEach
    fun setUp() {
        client = mock(Client::class.java,"client")
        clusterService = mock(ClusterService::class.java, "clusterservice")
        //val secureIndexClient = mock(SecureIndexClient::class.java)
        //whenever(SecureIndexClient(client)).thenReturn(secureIndexClient)
        NotificationEventIndex.initialize(client, clusterService)
    }


    @Test
    fun `index operation to get single event` () {
        val id = "index-1"
        val docInfo = DocInfo("index-1", 1, 1, 1)
        //val eventDoc = mock(NotificationEventDoc::class.java)
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val metadata = DocMetadata(
            lastUpdatedTimeMs,
            createdTimeMs,
            "tenant",
            listOf("User:user", "Role:sample_role", "BERole:sample_backend_role")
        )
        val sampleEventSource = EventSource(
            "title",
            "reference_id",
            Feature.ALERTING,
            tags = listOf("tag1", "tag2"),
            severity = SeverityType.INFO
        )
        val status = EventStatus(
            "config_id",
            "name",
            ConfigType.CHIME,
            deliveryStatus = DeliveryStatus("200", "success")
        )
        val sampleEvent = NotificationEvent(sampleEventSource, listOf(status))
        val eventDoc = NotificationEventDoc(metadata, sampleEvent)
        val expectedEventDocInfo = NotificationEventDocInfo(docInfo, eventDoc)

       // val getRequest = GetRequest(INDEX_NAME).id(id)
        //val mockActionFuture:ActionFuture<GetResponse> = mock(ActionFuture::class.java) as ActionFuture<GetResponse>
        //whenever(NotificationEventIndex.client.get(any())).thenReturn(mockActionFuture)

        //whenever(client.get(getRequest)).thenReturn(mockActionFuture)
        val clusterState = mock(ClusterState::class.java)

        whenever(clusterService.state()).thenReturn(clusterState)
        val mockRoutingTable = mock(RoutingTable::class.java)
        val mockHasIndex = mockRoutingTable.hasIndex(INDEX_NAME)

        // print("has index value is $mockHasIndex")

        whenever(clusterState.routingTable).thenReturn(mockRoutingTable)
        whenever(mockRoutingTable.hasIndex(INDEX_NAME)).thenReturn(mockHasIndex)

        //val actionFuture = NotificationEventIndex.client.admin().indices().create(request)



        val admin = mock(AdminClient::class.java)
        val indices = mock(IndicesAdminClient::class.java)
        val mockCreateClient:ActionFuture<CreateIndexResponse>  = mock(ActionFuture::class.java) as ActionFuture<CreateIndexResponse>

        whenever(client.admin()).thenReturn(admin)
        whenever(admin.indices()).thenReturn(indices)
        whenever(indices.create(any())).thenReturn(mockCreateClient)

        //val time = PluginSettings.operationTimeoutMs
        val mockActionGet = mock(CreateIndexResponse::class.java)

           // mockCreateClient.actionGet(PluginSettings.operationTimeoutMs)
        whenever(mockCreateClient.actionGet(anyLong())).thenReturn(mockActionGet)
        println("mockActionGet: $mockActionGet")

        //println("mockCreateClient: $mockCreateClient")
        //println("plugin timout: $time")

        val mockResponse = mock(AcknowledgedResponse::class.java)

        //whenever(mockActionGet.isAcknowledged).thenReturn(mockResponse.isAcknowledged)
        //whenever(mockActionGet.isAcknowledged).thenReturn(mockResponse)
        //when(mockActionGet.isAcknowledged).thenReturn(true)
        //doReturn(true).when(mockActionGet).isAcknowledged()
        //Mockito.`when`(mockActionGet.isAcknowledged()).thenReturn(true)

        val getRequest = GetRequest(INDEX_NAME).id(id)
        val mockActionFuture:ActionFuture<GetResponse> = mock(ActionFuture::class.java) as ActionFuture<GetResponse>
        //whenever(client.get(any())).thenReturn(mockActionFuture)

        //client = mock(SecureIndexClient::class.java)
        println("Mock action Future: $mockActionFuture")
        whenever(client.get(getRequest)).thenReturn(mockActionFuture)
        val mockThreadPool = mock(ThreadPool::class.java)
        val mockThreadContext = mock(ThreadContext::class.java)

        whenever(client.threadPool()).thenReturn(mockThreadPool)
        whenever(mockThreadPool.threadContext).thenReturn(mockThreadContext)
        whenever(client.get(getRequest)).thenReturn(mockActionFuture)

        val actualEventDocInfo = NotificationEventIndex.getNotificationEvent(id)
        //verify(clusterService.state(), atLeast(1))
        verify(mockCreateClient.actionGet(), atLeast(1))
        //verifyNoMoreInteractions()

        //val future = mock(client.admin().indices().create(request))
        /*
        val mockFuture = mock(ActionFuture::class.java)
        whenever(client.get(any())).thenReturn(mockFuture)
         */

        assertEquals(expectedEventDocInfo, actualEventDocInfo)

    }

}

