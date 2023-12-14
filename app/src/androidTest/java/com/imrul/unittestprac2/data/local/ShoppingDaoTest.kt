package com.imrul.unittestprac2.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
@OptIn(ExperimentalCoroutinesApi::class)

class ShoppingDaoTest {

    //to make sure they run sequentially
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ShoppingItemDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShoppingItemDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.shoppingDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertShoppingItemTest() = runTest {
        val shoppingItem = ShoppingItem("banana", 5, 8f, "url", id = 1)
        dao.insertShoppingItem(shoppingItem)
        var allShoppingItems: List<ShoppingItem>? = null
        // Launch a coroutine job and store the reference to it
        val job = launch {
            dao.observeAllShoppingItems().collect {
                allShoppingItems = it
            }
        }
        // Explicitly advance time to wait for the job to complete
        testScheduler.apply { advanceTimeBy(1000); runCurrent() } // Adjust the time as needed
        // Cancel the job to ensure proper cleanup
        job.cancelAndJoin()
        assertThat(allShoppingItems?.contains(shoppingItem))
    }

    @Test
    fun deleteShoppingItemTest() = runTest {
        val shoppingItem = ShoppingItem("banana", 5, 8f, "url", id = 1)
        dao.insertShoppingItem(shoppingItem)
        dao.deleteShoppingItem(shoppingItem)

        var allShoppingItems: List<ShoppingItem>? = null
        val job = launch {
            dao.observeAllShoppingItems().collect {
                allShoppingItems = it
            }
        }
        testScheduler.apply { advanceTimeBy(1000); runCurrent() }
        job.cancelAndJoin()

        assertThat(allShoppingItems).isEmpty()
    }

    @Test
    fun observeTotalPriceSumTest() = runTest {
        val shoppingItem1 = ShoppingItem("banana", 5, 8f, "url", id = 1)
        val shoppingItem2 = ShoppingItem("banana", 5, 8f, "url", id = 2)
        val shoppingItem3 = ShoppingItem("banana", 5, 8f, "url", id = 3)
        val shoppingItem4 = ShoppingItem("banana", 5, 8f, "url", id = 4)
        dao.insertShoppingItem(shoppingItem1)
        dao.insertShoppingItem(shoppingItem2)
        dao.insertShoppingItem(shoppingItem3)
        dao.insertShoppingItem(shoppingItem4)


        //get the flow from the database
        var totalPrice: Float? = null
        val job = launch {
            dao.observeTotalPrice().collect {
                totalPrice = it
            }
        }
        testScheduler.apply { advanceTimeBy(1000); runCurrent() }
        job.cancelAndJoin()

        assertThat(totalPrice).isEqualTo(160f)
    }





}

