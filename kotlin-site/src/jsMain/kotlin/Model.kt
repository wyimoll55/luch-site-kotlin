// Модель данных: часы и коллекции читаются из data/watches.json и data/collections.json.
// Данные собраны с официального сайта luch.by (коллекции, артикулы, цены переведены в BYN).

/** Одна модель в каталоге: наручные часы, будильник, настольные часы или аксессуар. */
external interface Watch {
    val id: String
    val article: String
    val name: String
    val type: String?             // «Часы», «Будильник», «Настольные часы», «Кольцо»…
    val collection: String
    val collectionSlug: String
    val category: String          // wrist | interior | accessories
    val gender: String?
    val mechanism: String?
    val case: String?
    val glass: String?
    val water: String?
    val priceByn: Int?            // цена в белорусских рублях
    val image: String?            // фото; если нет — часы рисуются кодом
    val dial: String              // цвет циферблата
    val hand: String              // цвет стрелок
    val oneHand: Boolean          // однострелочник
    val url: String?              // страница модели на luch.by
    val isNew: Boolean
    val limited: Boolean
    val chrono: Boolean
    val material: String?
    val color: String?
    val width: String?
}

/** Коллекция завода. */
external interface WatchCollection {
    val slug: String
    val name: String
    val about: String
    val gender: String?
    val mechanism: String?
    val count: Int
    val priceFrom: Int?
    val preview: String?
    val isNew: Boolean?
}

/** Все данные сайта — загружаются один раз при открытии страницы. */
object Store {
    var watches: List<Watch> = emptyList()
    var collections: List<WatchCollection> = emptyList()

    fun watch(id: String?): Watch? = watches.firstOrNull { it.id == id }
}

/** Значение поля модели по имени (нужно фильтрам каталога). */
fun Watch.field(key: String): String? {
    val v: dynamic = this.asDynamic()[key]
    return if (v == null) null else v.toString()
}

/** Логический флаг модели по имени: isNew, limited… */
fun Watch.flag(key: String): Boolean = this.asDynamic()[key] == true
