package com.gaurav.devagent.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

fun today(): String = LocalDate.now().toString()

@Serializable
enum class GoalCategory(val label: String, val short: String) {
    CODING("Coding", "Code"),
    SYSTEM_DESIGN("System Design", "SD"),
    AI("AI", "AI"),
}

/** A recurring daily goal. It counts as done only on the day stored in [doneOn]. */
@Serializable
data class Goal(
    val id: String = newId(),
    val category: GoalCategory,
    val text: String,
    val doneOn: String? = null,
) {
    val isDoneToday: Boolean get() = doneOn == today()
}

@Serializable
enum class Topic(val label: String) {
    AI("AI"),
    SYSTEM_DESIGN("System Design"),

    /** General feed: each item is classified by keywords and dropped if nothing matches. */
    MIXED("Mixed"),
}

@Serializable
data class Feed(val url: String, val name: String, val topic: Topic)

@Serializable
data class Headline(
    val title: String,
    val url: String,
    val source: String,
    val topic: Topic,
    val publishedAt: Long,
)

@Serializable
data class Reminder(
    val id: String = newId(),
    val title: String,
    val note: String = "",
    val triggerAt: Long,
    val repeatDaily: Boolean = false,
    val enabled: Boolean = true,
)

@Serializable
enum class CheckType(val label: String) {
    SITE_DOWN("Alert if site is down"),
    CONTAINS_TEXT("Alert when page contains text"),
    PAGE_CHANGED("Alert when page changes"),
}

@Serializable
data class Check(
    val id: String = newId(),
    val name: String,
    val url: String,
    val type: CheckType,
    val query: String = "",
    val enabled: Boolean = true,
    val lastRunAt: Long = 0,
    val lastStatus: String = "Not run yet",
    /** True while the alert condition holds, so we notify on transitions instead of every run. */
    val alerting: Boolean = false,
    val lastHash: String? = null,
)

@Serializable
data class Settings(
    val feeds: List<Feed> = DEFAULT_FEEDS,
    val aiKeywords: List<String> = DEFAULT_AI_KEYWORDS,
    val systemDesignKeywords: List<String> = DEFAULT_SD_KEYWORDS,
    val headlineHour: Int = 8,
    val nudgeHour: Int = 20,
    val checkIntervalMinutes: Int = 60,
)

@Serializable
data class AgentState(
    val goals: List<Goal> = DEFAULT_GOALS,
    val headlines: List<Headline> = emptyList(),
    val headlinesFetchedAt: Long = 0,
    val headlineStatus: String = "",
    val seenHeadlineUrls: List<String> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val checks: List<Check> = emptyList(),
    val progress: Map<Track, TrackProgress> = emptyMap(),
    val settings: Settings = Settings(),
)

val DEFAULT_GOALS = listOf(
    Goal(category = GoalCategory.CODING, text = "Ship 1 commit to a side project"),
)

val DEFAULT_FEEDS = listOf(
    Feed("https://hnrss.org/best", "Hacker News", Topic.MIXED),
    Feed("https://blog.bytebytego.com/feed", "ByteByteGo", Topic.SYSTEM_DESIGN),
    Feed("https://feed.infoq.com/architecture-design/", "InfoQ Architecture", Topic.SYSTEM_DESIGN),
    Feed("https://huggingface.co/blog/feed.xml", "Hugging Face", Topic.AI),
    Feed("https://simonwillison.net/atom/everything/", "Simon Willison", Topic.AI),
    Feed("https://newsletter.pragmaticengineer.com/feed", "Pragmatic Engineer", Topic.MIXED),
)

val DEFAULT_AI_KEYWORDS = listOf(
    "ai", "llm", "llms", "gpt", "openai", "anthropic", "claude", "gemini", "llama",
    "machine learning", "deep learning", "neural", "transformer", "agent", "agents",
    "rag", "inference", "fine-tuning", "embedding", "embeddings", "diffusion",
)

val DEFAULT_SD_KEYWORDS = listOf(
    "system design", "architecture", "distributed", "scalability", "scaling", "database",
    "postgres", "cache", "caching", "kafka", "microservice", "microservices", "latency",
    "consistency", "sharding", "load balancer", "queue", "kubernetes", "infrastructure",
    "outage", "replication",
)
