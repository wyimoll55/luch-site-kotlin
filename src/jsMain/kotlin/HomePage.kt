import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement

/** Слайд: либо картинка, либо цветной фон с подписью. */
data class Slide(
    val link: String,
    val image: String? = null,
    val alt: String = "",
    val background: String? = null,
    val title: String = "",
    val text: String = "",
    val dark: Boolean = false,
)

object HomePage {
    // Чтобы добавить слайд — допишите его в этот список
    private val slides = listOf(
        Slide("catalog.html", image = "images/slide-new.jpg", alt = "Новые коллекции"),
        Slide("catalog.html", image = "images/slide-brutalism.jpg", alt = "Новая коллекция «Брутализм»"),
        Slide("catalog.html", background = "linear-gradient(135deg, #8db9dc, #bcd4e9 45%, #ccd6d1)",
            title = "Эстетика 2000-х", text = "Хром, глянец и прозрачность", dark = true),
        Slide("catalog.html", background = "linear-gradient(135deg, #3b2a22, #8b1414 50%, #d9935a)",
            title = "Китайский стиль", text = "Символизм, природа и каллиграфия"),
    )

    private var index = 0
    private var timer = 0
    private lateinit var track: HTMLElement
    private lateinit var dots: List<HTMLElement>

    fun render() {
        track = byId("slides")
        track.innerHTML = slides.joinToString("") { s ->
            if (s.image != null)
                """<a class="slide" href="${s.link}"><img src="${s.image}" alt="${s.alt}" /></a>"""
            else
                """<a class="slide slide-text ${if (s.dark) "on-light" else ""}" href="${s.link}" style="background:${s.background}">
                     <span class="slide-label">Новая коллекция</span>
                     <span class="slide-title">${s.title}</span>
                     <span class="slide-text-small">${s.text}</span>
                   </a>"""
        }

        val dotsBox = byId("dots")
        dotsBox.innerHTML = slides.indices.joinToString("") { """<button aria-label="Слайд ${it + 1}"></button>""" }
        dots = queryAll("button", dotsBox)

        (document.querySelector(".prev") as HTMLElement).addEventListener("click", { show(index - 1); restart() })
        (document.querySelector(".next") as HTMLElement).addEventListener("click", { show(index + 1); restart() })
        dots.forEachIndexed { n, dot -> dot.addEventListener("click", { show(n); restart() }) }

        show(0)
        restart()
        alignMan()
    }

    private fun show(i: Int) {
        index = (i + slides.size) % slides.size     // по кругу
        track.style.setProperty("transform", "translateX(-${index * 100}%)")
        dots.forEachIndexed { n, dot -> dot.classList.toggle("active", n == index) }
    }

    /** Автопрокрутка каждые 5 секунд. */
    private fun restart() {
        window.clearInterval(timer)
        timer = window.setInterval({ show(index + 1) }, 5000)
    }

    /** Человек с часами ровно по высоте со слайдером (на компьютере). */
    private fun alignMan() {
        val man = document.querySelector(".hero-man") as HTMLImageElement
        val slider = byId("slider")
        val align = {
            if (window.innerWidth <= 800) man.style.removeProperty("height")
            else man.style.setProperty("height", "${slider.offsetHeight}px")
        }
        window.addEventListener("resize", { align() })
        man.addEventListener("load", { align() })
        align()
    }
}
