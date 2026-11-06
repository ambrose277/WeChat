package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testWeChatRepository_sendMessageAndReaction() {
    val repo = com.example.data.WeChatRepository()
    val initialMsgCount = repo.messages.value["c_sarah"]?.size ?: 0
    repo.sendMessage("c_sarah", "Unit test message")
    val updatedMsgCount = repo.messages.value["c_sarah"]?.size ?: 0
    assertEquals(initialMsgCount + 1, updatedMsgCount)

    val lastMsg = repo.messages.value["c_sarah"]?.last()
    assertNotNull(lastMsg)
    assertEquals("Unit test message", lastMsg?.text)

    // React to message
    repo.reactToMessage("c_sarah", lastMsg!!.id, "👍")
    val reactedMsg = repo.messages.value["c_sarah"]?.find { it.id == lastMsg.id }
    assertEquals(1, reactedMsg?.reactions?.get("👍"))
  }

  @Test
  fun testWeChatRepository_groupCreation() {
    val repo = com.example.data.WeChatRepository()
    val initialChatCount = repo.chats.value.size
    val groupId = repo.createGroup("Android Devs", "Test group description", listOf("u_sarah", "u_marcus"))
    assertTrue(groupId.startsWith("c_grp_"))
    assertEquals(initialChatCount + 1, repo.chats.value.size)
  }

  @Test
  fun testWeChatRepository_postInteractions() {
    val repo = com.example.data.WeChatRepository()
    val firstPost = repo.posts.value.first()
    val initialLikes = firstPost.likesCount
    val initialLiked = firstPost.isLikedByMe

    repo.toggleLikePost(firstPost.id)
    val updatedPost = repo.posts.value.find { it.id == firstPost.id }
    assertEquals(!initialLiked, updatedPost?.isLikedByMe)
  }
}
