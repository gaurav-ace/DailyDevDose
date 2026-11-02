package com.gaurav.devagent.news

import android.text.Html
import android.util.Xml
import com.gaurav.devagent.data.Feed
import com.gaurav.devagent.data.Headline
import com.gaurav.devagent.data.Settings
import com.gaurav.devagent.data.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class FetchResult(val itemCount: Int, val failedFeeds: Int, val picked: List<Headline>)

object HeadlineEngine {
    private const val DAY_MS = 24 * 60 * 60 * 1000L

    suspend fun fetch(settings: Settings, seenUrls: Set<String>, count: Int = 3): FetchResult =
        withContext(Dispatchers.IO) {
            val perFeed = coroutineScope {
                settings.feeds.map { feed -> async { runCatching { load(feed, settings) }.getOrNull() } }
                    .awaitAll()
            }
            val all = perFeed.filterNotNull().flatten()
            FetchResult(all.size, perFeed.count { it == null }, pickTop(all, seenUrls, count))
        }

    private fun load(feed: Feed, settings: Settings): List<Headline> {
        val res = Http.get(feed.url)
        if (res.code !in 200..299) error("HTTP ${res.code}")
        return parseFeed(res.body).mapNotNull { item ->
            val topic = classify(item.title, feed.topic, settings) ?: return@mapNotNull null
            Headline(item.title, item.link, feed.name, topic, item.published)
        }
    }

    private fun classify(title: String, feedTopic: Topic, settings: Settings): Topic? {
        if (feedTopic != Topic.MIXED) return feedTopic
        val t = title.lowercase()
        return when {
            settings.aiKeywords.any { matchesWord(t, it) } -> Topic.AI
            settings.systemDesignKeywords.any { matchesWord(t, it) } -> Topic.SYSTEM_DESIGN
            else -> null
        }
    }

    private fun matchesWord(text: String, keyword: String): Boolean {
        val k = keyword.trim().lowercase()
        if (k.isEmpty()) return false
        return Regex("(?<![a-z0-9])" + Regex.escape(k) + "(?![a-z0-9])").containsMatchIn(text)
    }

    /** Freshest unseen items: one AI, one system design, then fill from a different source. */
    private fun pickTop(items: List<Headline>, seen: Set<String>, count: Int): List<Headline> {
        val now = System.currentTimeMillis()
        val fresh = items.filter { it.url !in seen }.distinctBy { it.url }
        val recent = fresh.filter { now - it.publishedAt < 3 * DAY_MS }
            .ifEmpty { fresh.filter { now - it.publishedAt < 14 * DAY_MS } }
            .ifEmpty { fresh }
            .sortedByDescending { it.publishedAt }

        val picked = mutableListOf<Headline>()
        recent.firstOrNull { it.topic == Topic.AI }?.let { picked += it }
        recent.firstOrNull { it.topic == Topic.SYSTEM_DESIGN }?.let { picked += it }
        for (h in recent) {
            if (picked.size >= count) break
            if (h !in picked && picked.none { it.source == h.source }) picked += h
        }
        for (h in recent) {
            if (picked.size >= count) break
            if (h !in picked) picked += h
        }
        return picked.take(count)
    }

    private data class FeedItem(val title: String, val link: String, val published: Long)

    /** Minimal RSS 2.0 + Atom parser. Returns whatever was parsed before any malformed XML. */
    private fun parseFeed(xml: String): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        runCatching {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))
            var inItem = false
            var title = ""
            var link = ""
            var date = ""
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                val name = parser.name?.substringAfter(':')?.lowercase()
                when (event) {
                    XmlPullParser.START_TAG -> when {
                        name == "item" || name == "entry" -> {
                            inItem = true; title = ""; link = ""; date = ""
                        }
                        !inItem -> Unit
                        name == "title" && title.isEmpty() -> title = parser.nextText()
                        name == "link" -> {
                            val href = parser.getAttributeValue(null, "href")
                            val rel = parser.getAttributeValue(null, "rel")
                            if (href != null) {
                                if (link.isEmpty() && (rel == null || rel == "alternate")) link = href
                            } else if (link.isEmpty()) {
                                link = parser.nextText().trim()
                            }
                        }
                        name in setOf("pubdate", "published", "updated", "date") && date.isEmpty() ->
                            date = parser.nextText()
                    }
                    XmlPullParser.END_TAG -> if (name == "item" || name == "entry") {
                        inItem = false
                        val cleanTitle = cleanText(title)
                        if (cleanTitle.isNotBlank() && link.isNotBlank()) {
                            items += FeedItem(cleanTitle, link, parseDate(date))
                        }
                    }
                }
                event = parser.next()
            }
        }
        return items
    }

    private fun cleanText(s: String): String =
        Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString().replace(Regex("\\s+"), " ").trim()

    private val legacyDateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE, d MMM yyyy HH:mm Z",
        "dd MMM yyyy HH:mm:ss Z",
    )

    private fun parseDate(raw: String): Long {
        val s = raw.trim()
        if (s.isEmpty()) return 0
        runCatching { return OffsetDateTime.parse(s).toInstant().toEpochMilli() }
        runCatching { return ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }
        for (pattern in legacyDateFormats) {
            runCatching { return SimpleDateFormat(pattern, Locale.US).parse(s)!!.time }
        }
        return 0
    }
}
