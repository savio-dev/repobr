package com.cloudstream

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addEpisode
import com.lagradost.cloudstream3.LoadResponse.Companion.newAnimeLoadResponse
import org.jsoup.nodes.Element

class AnimesRollProvider : MainAPI() {
    override var mainUrl = "https://www.anroll.net"
    override var name = "AnimesROLL"
    override val hasMainPage = true
    override var lang = "pt"
    override val supportedTypes = setOf(TvType.Anime)

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(mainUrl).document

        // Últimos episódios
        val latest = document.select("div.edmaGy").mapNotNull { element ->
            element.toSearchResult(isEpisode = true)
        }

        // Lista de animes
        val animeList = document.select("div.jTVCGa").mapNotNull { element ->
            element.toSearchResult()
        }

        return newHomePageResponse(
            list = listOf(
                HomePageList("Últimos Episódios", latest),
                HomePageList("Lista de Animes", animeList)
            )
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        return document.select("div.jTVCGa").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.select("h1").firstOrNull()?.text() ?: ""
        val poster = document.select("img").firstOrNull()?.attr("src")

        val episodes = document.select("div.itemlistepisode a").mapIndexed { index, ep ->
            Episode(
                data = ep.attr("href"),
                name = ep.selectFirst(".titulo_episodio")?.text() ?: "Episódio ${index + 1}"
            )
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            posterUrl = poster
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    private fun Element.toSearchResult(isEpisode: Boolean = false): SearchResponse? {
        val link = selectFirst("a")?.attr("href") ?: return null
        val title = selectFirst("h1")?.text() ?: return null
        val poster = selectFirst("img")?.attr("src")
        return newAnimeSearchResponse(title, link, TvType.Anime) {
            this.posterUrl = poster
            if (isEpisode) {
                addDubStatus(isDub = false, isSub = true)
            }
        }
    }
}
