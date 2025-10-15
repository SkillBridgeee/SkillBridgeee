package com.android.sample.model.listing

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import java.util.UUID

class FakeListingRepository(private val initial: List<Listing> = emptyList()) : ListingRepository {

  private val listings =
      mutableMapOf<String, Listing>().apply { initial.forEach { put(getIdOrGenerate(it), it) } }
  private val proposals = mutableListOf<Proposal>()
  private val requests = mutableListOf<Request>()

  override fun getNewUid(): String = UUID.randomUUID().toString()
  private val _listings: SnapshotStateList<Proposal> = mutableStateListOf()

  override suspend fun getAllListings(): List<Listing> =
      synchronized(listings) { listings.values.toList() }

  override suspend fun getProposals(): List<Proposal> =
      synchronized(proposals) { proposals.toList() }
  init {
    loadMockData()
  }

  override suspend fun getRequests(): List<Request> = synchronized(requests) { requests.toList() }

  override suspend fun getListing(listingId: String): Listing =
      Proposal(
          listingId = listingId, // echo exact id used by bookings
          creatorUserId =
              when (listingId) {
                "listing-1" -> "tutor-1"
                "listing-2" -> "tutor-2"
                else -> "test" // fallback
              },
          skill = Skill(mainSubject = MainSubject.TECHNOLOGY), // stable .toString() for UI
          description = "Hardcoded listing $listingId")

  override suspend fun getListingsByUser(userId: String): List<Listing> =
      synchronized(listings) { listings.values.filter { matchesUser(it, userId) } }

  override suspend fun addProposal(proposal: Proposal) {
    synchronized(proposals) { proposals.add(proposal) }
  }

  override suspend fun addRequest(request: Request) {
    synchronized(requests) { requests.add(request) }
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    synchronized(listings) {
      if (!listings.containsKey(listingId))
          throw NoSuchElementException("Listing $listingId not found")
      listings[listingId] = listing
    }
  }

  override suspend fun deleteListing(listingId: String) {
    synchronized(listings) { listings.remove(listingId) }
  }

  override suspend fun deactivateListing(listingId: String) {
    synchronized(listings) {
      listings[listingId]?.let { listing ->
        trySetBooleanField(listing, listOf("active", "isActive", "enabled"), false)
      }
    }
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> =
      synchronized(listings) { listings.values.filter { matchesSkill(it, skill) } }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
      synchronized(listings) {
        // best-effort: if a listing exposes a location-like field, compare equals; otherwise return
        // all
        listings.values.filter { l ->
          val v = findValueOn(l, listOf("location", "place", "coords", "position"))
          if (v == null) true else v == location
        }
      }

  // --- Helpers ---

  private fun getIdOrGenerate(listing: Listing): String {
    val v = findValueOn(listing, listOf("listingId", "id", "listing_id"))
    return v?.toString() ?: UUID.randomUUID().toString()
  }

  private fun matchesUser(listing: Listing, userId: String): Boolean {
    val v = findValueOn(listing, listOf("creatorUserId", "creatorId", "ownerId", "userId"))
    return v?.toString() == userId
  }

  private fun matchesSkill(listing: Listing, skill: Skill): Boolean {
    val v = findValueOn(listing, listOf("skill", "skillType", "category")) ?: return false
    return v == skill || v.toString() == skill.toString()
  }

  private fun findValueOn(obj: Any, names: List<String>): Any? {
    try {
      // try getters / isX
      for (name in names) {
        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }
        val isMethod = "is" + name.replaceFirstChar { it.uppercaseChar() }
        val method =
            obj.javaClass.methods.firstOrNull { m ->
              m.parameterCount == 0 &&
                  (m.name.equals(getter, true) ||
                      m.name.equals(name, true) ||
                      m.name.equals(isMethod, true))
            }
        if (method != null) {
          try {
            val v = method.invoke(obj)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }
      }

      // try declared fields
      for (name in names) {
        try {
          val field = obj.javaClass.getDeclaredField(name)
          field.isAccessible = true
          val v = field.get(obj)
          if (v != null) return v
        } catch (_: Throwable) {
          /* ignore */
        }
      }
    } catch (_: Throwable) {
      // ignore reflection failures
    }
    return null
  }

  private fun trySetBooleanField(obj: Any, names: List<String>, value: Boolean) {
    try {
      // try declared fields
      for (name in names) {
        try {
          val f = obj.javaClass.getDeclaredField(name)
          f.isAccessible = true
          if (f.type == java.lang.Boolean.TYPE || f.type == java.lang.Boolean::class.java) {
            f.setBoolean(obj, value)
            return
          }
        } catch (_: Throwable) {
          /* ignore */
        }
      }

      // try setter e.g. setActive(boolean)
      for (name in names) {
        try {
          val setterName = "set" + name.replaceFirstChar { it.uppercaseChar() }
          val method =
              obj.javaClass.methods.firstOrNull { m ->
                m.name.equals(setterName, true) &&
                    m.parameterCount == 1 &&
                    (m.parameterTypes[0] == java.lang.Boolean.TYPE ||
                        m.parameterTypes[0] == java.lang.Boolean::class.java)
              }
          if (method != null) {
            method.invoke(obj, java.lang.Boolean.valueOf(value))
            return
          }
        } catch (_: Throwable) {
          /* ignore */
        }
      }
    } catch (_: Throwable) {
      /* ignore */
    }

  }

    private fun loadMockData() {
        _listings.addAll(
            listOf(
                Proposal(
                    "1",
                    "12",
                    Skill("1", MainSubject.MUSIC, "Piano"),
                    "Experienced piano teacher",
                    Location(37.7749, -122.4194),
                    hourlyRate = 25.0),
                Proposal(
                    "2",
                    "13",
                    Skill("2", MainSubject.ACADEMICS, "Math"),
                    "Math tutor for high school students",
                    Location(34.0522, -118.2437),
                    hourlyRate = 30.0),
                Proposal(
                    "3",
                    "14",
                    Skill("3", MainSubject.MUSIC, "Guitare"),
                    "Learn acoustic guitar basics",
                    Location(40.7128, -74.0060),
                    hourlyRate = 20.0)))
        }
}
