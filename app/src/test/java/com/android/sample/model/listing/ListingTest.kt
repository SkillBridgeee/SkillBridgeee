package com.android.sample.model.listing

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.Date
import org.junit.Assert
import org.junit.Test

class ListingTest {
  @Test
  fun testProposalCreationWithValidValues() {
    val now = Date()
    val location = Location()
    val skill = Skill()

    val proposal =
        Proposal(
            "proposal123",
            "user456",
            skill,
            "Expert in Java programming",
            location,
            now,
            true,
            50.0)

    Assert.assertEquals("proposal123", proposal.listingId)
    Assert.assertEquals("user456", proposal.creatorUserId)
    Assert.assertEquals(skill, proposal.skill)
    Assert.assertEquals("Expert in Java programming", proposal.description)
    Assert.assertEquals(location, proposal.location)
    Assert.assertEquals(now, proposal.createdAt)
    Assert.assertTrue(proposal.isActive)
    Assert.assertEquals(50.0, proposal.hourlyRate, 0.01)
  }

  @Test
  fun testProposalWithDefaultValues() {
    val proposal = Proposal()

    Assert.assertEquals("", proposal.listingId)
    Assert.assertEquals("", proposal.creatorUserId)
    Assert.assertNotNull(proposal.skill)
    Assert.assertEquals("", proposal.description)
    Assert.assertNotNull(proposal.location)
    Assert.assertNotNull(proposal.createdAt)
    Assert.assertTrue(proposal.isActive)
    Assert.assertEquals(0.0, proposal.hourlyRate, 0.01)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testProposalValidationNegativeHourlyRate() {
    Proposal("proposal123", "user456", Skill(), "Description", Location(), Date(), true, -10.0)
  }

  @Test
  fun testProposalWithZeroHourlyRate() {
    val proposal =
        Proposal("proposal123", "user456", Skill(), "Free tutoring", Location(), Date(), true, 0.0)

    Assert.assertEquals(0.0, proposal.hourlyRate, 0.01)
  }

  @Test
  fun testProposalInactive() {
    val proposal =
        Proposal("proposal123", "user456", Skill(), "Description", Location(), Date(), false, 50.0)

    Assert.assertFalse(proposal.isActive)
  }

  @Test
  fun testRequestCreationWithValidValues() {
    val now = Date()
    val location = Location()
    val skill = Skill()

    val request =
        Request(
            "request123", "user789", skill, "Looking for Python tutor", location, now, true, 100.0)

    Assert.assertEquals("request123", request.listingId)
    Assert.assertEquals("user789", request.creatorUserId)
    Assert.assertEquals(skill, request.skill)
    Assert.assertEquals("Looking for Python tutor", request.description)
    Assert.assertEquals(location, request.location)
    Assert.assertEquals(now, request.createdAt)
    Assert.assertTrue(request.isActive)
    Assert.assertEquals(100.0, request.hourlyRate, 0.01)
  }

  @Test
  fun testRequestWithDefaultValues() {
    val request = Request()

    Assert.assertEquals("", request.listingId)
    Assert.assertEquals("", request.creatorUserId)
    Assert.assertNotNull(request.skill)
    Assert.assertEquals("", request.description)
    Assert.assertNotNull(request.location)
    Assert.assertNotNull(request.createdAt)
    Assert.assertTrue(request.isActive)
    Assert.assertEquals(0.0, request.hourlyRate, 0.01)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testRequestValidationNegativeMaxBudget() {
    Request("request123", "user789", Skill(), "Description", Location(), Date(), true, -50.0)
  }

  @Test
  fun testRequestWithZeroMaxBudget() {
    val request =
        Request("request123", "user789", Skill(), "Budget flexible", Location(), Date(), true, 0.0)

    Assert.assertEquals(0.0, request.hourlyRate, 0.01)
  }

  @Test
  fun testRequestInactive() {
    val request =
        Request("request123", "user789", Skill(), "Description", Location(), Date(), false, 100.0)

    Assert.assertFalse(request.isActive)
  }

  @Test
  fun testProposalEquality() {
    val now = Date()
    val location = Location()
    val skill = Skill()

    val proposal1 =
        Proposal("proposal123", "user456", skill, "Description", location, now, true, 50.0)

    val proposal2 =
        Proposal("proposal123", "user456", skill, "Description", location, now, true, 50.0)

    Assert.assertEquals(proposal1, proposal2)
    Assert.assertEquals(proposal1.hashCode().toLong(), proposal2.hashCode().toLong())
  }

  @Test
  fun testRequestEquality() {
    val now = Date()
    val location = Location()
    val skill = Skill()

    val request1 =
        Request("request123", "user789", skill, "Description", location, now, true, 100.0)

    val request2 =
        Request("request123", "user789", skill, "Description", location, now, true, 100.0)

    Assert.assertEquals(request1, request2)
    Assert.assertEquals(request1.hashCode().toLong(), request2.hashCode().toLong())
  }

  @Test
  fun testProposalCopyFunctionality() {
    val original =
        Proposal(
            "proposal123",
            "user456",
            Skill(),
            "Original description",
            Location(),
            Date(),
            true,
            50.0)

    val updated =
        original.copy(
            "proposal123",
            "user456",
            original.skill,
            "Updated description",
            original.location,
            original.createdAt,
            false,
            75.0)

    Assert.assertEquals("proposal123", updated.listingId)
    Assert.assertEquals("Updated description", updated.description)
    Assert.assertFalse(updated.isActive)
    Assert.assertEquals(75.0, updated.hourlyRate, 0.01)
  }

  @Test
  fun testRequestCopyFunctionality() {
    val original =
        Request(
            "request123",
            "user789",
            Skill(),
            "Original description",
            Location(),
            Date(),
            true,
            100.0)

    val updated =
        original.copy(
            "request123",
            "user789",
            original.skill,
            "Updated description",
            original.location,
            original.createdAt,
            false,
            150.0)

    Assert.assertEquals("request123", updated.listingId)
    Assert.assertEquals("Updated description", updated.description)
    Assert.assertFalse(updated.isActive)
    Assert.assertEquals(150.0, updated.hourlyRate, 0.01)
  }

  @Test
  fun testProposalToString() {
    val proposal =
        Proposal("proposal123", "user456", Skill(), "Java tutor", Location(), Date(), true, 50.0)

    val proposalString = proposal.toString()
    Assert.assertTrue(proposalString.contains("proposal123"))
    Assert.assertTrue(proposalString.contains("user456"))
    Assert.assertTrue(proposalString.contains("Java tutor"))
  }

  @Test
  fun testRequestToString() {
    val request =
        Request(
            "request123",
            "user789",
            Skill(),
            "Python tutor needed",
            Location(),
            Date(),
            true,
            100.0)

    val requestString = request.toString()
    Assert.assertTrue(requestString.contains("request123"))
    Assert.assertTrue(requestString.contains("user789"))
    Assert.assertTrue(requestString.contains("Python tutor needed"))
  }

  @Test
  fun testProposalWithLargeHourlyRate() {
    val proposal =
        Proposal(
            "proposal123", "user456", Skill(), "Premium tutoring", Location(), Date(), true, 500.0)

    Assert.assertEquals(500.0, proposal.hourlyRate, 0.01)
  }

  @Test
  fun testRequestWithLargeMaxBudget() {
    val request =
        Request(
            "request123", "user789", Skill(), "Intensive course", Location(), Date(), true, 1000.0)

    Assert.assertEquals(1000.0, request.hourlyRate, 0.01)
  }
}
