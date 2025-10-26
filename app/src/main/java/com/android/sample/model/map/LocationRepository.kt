package com.android.sample.model.map

interface LocationRepository {

  /**
   * Performs a search for locations based on a given query string.
   *
   * @param query The text input used to search for matching locations. This could be an address,
   *   city name, landmark, etc.
   * @return A list of [Location] objects that match the query.
   */
  suspend fun search(query: String): List<Location>
}
