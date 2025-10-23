// kotlin
package com.android.sample.model.listing

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.Date
import org.junit.Assert.*
import org.junit.Test

class ListingTest {

  @Test
  fun `listing type enum contains expected values`() {
    // ensure enum names and valueOf work
    assertEquals(ListingType.PROPOSAL, ListingType.valueOf("PROPOSAL"))
    assertEquals(ListingType.REQUEST, ListingType.valueOf("REQUEST"))
    val values = ListingType.values()
    assertTrue(values.contains(ListingType.PROPOSAL))
    assertTrue(values.contains(ListingType.REQUEST))
  }

  @Test
  fun `proposal properties and behavior`() {
    val date = Date(0)
    val skill = Skill() // uses default
    val location = Location() // uses default
    val proposal =
        Proposal(
            listingId = "p1",
            creatorUserId = "user1",
            skill = skill,
            description = "teach Kotlin",
            location = location,
            createdAt = date,
            isActive = false,
            hourlyRate = 25.0,
            type = ListingType.PROPOSAL)

    // properties
    assertEquals("p1", proposal.listingId)
    assertEquals("user1", proposal.creatorUserId)
    assertEquals(skill, proposal.skill)
    assertEquals("teach Kotlin", proposal.description)
    assertEquals(location, proposal.location)
    assertEquals(date, proposal.createdAt)
    assertFalse(proposal.isActive)
    assertEquals(25.0, proposal.hourlyRate, 0.0)
    assertEquals(ListingType.PROPOSAL, proposal.type)

    // toString contains class name and fields
    assertTrue(proposal.toString().contains("Proposal"))

    // copy and equality/hashCode behavior
    val proposalCopy = proposal.copy(listingId = "p2")
    assertNotEquals(proposal, proposalCopy)
    assertEquals("p2", proposalCopy.listingId)
    assertNotEquals(proposal.hashCode(), proposalCopy.hashCode())
  }

  @Test
  fun `request properties and behavior`() {
    val date = Date(12345)
    val skill = Skill()
    val location = Location()
    val request =
        Request(
            listingId = "r1",
            creatorUserId = "user2",
            skill = skill,
            description = "need help with Android",
            location = location,
            createdAt = date,
            isActive = true,
            hourlyRate = 0.0,
            type = ListingType.REQUEST)

    // properties
    assertEquals("r1", request.listingId)
    assertEquals("user2", request.creatorUserId)
    assertEquals(skill, request.skill)
    assertEquals("need help with Android", request.description)
    assertEquals(location, request.location)
    assertEquals(date, request.createdAt)
    assertTrue(request.isActive)
    assertEquals(0.0, request.hourlyRate, 0.0)
    assertEquals(ListingType.REQUEST, request.type)

    // copy and equality/hashCode
    val requestCopy = request.copy(hourlyRate = 10.0)
    assertNotEquals(request, requestCopy)
    assertEquals(10.0, requestCopy.hourlyRate, 0.0)
    assertNotEquals(request.hashCode(), requestCopy.hashCode())
  }

  @Test
  fun `polymorphic list and filtering by type`() {
    val p = Proposal(listingId = "pX", createdAt = Date(0))
    val r = Request(listingId = "rX", createdAt = Date(0))
    val items: List<Listing> = listOf(p, r)

    val proposals = items.filter { it.type == ListingType.PROPOSAL }
    val requests = items.filter { it.type == ListingType.REQUEST }

    assertEquals(1, proposals.size)
    assertEquals(p, proposals.first())
    assertEquals(1, requests.size)
    assertEquals(r, requests.first())
  }
}
